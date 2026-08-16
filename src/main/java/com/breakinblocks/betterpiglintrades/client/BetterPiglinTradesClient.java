package com.breakinblocks.betterpiglintrades.client;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = BetterPiglinTrades.MOD_ID, value = Dist.CLIENT)
public class BetterPiglinTradesClient {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTradeOutputCache.clear();
    }
}
