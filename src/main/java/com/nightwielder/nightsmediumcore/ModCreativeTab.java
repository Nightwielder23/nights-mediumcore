package com.nightwielder.nightsmediumcore;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NightsMediumcore.MODID);

    public static final RegistryObject<CreativeModeTab> NIGHTS_MEDIUMCORE_TAB = CREATIVE_MODE_TABS.register("nights_mediumcore_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nightsmediumcore.nights_mediumcore_tab"))
                    .icon(() -> new ItemStack(ModItems.CRYSTAL_SHARD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HEART_ORE_ITEM.get());
                        output.accept(ModItems.CRYSTAL_SHARD.get());
                        output.accept(ModItems.CRYSTAL_HEART.get());
                        output.accept(ModItems.SUPREME_CRYSTAL_HEART.get());
                        output.accept(ModItems.HEART_RELIC.get());
                    })
                    .build());
}
