package com.nightwielder.nightsmediumcore;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoldenAppleHandler
{
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        String mode = ModConfig.HEART_RECOVERY_MODE.get();
        if (mode.equals("crystal"))
            return;

        ItemStack stack = event.getItem();
        boolean isEnchanted = stack.is(Items.ENCHANTED_GOLDEN_APPLE);
        boolean isRegular = stack.is(Items.GOLDEN_APPLE);

        if (!isEnchanted && !isRegular)
            return;

        ServerLevel overworld = player.server.overworld();
        HeartLossData data = HeartLossData.get(overworld);
        long currentTime = overworld.getGameTime();

        int currentLost = data.getHeartsLost(player.getUUID());
        if (currentLost <= 0)
            return;

        if (isEnchanted)
        {
            // Enchanted golden apple — restore all hearts, no cooldown
            data.setHeartsLost(player.getUUID(), 0);
            HeartLossHandler.applyModifier(player, 0);
        }
        else
        {
            // Regular golden apple — check combat cooldown
            long combatExpiry = data.getCombatCooldown(player.getUUID());
            if (currentTime < combatExpiry)
                return;

            // Check apple cooldown (2 minutes)
            long appleExpiry = data.getAppleCooldown(player.getUUID());
            if (currentTime < appleExpiry)
                return;

            // Restore 1 heart
            int newLost = currentLost - 1;
            data.setHeartsLost(player.getUUID(), newLost);
            HeartLossHandler.applyModifier(player, newLost);

            // Apply 2 minute cooldown (2400 ticks)
            data.setAppleCooldown(player.getUUID(), currentTime + 2400);
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
