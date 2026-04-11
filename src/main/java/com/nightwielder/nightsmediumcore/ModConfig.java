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
                .comment("The minimum number of base hearts a player can be reduced to.",
                         "Players will never lose hearts below this floor, even on death.",
                         "Range: 1 to 10. Default: 3")
                .defineInRange("heartFloor", 3, 1, 10);

        DEATH_GRACE_PERIOD_SECONDS = builder
                .comment("After losing a heart on death, the player is protected from losing",
                         "another heart for this many seconds. Set to 0 to disable.",
                         "Range: 0 to 600. Default: 60")
                .defineInRange("deathGracePeriodSeconds", 60, 0, 600);

        BED_REGEN_COOLDOWN_MINUTES = builder
                .comment("After restoring a heart by sleeping in a bed, the player must wait",
                         "this many minutes before sleeping can restore another heart.",
                         "Bed regen only works when the player has fewer than 7 base hearts.",
                         "Range: 0 to 1440. Default: 15")
                .defineInRange("bedRegenCooldownMinutes", 15, 0, 1440);

        SHOW_HARDCORE_HEARTS = builder
                .comment("When true, replaces the normal heart HUD with hardcore-style hearts.",
                         "Lost base hearts are shown as empty hardcore outlines.",
                         "Set to false to use the vanilla heart display instead.",
                         "Default: true")
                .define("showHardcoreHearts", true);

        builder.pop();
        builder.push("crystals");

        CRYSTAL_COMBAT_COOLDOWN_SECONDS = builder
                .comment("Controls two related cooldowns for life crystals:",
                         "1. After taking or dealing damage, the player cannot use a crystal for this many seconds.",
                         "2. After successfully using a crystal, the player cannot use another for this many seconds.",
                         "Range: 0 to 3600. Default: 30")
                .defineInRange("crystalCombatCooldownSeconds", 30, 0, 3600);

        builder.pop();

        SPEC = builder.build();
    }
}
