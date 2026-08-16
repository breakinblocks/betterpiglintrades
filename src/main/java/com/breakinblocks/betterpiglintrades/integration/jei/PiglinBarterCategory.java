package com.breakinblocks.betterpiglintrades.integration.jei;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PiglinBarterCategory implements IRecipeCategory<PiglinBarterRecipe> {

    public static final RecipeType<PiglinBarterRecipe> RECIPE_TYPE = RecipeType.create(
            BetterPiglinTrades.MOD_ID,
            "piglin_barter",
            PiglinBarterRecipe.class
    );

    private static final int WIDTH = 162;
    private static final int HEIGHT = 132;
    private static final int SLOTS_PER_ROW = 9;
    private static final int MAX_ROWS = 5;
    private static final int SLOT_SIZE = 18;
    private static final int OUTPUT_Y = 38;

    private final IDrawable icon;
    private final Component title;

    public PiglinBarterCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.PIGLIN_HEAD));
        this.title = Component.translatable("gui.betterpiglintrades.category.piglin_barter");
    }

    @Override
    public RecipeType<PiglinBarterRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PiglinBarterRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, (WIDTH - SLOT_SIZE) / 2, 2).addItemStack(recipe.input());

        int maxSlots = SLOTS_PER_ROW * MAX_ROWS;
        for (int i = 0; i < recipe.outputs().size() && i < maxSlots; i++) {
            OutputEntry entry = recipe.outputs().get(i);
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            builder.addSlot(RecipeIngredientRole.OUTPUT, col * SLOT_SIZE, OUTPUT_Y + row * SLOT_SIZE)
                    .addItemStack(new ItemStack(entry.item()))
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.translatable("gui.betterpiglintrades.chance", formatChance(entry.chance()))));
        }
    }

    @Override
    public void draw(PiglinBarterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int arrowX = (WIDTH - minecraft.font.width("v")) / 2;
        guiGraphics.drawString(minecraft.font, "v", arrowX, 24, 0xFF808080, false);

        int maxSlots = SLOTS_PER_ROW * MAX_ROWS;
        if (recipe.outputs().size() > maxSlots) {
            String overflow = "+" + (recipe.outputs().size() - maxSlots) + " more...";
            guiGraphics.drawString(minecraft.font, overflow, 0, HEIGHT - 10, 0xFF808080, false);
        }
    }

    private static String formatChance(float chance) {
        if (chance >= 10.0f) {
            return String.format("%.1f%%", chance);
        }
        return chance < 0.1f ? String.format("%.3f%%", chance) : String.format("%.2f%%", chance);
    }
}
