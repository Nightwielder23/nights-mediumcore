// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class HeartLossHandler
{
    public static final UUID MODIFIER_UUID = UUID.fromString("d5ec7a62-1b8c-4f92-9e5a-3c7d1f0a2b4e");
    private static final String MODIFIER_NAME = "nights_mediumcore.heart_loss";
    public static final int MAX_HEARTS = 10;

    public static int getMinHearts()
    {
        return ModConfig.HEART_FLOOR.get();
    }

    private static int getDeathGraceTicks()
    {
        return ModConfig.DEATH_GRACE_PERIOD_SECONDS.get() * 20;
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (!ModConfig.MEDIUMCORE_ENABLED.get())
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        int minHearts = getMinHearts();

        // Consume a lifesteal heart first if the player has any
        int lsHearts = data.getLifeStealHearts(player.getUUID());
        if (lsHearts > 0)
        {
            data.setLifeStealHearts(player.getUUID(), lsHearts - 1);
            data.setDeathGraceExpiry(player.getUUID(), currentTime + getDeathGraceTicks());
            return;
        }

        // Use base mediumcore hearts from SavedData, not total health including modded bonuses
        int currentLost = data.getHeartsLost(player.getUUID());
        int maxLoss = MAX_HEARTS - minHearts;

        if (currentLost >= maxLoss)
            return;

        // Only check the grace period when a heart could actually be lost
        long graceExpiry = data.getDeathGraceExpiry(player.getUUID());
        if (currentTime < graceExpiry)
            return;

        int newLost = currentLost + 1;
        data.setHeartsLost(player.getUUID(), newLost);
        data.setDeathGraceExpiry(player.getUUID(), currentTime + getDeathGraceTicks());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        int heartsLost = data.getHeartsLost(player.getUUID());

        applyModifier(player, heartsLost);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        int heartsLost = data.getHeartsLost(player.getUUID());

        // Clear combat status on respawn so crystals can be used immediately
        data.setCombatCooldown(player.getUUID(), 0L);

        applyModifier(player, heartsLost);

        if (ModConfig.RESPAWN_IMMUNITY_ENABLED.get())
        {
            int immunityTicks = ModConfig.RESPAWN_IMMUNITY_SECONDS.get() * 20;
            data.setRespawnImmunityExpiry(player.getUUID(), overworld.getGameTime() + immunityTicks);
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        // Only trigger on natural wake-up, not when the player manually leaves the bed
        if (event.wakeImmediately())
            return;

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0, false, true, true));

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        long bedExpiry = data.getBedRegenCooldown(player.getUUID());
        if (currentTime < bedExpiry)
            return;

        int currentLost = data.getHeartsLost(player.getUUID());
        if (currentLost <= 0)
            return;

        int currentHearts = MAX_HEARTS - currentLost;
        if (currentHearts >= ModConfig.BED_REGEN_HEART_THRESHOLD.get())
            return;

        int newLost = currentLost - 1;
        data.setHeartsLost(player.getUUID(), newLost);
        applyModifier(player, newLost);

        int cooldownTicks = ModConfig.BED_REGEN_COOLDOWN_MINUTES.get() * 60 * 20;
        data.setBedRegenCooldown(player.getUUID(), currentTime + cooldownTicks);
    }

    @SubscribeEvent
    public void onPlayerAttack(LivingAttackEvent event)
    {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player))
            return;

        markInCombat(player);
    }

    @SubscribeEvent
    public void onPlayerDamaged(LivingDamageEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        // Cancel all damage while respawn immunity is active
        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        if (ModConfig.RESPAWN_IMMUNITY_ENABLED.get()
                && currentTime < data.getRespawnImmunityExpiry(player.getUUID()))
        {
            event.setCanceled(true);
            return;
        }

        markInCombat(player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!(event.player instanceof ServerPlayer player))
            return;
        if (player.tickCount % 10 != 0)
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        long expiry = data.getRespawnImmunityExpiry(player.getUUID());

        if (expiry > 0 && currentTime >= expiry)
        {
            data.clearRespawnImmunity(player.getUUID());
        }
    }

    private static void markInCombat(ServerPlayer player)
    {
        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        int combatTicks = ModConfig.CRYSTAL_COMBAT_COOLDOWN_SECONDS.get() * 20;
        data.setCombatCooldown(player.getUUID(), currentTime + combatTicks);
    }

    public static void applyModifier(ServerPlayer player, int heartsLost)
    {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        AttributeModifier existing = healthAttr.getModifier(MODIFIER_UUID);
        if (existing != null)
        {
            healthAttr.removeModifier(MODIFIER_UUID);
        }

        if (heartsLost > 0)
        {
            // 2 health points per heart
            double reduction = -(heartsLost * 2.0);
            AttributeModifier modifier = new AttributeModifier(
                    MODIFIER_UUID, MODIFIER_NAME, reduction, AttributeModifier.Operation.ADDITION);
            healthAttr.addPermanentModifier(modifier);
        }

        float newMax = player.getMaxHealth();
        if (player.getHealth() > newMax)
        {
            player.setHealth(newMax);
        }

        // Force-sync attributes so the HUD updates immediately
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                player.getId(), java.util.Collections.singleton(healthAttr)));

        ModAdvancements.checkMaxHealthMilestones(player);
    }
}
