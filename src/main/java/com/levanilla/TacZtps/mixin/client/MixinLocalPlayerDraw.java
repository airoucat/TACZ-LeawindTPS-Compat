package com.levanilla.TacZtps.mixin.client;

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

        // AutoTools縺ｯ mc.hitResult 縺・null 縺ｮ譎ゅ↓豁ｦ蝎ｨ繧呈戟縺｡譖ｿ縺医ｋ縺ｨ繧ｯ繝ｩ繝・す繝･縺吶ｋ縲・
        // 騾壻ｿ｡蜃ｦ逅・ｒ繧ｭ繝｣繝ｳ繧ｻ繝ｫ縺吶ｋ縺ｨ蠑ｾ縺悟・縺ｪ縺上↑繧九◆繧√√ム繝溘・縺ｮ縲檎ｩｺ謖ｯ繧雁愛螳・miss)縲阪ｒ貂｡縺励※繧・ｊ驕弱＃縺吶・
        if (mc.hitResult == null && mc.player != null) {
            mc.hitResult = BlockHitResult.miss(mc.player.position(), Direction.UP, mc.player.blockPosition());
        }
    }
}
