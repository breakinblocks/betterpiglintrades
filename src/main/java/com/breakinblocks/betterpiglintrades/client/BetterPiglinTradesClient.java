package com.breakinblocks.betterpiglintrades.client;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterPiglinTrades.MOD_ID, value = Dist.CLIENT)
public class BetterPiglinTradesClient {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTradeOutputCache.clear();
    }
}
