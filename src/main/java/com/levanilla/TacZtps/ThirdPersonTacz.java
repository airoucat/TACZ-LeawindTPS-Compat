package com.levanilla.TacZtps;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value = ThirdPersonTacz.MOD_ID, dist = Dist.CLIENT)
public final class ThirdPersonTacz {
    public static final String MOD_ID = "levanilla_tacztps";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThirdPersonTacz(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "tac-leawindtps.toml");
    }
}
