package com.nightwielder.nightsmediumcore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightsMediumcore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HeartRenderer
{
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    // Hardcore row in icons.png is at V=45
    private static final int HC_V = 45;
    // U positions: each heart type is offset by 9px from base U=16
    private static final int CONTAINER_U = 16;  // index 0
    private static final int FULL_U = 34;       // index 2
    private static final int HALF_U = 43;       // index 3
    private static final int ABSORB_FULL_U = 88; // index 8
    private static final int ABSORB_HALF_U = 97; // index 9

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event)
    {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id()))
            return;

        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        if (mc.gameMode != null && !mc.gameMode.canHurtPlayer())
            return;

        GuiGraphics gfx = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // Vanilla heart position
        int left = screenWidth / 2 - 91;
        int top = screenHeight - 39;

        // Read hearts lost from the synced attribute modifier on the client
        int heartsLost = 0;
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null)
        {
            AttributeModifier modifier = healthAttr.getModifier(HeartLossHandler.MODIFIER_UUID);
            if (modifier != null)
            {
                heartsLost = (int) (Math.abs(modifier.getAmount()) / 2.0);
            }
        }

        int activeHearts = HeartLossHandler.MAX_HEARTS - heartsLost;
        int healthHalf = Mth.ceil(player.getHealth());
        int absorbHalf = Mth.ceil(player.getAbsorptionAmount());

        // Draw all 10 heart positions right-to-left (matches vanilla draw order)
        for (int i = HeartLossHandler.MAX_HEARTS - 1; i >= 0; i--)
        {
            int x = left + i * 8;
            int y = top;

            // Container outline — always drawn for every position
            gfx.blit(ICONS, x, y, CONTAINER_U, HC_V, 9, 9);

            // Only fill hearts that are within the current max
            if (i < activeHearts)
            {
                int hp = i * 2; // health-point threshold for this heart
                if (hp + 2 <= healthHalf)
                {
                    gfx.blit(ICONS, x, y, FULL_U, HC_V, 9, 9);
                }
                else if (hp + 1 <= healthHalf)
                {
                    gfx.blit(ICONS, x, y, HALF_U, HC_V, 9, 9);
                }
            }
            // Lost heart positions: only the container outline (already drawn)
        }

        // Draw absorption hearts in a row above
        if (absorbHalf > 0)
        {
            int absCount = Mth.ceil(absorbHalf / 2.0F);
            for (int i = 0; i < absCount && i < HeartLossHandler.MAX_HEARTS; i++)
            {
                int x = left + i * 8;
                int y = top - 10;

                gfx.blit(ICONS, x, y, CONTAINER_U, HC_V, 9, 9);

                int ap = i * 2;
                if (ap + 2 <= absorbHalf)
                {
                    gfx.blit(ICONS, x, y, ABSORB_FULL_U, HC_V, 9, 9);
                }
                else if (ap + 1 <= absorbHalf)
                {
                    gfx.blit(ICONS, x, y, ABSORB_HALF_U, HC_V, 9, 9);
                }
            }
        }
    }
}
