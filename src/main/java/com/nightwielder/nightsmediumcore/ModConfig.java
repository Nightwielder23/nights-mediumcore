package com.nightwielder.nightsmediumcore;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig
{
    public static final ForgeConfigSpec.IntValue HEART_FLOOR;
    public static final ForgeConfigSpec.IntValue DEATH_GRACE_PERIOD_SECONDS;
    public static final ForgeConfigSpec.IntValue CRYSTAL_COMBAT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue BED_REGEN_COOLDOWN_MINUTES;
    public static final ForgeConfigSpec.BooleanValue SHOW_HARDCORE_HEARTS;

    public static final ForgeConfigSpec SPEC;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hearts");

        HEART_FLOOR = builder
                .comment("Minimum number of hearts a player can be reduced to (1-10)")
                .defineInRange("heartFloor", 3, 1, 10);

        DEATH_GRACE_PERIOD_SECONDS = builder
                .comment("Seconds after a heart-losing death during which another death will not remove a heart")
                .defineInRange("deathGracePeriodSeconds", 60, 0, 600);

        SHOW_HARDCORE_HEARTS = builder
                .comment("Display hardcore-style heart textures instead of normal hearts")
                .define("showHardcoreHearts", true);

        builder.pop();
        builder.push("crystals");

        CRYSTAL_COMBAT_COOLDOWN_SECONDS = builder
                .comment("Cooldown in seconds after using any life crystal before another can be used")
                .defineInRange("crystalCombatCooldownSeconds", 30, 0, 3600);

        BED_REGEN_COOLDOWN_MINUTES = builder
                .comment("Cooldown in minutes for bed heart regeneration")
                .defineInRange("bedRegenCooldownMinutes", 15, 0, 1440);

        builder.pop();

        SPEC = builder.build();
    }
}
