package com.breakinblocks.betterpiglintrades.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Optional;

/**
 * Represents a piglin trade definition.
 *
 * @param item The item that piglins will accept for bartering
 * @param lootTable The loot table to use for generating rewards, required unless the trade is disabled
 * @param priority Higher priority trades are checked first (default 0)
 * @param enabled Set to false to take this item out of bartering (default true)
 */
public record PiglinTrade(Item item, Optional<Identifier> lootTable, int priority, boolean enabled) {

    public static final Codec<PiglinTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(PiglinTrade::item),
            Identifier.CODEC.optionalFieldOf("loot_table").forGetter(PiglinTrade::lootTable),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(PiglinTrade::priority),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PiglinTrade::enabled)
    ).apply(instance, PiglinTrade::new));
}
