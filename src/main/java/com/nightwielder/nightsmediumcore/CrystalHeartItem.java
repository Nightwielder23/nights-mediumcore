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

        // Check combat status (skip for supreme crystal)
        if (!isSupreme)
        {
            long combatExpiry = data.getCombatCooldown(serverPlayer.getUUID());
            if (currentTime < combatExpiry)
            {
                long remainingSeconds = (combatExpiry - currentTime) / 20;
                serverPlayer.sendSystemMessage(
                        Component.literal("You cannot use this in combat! " + remainingSeconds + "s remaining.")
                                .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }
        }

        // Check cooldown (skip for supreme crystal)
        if (!isSupreme)
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

        // Check if player needs healing
        int currentLost = data.getHeartsLost(serverPlayer.getUUID());
        if (currentLost <= 0)
        {
            serverPlayer.sendSystemMessage(
                    Component.literal("You are already at maximum base hearts!")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
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

        // Apply cooldown (skip for supreme crystal)
        if (!isSupreme)
        {
            int cooldownTicks = ModConfig.CRYSTAL_COMBAT_COOLDOWN_SECONDS.get() * 20;
            data.setCrystalCooldown(serverPlayer.getUUID(), currentTime + cooldownTicks);
        }

        // Consume item
        if (!serverPlayer.getAbilities().instabuild)
        {
            stack.shrink(1);
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
