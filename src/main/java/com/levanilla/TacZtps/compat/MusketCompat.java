package com.levanilla.TacZtps.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class MusketCompat {
    private static final String MOD_ID = "musketmod";
    private static final String GUN_ITEM_CLASS_NAME = "ewewukek.musketmod.GunItem";
    private static final String CLIENT_UTILITIES_CLASS_NAME = "ewewukek.musketmod.ClientUtilities";

    private static boolean lookupAttempted;
    private static Class<?> gunItemClass;
    private static Method isReadyMethod;

    private static boolean clientUtilitiesLookupAttempted;
    private static Field attackKeyDownField;

    private MusketCompat() {
    }

    public static boolean isReadyGun(ItemStack stack) {
        if (stack.isEmpty() || !resolveGunItemApi()) {
            return false;
        }

        if (!gunItemClass.isInstance(stack.getItem())) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(isReadyMethod.invoke(null, stack));
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | LinkageError ignored) {
            return false;
        }
    }

    public static void setAttackKeyDown(boolean attackKeyDown) {
        if (!resolveClientUtilitiesApi()) {
            return;
        }

        try {
            attackKeyDownField.setBoolean(null, attackKeyDown);
        } catch (IllegalAccessException | IllegalArgumentException | LinkageError ignored) {
        }
    }

    private static boolean resolveGunItemApi() {
        if (lookupAttempted) {
            return gunItemClass != null && isReadyMethod != null;
        }

        lookupAttempted = true;
        if (!ModList.get().isLoaded(MOD_ID)) {
            return false;
        }

        try {
            Class<?> resolvedGunItemClass = Class.forName(
                    GUN_ITEM_CLASS_NAME,
                    false,
                    MusketCompat.class.getClassLoader()
            );
            Method resolvedIsReadyMethod = resolvedGunItemClass.getMethod("isReady", ItemStack.class);
            if (resolvedIsReadyMethod.getReturnType() != boolean.class) {
                return false;
            }

            gunItemClass = resolvedGunItemClass;
            isReadyMethod = resolvedIsReadyMethod;
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean resolveClientUtilitiesApi() {
        if (clientUtilitiesLookupAttempted) {
            return attackKeyDownField != null;
        }

        clientUtilitiesLookupAttempted = true;
        if (!ModList.get().isLoaded(MOD_ID)) {
            return false;
        }

        try {
            Class<?> clientUtilitiesClass = Class.forName(
                    CLIENT_UTILITIES_CLASS_NAME,
                    false,
                    MusketCompat.class.getClassLoader()
            );
            Field resolvedAttackKeyDownField = clientUtilitiesClass.getField("attackKeyDown");
            if (resolvedAttackKeyDownField.getType() != boolean.class) {
                return false;
            }

            attackKeyDownField = resolvedAttackKeyDownField;
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }
}
