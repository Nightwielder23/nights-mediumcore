// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class PatchouliBookFactory
{
    private static final ResourceLocation BOOK_ID =
            new ResourceLocation(NightsMediumcore.MODID, "heart_codex");
    private static final ResourceLocation PATCHOULI_GUIDE =
            new ResourceLocation("patchouli", "guide_book");

    private PatchouliBookFactory() {}

    public static ItemStack createHeartCodexStack()
    {
        ItemStack reflected = tryReflectiveApi();
        if (!reflected.isEmpty())
            return reflected;
        return fallbackGuideStack();
    }

    private static ItemStack tryReflectiveApi()
    {
        try
        {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            Object stack = api.getClass()
                    .getMethod("getBookStack", ResourceLocation.class)
                    .invoke(api, BOOK_ID);
            if (stack instanceof ItemStack itemStack && !itemStack.isEmpty())
                return itemStack;
        }
        catch (Throwable ignored)
        {
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack fallbackGuideStack()
    {
        Item guide = ForgeRegistries.ITEMS.getValue(PATCHOULI_GUIDE);
        if (guide == null)
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(guide);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("patchouli:book", BOOK_ID.toString());
        return stack;
    }
}
