// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments
{
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NightsMediumcore.MODID);

    public static final RegistryObject<Enchantment> LIFE_LEECH = ENCHANTMENTS.register(
            "life_leech", () -> new WeaponEnchantment(Enchantment.Rarity.RARE, 3));

    public static final RegistryObject<Enchantment> LIFE_STEAL = ENCHANTMENTS.register(
            "life_steal", () -> new WeaponEnchantment(Enchantment.Rarity.RARE, 3));

    public static class WeaponEnchantment extends Enchantment
    {
        private final int maxLevel;

        public WeaponEnchantment(Rarity rarity, int maxLevel)
        {
            super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
            this.maxLevel = maxLevel;
        }

        @Override
        public int getMaxLevel()
        {
            return maxLevel;
        }

        @Override
        public int getMinCost(int level)
        {
            return 10 + (level - 1) * 8;
        }

        @Override
        public int getMaxCost(int level)
        {
            return getMinCost(level) + 20;
        }

        @Override
        public boolean canEnchant(ItemStack stack)
        {
            if (stack.getItem() == ModItems.VAMPIRIC_SCYTHE.get())
                return false;
            return super.canEnchant(stack);
        }

        @Override
        protected boolean checkCompatibility(Enchantment other)
        {
            return super.checkCompatibility(other) && other != this;
        }
    }
}
