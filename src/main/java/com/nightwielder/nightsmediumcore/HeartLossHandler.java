package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class HeartLossHandler
{
    public static final UUID MODIFIER_UUID = UUID.fromString("d5ec7a62-1b8c-4f92-9e5a-3c7d1f0a2b4e");
    private static final String MODIFIER_NAME = "nightsmediumcore.heart_loss";
    // Default max health is 20 (10 hearts). Floor is 6 (3 hearts). Max loss = 7 hearts.
    public static final int MAX_HEARTS = 10;
    public static final int MIN_HEARTS = 3;
    // 60 seconds * 20 ticks per second
    private static final int DEATH_GRACE_TICKS = 1200;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        // Check death grace period
        long graceExpiry = data.getDeathGraceExpiry(player.getUUID());
        if (currentTime < graceExpiry)
        {
            int remainingHearts = MAX_HEARTS - data.getHeartsLost(player.getUUID());
            player.sendSystemMessage(
                    Component.literal("You died! Heart protected by recent death grace period. You still have " +
                            remainingHearts + " max hearts.")
                            .withStyle(ChatFormatting.GOLD));
            return;
        }

        int currentLost = data.getHeartsLost(player.getUUID());
        int maxLoss = MAX_HEARTS - MIN_HEARTS;

        if (currentLost >= maxLoss)
        {
            player.sendSystemMessage(
                    Component.literal("You died! You are at the minimum of " + MIN_HEARTS + " hearts.")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        int newLost = currentLost + 1;
        data.setHeartsLost(player.getUUID(), newLost);
        data.setDeathGraceExpiry(player.getUUID(), currentTime + DEATH_GRACE_TICKS);

        int remainingHearts = MAX_HEARTS - newLost;

        player.sendSystemMessage(
                Component.literal("You died! You now have " + remainingHearts + " max hearts remaining.")
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

        applyModifier(player, heartsLost);
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
            // No reduction needed, just clamp health
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
