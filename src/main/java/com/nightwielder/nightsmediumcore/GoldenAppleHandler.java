// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoldenAppleHandler
{
    // Cache the item being consumed since Finish event fires after shrink(1),
    // which makes getItem() return Items.AIR when the last apple is eaten.
    private static final Map<UUID, Item> consumingItem = new HashMap<>();

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        Item item = event.getItem().getItem();
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE)
        {
            consumingItem.put(player.getUUID(), item);
        }
    }

    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            consumingItem.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        consumingItem.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        Item consumed = consumingItem.remove(player.getUUID());
        if (consumed == null)
            return;

        String mode = ModConfig.HEART_RECOVERY_MODE.get();
        if (mode.equals("crystal") || mode.equals("none"))
            return;

        boolean isEnchanted = consumed == Items.ENCHANTED_GOLDEN_APPLE;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        int currentLost = data.getHeartsLost(player.getUUID());
        int currentLs = data.getLifeStealHearts(player.getUUID());
        int lsCap = HeartLossHandler.MAX_HEARTS;
        boolean isCreative = player.getAbilities().instabuild;
        boolean mcOn = ModConfig.MEDIUMCORE_ENABLED.get();
        boolean lsOnly = !mcOn && ModConfig.LIFESTEAL_ENABLED.get();

        if (isEnchanted)
        {
            // Enchanted golden apple: no cooldown gating
            if (mcOn)
            {
                // Restore all mediumcore + lifesteal hearts
                if (currentLost <= 0 && currentLs >= lsCap)
                    return;
                data.setHeartsLost(player.getUUID(), 0);
                HeartLossHandler.applyModifier(player, 0);
                data.setLifeStealHearts(player.getUUID(), lsCap);
                LifeStealHandler.applyBonusModifier(player, lsCap);
            }
            else if (lsOnly)
            {
                // Restore 2 lifesteal hearts (up to cap)
                if (currentLs >= lsCap)
                    return;
                int add = Math.min(2, lsCap - currentLs);
                int newLs = currentLs + add;
                data.setLifeStealHearts(player.getUUID(), newLs);
                LifeStealHandler.applyBonusModifier(player, newLs);
            }
            else
            {
                return;
            }
        }
        else
        {
            // Regular golden apple — check combat cooldown if enabled
            if (!isCreative && ModConfig.APPLE_COMBAT_COOLDOWN.get())
            {
                long combatExpiry = data.getCombatCooldown(player.getUUID());
                if (currentTime < combatExpiry)
                    return;
            }

            // Check apple cooldown if configured
            int cooldownSeconds = ModConfig.APPLE_COOLDOWN_SECONDS.get();
            if (!isCreative && cooldownSeconds > 0)
            {
                long appleExpiry = data.getAppleCooldown(player.getUUID());
                if (currentTime < appleExpiry)
                {
                    long remainingSeconds = (appleExpiry - currentTime) / 20;
                    long minutes = remainingSeconds / 60;
                    long seconds = remainingSeconds % 60;
                    player.sendSystemMessage(Component.literal(
                            "Golden apple heart restore is on cooldown! " + minutes + "m " + seconds + "s remaining.")
                            .withStyle(ChatFormatting.YELLOW));
                    return;
                }
            }

            // Restore 1 heart: mediumcore first, then lifesteal
            if (mcOn && currentLost > 0)
            {
                int newLost = currentLost - 1;
                data.setHeartsLost(player.getUUID(), newLost);
                HeartLossHandler.applyModifier(player, newLost);
            }
            else if (currentLs < lsCap)
            {
                int newLs = currentLs + 1;
                data.setLifeStealHearts(player.getUUID(), newLs);
                LifeStealHandler.applyBonusModifier(player, newLs);
            }
            else
            {
                return;
            }

            // Apply apple cooldown if configured
            if (!isCreative && cooldownSeconds > 0)
            {
                data.setAppleCooldown(player.getUUID(), currentTime + (long) cooldownSeconds * 20);
            }
        }

        // Heart particles and level-up sound
        ServerLevel serverLevel = player.serverLevel();
        serverLevel.sendParticles(ParticleTypes.HEART,
                player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.5, 0.5, 0.5, 0.1);
        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.7f);
        player.setHealth(player.getMaxHealth());
    }
}
