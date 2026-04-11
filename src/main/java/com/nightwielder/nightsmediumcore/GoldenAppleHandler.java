// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.core.particles.ParticleTypes;
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
        if (mode.equals("crystal"))
            return;

        boolean isEnchanted = consumed == Items.ENCHANTED_GOLDEN_APPLE;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        int currentLost = data.getHeartsLost(player.getUUID());
        if (currentLost <= 0)
            return;

        boolean isCreative = player.getAbilities().instabuild;

        if (isEnchanted)
        {
            // Enchanted golden apple — restore all hearts, no cooldowns
            data.setHeartsLost(player.getUUID(), 0);
            HeartLossHandler.applyModifier(player, 0);
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
                    return;
            }

            // Restore 1 heart
            int newLost = currentLost - 1;
            data.setHeartsLost(player.getUUID(), newLost);
            HeartLossHandler.applyModifier(player, newLost);

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
