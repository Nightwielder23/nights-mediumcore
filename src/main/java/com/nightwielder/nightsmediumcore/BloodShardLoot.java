// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodShardLoot
{
    private static final ResourceLocation EVOKER = new ResourceLocation("minecraft", "entities/evoker");
    private static final ResourceLocation STRONGHOLD_LIBRARY = new ResourceLocation("minecraft", "chests/stronghold_library");

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event)
    {
        ResourceLocation name = event.getName();
        if (EVOKER.equals(name))
        {
            event.getTable().addPool(LootPool.lootPool()
                    .name("nightsmediumcore_blood_shard")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.BLOOD_SHARD.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.03F)))
                    .build());
        }
        else if (STRONGHOLD_LIBRARY.equals(name))
        {
            event.getTable().addPool(LootPool.lootPool()
                    .name("nightsmediumcore_blood_shard")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.BLOOD_SHARD.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.02F)))
                    .build());
        }
    }
}
