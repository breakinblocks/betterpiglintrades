package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.client.ClientTradeOutputCache;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Side-agnostic lookup for whether an item participates in piglin bartering.
 * The server answers from the loaded datapack definitions, the client from the synced output cache.
 */
public final class TradeItems {
    private TradeItems() {
    }

    public static boolean isTradeItem(ItemStack stack) {
        return isTradeItem(stack.getItem());
    }

    public static boolean isTradeItem(Item item) {
        return PiglinTradeManager.INSTANCE.isValidTradeItem(item)
                || ClientTradeOutputCache.getOutputsForItem(item).isPresent();
    }
}
