package com.breakinblocks.betterpiglintrades;

import com.breakinblocks.betterpiglintrades.data.OutputEntry;
import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import com.breakinblocks.betterpiglintrades.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@Mod(BetterPiglinTrades.MOD_ID)
public class BetterPiglinTrades {
    public static final String MOD_ID = "betterpiglintrades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public BetterPiglinTrades() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PiglinTradeManager.INSTANCE);
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        Map<Item, List<OutputEntry>> outputs = PiglinTradeManager.INSTANCE.getResolvedOutputs();
        for (ServerPlayer player : event.getPlayers()) {
            NetworkHandler.sendToPlayer(player, outputs);
        }
    }
}
