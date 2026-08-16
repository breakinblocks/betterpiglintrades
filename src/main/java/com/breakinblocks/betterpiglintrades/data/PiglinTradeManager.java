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

    private Map<Item, PiglinTrade> tradesByItem = Map.of();
    private List<PiglinTrade> allTrades = List.of();
    private Map<Item, List<OutputEntry>> resolvedOutputs = Map.of();

    public PiglinTradeManager() {
        super(GSON, "piglin_trades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, PiglinTrade> winners = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                PiglinTrade trade = PiglinTrade.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> BetterPiglinTrades.LOGGER.error("Failed to parse piglin trade {}: {}", id, error))
                        .orElseThrow(() -> new IllegalStateException("Failed to parse piglin trade " + id));

                if (trade.enabled() && trade.lootTable().isEmpty()) {
                    BetterPiglinTrades.LOGGER.error("Piglin trade {} is enabled but has no loot_table, ignoring it", id);
                    continue;
                }

                PiglinTrade existing = winners.get(trade.item());
                if (existing == null || trade.priority() > existing.priority()) {
                    winners.put(trade.item(), trade);
                }
            } catch (Exception e) {
                BetterPiglinTrades.LOGGER.error("Failed to load piglin trade {}: {}", id, e.getMessage());
            }
        }

        int disabled = 0;
        Map<Item, PiglinTrade> newTradesByItem = new HashMap<>();
        for (Map.Entry<Item, PiglinTrade> entry : winners.entrySet()) {
            if (entry.getValue().enabled()) {
                newTradesByItem.put(entry.getKey(), entry.getValue());
            } else {
                disabled++;
            }
        }

        this.tradesByItem = Map.copyOf(newTradesByItem);
        this.allTrades = List.copyOf(newTradesByItem.values());
        this.resolvedOutputs = resolveAllOutputs(resourceManager, this.tradesByItem);

        BetterPiglinTrades.LOGGER.info("Loaded {} piglin trades ({} disabled)", newTradesByItem.size(), disabled);
    }

    private static Map<Item, List<OutputEntry>> resolveAllOutputs(ResourceManager resourceManager, Map<Item, PiglinTrade> trades) {
        Map<Item, List<OutputEntry>> resolved = new HashMap<>();

        for (Map.Entry<Item, PiglinTrade> entry : trades.entrySet()) {
            ResourceLocation lootTable = entry.getValue().lootTable().orElseThrow();
            List<OutputEntry> outputs = LootTableParser.parseOutputs(resourceManager, lootTable);
            if (!outputs.isEmpty()) {
                resolved.put(entry.getKey(), List.copyOf(outputs));
            }
        }

        return Map.copyOf(resolved);
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
        return allTrades;
    }

    /**
     * Gets all registered trades mapped by item.
     */
    public Map<Item, PiglinTrade> getAllTrades() {
        return tradesByItem;
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
    public Map<Item, List<OutputEntry>> getResolvedOutputs() {
        return resolvedOutputs;
    }

    /**
     * Gets the resolved outputs for a specific trade item.
     */
    public Optional<List<OutputEntry>> getOutputsForItem(Item item) {
        return Optional.ofNullable(resolvedOutputs.get(item));
    }
}
