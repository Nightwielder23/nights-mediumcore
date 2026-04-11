package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private static final String MODIFIER_NAME = "nightsmediumcore.heart_loss";
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

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        int minHearts = getMinHearts();

        // Check death grace period
        long graceExpiry = data.getDeathGraceExpiry(player.getUUID());
        if (currentTime < graceExpiry)
        {
            player.sendSystemMessage(
                    Component.literal("Heart protected \u2014 death grace active.")
                            .withStyle(ChatFormatting.GOLD));
            return;
        }

        int currentLost = data.getHeartsLost(player.getUUID());
        int maxLoss = MAX_HEARTS - minHearts;

        if (currentLost >= maxLoss)
        {
            player.sendSystemMessage(
                    Component.literal("You died! You are at the minimum of " + minHearts + " base hearts.")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        int newLost = currentLost + 1;
        data.setHeartsLost(player.getUUID(), newLost);
        data.setDeathGraceExpiry(player.getUUID(), currentTime + getDeathGraceTicks());

        int remainingHearts = MAX_HEARTS - newLost;

        player.sendSystemMessage(
                Component.literal("You died and lost a heart! You now have " + remainingHearts + " base hearts remaining.")
                        .withStyle(ChatFormatting.RED));
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
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        // Only trigger on natural wake-up (not manually leaving bed)
        if (event.wakeImmediately())
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        // Check bed regen cooldown
        long bedExpiry = data.getBedRegenCooldown(player.getUUID());
        if (currentTime < bedExpiry)
            return;

        int currentLost = data.getHeartsLost(player.getUUID());
        if (currentLost <= 0)
            return;

        int currentHearts = MAX_HEARTS - currentLost;
        if (currentHearts >= 7)
            return;

        // Restore 1 heart
        int newLost = currentLost - 1;
        data.setHeartsLost(player.getUUID(), newLost);
        applyModifier(player, newLost);

        // Apply bed regen cooldown
        int cooldownTicks = ModConfig.BED_REGEN_COOLDOWN_MINUTES.get() * 60 * 20;
        data.setBedRegenCooldown(player.getUUID(), currentTime + cooldownTicks);

        int newHearts = MAX_HEARTS - newLost;
        player.sendSystemMessage(
                Component.literal("You feel rested and restored a heart! You now have " + newHearts + " base hearts.")
                        .withStyle(ChatFormatting.GREEN));
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

        markInCombat(player);
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

        // Remove existing modifier if present
        AttributeModifier existing = healthAttr.getModifier(MODIFIER_UUID);
        if (existing != null)
        {
            healthAttr.removeModifier(MODIFIER_UUID);
        }

        if (heartsLost <= 0)
        {
            float newMax = player.getMaxHealth();
            if (player.getHealth() > newMax)
            {
                player.setHealth(newMax);
            }
            return;
        }

        // Each heart = 2 health points
        double reduction = -(heartsLost * 2.0);
        AttributeModifier modifier = new AttributeModifier(
                MODIFIER_UUID, MODIFIER_NAME, reduction, AttributeModifier.Operation.ADDITION);
        healthAttr.addPermanentModifier(modifier);

        // Clamp current health if it exceeds new max
        float newMax = player.getMaxHealth();
        if (player.getHealth() > newMax)
        {
            player.setHealth(newMax);
        }
    }
}
