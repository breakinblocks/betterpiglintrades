package com.breakinblocks.betterpiglintrades.data;

import net.minecraft.world.item.Item;

/**
 * A single possible barter reward and its percentage chance of being rolled.
 *
 * @param item The reward item
 * @param chance Percentage chance out of 100, derived from the loot table entry weights
 */
public record OutputEntry(Item item, float chance) {
}
