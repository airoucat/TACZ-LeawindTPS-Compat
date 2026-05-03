package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.levanilla.TacZtps.compat.RecoilCallGuard;
import com.tacz.guns.client.event.CameraSetupEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CameraSetupEvent.class, remap = false)
public final class MixinCameraSetupEvent {
    @Inject(
            method = "applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void levanilla$cancelNativeRecoilForLeawind(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        if (!RecoilCallGuard.isManualCall() && LeawindBridge.isThirdPersonRendering()) {
            ci.cancel();
        }
    }
}
