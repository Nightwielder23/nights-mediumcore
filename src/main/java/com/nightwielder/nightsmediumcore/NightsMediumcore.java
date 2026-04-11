package com.nightwielder.nightsmediumcore;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
        IEventBus modEventBus = context.getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(new HeartLossHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        LOGGER.info("Night's Mediumcore loaded");
    }

    private void onRegisterCommands(RegisterCommandsEvent event)
    {
        ModCommands.register(event.getDispatcher());
    }
}
