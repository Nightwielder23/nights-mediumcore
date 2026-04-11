// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NightsMediumcore.MODID);

    public static final RegistryObject<Item> CRYSTAL_SHARD = ITEMS.register("crystal_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRYSTAL_HEART = ITEMS.register("crystal_heart",
            () -> new CrystalHeartItem(new Item.Properties().stacksTo(16), 1, "Crystal Heart"));

    public static final RegistryObject<Item> SUPREME_CRYSTAL_HEART = ITEMS.register("supreme_crystal_heart",
            () -> new CrystalHeartItem(new Item.Properties().stacksTo(16), HeartLossHandler.MAX_HEARTS, "Supreme Crystal Heart"));

    public static final RegistryObject<Item> HEART_ORE_ITEM = ITEMS.register("heart_ore",
            () -> new BlockItem(ModBlocks.HEART_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> HEART_RELIC = ITEMS.register("heart_relic",
            () -> new Item(new Item.Properties().stacksTo(1)));
}
