// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public class CrystalHeartItem extends Item
{
    private final int heartsToRestore;
    private final String itemDisplayName;

    public CrystalHeartItem(Properties properties, int heartsToRestore, String itemDisplayName)
    {
        super(properties);
        this.heartsToRestore = heartsToRestore;
        this.itemDisplayName = itemDisplayName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        if (!(player instanceof ServerPlayer serverPlayer))
            return InteractionResultHolder.pass(stack);

        // Block crystal usage in "apple" mode
        if (ModConfig.HEART_RECOVERY_MODE.get().equals("apple"))
            return InteractionResultHolder.fail(stack);

        ServerLevel overworld = serverPlayer.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        boolean isSupreme = heartsToRestore >= HeartLossHandler.MAX_HEARTS;

        boolean isCreative = serverPlayer.getAbilities().instabuild;

        // Check if player needs healing first (creative mode bypasses this check)
        int currentLost = data.getHeartsLost(serverPlayer.getUUID());
        if (currentLost <= 0 && !isCreative)
        {
            serverPlayer.sendSystemMessage(
                    Component.literal("You are already at maximum base hearts!")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        // Check combat status (skip for supreme crystal and creative mode)
        if (!isSupreme && !isCreative)
        {
            long combatExpiry = data.getCombatCooldown(serverPlayer.getUUID());
            if (currentTime < combatExpiry)
            {
                long remainingSeconds = (combatExpiry - currentTime) / 20;
                serverPlayer.sendSystemMessage(
                        Component.literal("Cannot use Crystal Heart while in combat! " + remainingSeconds + "s remaining.")
                                .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }
        }

        // Check usage cooldown (skip for supreme crystal and creative mode)
        if (!isSupreme && !isCreative)
        {
            long expiry = data.getCrystalCooldown(serverPlayer.getUUID());
            if (currentTime < expiry)
            {
                long remainingTicks = expiry - currentTime;
                long remainingSeconds = remainingTicks / 20;
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                serverPlayer.sendSystemMessage(
                        Component.literal("Crystal is on cooldown! " + minutes + "m " + seconds + "s remaining.")
                                .withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.fail(stack);
            }
        }

        // Restore hearts
        int newLost;
        int actualRestore;
        if (isSupreme)
        {
            newLost = 0;
            actualRestore = currentLost;
        }
        else
        {
            actualRestore = Math.min(heartsToRestore, currentLost);
            newLost = currentLost - actualRestore;
        }
        data.setHeartsLost(serverPlayer.getUUID(), newLost);

        // Apply the updated modifier
        HeartLossHandler.applyModifier(serverPlayer, newLost);

        // Apply usage cooldown (skip for supreme crystal and creative mode)
        if (!isSupreme && !isCreative)
        {
            int cooldownTicks = ModConfig.CRYSTAL_USAGE_COOLDOWN_SECONDS.get() * 20;
            data.setCrystalCooldown(serverPlayer.getUUID(), currentTime + cooldownTicks);
        }

        // Consume item
        if (!serverPlayer.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        // Apply enchanted golden apple effects for supreme crystal (30 seconds = 600 ticks)
        if (isSupreme)
        {
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        }
        else
        {
            // Regular crystal heart grants Regen 1 for 10 seconds (200 ticks)
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
        }

        // Spawn heart particles around the player
        ServerLevel serverLevel = serverPlayer.serverLevel();
        double px = serverPlayer.getX();
        double py = serverPlayer.getY() + 1.0;
        double pz = serverPlayer.getZ();
        serverLevel.sendParticles(ParticleTypes.HEART, px, py, pz, 12, 0.5, 0.5, 0.5, 0.1);

        // Play level-up sound at reduced pitch
        serverLevel.playSound(null, serverPlayer.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.7f);

        // Force health sync to client
        serverPlayer.setHealth(serverPlayer.getMaxHealth());

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
