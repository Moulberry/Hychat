package io.github.moulberry.hychat.gui;

import io.github.moulberry.hychat.chat.ExtendedChatLine;
import io.github.moulberry.hychat.mixins.GuiScreenAccessor;
import io.github.moulberry.hychat.resources.CursorIcons;
import io.github.moulberry.hychat.resources.Icons;
import io.github.moulberry.hychat.util.MiscUtils;
import io.github.moulberry.hychat.util.RenderUtils;
import io.github.moulberry.hychat.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class GuiChatArray extends Gui {

    public final GuiChatBox chatBox;

    private String textPopup = null;
    private int textPopupX;
    private int textPopupY;
    private long textPopupMillis;

    private int grabbedDirection = -1;

    public GuiChatArray(GuiChatBox chatBox) {
        this.chatBox = chatBox;
    }

    public boolean isChatOpen() {
        return Minecraft.getMinecraft().currentScreen instanceof GuiChat;
    }

    private void startTextPopup(String textPopup, int textPopupX, int textPopupY) {
        this.textPopup = textPopup;
        this.textPopupX = textPopupX;
        this.textPopupY = textPopupY;
        this.textPopupMillis = System.currentTimeMillis();
    }
    
    public void render(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int updateCounter, int x, int bottomY) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int chatLineCount = chatBox.getChatHeight(scaledResolution)/9;
        int chatWidth = chatBox.getChatWidth(scaledResolution);
        boolean chatOpen = isChatOpen();
        int scrollPos = chatBox.getScrollPos();

        int drawnLines = 0;
        int maxLines = chatLinesWrapped.size();
        int maxOpacity = (int)(255 * (Minecraft.getMinecraft().gameSettings.chatOpacity * 0.9F + 0.1F));

        boolean hoverHorz = chatOpen && mouseX >= x && mouseX <= x+chatWidth+4;

        float chatScale = chatBox.getChatScale();
        float lineHeight = 9*chatScale;
        if (maxLines > 0) {

            //GlStateManager.pushMatrix();
            //GlStateManager.translate(x, bottomY, 0.0F);
            //GlStateManager.scale(chatScale, chatScale, 1.0F);

            for (int lineIndex = 0; lineIndex < chatLineCount; ++lineIndex) {
                if(lineIndex + scrollPos >= chatLinesWrapped.size()) {
                    break;
                }
                ChatLine chatline = chatLinesWrapped.get(lineIndex + scrollPos);

                if (chatline != null) {
                    int deltaTicks = updateCounter - chatline.getUpdatedCounter();

                    if (deltaTicks < 200 || chatOpen) {
                        int opacity = maxOpacity;
                        if(!chatOpen && deltaTicks > 180) {
                            double factor = 1-(deltaTicks-180)/20.0;
                            opacity = (int)(maxOpacity*factor*factor);
                        }

                        if (opacity > 3) {
                            drawnLines++;
                            float verticalOffset = lineIndex*lineHeight;

                            int top = (int)(bottomY - verticalOffset - lineHeight);
                            int bottom = (int)(bottomY - verticalOffset);
                            drawRect(x, top,
                                    x + chatWidth, bottom,
                                    (opacity/2) << 24);

                            GlStateManager.enableBlend();

                            GlStateManager.pushMatrix();
                            GlStateManager.translate(x, bottomY-verticalOffset-lineHeight+1, 0);
                            GlStateManager.scale(chatScale, chatScale, 1);
                            String s = chatline.getChatComponent().getFormattedText();
                            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s,
                                    0, 0, 0xffffff | (opacity << 24));
                            GlStateManager.popMatrix();

                            if(hoverHorz && mouseY >= top && mouseY < bottom && !chatBox.isEditing()) {
                                Minecraft.getMinecraft().getTextureManager().bindTexture(Icons.COPY);
                                GlStateManager.color(1, 1, 1, 1);
                                RenderUtils.drawTexturedRect(x+chatWidth-(int)lineHeight-1, top,
                                        (int)lineHeight, (int)lineHeight);
                            }
                        }
                    }
                }
            }

            //Scroll bar
            /*if (chatOpen && maxLines != drawnLines) {
                int fontHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
                GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                int maxHeight = maxLines * fontHeight + maxLines;
                int drawnHeight = drawnLines * fontHeight + drawnLines;
                int bottom = -scrollPos * drawnHeight / maxLines;
                int height = drawnHeight * drawnHeight / maxHeight;

                int k3 = scrollPos > 0 ? 170 : 96;
                int l3 = scrollPos > 0 ? 13382451 : 3355562;
                drawRect(0, bottom, 2, bottom - height, l3 + (k3 << 24));
                drawRect(2, bottom, 1, bottom - height, 13421772 + (k3 << 24));
            }*/

            //GlStateManager.popMatrix();
        }

        if(chatOpen) {
            int top = (int)(bottomY - drawnLines*lineHeight);
            boolean locked = chatBox.isLocked();
            if(locked) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(Icons.LOCK_T);
            } else {
                Minecraft.getMinecraft().getTextureManager().bindTexture(Icons.LOCK);
            }
            GlStateManager.color(1, 1, 1, 1);
            RenderUtils.drawTexturedRect(x+chatWidth+1, top, (int)lineHeight, (int)lineHeight);

            long currentTime = System.currentTimeMillis();
            if(textPopup != null && currentTime - textPopupMillis < 2000) {
                int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(textPopup);
                RenderUtils.drawFloatingRect(textPopupX, textPopupY-12, stringWidth+4, 12);
                Minecraft.getMinecraft().fontRendererObj.drawString(textPopup, textPopupX+2, textPopupY-10,
                        0xff202020);
            }
        }
    }

    public void mouseInput(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int x, int bottomY) {
        if(chatBox.isEditing()) {
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        if(Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
            if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
                IChatComponent hovered = getHoveredComponent(chatLinesWrapped, Mouse.getX(), Mouse.getY(), x, bottomY);
                if (((GuiScreenAccessor)Minecraft.getMinecraft().currentScreen).invokeHandleComponentClick(hovered)) {
                    return;
                }
            }
            boolean chatOpen = isChatOpen();
            int chatLineCount = chatBox.getChatHeight(scaledResolution)/9;
            int chatWidth = chatBox.getChatWidth(scaledResolution);
            int scrollPos = chatBox.getScrollPos();
            int maxLines = chatLinesWrapped.size();
            int drawnLines = 0;

            boolean hoverHorz = mouseX >= x && mouseX <= x+chatWidth+4;

            float chatScale = chatBox.getChatScale();
            float lineHeight = 9 * chatScale;
            if (maxLines > 0) {
                for (int lineIndex = 0; lineIndex < chatLineCount; ++lineIndex) {
                    if (lineIndex + scrollPos >= chatLinesWrapped.size()) {
                        break;
                    }
                    ExtendedChatLine chatline = chatLinesWrapped.get(lineIndex + scrollPos);

                    if (chatline != null) {
                        drawnLines++;
                        float verticalOffset = lineIndex * lineHeight;

                        int top = (int) (bottomY - verticalOffset - lineHeight);
                        int bottom = (int) (bottomY - verticalOffset);

                        if(hoverHorz && mouseX >= x+chatWidth-(int)lineHeight-1 &&
                                mouseX <= x+chatWidth-1 && mouseY >= top && mouseY < bottom) {
                            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                                MiscUtils.copyToClipboard(chatline.getFullLine().getFormattedText());
                                startTextPopup("Copied formatted to clipboard", x+chatWidth-3, top+1);
                            } else {
                                MiscUtils.copyToClipboard(StringUtils.cleanColour(chatline.getFullLine().getUnformattedText()));
                                startTextPopup("Copied to clipboard", x+chatWidth-3, top+1);
                            }
                            return;
                        }
                    }
                }
            }

            if(chatOpen) {
                int top = (int) (bottomY - drawnLines * lineHeight);
                if(mouseX >= x+chatWidth+1 && mouseX <= x+chatWidth+1+lineHeight &&
                        mouseY >= top && mouseY <= top+lineHeight) {
                    chatBox.setLocked(!chatBox.isLocked());
                }
            }

        }
    }

    public IChatComponent getHoveredComponent(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int x, int bottomY) {
        if (!isChatOpen() || chatBox.isEditing()) {
            return null;
        } else {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledResolution.getScaleFactor();
            float f = chatBox.getChatScale();
            int j = mouseX/i - x - 1;
            int k = mouseY/i - scaledResolution.getScaledHeight() + bottomY + 1;
            j = MathHelper.floor_float((float) j / f);
            k = MathHelper.floor_float((float) k / f);

            if (j >= 0 && k >= 0) {
                int l = Math.min(chatBox.getChatHeight(scaledResolution)/9, chatLinesWrapped.size());

                if (j <= MathHelper.floor_float((float) chatBox.getChatWidth(scaledResolution) / chatBox.getChatScale()) &&
                        k < Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * l + l) {
                    int i1 = k / Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + chatBox.getScrollPos();

                    if (i1 >= 0 && i1 < chatLinesWrapped.size()) {
                        ChatLine chatline = chatLinesWrapped.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                            if (ichatcomponent instanceof ChatComponentText) {
                                j1 += Minecraft.getMinecraft().fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j) {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
    
}
