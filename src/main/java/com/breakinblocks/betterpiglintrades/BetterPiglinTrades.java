package com.breakinblocks.betterpiglintrades;

import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import com.breakinblocks.betterpiglintrades.network.ClientPayloadHandler;
import com.breakinblocks.betterpiglintrades.network.SyncTradeOutputsPayload;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

@Mod(BetterPiglinTrades.MOD_ID)
public class BetterPiglinTrades {
    public static final String MOD_ID = "betterpiglintrades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterPiglinTrades(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PiglinTradeManager.INSTANCE);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.0.0");

        registrar.playToClient(
                SyncTradeOutputsPayload.TYPE,
                SyncTradeOutputsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSyncTradeOutputs
        );
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SyncTradeOutputsPayload(PiglinTradeManager.INSTANCE.getResolvedOutputs()));
        }
    }
}
