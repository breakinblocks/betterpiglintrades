package com.breakinblocks.betterpiglintrades;

import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import com.breakinblocks.betterpiglintrades.network.SyncTradeOutputsPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(BetterPiglinTrades.MOD_ID)
public class BetterPiglinTrades {
    public static final String MOD_ID = "betterpiglintrades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public BetterPiglinTrades(IEventBus eventBus, ModContainer container, Dist dist) {
        eventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MOD_ID);
        registrar.playToClient(
                SyncTradeOutputsPayload.TYPE,
                SyncTradeOutputsPayload.STREAM_CODEC,
                SyncTradeOutputsPayload::handleOnClient
        );
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(id("piglin_trades"), PiglinTradeManager.INSTANCE);
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        SyncTradeOutputsPayload payload = new SyncTradeOutputsPayload(PiglinTradeManager.INSTANCE.getResolvedOutputs());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}
