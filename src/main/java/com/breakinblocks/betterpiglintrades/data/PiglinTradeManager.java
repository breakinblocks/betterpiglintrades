package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
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
public class PiglinTradeManager extends SimpleJsonResourceReloadListener<PiglinTrade> {
    public static final PiglinTradeManager INSTANCE = new PiglinTradeManager();

    private Map<Item, PiglinTrade> tradesByItem = new HashMap<>();
    private List<PiglinTrade> allTrades = new ArrayList<>();
    private Map<Item, List<OutputEntry>> resolvedOutputs = new HashMap<>();

    public PiglinTradeManager() {
        super(PiglinTrade.CODEC, FileToIdConverter.json("piglin_trades"));
    }

    @Override
    protected void apply(Map<Identifier, PiglinTrade> tradeMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, PiglinTrade> newTradesByItem = new HashMap<>();
        List<PiglinTrade> newAllTrades = new ArrayList<>();

        for (Map.Entry<Identifier, PiglinTrade> entry : tradeMap.entrySet()) {
            Identifier id = entry.getKey();
            PiglinTrade trade = entry.getValue();

            try {
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
            List<OutputEntry> outputs = LootTableParser.parseOutputs(resourceManager, entry.getValue().lootTable());
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
    public Map<Item, List<OutputEntry>> getResolvedOutputs() {
        return Collections.unmodifiableMap(resolvedOutputs);
    }

    public Optional<List<OutputEntry>> getOutputsForItem(Item item) {
        return Optional.ofNullable(resolvedOutputs.get(item));
    }
}
