package com.levanilla.TacZtps.mixin.client;

import com.github.leawind.thirdperson.core.CameraAgent;
import com.github.leawind.thirdperson.util.math.Zone;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CameraAgent.class, remap = false)
public abstract class MixinLeawindCameraAgent {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Camera tempCamera;

    @Redirect(
            method = "limitRotateCenter(Lorg/joml/Vector3d;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/leawind/thirdperson/util/math/Zone;withMax(D)Lcom/github/leawind/thirdperson/util/math/Zone;"
            ),
            remap = false
    )
    private Zone levanilla$safeWithMax(Zone zone, double max) {
        return zone.withMax(Math.max(max, zone.min));
    }

    @Redirect(
            method = "limitRotateCenter(Lorg/joml/Vector3d;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/leawind/thirdperson/util/math/Zone;withMin(D)Lcom/github/leawind/thirdperson/util/math/Zone;"
            ),
            remap = false
    )
    private Zone levanilla$safeWithMin(Zone zone, double min) {
        return zone.withMin(Math.min(min, zone.max));
    }

    @Inject(
            method = "updateTempCameraRotationPosition(F)V",
            at = @At("HEAD"),
            remap = false
    )
    private void levanilla$bindTempCameraEntity(float partialTick, CallbackInfo ci) {
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            cameraEntity = minecraft.player;
        }
        if (cameraEntity == null) {
            return;
        }

        MixinCameraAccessor tempCameraAccessor = (MixinCameraAccessor) tempCamera;
        float eyeHeight = cameraEntity.getEyeHeight();
        tempCameraAccessor.levanilla$setEntity(cameraEntity);
        tempCameraAccessor.levanilla$setLevel(cameraEntity.level());
        tempCameraAccessor.levanilla$setEyeHeightOld(eyeHeight);
        tempCameraAccessor.levanilla$setEyeHeight(eyeHeight);
    }
}
