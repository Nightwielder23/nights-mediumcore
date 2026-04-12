// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
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
            ModItems::createHeartRelic);

    public static final RegistryObject<Item> LIVING_HEART = ITEMS.register("living_heart",
            () -> new LivingHeartItem(new Item.Properties().stacksTo(16)));

    private static Item createHeartRelic()
    {
        if (ModList.get().isLoaded("curios"))
        {
            return createCuriosRelic();
        }
        return new Item(new Item.Properties().stacksTo(1));
    }

    // Separate class isolates HeartRelicItem (implements ICurioItem) from ModItems bytecode,
    // preventing NoClassDefFoundError when Curios is not installed
    private static Item createCuriosRelic()
    {
        return CuriosItemFactory.createHeartRelic();
    }
}
