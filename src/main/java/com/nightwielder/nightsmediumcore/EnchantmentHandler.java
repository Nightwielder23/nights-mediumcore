// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnchantmentHandler
{
    private static final float VAMPIRISM_PER_LEVEL = 0.001F;
    private static final float LIFESTEAL_PER_LEVEL = 0.03F;

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

        int level = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.VAMPIRISM.get(), killer.getMainHandItem());
        if (level <= 0)
            return;

        float chance = level * VAMPIRISM_PER_LEVEL;
        if (killer.getRandom().nextFloat() < chance)
        {
            ServerLevel level2 = killer.serverLevel();
            ItemStack stack = new ItemStack(ModItems.LIVING_HEART.get());
            ItemEntity drop = new ItemEntity(level2, victim.getX(), victim.getY(), victim.getZ(), stack);
            drop.setDefaultPickUpDelay();
            level2.addFreshEntity(drop);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event)
    {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker))
            return;
        LivingEntity victim = event.getEntity();
        if (attacker.equals(victim))
            return;

        ItemStack weapon = attacker.getMainHandItem();
        int enchLevel;
        if (victim instanceof Player)
            enchLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.LIFE_LEECH.get(), weapon);
        else if (victim instanceof Enemy)
            enchLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.LIFE_STEAL.get(), weapon);
        else
            return;

        if (enchLevel <= 0)
            return;

        float heal = event.getAmount() * enchLevel * LIFESTEAL_PER_LEVEL;
        if (heal > 0)
            attacker.heal(heal);
    }
}
