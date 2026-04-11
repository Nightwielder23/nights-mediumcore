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

    public static final RegistryObject<Item> LIFE_CRYSTAL = ITEMS.register("life_crystal",
            () -> new LifeCrystalItem(new Item.Properties().stacksTo(16), 1, "Life Crystal"));

    public static final RegistryObject<Item> SUPREME_LIFE_CRYSTAL = ITEMS.register("supreme_life_crystal",
            () -> new LifeCrystalItem(new Item.Properties().stacksTo(16), HeartLossHandler.MAX_HEARTS, "Supreme Life Crystal"));

    public static final RegistryObject<Item> LIFE_ORE_ITEM = ITEMS.register("life_ore",
            () -> new BlockItem(ModBlocks.LIFE_ORE.get(), new Item.Properties()));
}
