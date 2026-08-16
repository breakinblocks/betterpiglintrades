package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncTradeOutputsPayload(Map<Item, List<OutputEntry>> tradeOutputs) implements CustomPacketPayload {

    public static final Type<SyncTradeOutputsPayload> TYPE = new Type<>(
            BetterPiglinTrades.id("sync_trade_outputs")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncTradeOutputsPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncTradeOutputsPayload decode(FriendlyByteBuf buf) {
            int mapSize = buf.readVarInt();
            Map<Item, List<OutputEntry>> outputs = new HashMap<>();

            for (int i = 0; i < mapSize; i++) {
                Item tradeItem = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                int listSize = buf.readVarInt();
                List<OutputEntry> entries = new ArrayList<>(listSize);

                for (int j = 0; j < listSize; j++) {
                    Item output = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                    entries.add(new OutputEntry(output, buf.readFloat()));
                }

                outputs.put(tradeItem, entries);
            }

            return new SyncTradeOutputsPayload(outputs);
        }

        @Override
        public void encode(FriendlyByteBuf buf, SyncTradeOutputsPayload payload) {
            buf.writeVarInt(payload.tradeOutputs.size());

            for (Map.Entry<Item, List<OutputEntry>> entry : payload.tradeOutputs.entrySet()) {
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
                buf.writeVarInt(entry.getValue().size());

                for (OutputEntry output : entry.getValue()) {
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(output.item()));
                    buf.writeFloat(output.chance());
                }
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
