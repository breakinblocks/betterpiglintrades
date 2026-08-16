package com.breakinblocks.betterpiglintrades.mixin;

import com.breakinblocks.betterpiglintrades.BetterPiglinTrades;
import com.breakinblocks.betterpiglintrades.data.PiglinTrade;
import com.breakinblocks.betterpiglintrades.data.PiglinTradeManager;
import com.breakinblocks.betterpiglintrades.data.TradeItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
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
        if (TradeItems.isTradeItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canAdmire", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$canAdmire(Piglin piglin, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!TradeItems.isTradeItem(stack)) {
            return;
        }

        boolean admiringDisabled = piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
        boolean admiringItem = piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);

        if (!admiringDisabled && !admiringItem && piglin.isAdult()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "wantsToPickup", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$wantsToPickup(Piglin piglin, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ItemTags.PIGLIN_REPELLENTS) || stack.is(Items.GOLD_INGOT)) {
            return;
        }

        if (TradeItems.isTradeItem(stack)) {
            cir.setReturnValue(piglin.canPickUpLoot() && piglin.getOffhandItem().isEmpty());
        }
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$stopHoldingOffHandItem(ServerLevel level, Piglin piglin, boolean barterSuccess, CallbackInfo ci) {
        ItemStack offhandItem = piglin.getItemInHand(InteractionHand.OFF_HAND);

        Optional<PiglinTrade> tradeOpt = PiglinTradeManager.INSTANCE.getTradeForItem(offhandItem);
        if (tradeOpt.isPresent() && piglin.isAdult()) {
            PiglinTrade trade = tradeOpt.get();
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            if (barterSuccess) {
                betterpiglintrades$throwItems(piglin, betterpiglintrades$generateLoot(level, piglin, trade));
            }
            ci.cancel();
        }
    }

    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void betterpiglintrades$getBarterResponseItems(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        ItemStack heldItem = piglin.getOffhandItem();

        Optional<PiglinTrade> tradeOpt = PiglinTradeManager.INSTANCE.getTradeForItem(heldItem);
        if (tradeOpt.isPresent() && piglin.level() instanceof ServerLevel serverLevel) {
            cir.setReturnValue(betterpiglintrades$generateLoot(serverLevel, piglin, tradeOpt.get()));
        }
    }

    @Unique
    private static void betterpiglintrades$throwItems(Piglin piglin, List<ItemStack> items) {
        if (items.isEmpty()) {
            return;
        }

        Vec3 target = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER)
                .map(Entity::position)
                .orElseGet(() -> {
                    Vec3 nearby = LandRandomPos.getPos(piglin, 4, 2);
                    return nearby == null ? piglin.position() : nearby;
                });

        piglin.swing(InteractionHand.OFF_HAND);
        for (ItemStack stack : items) {
            BehaviorUtils.throwItem(piglin, stack, target.add(0.0, 1.0, 0.0));
        }
    }

    @Unique
    private static List<ItemStack> betterpiglintrades$generateLoot(ServerLevel level, Piglin piglin, PiglinTrade trade) {
        try {
            ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, trade.lootTable().orElseThrow());
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableKey);

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
