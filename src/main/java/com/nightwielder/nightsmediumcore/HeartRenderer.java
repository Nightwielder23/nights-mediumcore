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

        // Draw all 10 heart positions right-to-left
        for (int i = HeartLossHandler.MAX_HEARTS - 1; i >= 0; i--)
        {
            int x = left + i * 8;
            int y = top;

            if (i < activeHearts)
            {
                // Active heart slot — draw container outline, then fill based on current HP
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
            else
            {
                // Lost heart slot — draw only the empty container outline
                gfx.blit(ICONS, x, y, CONTAINER_U, CONTAINER_V, 9, 9);
            }
        }

        // Draw absorption hearts in a row above
        if (absorbHalf > 0)
        {
            int absCount = Mth.ceil(absorbHalf / 2.0F);
            for (int i = 0; i < absCount && i < HeartLossHandler.MAX_HEARTS; i++)
            {
                int x = left + i * 8;
                int y = top - 10;

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
