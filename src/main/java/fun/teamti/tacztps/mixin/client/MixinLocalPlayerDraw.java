package fun.teamti.tacztps.mixin.client;

import com.tacz.guns.client.gameplay.LocalPlayerDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LocalPlayerDraw.class, remap = false)
public class MixinLocalPlayerDraw {

    @Inject(method = "draw", at = @At("HEAD"))
    private void preventAutoToolsCrash(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // AutoToolsは mc.hitResult が null の時に武器を持ち替えるとクラッシュする。
        // 通信処理をキャンセルすると弾が出なくなるため、ダミーの「空振り判定(miss)」を渡してやり過ごす。
        if (mc.hitResult == null && mc.player != null) {
            mc.hitResult = BlockHitResult.miss(mc.player.position(), Direction.UP, mc.player.blockPosition());
        }
    }
}