package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Manages piglin trade definitions loaded from datapacks.
 * Trades are loaded from: data/&lt;namespace&gt;/piglin_trades/&lt;name&gt;.json
 */
public class PiglinTradeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final PiglinTradeManager INSTANCE = new PiglinTradeManager();

    private Map<Item, PiglinTrade> tradesByItem = new HashMap<>();
    private List<PiglinTrade> allTrades = new ArrayList<>();
    private Map<Item, List<Item>> resolvedOutputs = new HashMap<>();

    public PiglinTradeManager() {
        super(GSON, "piglin_trades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, PiglinTrade> newTradesByItem = new HashMap<>();
        List<PiglinTrade> newAllTrades = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                PiglinTrade trade = PiglinTrade.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> BetterPiglinTrades.LOGGER.error("Failed to parse piglin trade {}: {}", id, error))
                        .orElseThrow(() -> new IllegalStateException("Failed to parse piglin trade " + id));

                PiglinTrade existing = newTradesByItem.get(trade.item());
                if (existing == null || trade.priority() > existing.priority()) {
                    newTradesByItem.put(trade.item(), trade);
                }
                newAllTrades.add(trade);
            } catch (Exception e) {
                BetterPiglinTrades.LOGGER.error("Failed to load piglin trade {}: {}", id, e.getMessage());
            }
        }

        this.tradesByItem = newTradesByItem;
        this.allTrades = newAllTrades;
        this.resolvedOutputs.clear();
        resolveAllOutputs(resourceManager);

        BetterPiglinTrades.LOGGER.info("Loaded {} piglin trades ({} unique items)", newAllTrades.size(), newTradesByItem.size());
    }

    private void resolveAllOutputs(ResourceManager resourceManager) {
        for (Map.Entry<Item, PiglinTrade> entry : tradesByItem.entrySet()) {
            List<Item> outputs = LootTableParser.parseOutputs(resourceManager, entry.getValue().lootTable());
            if (!outputs.isEmpty()) {
                resolvedOutputs.put(entry.getKey(), outputs);
            }
        }
    }

    /**
     * Gets the trade definition for a given item, if one exists.
     */
    public Optional<PiglinTrade> getTradeForItem(Item item) {
        return Optional.ofNullable(tradesByItem.get(item));
    }

    /**
     * Gets the trade definition for a given item stack, if one exists.
     */
    public Optional<PiglinTrade> getTradeForItem(ItemStack stack) {
        return getTradeForItem(stack.getItem());
    }

    /**
     * Checks if an item is a valid piglin trade item.
     */
    public boolean isValidTradeItem(Item item) {
        return tradesByItem.containsKey(item);
    }

    /**
     * Checks if an item stack is a valid piglin trade item.
     */
    public boolean isValidTradeItem(ItemStack stack) {
        return isValidTradeItem(stack.getItem());
    }

    /**
     * Gets all registered trades as a list.
     */
    public List<PiglinTrade> getAllTradesList() {
        return Collections.unmodifiableList(allTrades);
    }

    /**
     * Gets all registered trades mapped by item.
     */
    public Map<Item, PiglinTrade> getAllTrades() {
        return Collections.unmodifiableMap(tradesByItem);
    }

    /**
     * Gets the number of registered trades.
     */
    public int getTradeCount() {
        return tradesByItem.size();
    }

    /**
     * Gets all resolved outputs for all trades.
     * This is used for syncing to clients for JEI display.
     */
    public Map<Item, List<Item>> getResolvedOutputs() {
        return Collections.unmodifiableMap(resolvedOutputs);
    }

    /**
     * Gets the resolved outputs for a specific trade item.
     */
    public Optional<List<Item>> getOutputsForItem(Item item) {
        return Optional.ofNullable(resolvedOutputs.get(item));
    }
}
