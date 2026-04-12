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

public class BloodRelicItem extends Item implements ICurioItem
{
    public static final UUID MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f23456789012");
    private static final String MODIFIER_NAME = "nightsmediumcore.blood_relic";

    public BloodRelicItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack)
    {
        return HashMultimap.create();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player))
            return;

        applyOrUpdateModifier(player);

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
                    player.setHealth(player.getMaxHealth());
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
}
