package com.breakinblocks.betterpiglintrades.data;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses loot table JSON files on the server side to extract the possible item outputs
 * along with the chance of each one being rolled.
 */
public class LootTableParser {
    private static final Gson GSON = new Gson();

    private record WeightedItem(Item item, int weight) {
    }

    public static List<OutputEntry> parseOutputs(ResourceManager resourceManager, ResourceLocation lootTableId) {
        ResourceLocation resourcePath = ResourceLocation.fromNamespaceAndPath(
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
        if (!json.has("pools")) {
            return List.of();
        }

        Map<Item, Float> chances = new LinkedHashMap<>();

        for (JsonElement poolElement : json.getAsJsonArray("pools")) {
            JsonObject pool = poolElement.getAsJsonObject();
            if (!pool.has("entries")) {
                continue;
            }

            List<WeightedItem> collected = new ArrayList<>();
            for (JsonElement entryElement : pool.getAsJsonArray("entries")) {
                collectWeightedItems(entryElement.getAsJsonObject(), collected, 1);
            }

            int poolWeight = collected.stream().mapToInt(WeightedItem::weight).sum();
            if (poolWeight <= 0) {
                continue;
            }

            for (WeightedItem weighted : collected) {
                chances.merge(weighted.item(), weighted.weight() * 100.0f / poolWeight, Float::sum);
            }
        }

        List<OutputEntry> results = new ArrayList<>(chances.size());
        for (Map.Entry<Item, Float> entry : chances.entrySet()) {
            results.add(new OutputEntry(entry.getKey(), Math.min(entry.getValue(), 100.0f)));
        }

        results.sort((a, b) -> Float.compare(b.chance(), a.chance()));
        return results;
    }

    private static void collectWeightedItems(JsonObject entry, List<WeightedItem> results, int inheritedWeight) {
        String type = entry.has("type") ? entry.get("type").getAsString() : "";
        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : inheritedWeight;

        if (type.equals("minecraft:item") && entry.has("name")) {
            ResourceLocation itemId = ResourceLocation.parse(entry.get("name").getAsString());
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != Items.AIR) {
                results.add(new WeightedItem(item, weight));
            }
        } else if ((type.equals("minecraft:alternatives") || type.equals("minecraft:group") || type.equals("minecraft:sequence"))
                && entry.has("children")) {
            for (JsonElement child : entry.getAsJsonArray("children")) {
                collectWeightedItems(child.getAsJsonObject(), results, weight);
            }
        }
    }
}
