// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection-based Curios API integration.
 * Only called when Curios is confirmed loaded via ModList check.
 * Uses reflection to avoid compile-time dependency on Curios.
 */
public class CuriosCompat
{
    private static Method getCuriosInventory;
    private static Method findFirstCurio;
    private static boolean initialized = false;
    private static boolean available = false;

    private static void init()
    {
        if (initialized)
            return;
        initialized = true;
        try
        {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            getCuriosInventory = apiClass.getMethod("getCuriosInventory", LivingEntity.class);

            Class<?> handlerClass = Class.forName("top.theillusivec4.curios.api.type.inventory.ICuriosItemHandler");
            findFirstCurio = handlerClass.getMethod("findFirstCurio", Item.class);

            available = true;
        }
        catch (Exception e)
        {
            available = false;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean hasRelicEquipped(Player player, Item relicItem)
    {
        init();
        if (!available)
            return false;

        try
        {
            Object lazyOpt = getCuriosInventory.invoke(null, player);
            if (!(lazyOpt instanceof LazyOptional<?> lazy) || !lazy.isPresent())
                return false;

            Object handler = lazy.resolve().orElse(null);
            if (handler == null)
                return false;

            Object result = findFirstCurio.invoke(handler, relicItem);
            if (result instanceof Optional<?> opt)
            {
                return opt.isPresent();
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
