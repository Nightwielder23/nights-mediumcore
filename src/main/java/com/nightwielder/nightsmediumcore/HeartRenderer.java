// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation(NightsMediumcore.MODID, "textures/gui/mediumcore_heart.png");

    // Vanilla heart container outline from icons.png (normal style)
    private static final int CONTAINER_U = 16;
    private static final int CONTAINER_V = 0;

    // Custom heart texture: opaque content bounds within the 256x256 PNG
    private static final int HEART_SRC_X = 46;
    private static final int HEART_SRC_Y = 54;
    private static final int HEART_SRC_W = 162;
    private static final int HEART_SRC_H = 162;

    // Absorption hearts from vanilla icons.png
    private static final int ABSORB_FULL_U = 160;
    private static final int ABSORB_HALF_U = 169;
    private static final int ABSORB_V = 0;

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

        // Derive total hearts from actual max health (accounts for heart loss + relic bonus)
        int totalHearts = Mth.ceil(player.getMaxHealth() / 2.0F);
        int healthHalf = Mth.ceil(player.getHealth());
        int absorbHalf = Mth.ceil(player.getAbsorptionAmount());
        int rows = (totalHearts + 9) / 10;

        // Draw heart containers and fills, supporting multiple rows of 10
        for (int i = totalHearts - 1; i >= 0; i--)
        {
            int col = i % 10;
            int row = i / 10;
            int x = left + col * 8;
            int y = top - row * 10;

            // Container outline
            gfx.blit(ICONS, x, y, CONTAINER_U, CONTAINER_V, 9, 9);

            int hp = i * 2;
            if (hp + 2 <= healthHalf)
            {
                // Full heart — draw custom texture scaled to 9x9
                gfx.blit(HEART_TEXTURE, x, y, 9, 9,
                        HEART_SRC_X, HEART_SRC_Y, HEART_SRC_W, HEART_SRC_H, 256, 256);
            }
            else if (hp + 1 <= healthHalf)
            {
                // Half heart — draw left half of custom texture
                gfx.blit(HEART_TEXTURE, x, y, 5, 9,
                        HEART_SRC_X, HEART_SRC_Y, HEART_SRC_W / 2, HEART_SRC_H, 256, 256);
            }
        }

        // Draw absorption hearts above the topmost heart row
        if (absorbHalf > 0)
        {
            int absTop = top - (rows - 1) * 10 - 10;
            int absCount = Mth.ceil(absorbHalf / 2.0F);
            for (int i = 0; i < absCount && i < 10; i++)
            {
                int x = left + i * 8;
                int y = absTop;

                gfx.blit(ICONS, x, y, CONTAINER_U, CONTAINER_V, 9, 9);

                int ap = i * 2;
                if (ap + 2 <= absorbHalf)
                {
                    gfx.blit(ICONS, x, y, ABSORB_FULL_U, ABSORB_V, 9, 9);
                }
                else if (ap + 1 <= absorbHalf)
                {
                    gfx.blit(ICONS, x, y, ABSORB_HALF_U, ABSORB_V, 9, 9);
                }
            }
        }
    }
}
