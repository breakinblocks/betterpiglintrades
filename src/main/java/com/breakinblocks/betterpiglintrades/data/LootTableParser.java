package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses loot table JSON files on the server side to extract possible item outputs.
 */
public class LootTableParser {
    private static final Gson GSON = new Gson();

    /**
     * Parses a loot table from the server's resource manager and extracts all possible item outputs.
     */
    public static List<Item> parseOutputs(ResourceManager resourceManager, ResourceLocation lootTableId) {
        List<Item> outputs = new ArrayList<>();
        ResourceLocation resourcePath = ResourceLocation.fromNamespaceAndPath(
                lootTableId.getNamespace(),
                "loot_table/" + lootTableId.getPath() + ".json"
        );

        Optional<Resource> resourceOpt = resourceManager.getResource(resourcePath);
        if (resourceOpt.isEmpty()) {
            BetterPiglinTrades.LOGGER.warn("Could not find loot table resource: {}", resourcePath);
            return outputs;
        }

        try (BufferedReader reader = resourceOpt.get().openAsReader()) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            extractItemsFromLootTable(json, outputs);
        } catch (IOException e) {
            BetterPiglinTrades.LOGGER.error("Failed to read loot table {}: {}", lootTableId, e.getMessage());
        }

        return outputs;
    }

    private static void extractItemsFromLootTable(JsonObject json, List<Item> outputs) {
        if (!json.has("pools")) return;

        JsonArray pools = json.getAsJsonArray("pools");
        for (JsonElement poolElement : pools) {
            JsonObject pool = poolElement.getAsJsonObject();
            if (!pool.has("entries")) continue;

            JsonArray entries = pool.getAsJsonArray("entries");
            for (JsonElement entryElement : entries) {
                extractItemFromEntry(entryElement.getAsJsonObject(), outputs);
            }
        }
    }

    private static void extractItemFromEntry(JsonObject entry, List<Item> outputs) {
        String type = entry.has("type") ? entry.get("type").getAsString() : "";

        if (type.equals("minecraft:item") && entry.has("name")) {
            ResourceLocation itemId = ResourceLocation.parse(entry.get("name").getAsString());
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != null && item != Items.AIR && !outputs.contains(item)) {
                outputs.add(item);
            }
        } else if ((type.equals("minecraft:alternatives") || type.equals("minecraft:group") || type.equals("minecraft:sequence"))
                && entry.has("children")) {
            for (JsonElement child : entry.getAsJsonArray("children")) {
                extractItemFromEntry(child.getAsJsonObject(), outputs);
            }
        }
    }
}
