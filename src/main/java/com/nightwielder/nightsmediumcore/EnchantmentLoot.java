// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnchantmentLoot
{
    private static final ResourceLocation STRONGHOLD_LIBRARY = new ResourceLocation("minecraft", "chests/stronghold_library");
    private static final ResourceLocation BASTION_TREASURE = new ResourceLocation("minecraft", "chests/bastion_treasure");
    private static final ResourceLocation NETHER_FORTRESS = new ResourceLocation("minecraft", "chests/nether_bridge");
    private static final ResourceLocation ANCIENT_CITY = new ResourceLocation("minecraft", "chests/ancient_city");

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event)
    {
        ResourceLocation name = event.getName();

        if (STRONGHOLD_LIBRARY.equals(name))
        {
            event.getTable().addPool(bookPool("nightsmediumcore_life_steal_book", "nightsmediumcore:life_steal", 0.02F));
            event.getTable().addPool(bookPool("nightsmediumcore_life_leech_book", "nightsmediumcore:life_leech", 0.02F));
        }
        else if (BASTION_TREASURE.equals(name))
        {
            event.getTable().addPool(bookPool("nightsmediumcore_life_steal_book", "nightsmediumcore:life_steal", 0.01F));
            event.getTable().addPool(bookPool("nightsmediumcore_life_leech_book", "nightsmediumcore:life_leech", 0.01F));
        }
        else if (NETHER_FORTRESS.equals(name))
            event.getTable().addPool(bookPool("nightsmediumcore_vampirism_book", "nightsmediumcore:vampirism", 0.02F));
        else if (ANCIENT_CITY.equals(name))
            event.getTable().addPool(bookPool("nightsmediumcore_vampirism_book", "nightsmediumcore:vampirism", 0.01F));
    }

    private static LootPool bookPool(String poolName, String enchantId, float chance)
    {
        return LootPool.lootPool()
                .name(poolName)
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(bookEntry(enchantId, 1, 60))
                .add(bookEntry(enchantId, 2, 30))
                .add(bookEntry(enchantId, 3, 10))
                .build();
    }

    private static LootPoolSingletonContainer.Builder<?> bookEntry(String enchantId, int level, int weight)
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        CompoundTag ench = new CompoundTag();
        ench.putString("id", enchantId);
        ench.putShort("lvl", (short) level);
        list.add(ench);
        tag.put("StoredEnchantments", list);

        return LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                .setWeight(weight)
                .apply(SetNbtFunction.setTag(tag));
    }
}
