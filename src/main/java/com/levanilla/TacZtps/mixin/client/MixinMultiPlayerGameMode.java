package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.levanilla.TacZtps.compat.MusketCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MultiPlayerGameMode.class, priority = 1500)
public final class MixinMultiPlayerGameMode {
    @Inject(method = "useItem", at = @At("HEAD"))
    private void levanilla$refreshMusketAttackKey(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!(player instanceof LocalPlayer localPlayer) || !LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        ItemStack stack = localPlayer.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && MusketCompat.isReadyGun(stack)) {
            MusketCompat.setAttackKeyDown(Minecraft.getInstance().options.keyAttack.isDown());
        }
    }

    @Inject(
            method = "useItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            )
    )
    private void levanilla$syncMusketShotRotation(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!(player instanceof LocalPlayer localPlayer) || !LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        ItemStack stack = localPlayer.getItemInHand(hand);
        if (MusketCompat.isReadyGun(stack)) {
            LeawindBridge.syncPlayerRotationToCrosshairTarget(localPlayer);
        }
    }
}
