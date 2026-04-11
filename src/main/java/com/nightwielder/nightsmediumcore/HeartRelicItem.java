// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Collections;
import java.util.UUID;

public class HeartRelicItem extends Item implements ICurioItem
{
    public HeartRelicItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack)
    {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slotContext.entity() != null)
        {
            AttributeInstance healthAttr = slotContext.entity().getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null)
            {
                // Calculate base max health excluding this modifier
                double currentMax = healthAttr.getValue();
                AttributeModifier existing = healthAttr.getModifier(uuid);
                if (existing != null)
                {
                    currentMax -= existing.getAmount();
                }

                // 20% of max hearts rounded up, each heart = 2 HP
                int baseHearts = (int) Math.round(currentMax / 2.0);
                int bonusHearts = (int) Math.ceil(baseHearts * 0.2);
                double bonusHP = bonusHearts * 2.0;
                if (bonusHP < 2.0)
                {
                    bonusHP = 2.0;
                }

                modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                        uuid, "nightsmediumcore.heart_relic", bonusHP, AttributeModifier.Operation.ADDITION));
            }
        }
        return modifiers;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player))
            return;
        if (player.tickCount % 35 != 0)
            return;

        // Apply Regen 1 with 40-tick duration, refreshed every 35 ticks
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player)
        {
            // Apply regen immediately on equip
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));

            // Force attribute sync to client
            AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null)
            {
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player)
        {
            // Curios automatically removes modifiers from getAttributeModifiers
            // Clamp health to new max after modifier removal
            AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null)
            {
                if (player.getHealth() > player.getMaxHealth())
                {
                    player.setHealth(player.getMaxHealth());
                }
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }

            // Clear regen effect
            player.removeEffect(MobEffects.REGENERATION);
        }
    }
}
