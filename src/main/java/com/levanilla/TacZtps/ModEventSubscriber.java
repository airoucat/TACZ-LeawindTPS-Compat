package com.levanilla.TacZtps;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.levanilla.TacZtps.compat.RecoilCallGuard;
import com.tacz.guns.client.event.CameraSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = ThirdPersonTacz.MOD_ID)
public final class ModEventSubscriber {
    private ModEventSubscriber() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCameraRotateThirdPerson(ViewportEvent.ComputeCameraAngles event) {
        if (!LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        float oldXRot = player.getXRot();
        float oldYRot = player.getYRot();

        RecoilCallGuard.runManual(() -> CameraSetupEvent.applyCameraRecoil(event));

        float dXRot = player.getXRot() - oldXRot;
        float dYRot = player.getYRot() - oldYRot;

        player.setXRot(oldXRot);
        player.setYRot(oldYRot);

        LeawindBridge.turnCamera(dYRot, dXRot);
    }
}
