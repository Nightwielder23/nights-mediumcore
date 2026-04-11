package com.nightwielder.nightsmediumcore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, NightsMediumcore.MODID);

    public static final RegistryObject<Block> LIFE_ORE = BLOCKS.register("life_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
}
