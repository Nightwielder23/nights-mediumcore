// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

public class ModAdvancements
{
    public static void grant(ServerPlayer player, String name)
    {
        ServerAdvancementManager manager = player.server.getAdvancements();
        Advancement adv = manager.getAdvancement(new ResourceLocation(NightsMediumcore.MODID, name));
        if (adv == null)
            return;
        PlayerAdvancements tracker = player.getAdvancements();
        AdvancementProgress progress = tracker.getOrStartProgress(adv);
        if (progress.isDone())
            return;
        for (String criterion : progress.getRemainingCriteria())
        {
            tracker.award(adv, criterion);
        }
    }

    public static void checkMaxHealthMilestones(ServerPlayer player)
    {
        float max = player.getMaxHealth();
        if (max >= 30.0F)
            grant(player, "heart_hoarder");
        if (max >= 40.0F)
            grant(player, "full_health");
    }
}
