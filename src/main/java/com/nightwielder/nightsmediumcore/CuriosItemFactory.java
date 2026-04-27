// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

/**
 * Isolated factory to prevent HeartRelicItem (which implements ICurioItem)
 * from being classloaded when Curios is not installed.
 * This class is only ever loaded when ModList confirms Curios is present.
 */
public class CuriosItemFactory
{
    public static Item createHeartRelic()
    {
        if (!ModList.get().isLoaded("curios"))
            return new Item(new Item.Properties().stacksTo(1));
        return new HeartRelicItem(new Item.Properties().stacksTo(1));
    }
}
