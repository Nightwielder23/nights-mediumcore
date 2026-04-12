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

        int current = data.getLifeStealHearts(serverPlayer.getUUID());
        int cap = LifeStealHandler.resolvedHeartCap();
        if (current >= cap)
        {
            serverPlayer.sendSystemMessage(Component.literal(
                    "You have reached the lifesteal heart cap!").withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        int updated = current + 1;
        data.setLifeStealHearts(serverPlayer.getUUID(), updated);
        LifeStealHandler.applyBonusModifier(serverPlayer, updated);

        data.updatePeakMaxHearts(serverPlayer.getUUID(),
                (int) serverPlayer.getMaxHealth() / 2);

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
