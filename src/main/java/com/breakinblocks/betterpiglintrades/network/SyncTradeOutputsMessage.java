package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.client.ClientTradeOutputCache;
import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SyncTradeOutputsMessage {
    private final Map<Item, List<OutputEntry>> tradeOutputs;

    public SyncTradeOutputsMessage(Map<Item, List<OutputEntry>> tradeOutputs) {
        this.tradeOutputs = tradeOutputs;
    }

    public static void encode(SyncTradeOutputsMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.tradeOutputs.size());

        for (Map.Entry<Item, List<OutputEntry>> entry : msg.tradeOutputs.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            buf.writeVarInt(entry.getValue().size());

            for (OutputEntry output : entry.getValue()) {
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(output.item()));
                buf.writeFloat(output.chance());
            }
        }
    }

    public static SyncTradeOutputsMessage decode(FriendlyByteBuf buf) {
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

        return new SyncTradeOutputsMessage(outputs);
    }

    public static void handle(SyncTradeOutputsMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientTradeOutputCache.updateCache(msg.tradeOutputs));
        ctx.get().setPacketHandled(true);
    }

    public Map<Item, List<OutputEntry>> getTradeOutputs() {
        return tradeOutputs;
    }
}
