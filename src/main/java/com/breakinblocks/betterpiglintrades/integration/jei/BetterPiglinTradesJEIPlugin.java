package com.breakinblocks.betterpiglintrades.integration.jei;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.client.ClientTradeOutputCache;
import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class BetterPiglinTradesJEIPlugin implements IModPlugin {

    private static final Identifier PLUGIN_ID = BetterPiglinTrades.id("jei_plugin");

    private static IJeiRuntime jeiRuntime;

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PiglinBarterCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PiglinBarterCategory.RECIPE_TYPE, buildRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (Item item : ClientTradeOutputCache.getAllOutputs().keySet()) {
            registration.addCraftingStation(PiglinBarterCategory.RECIPE_TYPE, item);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        ClientTradeOutputCache.setOnCacheUpdated(BetterPiglinTradesJEIPlugin::reloadRecipes);
    }

    public static void reloadRecipes() {
        if (jeiRuntime == null) return;
        var recipeManager = jeiRuntime.getRecipeManager();
        recipeManager.hideRecipes(PiglinBarterCategory.RECIPE_TYPE,
                recipeManager.createRecipeLookup(PiglinBarterCategory.RECIPE_TYPE).get().toList());
        recipeManager.addRecipes(PiglinBarterCategory.RECIPE_TYPE, buildRecipes());
    }

    private static List<PiglinBarterRecipe> buildRecipes() {
        List<PiglinBarterRecipe> recipes = new ArrayList<>();

        for (Map.Entry<Item, List<OutputEntry>> entry : ClientTradeOutputCache.getAllOutputs().entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            recipes.add(new PiglinBarterRecipe(new ItemStack(entry.getKey()), entry.getValue()));
        }

        return recipes;
    }
}
