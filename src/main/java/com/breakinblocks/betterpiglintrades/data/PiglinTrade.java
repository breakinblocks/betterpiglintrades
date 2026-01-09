package com.breakinblocks.betterpiglintrades.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Represents a piglin trade definition.
 *
 * @param item The item that piglins will accept for bartering
 * @param lootTable The loot table to use for generating rewards
 * @param priority Higher priority trades are checked first (default 0)
 */
public record PiglinTrade(Item item, Identifier lootTable, int priority) {

    public static final Codec<PiglinTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(PiglinTrade::item),
            Identifier.CODEC.fieldOf("loot_table").forGetter(PiglinTrade::lootTable),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(PiglinTrade::priority)
    ).apply(instance, PiglinTrade::new));

    public PiglinTrade(Item item, Identifier lootTable) {
        this(item, lootTable, 0);
    }
}
