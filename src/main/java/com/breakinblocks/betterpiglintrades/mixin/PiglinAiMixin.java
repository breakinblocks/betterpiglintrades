package com.breakinblocks.betterpiglintrades.mixin;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.data.PiglinTrade;
import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(PiglinAi.class)
public class PiglinAiMixin {

    @Inject(method = "isLovedItem", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$isLovedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PiglinTradeManager.INSTANCE.isValidTradeItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isBarterCurrency", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$isBarterCurrency(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PiglinTradeManager.INSTANCE.isValidTradeItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$mobInteract(Piglin piglin, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (PiglinTradeManager.INSTANCE.isValidTradeItem(itemStack) && !itemStack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
            if (betterpiglintrades$canAdmire(piglin, itemStack)) {
                ItemStack singleItem = itemStack.split(1);
                piglin.setItemInHand(InteractionHand.OFF_HAND, singleItem);
                betterpiglintrades$admireGoldItem(piglin);
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Unique
    private static boolean betterpiglintrades$canAdmire(Piglin piglin, ItemStack stack) {
        boolean admiringDisabled = piglin.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ADMIRING_DISABLED);
        return !admiringDisabled
                && !piglin.isBaby()
                && piglin.getOffhandItem().isEmpty()
                && !piglin.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ADMIRING_ITEM);
    }

    @Unique
    private static void betterpiglintrades$admireGoldItem(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(
                net.minecraft.world.entity.ai.memory.MemoryModuleType.ADMIRING_ITEM,
                true,
                120L
        );
    }

    @Inject(method = "wantsToPickup", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$wantsToPickup(Piglin piglin, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PiglinTradeManager.INSTANCE.isValidTradeItem(stack) && !stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
            cir.setReturnValue(piglin.canPickUpLoot() && piglin.getOffhandItem().isEmpty());
        }
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$stopHoldingOffHandItem(Piglin piglin, boolean barterSuccess, CallbackInfo ci) {
        ItemStack offhandItem = piglin.getItemInHand(InteractionHand.OFF_HAND);

        Optional<PiglinTrade> tradeOpt = PiglinTradeManager.INSTANCE.getTradeForItem(offhandItem);
        if (tradeOpt.isPresent() && piglin.isAdult()) {
            PiglinTrade trade = tradeOpt.get();
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            if (barterSuccess && piglin.level() instanceof ServerLevel serverLevel) {
                List<ItemStack> responseItems = betterpiglintrades$generateLoot(serverLevel, piglin, trade);
                if (!responseItems.isEmpty()) {
                    betterpiglintrades$throwItems(piglin, responseItems);
                }
            }
            ci.cancel();
        }
    }

    @Unique
    private static void betterpiglintrades$throwItems(Piglin piglin, List<ItemStack> items) {
        for (ItemStack stack : items) {
            piglin.spawnAtLocation(stack.copy());
        }
    }

    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$getBarterResponseItems(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        ItemStack heldItem = piglin.getOffhandItem();

        Optional<PiglinTrade> tradeOpt = PiglinTradeManager.INSTANCE.getTradeForItem(heldItem);
        if (tradeOpt.isPresent()) {
            PiglinTrade trade = tradeOpt.get();
            if (piglin.level() instanceof ServerLevel serverLevel) {
                cir.setReturnValue(betterpiglintrades$generateLoot(serverLevel, piglin, trade));
            }
        }
    }

    @Unique
    private static List<ItemStack> betterpiglintrades$generateLoot(ServerLevel level, Piglin piglin, PiglinTrade trade) {
        try {
            ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, trade.lootTable());
            LootTable lootTable = level.getServer().reloadableRegistries()
                    .getLootTable(lootTableKey);

            LootParams lootParams = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, piglin)
                    .create(LootContextParamSets.PIGLIN_BARTER);

            return lootTable.getRandomItems(lootParams);
        } catch (Exception e) {
            BetterPiglinTrades.LOGGER.error("Failed to generate loot for trade {}: {}", trade.lootTable(), e.getMessage());
            return Collections.emptyList();
        }
    }
}
