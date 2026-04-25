// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeartRelicHandler
{
    public static final UUID RELIC_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String MODIFIER_NAME = "nights_mediumcore.heart_relic";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!(event.player instanceof ServerPlayer player))
            return;

        // With Curios loaded, HeartRelicItem handles everything via ICurioItem
        if (ModList.get().isLoaded("curios"))
            return;

        // Inventory-based fallback for when Curios is not installed
        boolean hasRelicInInventory = checkInventory(player);
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        if (hasRelicInInventory)
        {
            AttributeModifier existing = healthAttr.getModifier(RELIC_MODIFIER_UUID);
            HeartLossData data = HeartLossData.get(player.server.overworld());
            int heartsLost = data.getHeartsLost(player.getUUID());
            double mediumcoreHearts = HeartLossHandler.MAX_HEARTS - heartsLost;
            int bonusHearts = (int) Math.ceil(mediumcoreHearts * 0.2);
            double bonusHP = bonusHearts * 2.0;
            if (bonusHP < 4.0)
            {
                bonusHP = 4.0;
            }

            // Only re-apply when the bonus value actually changed
            if (existing == null || existing.getAmount() != bonusHP)
            {
                if (existing != null)
                {
                    healthAttr.removeModifier(RELIC_MODIFIER_UUID);
                }
                healthAttr.addPermanentModifier(new AttributeModifier(
                        RELIC_MODIFIER_UUID, MODIFIER_NAME, bonusHP, AttributeModifier.Operation.ADDITION));

                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }

            // Refresh Regen I once per second with a 5-second duration so it never lapses
            if (player.tickCount % 20 == 0)
            {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
            }
        }
        else
        {
            AttributeModifier existing = healthAttr.getModifier(RELIC_MODIFIER_UUID);
            if (existing != null)
            {
                healthAttr.removeModifier(RELIC_MODIFIER_UUID);

                if (player.getHealth() > player.getMaxHealth())
                {
                    player.setHealth(player.getMaxHealth());
                }

                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }
        }
    }

    private static boolean checkInventory(ServerPlayer player)
    {
        // Hotbar plus main inventory only (slots 0-35); armor and offhand are excluded
        for (int i = 0; i < 36; i++)
        {
            if (player.getInventory().getItem(i).is(ModItems.HEART_RELIC.get()))
            {
                return true;
            }
        }
        return false;
    }
}
