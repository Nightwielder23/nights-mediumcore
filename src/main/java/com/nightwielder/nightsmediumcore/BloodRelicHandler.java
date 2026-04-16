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
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodRelicHandler
{
    public static final UUID MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f23456789012");
    private static final String MODIFIER_NAME = "nights_mediumcore.blood_relic";
    private static final float HOSTILE_DROP_CHANCE = 0.005F;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!(event.player instanceof ServerPlayer player))
            return;

        // When Curios is loaded, BloodRelicItem handles everything via ICurioItem
        if (ModList.get().isLoaded("curios"))
            return;

        // Inventory fallback — only runs when Curios is NOT installed
        boolean has = hasInMainInventory(player);
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        if (has)
        {
            applyOrUpdateModifier(player);
            if (player.tickCount % 20 == 0)
            {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, true, false, true));
            }
        }
        else
        {
            AttributeModifier existing = healthAttr.getModifier(MODIFIER_UUID);
            if (existing != null)
            {
                healthAttr.removeModifier(MODIFIER_UUID);
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

    public static void applyOrUpdateModifier(ServerPlayer player)
    {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        AttributeModifier existing = healthAttr.getModifier(MODIFIER_UUID);
        HeartLossData data = HeartLossData.get(player.server.overworld());
        int heartsLost = data.getHeartsLost(player.getUUID());
        double mediumcoreHearts = HeartLossHandler.MAX_HEARTS - heartsLost;
        int bonusHearts = (int) Math.ceil(mediumcoreHearts * 0.2);
        double bonusHP = bonusHearts * 2.0;
        if (bonusHP < 4.0)
            bonusHP = 4.0;

        if (existing == null || existing.getAmount() != bonusHP)
        {
            if (existing != null)
                healthAttr.removeModifier(MODIFIER_UUID);
            healthAttr.addPermanentModifier(new AttributeModifier(
                    MODIFIER_UUID, MODIFIER_NAME, bonusHP, AttributeModifier.Operation.ADDITION));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                    player.getId(), Collections.singleton(healthAttr)));
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
                return CuriosCheck.isEquipped(player);
            }
            catch (Throwable ignored) {}
        }
        return false;
    }

    // Inner helper — isolates Curios API references so this class verifies and loads
    // cleanly when Curios is absent. Only touched behind a ModList.isLoaded check.
    private static final class CuriosCheck
    {
        static boolean isEquipped(ServerPlayer player)
        {
            return CuriosItemFactory.isEquippedInCurios(player, ModItems.BLOOD_RELIC.get());
        }
    }
}
