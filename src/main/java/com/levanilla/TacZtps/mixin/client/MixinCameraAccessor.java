package com.levanilla.TacZtps.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface MixinCameraAccessor {
    @Accessor("entity")
    void levanilla$setEntity(Entity entity);

    @Accessor("level")
    void levanilla$setLevel(BlockGetter level);

    @Accessor("eyeHeightOld")
    void levanilla$setEyeHeightOld(float eyeHeightOld);

    @Accessor("eyeHeight")
    void levanilla$setEyeHeight(float eyeHeight);
}
