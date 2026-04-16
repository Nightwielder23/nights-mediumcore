// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemTooltipHandler
{
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event)
    {
        Item item = event.getItemStack().getItem();
        List<Component> tooltip = event.getToolTip();

        if (item == ModItems.CRYSTAL_SHARD.get())
        {
            addLore(tooltip, "A shard of crystallized resolve, warm with borrowed life.");
        }
        else if (item == ModItems.CRYSTAL_HEART.get())
        {
            addEffect(tooltip, "Right-click to restore one lost heart.");
            addEffect(tooltip, "Grants Regeneration II.");
            addDetail(tooltip, "3 minute cooldown \u00b7 unusable within 30s of combat.");
            addLore(tooltip, "A heart forged from patience and pressure.");
        }
        else if (item == ModItems.SUPREME_CRYSTAL_HEART.get())
        {
            addEffect(tooltip, "Restores every lost heart at once.");
            addEffect(tooltip, "Grants Enchanted Golden Apple effects.");
            addLore(tooltip, "The first heart, carved from stone that refused to die.");
        }
        else if (item == ModItems.HEART_RELIC.get())
        {
            addDetail(tooltip, "Worn in a Curios charm slot:");
            addEffect(tooltip, "+20% max hearts and permanent Regeneration I.");
            addLore(tooltip, "An heirloom that remembers every heartbeat before yours.");
        }
        else if (item == ModItems.BLOOD_SHARD.get())
        {
            addLore(tooltip, "A fragment of clotted life, pulsing faintly in the dark.");
        }
        else if (item == ModItems.BLOODY_HEART.get())
        {
            addLore(tooltip, "A heart drowned in crimson \u2014 raw material for darker work.");
        }
        else if (item == ModItems.BLOOD_RELIC.get())
        {
            addEffect(tooltip, "Keeps all Heart Relic effects.");
            addEffect(tooltip, "0.5% chance for hostile mobs to drop a Living Heart on kill.");
            addLore(tooltip, "A relic steeped in the blood of a thousand fallen beasts.");
        }
        else if (item == ModItems.VAMPIRIC_SCYTHE.get())
        {
            addEffect(tooltip, "Heals 12% of damage dealt.");
            addEffect(tooltip, "0.5% chance for mobs to drop a Living Heart on kill.");
            addLore(tooltip, "A curved edge that drinks deeply from every wound.");
        }
    }

    private static void addEffect(List<Component> tooltip, String text)
    {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.DARK_RED));
    }

    private static void addDetail(List<Component> tooltip, String text)
    {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.GRAY));
    }

    private static void addLore(List<Component> tooltip, String text)
    {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
