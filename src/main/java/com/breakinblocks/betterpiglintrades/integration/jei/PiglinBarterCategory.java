package com.breakinblocks.betterpiglintrades.integration.jei;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PiglinBarterCategory implements IRecipeCategory<PiglinBarterRecipe> {

    public static final IRecipeType<PiglinBarterRecipe> RECIPE_TYPE = IRecipeType.create(
            BetterPiglinTrades.MOD_ID,
            "piglin_barter",
            PiglinBarterRecipe.class
    );

    private static final int WIDTH = 162;
    private static final int HEIGHT = 132;
    private static final int SLOTS_PER_ROW = 9;
    private static final int MAX_ROWS = 5;
    private static final int SLOT_SIZE = 18;

    private final IDrawable icon;
    private final Component title;

    public PiglinBarterCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.PIGLIN_HEAD));
        this.title = Component.translatable("gui.betterpiglintrades.category.piglin_barter");
    }

    @Override
    public IRecipeType<PiglinBarterRecipe> getRecipeType() {
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
        builder.addSlot(RecipeIngredientRole.INPUT, (WIDTH - SLOT_SIZE) / 2, 2).add(recipe.input());

        int maxSlots = SLOTS_PER_ROW * MAX_ROWS;
        int outputY = 38;
        for (int i = 0; i < recipe.outputs().size() && i < maxSlots; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            builder.addSlot(RecipeIngredientRole.OUTPUT, col * SLOT_SIZE, outputY + row * SLOT_SIZE)
                    .add(new ItemStack(recipe.outputs().get(i).item()));
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PiglinBarterRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int outputY = 38;
        int maxSlots = SLOTS_PER_ROW * MAX_ROWS;

        for (int i = 0; i < recipe.outputs().size() && i < maxSlots; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            int slotX = col * SLOT_SIZE;
            int slotY = outputY + row * SLOT_SIZE;

            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                OutputEntry entry = recipe.outputs().get(i);
                String chanceStr = entry.chance() < 1.0f
                        ? String.format("%.2f%%", entry.chance())
                        : String.format("%.1f%%", entry.chance());
                tooltip.add(Component.translatable("gui.betterpiglintrades.chance", chanceStr));
                break;
            }
        }
    }

    @Override
    public void draw(PiglinBarterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int arrowX = (WIDTH - minecraft.font.width("v")) / 2;
        guiGraphics.text(minecraft.font, "v", arrowX, 24, 0xFF808080, false);

        int maxSlots = SLOTS_PER_ROW * MAX_ROWS;
        if (recipe.outputs().size() > maxSlots) {
            String overflow = "+" + (recipe.outputs().size() - maxSlots) + " more...";
            guiGraphics.text(minecraft.font, overflow, 0, HEIGHT - 10, 0xFF808080, false);
        }
    }
}
