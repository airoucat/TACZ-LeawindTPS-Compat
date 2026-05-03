package com.levanilla.TacZtps.mixin.client;

import com.tacz.guns.client.gameplay.LocalPlayerDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LocalPlayerDraw.class, remap = false)
public final class MixinLocalPlayerDraw {
    @Inject(
            method = "draw(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void levanilla$preventNullHitResultCrash(ItemStack lastItem, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hitResult == null && mc.player != null) {
            mc.hitResult = BlockHitResult.miss(
                    mc.player.position(),
                    Direction.UP,
                    mc.player.blockPosition()
            );
        }
    }
}
