package com.breakinblocks.betterpiglintrades.client;

import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import net.minecraft.world.item.Item;

import java.util.*;

public class ClientTradeOutputCache {
    private static Map<Item, List<OutputEntry>> cachedOutputs = new HashMap<>();
    private static Runnable onCacheUpdated = () -> {};

    public static void setOnCacheUpdated(Runnable callback) {
        onCacheUpdated = callback;
    }

    public static void updateCache(Map<Item, List<OutputEntry>> outputs) {
        cachedOutputs = new HashMap<>(outputs);
        onCacheUpdated.run();
    }

    public static Optional<List<OutputEntry>> getOutputsForItem(Item item) {
        return Optional.ofNullable(cachedOutputs.get(item));
    }

    public static Map<Item, List<OutputEntry>> getAllOutputs() {
        return Collections.unmodifiableMap(cachedOutputs);
    }

    public static boolean hasOutputs() {
        return !cachedOutputs.isEmpty();
    }

    public static void clear() {
        cachedOutputs.clear();
    }
}
