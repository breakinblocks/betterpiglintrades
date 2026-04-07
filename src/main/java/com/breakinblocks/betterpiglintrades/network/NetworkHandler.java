package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.Map;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BetterPiglinTrades.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SyncTradeOutputsMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncTradeOutputsMessage::encode)
                .decoder(SyncTradeOutputsMessage::decode)
                .consumerMainThread(SyncTradeOutputsMessage::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Map<Item, List<Item>> tradeOutputs) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncTradeOutputsMessage(tradeOutputs));
    }
}
