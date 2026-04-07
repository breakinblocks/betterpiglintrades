package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.client.ClientTradeOutputCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SyncTradeOutputsMessage {
    private final Map<Item, List<Item>> tradeOutputs;

    public SyncTradeOutputsMessage(Map<Item, List<Item>> tradeOutputs) {
        this.tradeOutputs = tradeOutputs;
    }

    public static void encode(SyncTradeOutputsMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.tradeOutputs.size());

        for (Map.Entry<Item, List<Item>> entry : msg.tradeOutputs.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            buf.writeVarInt(entry.getValue().size());

            for (Item item : entry.getValue()) {
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
            }
        }
    }

    public static SyncTradeOutputsMessage decode(FriendlyByteBuf buf) {
        int mapSize = buf.readVarInt();
        Map<Item, List<Item>> outputs = new HashMap<>();

        for (int i = 0; i < mapSize; i++) {
            Item tradeItem = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
            int listSize = buf.readVarInt();
            List<Item> outputItems = new ArrayList<>();

            for (int j = 0; j < listSize; j++) {
                outputItems.add(BuiltInRegistries.ITEM.get(buf.readResourceLocation()));
            }

            outputs.put(tradeItem, outputItems);
        }

        return new SyncTradeOutputsMessage(outputs);
    }

    public static void handle(SyncTradeOutputsMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientTradeOutputCache.updateCache(msg.tradeOutputs);
        });
        ctx.get().setPacketHandled(true);
    }

    public Map<Item, List<Item>> getTradeOutputs() {
        return tradeOutputs;
    }
}
