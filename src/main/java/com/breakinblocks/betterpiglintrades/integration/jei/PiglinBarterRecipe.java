package com.breakinblocks.betterpiglintrades.integration.jei;

import com.breakinblocks.betterpiglintrades.data.PiglinTrade;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Wrapper class representing a piglin barter trade for JEI display.
 */
public record PiglinBarterRecipe(
        ItemStack input,
        List<ItemStack> possibleOutputs,
        PiglinTrade trade
) {
}
