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
    private static final UUID MODIFIER_UUID = HeartRelicHandler.RELIC_MODIFIER_UUID;
    private static final String MODIFIER_NAME = "nightsmediumcore.heart_relic";

    public HeartRelicItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack)
    {
        // Modifier is managed manually in curioTick/onEquip/onUnequip using a fixed UUID
        // to prevent stacking from Curios' auto-management
        return HashMultimap.create();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player))
            return;

        applyOrUpdateModifier(player);

        // Apply Regen 1 every 20 ticks with 25-tick duration
        if (player.tickCount % 20 == 0)
        {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, true, false, true));
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player)
        {
            applyOrUpdateModifier(player);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, true, false, true));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player)
        {
            AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null)
            {
                healthAttr.removeModifier(MODIFIER_UUID);

                if (player.getHealth() > player.getMaxHealth())
                {
                    player.setHealth(player.getMaxHealth());
                }

                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }

            player.removeEffect(MobEffects.REGENERATION);
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)
    {
        return true;
    }

    private void applyOrUpdateModifier(ServerPlayer player)
    {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        // Calculate base max health excluding the relic modifier
        double currentMax = player.getMaxHealth();
        AttributeModifier existing = healthAttr.getModifier(MODIFIER_UUID);
        if (existing != null)
        {
            currentMax -= existing.getAmount();
        }

        // ceil(currentTotalMaxHearts * 0.20) * 2 health points
        double baseHearts = currentMax / 2.0;
        int bonusHearts = (int) Math.ceil(baseHearts * 0.2);
        double bonusHP = bonusHearts * 2.0;

        // Only apply or update if the value changed
        if (existing == null || existing.getAmount() != bonusHP)
        {
            if (existing != null)
            {
                healthAttr.removeModifier(MODIFIER_UUID);
            }
            healthAttr.addPermanentModifier(new AttributeModifier(
                    MODIFIER_UUID, MODIFIER_NAME, bonusHP, AttributeModifier.Operation.ADDITION));

            player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                    player.getId(), Collections.singleton(healthAttr)));
        }
    }
}
