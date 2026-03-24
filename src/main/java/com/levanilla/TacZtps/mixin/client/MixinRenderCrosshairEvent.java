package com.levanilla.TacZtps.mixin.client;

import com.github.leawind.thirdperson.ThirdPersonStatus;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.client.event.RenderCrosshairEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RenderCrosshairEvent.class, remap = false)
public class MixinRenderCrosshairEvent {

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", remap = true))
    private static boolean modifyCrosshairVisibilityCheck(boolean original) {
        return original || ThirdPersonStatus.shouldRenderThirdPersonCrosshair();
    }

    @ModifyExpressionValue(method = { "onRenderOverlay", "lambda$onRenderOverlay$0" }, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/gameplay/IClientPlayerGunOperator;getClientAimingProgress(F)F", remap = false))
    private static float forceShowCrosshairWhenAim(float oldValue) {
        return ThirdPersonStatus.shouldRenderThirdPersonCrosshair() ? 0 : oldValue;
    }
}
