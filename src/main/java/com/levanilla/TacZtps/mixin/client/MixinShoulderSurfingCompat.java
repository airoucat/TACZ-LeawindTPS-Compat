package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShoulderSurfingCompat.class, remap = false)
public final class MixinShoulderSurfingCompat {
    @Inject(
            method = "showCrosshair()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void levanilla$forceCrosshairForLeawind(CallbackInfoReturnable<Boolean> cir) {
        if (LeawindBridge.shouldRenderCrosshair()) {
            cir.setReturnValue(true);
        }
    }
}
