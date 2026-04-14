// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class LifeStealHandler
{
    public static final UUID BONUS_MODIFIER_UUID = UUID.fromString("a7b3c1d2-4e5f-6a7b-8c9d-0e1f2a3b4c5d");
    private static final String BONUS_MODIFIER_NAME = "nightsmediumcore.lifesteal_bonus";

    public static double resolvedDropChance()
    {
        double configured = ModConfig.LIFESTEAL_DROP_CHANCE.get();
        if (configured >= 0.0D)
            return configured;
        return ModConfig.MEDIUMCORE_ENABLED.get() ? 0.5D : 1.0D;
    }

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
        data.setLifeStealRespawnTime(player.getUUID(), overworld.getGameTime());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!ModConfig.LIFESTEAL_ENABLED.get())
            return;
        if (!(event.getEntity() instanceof ServerPlayer victim))
            return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer))
            return;
        if (victim.getUUID().equals(killer.getUUID()))
            return;

        ServerLevel overworld = victim.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long now = overworld.getGameTime();

        long respawnTime = data.getLifeStealRespawnTime(victim.getUUID());
        int cooldownTicks = ModConfig.LIFESTEAL_RESPAWN_COOLDOWN.get() * 20;
        if (respawnTime > 0 && now - respawnTime < cooldownTicks)
            return;

        UUID vid = victim.getUUID();
        int lsHearts = data.getLifeStealHearts(vid);
        if (lsHearts > 0)
        {
            data.setLifeStealHearts(vid, lsHearts - 1);
        }
        else
        {
            int minHearts = ModConfig.HEART_FLOOR.get();
            int currentLost = data.getHeartsLost(vid);
            int maxLoss = HeartLossHandler.MAX_HEARTS - minHearts;
            if (currentLost < maxLoss)
            {
                data.setHeartsLost(vid, currentLost + 1);
            }
        }

        double chance = resolvedDropChance();
        if (victim.getRandom().nextDouble() < chance)
        {
            ItemStack stack = new ItemStack(ModItems.LIVING_HEART.get());
            ItemEntity drop = new ItemEntity(overworld, victim.getX(), victim.getY(), victim.getZ(), stack);
            drop.setDefaultPickUpDelay();
            overworld.addFreshEntity(drop);
        }
    }
}
