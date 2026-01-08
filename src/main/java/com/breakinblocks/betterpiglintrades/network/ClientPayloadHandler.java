package com.breakinblocks.betterpiglintrades.network;

import com.breakinblocks.betterpiglintrades.client.ClientTradeOutputCache;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleSyncTradeOutputs(SyncTradeOutputsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientTradeOutputCache.updateCache(payload.tradeOutputs());
        });
    }
}
