package fun.teamti.tacztps.mixin.client;

import com.github.leawind.thirdperson.ThirdPersonStatus;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.event.TickAnimationEvent;
import fun.teamti.tacztps.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TickAnimationEvent.class, remap = false)
public class MixinTickAnimationEvent {

    @Inject(method = "tickAnimation", at = @At("HEAD"))
    private static void onTickAnimation(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainhandItem = player.getMainHandItem();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) return;

        IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);

        if (operator != null && operator.isAim()) {

            // ★ ここにあった「体を無理やり頭の向きに同期させて固定する処理」を跡形もなく消し去りました！

            ResourceLocation scopeId = iGun.getAttachmentId(mainhandItem, AttachmentType.SCOPE);
            boolean shouldInvert = false;

            if (ClientConfig.SWITCH_FIRST_PERSON_AIMING.get()) {
                shouldInvert = true;
            } else if (!DefaultAssets.isEmptyAttachmentId(scopeId) && ClientConfig.SWITCH_FIRST_PERSON_SCOPING.get()) {
                shouldInvert = true;
            }

            if (shouldInvert) {
                setLeawindPerspectiveInverted(true);
            }
        }
    }

    private static void setLeawindPerspectiveInverted(boolean value) {
        try {
            Class<?> gameStatusClass = Class.forName("com.github.leawind.api.base.GameStatus");
            java.lang.reflect.Field field = gameStatusClass.getField("isPerspectiveInverted");
            field.setBoolean(null, value);
        } catch (Exception ignored) {}
    }
}