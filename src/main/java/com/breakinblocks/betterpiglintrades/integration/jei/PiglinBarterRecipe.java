package com.breakinblocks.betterpiglintrades.integration.jei;

import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import com.breakinblocks.betterpiglintrades.data.PiglinTrade;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PiglinBarterRecipe(
        ItemStack input,
        List<OutputEntry> outputs,
        PiglinTrade trade
) {
}
