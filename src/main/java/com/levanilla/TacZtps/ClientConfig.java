package com.levanilla.TacZtps;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_AIMING;
    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_SCOPING;

    static {
        BUILDER.push("perspective");

        SWITCH_FIRST_PERSON_AIMING = BUILDER
                .comment(
                        "When true, aiming any TaCZ gun forces Leawind's Third Person into first-person transition.",
                        "This option takes precedence over switch_first_person_scoping."
                )
                .define("switch_first_person_aiming", false);

        SWITCH_FIRST_PERSON_SCOPING = BUILDER
                .comment(
                        "When true, aiming a TaCZ gun with an external or built-in scope forces Leawind's Third Person into first-person transition."
                )
                .define("switch_first_person_scoping", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ClientConfig() {
    }
}
