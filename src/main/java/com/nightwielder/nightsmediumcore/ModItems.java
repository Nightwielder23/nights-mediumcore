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

    public static final RegistryObject<Item> LIFE_SHARD = ITEMS.register("life_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> LIFE_ORE_ITEM = ITEMS.register("life_ore",
            () -> new BlockItem(ModBlocks.LIFE_ORE.get(), new Item.Properties()));
}
