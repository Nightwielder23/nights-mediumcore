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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeartLossHandler
{
    public static final UUID MODIFIER_UUID = UUID.fromString("d5ec7a62-1b8c-4f92-9e5a-3c7d1f0a2b4e");
    private static final String MODIFIER_NAME = "nights_mediumcore.heart_loss";
    public static final int MAX_HEARTS = 10;

    // UUIDs already handled in their current death. Cleared on respawn.
    private static final Set<UUID> PROCESSED_DEATHS = ConcurrentHashMap.newKeySet();

    public static int getMinHearts()
    {
        return Math.max(ModConfig.BASE_HEARTS.get(), ModConfig.FLOOR_HEARTS.get());
    }

    private static int getDeathGraceTicks()
    {
        return ModConfig.DEATH_GRACE_PERIOD_SECONDS.get() * 20;
    }

    // Player's max HP with our heart-loss and lifesteal modifiers stripped off. Lets the
    // first-login capture work correctly even when saved modifiers are already in place.
    public static double computeNaturalMaxHealth(ServerPlayer player)
    {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return MAX_HEARTS * 2.0;
        double current = player.getMaxHealth();
        AttributeModifier hl = healthAttr.getModifier(MODIFIER_UUID);
        AttributeModifier ls = healthAttr.getModifier(LifeStealHandler.BONUS_MODIFIER_UUID);
        if (hl != null) current -= hl.getAmount();
        if (ls != null) current -= ls.getAmount();
        return current;
    }

    // Player's recorded starting hearts. Falls back to a live computation if none recorded yet.
    public static int getInitialHearts(ServerPlayer player)
    {
        if (player == null || player.server == null)
            return MAX_HEARTS;
        HeartLossData data = HeartLossData.get(player.server.overworld());
        double saved = data.getInitialMaxHealth(player.getUUID());
        if (saved > 0)
            return (int) Math.round(saved / 2.0);
        return (int) Math.round(computeNaturalMaxHealth(player) / 2.0);
    }

    // Use Clone instead of LivingDeathEvent: the Corpse mod delays the original player's
    // removal to spawn its corpse, and reading attributes mid-death makes Clone fire with a
    // null removal reason, which hangs the server thread.
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath())
            return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer))
            return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer))
            return;
        if (!ModConfig.MEDIUMCORE_ENABLED.get())
            return;

        // If something delays the original's removal, getRemovalReason() is null.
        // Skip rather than touch a half-removed entity.
        if (oldPlayer.getRemovalReason() == null)
            return;

        UUID playerId = oldPlayer.getUUID();

        // Skip if Clone has already fired for this death
        if (!PROCESSED_DEATHS.add(playerId))
            return;

        ServerLevel overworld = newPlayer.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        // Consume a lifesteal heart first if the player has any
        int lsHearts = data.getLifeStealHearts(playerId);
        if (lsHearts > 0)
        {
            data.setLifeStealHearts(playerId, lsHearts - 1);
            data.setDeathGraceExpiry(playerId, currentTime + getDeathGraceTicks());
            return;
        }

        // Heart loss is bounded by each player's recorded starting hearts, not a global constant
        int currentLost = data.getHeartsLost(playerId);
        int initialHearts = getInitialHearts(newPlayer);
        int maxLoss = Math.max(0, initialHearts - getMinHearts());

        if (currentLost >= maxLoss)
            return;

        // Only check the grace period when a heart could actually be lost
        long graceExpiry = data.getDeathGraceExpiry(playerId);
        if (currentTime < graceExpiry)
            return;

        int newLost = currentLost + 1;
        data.setHeartsLost(playerId, newLost);
        data.setDeathGraceExpiry(playerId, currentTime + getDeathGraceTicks());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);

        // Record the natural starting max HP on first-ever login. The dynamic floor uses this.
        if (!data.hasInitialMaxHealth(player.getUUID()))
        {
            data.setInitialMaxHealth(player.getUUID(), computeNaturalMaxHealth(player));
        }

        int heartsLost = data.getHeartsLost(player.getUUID());

        // If the floor was raised since they last logged in, clamp existing loss to the new bound
        int initialHearts = getInitialHearts(player);
        int maxLoss = Math.max(0, initialHearts - getMinHearts());
        if (heartsLost > maxLoss)
        {
            heartsLost = maxLoss;
            data.setHeartsLost(player.getUUID(), heartsLost);
        }

        applyModifier(player, heartsLost);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        PROCESSED_DEATHS.remove(player.getUUID());

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

        int currentHearts = getInitialHearts(player) - currentLost;
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
