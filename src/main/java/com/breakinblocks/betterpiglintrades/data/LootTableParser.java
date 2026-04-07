package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LootTableParser {
    private static final Gson GSON = new Gson();

    private record WeightedItem(Item item, int weight) {}

    public static List<OutputEntry> parseOutputs(ResourceManager resourceManager, Identifier lootTableId) {
        Identifier resourcePath = Identifier.fromNamespaceAndPath(
                lootTableId.getNamespace(),
                "loot_table/" + lootTableId.getPath() + ".json"
        );

        Optional<Resource> resourceOpt = resourceManager.getResource(resourcePath);
        if (resourceOpt.isEmpty()) {
            BetterPiglinTrades.LOGGER.warn("Could not find loot table resource: {}", resourcePath);
            return List.of();
        }

        try (BufferedReader reader = resourceOpt.get().openAsReader()) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return extractWeightedItems(json);
        } catch (Exception e) {
            BetterPiglinTrades.LOGGER.error("Failed to read loot table {}: {}", lootTableId, e.getMessage());
            return List.of();
        }
    }

    private static List<OutputEntry> extractWeightedItems(JsonObject json) {
        if (!json.has("pools")) return List.of();

        List<WeightedItem> allItems = new ArrayList<>();

        JsonArray pools = json.getAsJsonArray("pools");
        for (JsonElement poolElement : pools) {
            JsonObject pool = poolElement.getAsJsonObject();
            if (!pool.has("entries")) continue;

            JsonArray entries = pool.getAsJsonArray("entries");
            for (JsonElement entryElement : entries) {
                collectWeightedItems(entryElement.getAsJsonObject(), allItems);
            }
        }

        if (allItems.isEmpty()) return List.of();

        int totalWeight = allItems.stream().mapToInt(WeightedItem::weight).sum();
        List<OutputEntry> results = new ArrayList<>();
        for (WeightedItem wi : allItems) {
            if (!results.stream().anyMatch(e -> e.item() == wi.item())) {
                float chance = (float) wi.weight() / totalWeight * 100.0f;
                results.add(new OutputEntry(wi.item(), chance));
            }
        }
        return results;
    }

    private static void collectWeightedItems(JsonObject entry, List<WeightedItem> results) {
        String type = entry.has("type") ? entry.get("type").getAsString() : "";
        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;

        if (type.equals("minecraft:item")) {
            String itemIdStr = null;
            if (entry.has("name")) {
                itemIdStr = entry.get("name").getAsString();
            } else if (entry.has("id")) {
                itemIdStr = entry.get("id").getAsString();
            }

            if (itemIdStr != null) {
                Identifier itemId = Identifier.parse(itemIdStr);
                BuiltInRegistries.ITEM.get(itemId).map(Holder::value).ifPresent(item -> {
                    if (item != Items.AIR) {
                        results.add(new WeightedItem(item, weight));
                    }
                });
            }
        } else if ((type.equals("minecraft:alternatives") || type.equals("minecraft:group") || type.equals("minecraft:sequence"))
                && entry.has("children")) {
            for (JsonElement child : entry.getAsJsonArray("children")) {
                JsonObject childObj = child.getAsJsonObject();
                if (!childObj.has("weight")) {
                    childObj.addProperty("weight", weight);
                }
                collectWeightedItems(childObj, results);
            }
        }
    }
}
