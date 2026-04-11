// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.item.Item;

/**
 * Isolated factory to prevent HeartRelicItem (which implements ICurioItem)
 * from being classloaded when Curios is not installed.
 * This class is only ever loaded when ModList confirms Curios is present.
 */
public class CuriosItemFactory
{
    public static Item createHeartRelic()
    {
        return new HeartRelicItem(new Item.Properties().stacksTo(1));
    }
}
