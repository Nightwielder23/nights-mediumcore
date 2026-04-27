// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig
{
    public static final ForgeConfigSpec.IntValue FLOOR_HEARTS;
    public static final ForgeConfigSpec.IntValue DEATH_GRACE_PERIOD_SECONDS;
    public static final ForgeConfigSpec.IntValue CRYSTAL_COMBAT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue CRYSTAL_USAGE_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue BED_REGEN_COOLDOWN_MINUTES;
    public static final ForgeConfigSpec.IntValue BED_REGEN_HEART_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue SHOW_HARDCORE_HEARTS;
    public static final ForgeConfigSpec.BooleanValue RESPAWN_IMMUNITY_ENABLED;
    public static final ForgeConfigSpec.IntValue RESPAWN_IMMUNITY_SECONDS;
    public static final ForgeConfigSpec.ConfigValue<String> HEART_RECOVERY_MODE;
    public static final ForgeConfigSpec.BooleanValue APPLE_COMBAT_COOLDOWN;
    public static final ForgeConfigSpec.IntValue APPLE_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue MEDIUMCORE_ENABLED;

    public static final ForgeConfigSpec SPEC;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hearts");

        FLOOR_HEARTS = builder
                .comment("Hard floor for hearts a player can have, regardless of deaths.",
                         "Heart loss can drive a player's effective max down toward this number, but no further.",
                         "Range: 1 to 30. Default: 3")
                .defineInRange("floorHearts", 3, 1, 30);

        DEATH_GRACE_PERIOD_SECONDS = builder
                .comment("After losing a heart on death, the player is protected from losing",
                         "another heart for this many seconds. Set to 0 to disable.",
                         "Range: 0 to 600. Default: 60")
                .defineInRange("deathGracePeriodSeconds", 60, 0, 600);

        BED_REGEN_COOLDOWN_MINUTES = builder
                .comment("After restoring a heart by sleeping in a bed, the player must wait",
                         "this many minutes before sleeping can restore another heart.",
                         "Range: 0 to 1440. Default: 15")
                .defineInRange("bedRegenCooldownMinutes", 15, 0, 1440);

        BED_REGEN_HEART_THRESHOLD = builder
                .comment("The maximum number of base hearts a player can have before bed regen stops.",
                         "If a player has this many or more base hearts, sleeping will not restore a heart.",
                         "Range: 1 to 10. Default: 7")
                .defineInRange("bedRegenHeartThreshold", 7, 1, 10);

        SHOW_HARDCORE_HEARTS = builder
                .comment("When true, replaces the normal heart HUD with hardcore-style hearts.",
                         "Lost base hearts are shown as empty hardcore outlines.",
                         "Set to false to use the vanilla heart display instead.",
                         "Default: true")
                .define("showHardcoreHearts", true);

        RESPAWN_IMMUNITY_ENABLED = builder
                .comment("When true, the player receives temporary damage immunity after respawning.",
                         "Set to false to disable respawn immunity entirely.",
                         "Default: true")
                .define("respawnImmunityEnabled", true);

        RESPAWN_IMMUNITY_SECONDS = builder
                .comment("How many seconds of damage immunity the player receives after respawning.",
                         "Only applies when respawnImmunityEnabled is true.",
                         "Range: 1 to 60. Default: 5")
                .defineInRange("respawnImmunitySeconds", 5, 1, 60);

        MEDIUMCORE_ENABLED = builder
                .comment("When true, the mediumcore heart-loss mechanics are active.",
                         "Default: true")
                .define("mediumcoreEnabled", true);

        builder.pop();
        builder.push("crystals");

        CRYSTAL_COMBAT_COOLDOWN_SECONDS = builder
                .comment("After taking or dealing damage, the player cannot use a Crystal Heart",
                         "for this many seconds. Supreme Crystal Hearts ignore this cooldown.",
                         "Range: 0 to 600. Default: 30")
                .defineInRange("crystalCombatCooldownSeconds", 30, 0, 600);

        CRYSTAL_USAGE_COOLDOWN_SECONDS = builder
                .comment("After successfully using a Crystal Heart, the player cannot use another",
                         "for this many seconds. Supreme Crystal Hearts ignore this cooldown.",
                         "This is separate from the combat cooldown.",
                         "Range: 0 to 3600. Default: 180")
                .defineInRange("crystalUsageCooldownSeconds", 180, 0, 3600);

        HEART_RECOVERY_MODE = builder
                .comment("Controls which methods can restore lost maximum hearts.",
                         "crystal = only Crystal Heart items restore hearts (default).",
                         "apple = only golden apples restore hearts.",
                         "both = both Crystal Hearts and golden apples restore hearts.",
                         "none = no items restore hearts (Crystal Hearts still grant regen; golden apples are vanilla-only).",
                         "Default: crystal")
                .define("heartRecoveryMode", "crystal", v -> v instanceof String s
                        && (s.equals("crystal") || s.equals("apple") || s.equals("both") || s.equals("none")));

        APPLE_COMBAT_COOLDOWN = builder
                .comment("If true, golden apples cannot restore hearts while in combat.",
                         "Enchanted golden apples always ignore this.",
                         "Default: false")
                .define("appleCombatCooldown", false);

        APPLE_COOLDOWN_SECONDS = builder
                .comment("Seconds between golden apple heart restores. Set to 0 to disable.",
                         "Enchanted golden apples always ignore this.",
                         "Range: 0 to 3600. Default: 0")
                .defineInRange("appleCooldownSeconds", 0, 0, 3600);

        builder.pop();

        SPEC = builder.build();
    }
}
