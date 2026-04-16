// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class VampiricScytheItem extends SwordItem
{
    public VampiricScytheItem(Properties properties)
    {
        super(Tiers.NETHERITE, 9, -3.0F, properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        if (isBlockedEnchantment(enchantment))
            return false;
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book)
    {
        return true;
    }

    public static boolean isBlockedEnchantment(Enchantment enchantment)
    {
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null)
            return false;
        String path = id.getPath().toLowerCase();
        return path.contains("life_leech") || path.contains("life_steal")
                || path.contains("vampirism") || path.contains("lifesteal");
    }
}
