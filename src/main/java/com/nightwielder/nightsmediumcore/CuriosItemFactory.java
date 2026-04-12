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

    public static Item createBloodRelic()
    {
        return new BloodRelicItem(new Item.Properties().stacksTo(1));
    }

    public static boolean isEquippedInCurios(net.minecraft.world.entity.player.Player player, Item item)
    {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .map(inv -> !inv.findCurios(item).isEmpty())
                .orElse(false);
    }
}
