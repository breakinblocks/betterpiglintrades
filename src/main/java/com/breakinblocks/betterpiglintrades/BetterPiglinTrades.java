package com.breakinblocks.betterpiglintrades;

import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import com.breakinblocks.betterpiglintrades.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

@Mod(BetterPiglinTrades.MOD_ID)
public class BetterPiglinTrades {
    public static final String MOD_ID = "betterpiglintrades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterPiglinTrades() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PiglinTradeManager.INSTANCE);
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(serverPlayer, PiglinTradeManager.INSTANCE.getResolvedOutputs());
        }
    }
}
