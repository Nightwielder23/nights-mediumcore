package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public class LifeCrystalItem extends Item
{
    private final int heartsToRestore;
    private final String itemDisplayName;

    public LifeCrystalItem(Properties properties, int heartsToRestore, String itemDisplayName)
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

        // Check cooldown
        long currentTime = overworld.getGameTime();
        long expiry = data.getCooldownExpiry(serverPlayer.getUUID());
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

        // Check if player needs healing
        int currentLost = data.getHeartsLost(serverPlayer.getUUID());
        if (currentLost <= 0)
        {
            serverPlayer.sendSystemMessage(
                    Component.literal("You are already at maximum health!")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stack);
        }

        // Restore hearts
        int actualRestore = Math.min(heartsToRestore, currentLost);
        int newLost = currentLost - actualRestore;
        data.setHeartsLost(serverPlayer.getUUID(), newLost);

        // Apply the updated modifier
        HeartLossHandler.applyModifier(serverPlayer, newLost);

        // Apply cooldown from config
        int cooldownTicks = ModConfig.CRYSTAL_COMBAT_COOLDOWN_SECONDS.get() * 20;
        data.setCooldownExpiry(serverPlayer.getUUID(), currentTime + cooldownTicks);

        // Consume item
        if (!serverPlayer.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        // Send message
        int currentHearts = HeartLossHandler.MAX_HEARTS - newLost;
        serverPlayer.sendSystemMessage(
                Component.literal("Used " + itemDisplayName + "! Restored " + actualRestore +
                        " heart" + (actualRestore > 1 ? "s" : "") + ". You now have " +
                        currentHearts + " max hearts.")
                        .withStyle(ChatFormatting.GREEN));

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
