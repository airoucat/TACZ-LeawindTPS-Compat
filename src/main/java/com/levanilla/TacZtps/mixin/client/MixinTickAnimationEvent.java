package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.ClientConfig;
import com.levanilla.TacZtps.compat.LeawindBridge;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.event.TickAnimationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TickAnimationEvent.class, remap = false)
public final class MixinTickAnimationEvent {
    @Inject(
            method = "tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V",
            at = @At("HEAD"),
            remap = false
    )
    private static void levanilla$switchFirstPersonWhenAiming(ClientTickEvent.Pre event, CallbackInfo ci) {
        if (!LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }

        IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
        if (!operator.isAim()) {
            return;
        }

        ResourceLocation scopeId = iGun.getAttachmentId(mainHandItem, AttachmentType.SCOPE);
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(mainHandItem, AttachmentType.SCOPE);
        }

        boolean hasScope = !scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID);
        boolean shouldInvert = ClientConfig.SWITCH_FIRST_PERSON_AIMING.get()
                || (hasScope && ClientConfig.SWITCH_FIRST_PERSON_SCOPING.get());

        if (shouldInvert) {
            LeawindBridge.invertPerspective();
        }
    }
}
