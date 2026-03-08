package fun.teamti.tacztps.mixin.client;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow protected abstract void setRotation(float pYRot, float pXRot);
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Inject(method = "setup", at = @At("TAIL"))
    private void applyTaczRecoilToLeawind(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {

        if (ThirdPerson.isAvailable() && ThirdPersonStatus.isRenderingInThirdPerson()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            try {
                // インターフェースにない隠しメソッドを、プレイヤーオブジェクトから直接探し出す（最強の検索ロジック）
                Object player = mc.player;
                Method getRecoilMethod = null;

                for (Method m : player.getClass().getMethods()) {
                    if (m.getName().toLowerCase().contains("recoil") && m.getParameterCount() == 0) {
                        getRecoilMethod = m;
                        break;
                    }
                }

                if (getRecoilMethod != null) {
                    Object recoilData = getRecoilMethod.invoke(player);
                    if (recoilData != null) {
                        float pitch = 0, yaw = 0;
                        for (Method m : recoilData.getClass().getMethods()) {
                            String name = m.getName().toLowerCase();
                            if (name.contains("pitch") && m.getReturnType() == float.class) {
                                pitch = (float) m.invoke(recoilData);
                            } else if (name.contains("yaw") && m.getReturnType() == float.class) {
                                yaw = (float) m.invoke(recoilData);
                            }
                        }

                        // Leawindのカメラ角度に反動を強制加算
                        if (pitch != 0 || yaw != 0) {
                            this.setRotation(this.yRot + yaw, this.xRot + pitch);
                        }
                    }
                }
            } catch (Exception ignored) {
                // エラー時は何もしない（クラッシュ防止）
            }
        }
    }
}