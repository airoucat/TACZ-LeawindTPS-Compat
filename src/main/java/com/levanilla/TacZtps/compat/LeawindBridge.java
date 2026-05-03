package com.levanilla.TacZtps.compat;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;

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

    public static void invertPerspective() {
        ThirdPersonStatus.isPerspectiveInverted = true;
    }
}
