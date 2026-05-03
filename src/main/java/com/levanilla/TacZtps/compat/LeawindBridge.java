package com.levanilla.TacZtps.compat;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public final class LeawindBridge {
    private LeawindBridge() {
    }

    public static boolean isThirdPersonRendering() {
        return ThirdPerson.isAvailable() && ThirdPersonStatus.isRenderingInThirdPerson();
    }

    public static boolean shouldRenderCrosshair() {
        return ThirdPersonStatus.shouldRenderThirdPersonCrosshair();
    }

    public static void turnCamera(float dYRot, float dXRot) {
        ThirdPerson.CAMERA_AGENT.turnCamera(dYRot, dXRot);
    }

    public static void syncPlayerRotationToCrosshairTarget(LocalPlayer player) {
        HitResult hitResult = ThirdPerson.CAMERA_AGENT.getHitResult();
        if (hitResult != null && syncPlayerRotationToTarget(player, hitResult.getLocation())) {
            return;
        }

        syncPlayerRotationToCamera(player);
    }

    private static void syncPlayerRotationToCamera(LocalPlayer player) {
        Vector2d rotation = ThirdPerson.CAMERA_AGENT.getRotation();
        if (!Double.isFinite(rotation.x) || !Double.isFinite(rotation.y)) {
            return;
        }

        syncPlayerRotation(player, (float) rotation.x, (float) rotation.y);
    }

    private static boolean syncPlayerRotationToTarget(LocalPlayer player, Vec3 target) {
        if (!Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)) {
            return false;
        }

        Vec3 delta = target.subtract(player.getEyePosition(1.0F));
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontalDistance < 1.0E-4D) {
            return false;
        }

        float yRot = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float xRot = (float) -Math.toDegrees(Math.atan2(delta.y, horizontalDistance));
        syncPlayerRotation(player, xRot, yRot);
        return true;
    }

    private static void syncPlayerRotation(LocalPlayer player, float xRot, float yRot) {
        player.setXRot(xRot);
        player.setYRot(yRot);

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundMovePlayerPacket.Rot(yRot, xRot, player.onGround()));
        }
    }

    public static void invertPerspective() {
        ThirdPersonStatus.isPerspectiveInverted = true;
    }
}
