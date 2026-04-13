// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodRelicHandler
{
    private static final float HOSTILE_DROP_CHANCE = 0.005F;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!(event.player instanceof ServerPlayer player))
            return;
        if (!ModList.get().isLoaded("curios"))
            return;

        boolean has = hasInMainInventory(player);
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        if (has)
        {
            BloodRelicItem.applyOrUpdateModifier(player);
            if (player.tickCount % 20 == 0)
            {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, true, false, true));
            }
        }
        else
        {
            AttributeModifier existing = healthAttr.getModifier(BloodRelicItem.MODIFIER_UUID);
            if (existing != null)
            {
                healthAttr.removeModifier(BloodRelicItem.MODIFIER_UUID);
                if (player.getHealth() > player.getMaxHealth())
                    player.setHealth(player.getMaxHealth());
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event)
    {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player)
            return;
        if (!(victim instanceof Enemy))
            return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer))
            return;
        if (!playerHasBloodRelic(killer))
            return;

        if (killer.getRandom().nextFloat() < HOSTILE_DROP_CHANCE)
        {
            ServerLevel level = killer.serverLevel();
            ItemStack stack = new ItemStack(ModItems.LIVING_HEART.get());
            ItemEntity drop = new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), stack);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
    }

    private static boolean hasInMainInventory(Player player)
    {
        for (int i = 0; i < 36; i++)
        {
            if (player.getInventory().getItem(i).is(ModItems.BLOOD_RELIC.get()))
                return true;
        }
        return false;
    }

    private static boolean playerHasBloodRelic(ServerPlayer player)
    {
        if (hasInMainInventory(player))
            return true;
        if (ModList.get().isLoaded("curios"))
        {
            try
            {
                return CuriosItemFactory.isEquippedInCurios(player, ModItems.BLOOD_RELIC.get());
            }
            catch (Throwable ignored) {}
        }
        return false;
    }
}
