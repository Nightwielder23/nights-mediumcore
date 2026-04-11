package com.nightwielder.nightsmediumcore;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(NightsMediumcore.MODID)
public class NightsMediumcore
{
    public static final String MODID = "nightsmediumcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NightsMediumcore(FMLJavaModLoadingContext context)
    {
        MinecraftForge.EVENT_BUS.register(new HeartLossHandler());
        LOGGER.info("Night's Mediumcore loaded");
    }
}
