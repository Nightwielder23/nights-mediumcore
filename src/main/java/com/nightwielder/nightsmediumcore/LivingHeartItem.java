// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LivingHeartItem extends Item
{
    public LivingHeartItem(Properties properties)
    {
        super(properties);
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
        boolean lifeStealOn = ModConfig.LIFESTEAL_ENABLED.get();
        boolean isCreative = serverPlayer.getAbilities().instabuild;

        int heartsLost = data.getHeartsLost(serverPlayer.getUUID());
        int currentLs = data.getLifeStealHearts(serverPlayer.getUUID());
        int mcMax = HeartLossHandler.MAX_HEARTS;
        int lsMax = HeartLossHandler.MAX_HEARTS;
        int mcHearts = mcMax - heartsLost;

        if (mcHearts >= mcMax && currentLs >= lsMax)
        {
            serverPlayer.sendSystemMessage(Component.literal(
                    "You have reached the maximum heart cap!").withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        if (!lifeStealOn && !isCreative)
        {
            long expiry = data.getCrystalCooldown(serverPlayer.getUUID());
            if (currentTime < expiry)
            {
                long remainingSeconds = (expiry - currentTime) / 20;
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                serverPlayer.sendSystemMessage(
                        Component.literal("Living Heart is on cooldown! " + minutes + "m " + seconds + "s remaining.")
                                .withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.fail(stack);
            }
        }

        if (mcHearts < mcMax)
        {
            int newLost = heartsLost - 1;
            data.setHeartsLost(serverPlayer.getUUID(), newLost);
            HeartLossHandler.applyModifier(serverPlayer, newLost);
        }
        else
        {
            int newLs = currentLs + 1;
            data.setLifeStealHearts(serverPlayer.getUUID(), newLs);
            LifeStealHandler.applyBonusModifier(serverPlayer, newLs);
        }

        if (!lifeStealOn && !isCreative)
        {
            int cooldownTicks = ModConfig.CRYSTAL_USAGE_COOLDOWN_SECONDS.get() * 20;
            data.setCrystalCooldown(serverPlayer.getUUID(), currentTime + cooldownTicks);
        }

        String mode = ModConfig.HEART_RECOVERY_MODE.get();
        boolean mcOn = ModConfig.MEDIUMCORE_ENABLED.get();
        boolean giveRegen = mcOn || mode.equals("both");
        if (giveRegen)
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

        if (!serverPlayer.getAbilities().instabuild)
            stack.shrink(1);

        ServerLevel sl = serverPlayer.serverLevel();
        sl.sendParticles(ParticleTypes.HEART,
                serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                12, 0.5, 0.5, 0.5, 0.1);
        sl.playSound(null, serverPlayer.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);

        serverPlayer.setHealth(serverPlayer.getMaxHealth());

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.literal("Soulbound").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Grants +1 permanent max heart").withStyle(ChatFormatting.RED));
    }
}
