package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncTradeOutputsPayload(Map<Item, List<Item>> tradeOutputs) implements CustomPacketPayload {

    public static final Type<SyncTradeOutputsPayload> TYPE = new Type<>(
            BetterPiglinTrades.id("sync_trade_outputs")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncTradeOutputsPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncTradeOutputsPayload decode(FriendlyByteBuf buf) {
            int mapSize = buf.readVarInt();
            Map<Item, List<Item>> outputs = new HashMap<>();

            for (int i = 0; i < mapSize; i++) {
                Item tradeItem = BuiltInRegistries.ITEM.getValue(buf.readIdentifier());
                int listSize = buf.readVarInt();
                List<Item> outputItems = new ArrayList<>();

                for (int j = 0; j < listSize; j++) {
                    outputItems.add(BuiltInRegistries.ITEM.getValue(buf.readIdentifier()));
                }

                outputs.put(tradeItem, outputItems);
            }

            return new SyncTradeOutputsPayload(outputs);
        }

        @Override
        public void encode(FriendlyByteBuf buf, SyncTradeOutputsPayload payload) {
            buf.writeVarInt(payload.tradeOutputs.size());

            for (Map.Entry<Item, List<Item>> entry : payload.tradeOutputs.entrySet()) {
                buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(entry.getKey()));
                buf.writeVarInt(entry.getValue().size());

                for (Item item : entry.getValue()) {
                    buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
                }
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
