// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID)
public class HeartCodexJoinHandler
{
    private static final String RECEIVED_TAG = "nightsmediumcore_received_heart_codex";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!ModList.get().isLoaded("patchouli"))
            return;
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        CompoundTag persisted = player.getPersistentData();
        CompoundTag forgeData = persisted.getCompound(Player.PERSISTED_NBT_TAG);
        if (forgeData.getBoolean(RECEIVED_TAG))
            return;

        ItemStack stack = ModItems.getHeartCodexStack();
        if (stack.isEmpty())
            return;

        if (!player.getInventory().add(stack))
            player.drop(stack, false);

        forgeData.putBoolean(RECEIVED_TAG, true);
        persisted.put(Player.PERSISTED_NBT_TAG, forgeData);
    }
}
