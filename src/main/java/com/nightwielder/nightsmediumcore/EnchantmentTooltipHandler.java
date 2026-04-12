// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class EnchantmentTooltipHandler
{
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (enchants.isEmpty())
            return;

        if (enchants.containsKey(ModEnchantments.LIFE_LEECH.get()))
        {
            tooltip.add(Component.literal("Each level heals 3% of damage dealt to players.")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Max level 3.")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (enchants.containsKey(ModEnchantments.LIFE_STEAL.get()))
        {
            tooltip.add(Component.literal("Each level heals 3% of damage dealt to hostile mobs.")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Max level 3.")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (enchants.containsKey(ModEnchantments.VAMPIRISM.get()))
        {
            tooltip.add(Component.literal("Each level gives a 0.1% chance for hostile mobs to drop a Living Heart on kill.")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Max level 3.")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
