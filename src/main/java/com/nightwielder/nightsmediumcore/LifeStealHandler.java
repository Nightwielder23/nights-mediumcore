// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class LifeStealHandler
{
    public static final UUID BONUS_MODIFIER_UUID = UUID.fromString("a7b3c1d2-4e5f-6a7b-8c9d-0e1f2a3b4c5d");
    private static final String BONUS_MODIFIER_NAME = "nights_mediumcore.lifesteal_bonus";

    public static int resolvedHeartCap()
    {
        int configured = ModConfig.LIFESTEAL_HEART_CAP.get();
        if (configured >= 0)
            return configured;
        return ModConfig.MEDIUMCORE_ENABLED.get() ? 10 : Integer.MAX_VALUE;
    }

    public static void applyBonusModifier(ServerPlayer player, int bonus)
    {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null)
            return;
        if (attr.getModifier(BONUS_MODIFIER_UUID) != null)
            attr.removeModifier(BONUS_MODIFIER_UUID);
        if (bonus > 0)
        {
            AttributeModifier mod = new AttributeModifier(
                    BONUS_MODIFIER_UUID, BONUS_MODIFIER_NAME, bonus * 2.0D,
                    AttributeModifier.Operation.ADDITION);
            attr.addPermanentModifier(mod);
        }
        float newMax = player.getMaxHealth();
        if (player.getHealth() > newMax)
            player.setHealth(newMax);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                player.getId(), java.util.Collections.singleton(attr)));

        ModAdvancements.checkMaxHealthMilestones(player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        applyBonusModifier(player, data.getLifeStealHearts(player.getUUID()));
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        applyBonusModifier(player, data.getLifeStealHearts(player.getUUID()));
    }
}
