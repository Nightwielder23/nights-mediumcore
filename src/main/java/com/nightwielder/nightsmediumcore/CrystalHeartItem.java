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

        // Determine whether hearts are restored and how many
        boolean restoresHearts;
        int mcRestore = 0;
        int lsRestore = 0;

        if (noneMode)
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
            int want = isSupreme ? 2 : 1;
            lsRestore = Math.min(want, Math.max(0, lsCap - currentLs));
        }

        // If restoration would do nothing, refuse (creative bypasses)
        if (restoresHearts && mcRestore <= 0 && lsRestore <= 0 && !isCreative)
        {
            serverPlayer.sendSystemMessage(
                    Component.literal("You are already at maximum hearts!")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        // Combat and usage cooldowns: skip for supreme and creative
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

        // Apply heart restoration
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

        // Apply usage cooldown (skip for supreme and creative)
        if (!isSupreme && !isCreative)
        {
            int cooldownTicks = ModConfig.CRYSTAL_USAGE_COOLDOWN_SECONDS.get() * 20;
            data.setCrystalCooldown(serverPlayer.getUUID(), currentTime + cooldownTicks);
        }

        // Consume item
        if (!isCreative)
            stack.shrink(1);

        // Apply effects per spec
        if (isSupreme)
        {
            if (noneMode || mcOn)
            {
                // crystal/both/apple with mediumcore on — full suite
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            else
            {
                // lifesteal only — crystal: no effects, both: Regen 2 for 10s
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
                // lifesteal only — crystal: no regen, both: Regen 2 for 10s
                if (mode.equals("both"))
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            }
        }

        // Particles + sound + sync
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
