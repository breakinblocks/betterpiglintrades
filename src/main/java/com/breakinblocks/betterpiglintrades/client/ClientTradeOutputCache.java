package com.breakinblocks.betterpiglintrades.client;

import com.breakinblocks.betterpiglintrades.integration.jei.BetterPiglinTradesJEIPlugin;
import net.minecraft.world.item.Item;

import java.util.*;

public class ClientTradeOutputCache {
    private static Map<Item, List<Item>> cachedOutputs = new HashMap<>();

    public static void updateCache(Map<Item, List<Item>> outputs) {
        cachedOutputs = new HashMap<>(outputs);
        BetterPiglinTradesJEIPlugin.reloadRecipes();
    }

    public static Optional<List<Item>> getOutputsForItem(Item item) {
        return Optional.ofNullable(cachedOutputs.get(item));
    }

    public static Map<Item, List<Item>> getAllOutputs() {
        return Collections.unmodifiableMap(cachedOutputs);
    }

    public static boolean hasOutputs() {
        return !cachedOutputs.isEmpty();
    }

    public static void clear() {
        cachedOutputs.clear();
    }
}
