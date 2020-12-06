package io.github.moulberry.hychat.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class RenderUtils {

    public static void drawFloatingRect(int x, int y, int width, int height) {
        GlStateManager.enableDepth();

        GlStateManager.translate(0, 0, 1);

        Gui.drawRect(x+2, y+2, x+width+2,
                y+height+2, 0xa0000000);

        GlStateManager.depthFunc(GL11.GL_LESS);
        GlStateManager.translate(0, 0, 1);
        Gui.drawRect(x+1, y+1, x+width-1,
                y+height-1, 0xffc0c0c0);
        Gui.drawRect(x, y, x+width-1,
                y+height-1, 0xfff0f0f0);
        Gui.drawRect(x, y, x+width,
                y+height, 0xff909090);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);

        GlStateManager.translate(0, 0, -2);

        GlStateManager.disableDepth();
    }

    public static void drawTexturedRect(float x, float y, float width, float height) {
        drawTexturedRect(x, y, width, height, 0, 1, 0 , 1);
    }

    public static void drawTexturedRect(float x, float y, float width, float height, int filter) {
        drawTexturedRect(x, y, width, height, 0, 1, 0 , 1, filter);
    }

    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax) {
        drawTexturedRect(x, y, width, height, uMin, uMax, vMin , vMax, GL11.GL_NEAREST);
    }

    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer
                .pos(x, y+height, 0.0D)
                .tex(uMin, vMax).endVertex();
        worldrenderer
                .pos(x+width, y+height, 0.0D)
                .tex(uMax, vMax).endVertex();
        worldrenderer
                .pos(x+width, y, 0.0D)
                .tex(uMax, vMin).endVertex();
        worldrenderer
                .pos(x, y, 0.0D)
                .tex(uMin, vMin).endVertex();
        tessellator.draw();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.disableBlend();
    }

    public static void drawGradientRect(int zLevel, int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
        float startBlue = (float)(startColor & 255) / 255.0F;
        float endAlpha = (float)(endColor >> 24 & 255) / 255.0F;
        float endRed = (float)(endColor >> 16 & 255) / 255.0F;
        float endGreen = (float)(endColor >> 8 & 255) / 255.0F;
        float endBlue = (float)(endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

}
