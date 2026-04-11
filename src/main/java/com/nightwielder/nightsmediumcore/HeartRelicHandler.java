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
    private static final String MODIFIER_NAME = "nightsmediumcore.heart_relic";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!(event.player instanceof ServerPlayer player))
            return;
        if (player.tickCount % 20 != 0)
            return;

        boolean hasRelic = checkForRelic(player);
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null)
            return;

        if (hasRelic)
        {
            // Calculate base max health without the relic modifier
            double currentMax = player.getMaxHealth();
            AttributeModifier existing = healthAttr.getModifier(RELIC_MODIFIER_UUID);
            if (existing != null)
            {
                currentMax -= existing.getAmount();
            }

            // 20% of total max hearts rounded up, each heart = 2 HP
            int baseHearts = (int) Math.round(currentMax / 2.0);
            int bonusHearts = (int) Math.ceil(baseHearts * 0.2);
            double bonusHP = bonusHearts * 2.0;

            if (bonusHP < 2.0)
            {
                bonusHP = 2.0;
            }

            // Apply or update modifier only if the value changed
            if (existing == null || existing.getAmount() != bonusHP)
            {
                if (existing != null)
                {
                    healthAttr.removeModifier(RELIC_MODIFIER_UUID);
                }
                healthAttr.addPermanentModifier(new AttributeModifier(
                        RELIC_MODIFIER_UUID, MODIFIER_NAME, bonusHP, AttributeModifier.Operation.ADDITION));

                // Sync to client
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }

            // Apply Regen 1 with 25-tick duration, refreshed every 20 ticks
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, true, false, true));
        }
        else
        {
            // Remove relic modifier if present
            AttributeModifier existing = healthAttr.getModifier(RELIC_MODIFIER_UUID);
            if (existing != null)
            {
                healthAttr.removeModifier(RELIC_MODIFIER_UUID);

                // Clamp health to new max
                if (player.getHealth() > player.getMaxHealth())
                {
                    player.setHealth(player.getMaxHealth());
                }

                // Sync to client
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                        player.getId(), Collections.singleton(healthAttr)));
            }
            // Regen 1 will expire naturally within 25 ticks
        }
    }

    private static boolean checkForRelic(ServerPlayer player)
    {
        // Check main inventory slots 0-35 (hotbar + main inventory, not armor or offhand)
        for (int i = 0; i < 36; i++)
        {
            if (player.getInventory().getItem(i).is(ModItems.HEART_RELIC.get()))
            {
                return true;
            }
        }

        // Additionally check Curios charm slot if Curios is loaded
        if (ModList.get().isLoaded("curios"))
        {
            return CuriosCompat.hasRelicEquipped(player, ModItems.HEART_RELIC.get());
        }

        return false;
    }
}
