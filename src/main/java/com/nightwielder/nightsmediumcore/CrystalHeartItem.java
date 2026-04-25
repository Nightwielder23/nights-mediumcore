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

        ServerLevel overworld = serverPlayer.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();
        boolean isSupreme = heartsToRestore >= HeartLossHandler.MAX_HEARTS;
        boolean isCreative = serverPlayer.getAbilities().instabuild;

        String mode = ModConfig.HEART_RECOVERY_MODE.get();
        boolean mcOn = ModConfig.MEDIUMCORE_ENABLED.get();
        boolean lsOnly = !mcOn && ModConfig.LIFESTEAL_ENABLED.get();

        if (!mcOn && !lsOnly)
            return InteractionResultHolder.fail(stack);
        if (lsOnly && mode.equals("apple"))
            return InteractionResultHolder.fail(stack);

        boolean noneMode = mode.equals("none");

        int currentLost = data.getHeartsLost(serverPlayer.getUUID());
        int currentLs = data.getLifeStealHearts(serverPlayer.getUUID());
        int lsCap = LifeStealHandler.resolvedHeartCap();

        boolean restoresHearts;
        int mcRestore = 0;
        int lsRestore = 0;

        if (lsOnly && isSupreme && (mode.equals("crystal") || mode.equals("none") || mode.equals("both")))
        {
            restoresHearts = true;
            int totalCap = Math.max(lsCap, 20);
            lsRestore = Math.min(2, Math.max(0, totalCap - currentLs));
        }
        else if (noneMode)
        {
            restoresHearts = false;
        }
        else if (mcOn)
        {
            if (mode.equals("apple"))
            {
                restoresHearts = false;
            }
            else
            {
                restoresHearts = true;
                mcRestore = isSupreme ? currentLost : Math.min(1, currentLost);
            }
        }
        else
        {
            restoresHearts = true;
            lsRestore = Math.min(1, Math.max(0, lsCap - currentLs));
        }

        // Creative bypasses the no-op refusal
        if (restoresHearts && mcRestore <= 0 && lsRestore <= 0 && !isCreative)
        {
            serverPlayer.sendSystemMessage(
                    Component.literal("You are already at maximum hearts!")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        // Supreme and creative skip combat and usage cooldowns
        if (!isSupreme && !isCreative && restoresHearts)
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

            long expiry = data.getCrystalCooldown(serverPlayer.getUUID());
            if (currentTime < expiry)
            {
                long remainingSeconds = (expiry - currentTime) / 20;
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                serverPlayer.sendSystemMessage(
                        Component.literal("Crystal is on cooldown! " + minutes + "m " + seconds + "s remaining.")
                                .withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.fail(stack);
            }
        }

        if (mcRestore > 0)
        {
            int newLost = currentLost - mcRestore;
            data.setHeartsLost(serverPlayer.getUUID(), newLost);
            HeartLossHandler.applyModifier(serverPlayer, newLost);
        }
        if (lsRestore > 0)
        {
            int newLs = currentLs + lsRestore;
            data.setLifeStealHearts(serverPlayer.getUUID(), newLs);
            LifeStealHandler.applyBonusModifier(serverPlayer, newLs);
        }

        if (!isSupreme && !isCreative && restoresHearts)
        {
            int cooldownTicks = ModConfig.CRYSTAL_USAGE_COOLDOWN_SECONDS.get() * 20;
            data.setCrystalCooldown(serverPlayer.getUUID(), currentTime + cooldownTicks);
        }

        if (!isCreative)
            stack.shrink(1);

        if (isSupreme)
        {
            if (noneMode || mcOn)
            {
                // crystal, both, or apple mode with mediumcore on: full effect suite
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            else
            {
                // lifesteal only. crystal mode gives no effects; both mode gets Regen 2 for 10s
                if (mode.equals("both"))
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            }
        }
        else
        {
            if (noneMode)
            {
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            }
            else if (mcOn)
            {
                if (mode.equals("apple"))
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));
                else
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            }
            else
            {
                // lifesteal only. crystal mode gives no regen; both mode gets Regen 2 for 10s
                if (mode.equals("both"))
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            }
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.sendParticles(ParticleTypes.HEART,
                serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                12, 0.5, 0.5, 0.5, 0.1);
        serverLevel.playSound(null, serverPlayer.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.7f);
        serverPlayer.setHealth(serverPlayer.getMaxHealth());

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
