package io.github.moulberry.hychat.gui;

import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.Resources;
import io.github.moulberry.hychat.chat.ExtendedChatLine;
import io.github.moulberry.hychat.core.ChromaColour;
import io.github.moulberry.hychat.core.ColourWheel;
import io.github.moulberry.hychat.core.GuiElement;
import io.github.moulberry.hychat.core.util.lerp.LerpUtils;
import io.github.moulberry.hychat.mixins.GuiScreenAccessor;
import io.github.moulberry.hychat.core.util.MiscUtils;
import io.github.moulberry.hychat.core.util.render.RenderUtils;
import io.github.moulberry.hychat.core.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class GuiChatArray extends Gui {

    public GuiChatBox chatBox;

    private String textPopup = null;
    private int textPopupX;
    private int textPopupY;
    private long textPopupMillis;

    private GuiElement activeGuiElement = null;

    private GuiChatArray() { //Default constructor for Gson
    }

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

    public int renderChat(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int updateCounter,
                           int x, int bottomY, int chatLineCount, int scrollPos, boolean chatOpen,
                          float lineHeight, int chatWidth, float chatScale, boolean overlays) {
        int argb = ChromaColour.specialToChromaRGB(chatBox.getBackgroundColour());
        int rgb = argb & 0xffffff;
        int maxOpacity = (argb >> 24) & 0xFF;
        int drawnLines = 0;
        boolean lastEmpty = false;

        for (int lineIndex = 0; lineIndex < chatLineCount+50; ++lineIndex) {
            if(drawnLines >= chatLineCount) {
                break;
            }
            if(lineIndex + scrollPos >= chatLinesWrapped.size()) {
                break;
            }
            ExtendedChatLine chatline = chatLinesWrapped.get(lineIndex + scrollPos);

            if (chatline != null) {
                HyChat.getInstance().getChatManager().viewMessage(chatline.getUniqueId());
                int deltaTicks = updateCounter - chatline.getUpdatedCounter();

                if (deltaTicks < 200 || chatOpen) {
                    int opacity = maxOpacity;
                    int textOpacity = 255;
                    if(!chatOpen && deltaTicks > 180) {
                        double factor = 1-(deltaTicks-180)/20.0;
                        opacity = (int)(maxOpacity*factor*factor);
                        textOpacity = (int)(textOpacity*factor*factor);
                    }

                    if (opacity > 3 || textOpacity > 3) {
                        String s = chatline.getChatComponent().getFormattedText();
                        int sWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                        String cleanS = StringUtils.cleanColour(s);

                        if(cleanS.trim().length() == 0) {
                            if(lastEmpty && HyChat.getInstance().getChatManager().getConfig().tweaks.stackEmptyLines) {
                                continue;
                            }
                            lastEmpty = true;
                        } else {
                            lastEmpty = false;
                        }

                        if(HyChat.getInstance().getChatManager().getConfig().tweaks.connectedDividers && chatline.isDivider() &&
                                    !cleanS.isEmpty()) {
                            char last = cleanS.charAt(cleanS.length()-1);
                            if(last == '-') {
                                s = s.replaceAll("\\u00A7[0-9a-f]", "$0\u00A7m");
                                sWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                            } else if(last == '\u25AC') {
                                s = s.replaceAll("(\\u00A7r)+$", "");
                                int chatlineLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                                int deltaLen = Minecraft.getMinecraft().fontRendererObj
                                        .getStringWidth(s+last) - chatlineLen;
                                if(deltaLen > 0) {
                                    StringBuilder sb = new StringBuilder(s);
                                    while(chatlineLen + deltaLen < chatWidth/chatScale) {
                                        sb.append(last);
                                        chatlineLen += deltaLen;
                                    }
                                    s = sb.toString();
                                    sWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                                }
                            }
                        }

                        if(HyChat.getInstance().getChatManager().getConfig().tweaks.smartDividers && chatline.isDivider()) {
                            if(lineIndex + scrollPos + 1 < chatLinesWrapped.size()) {
                                ExtendedChatLine next =  chatLinesWrapped.get(lineIndex + scrollPos + 1);
                                if(next.isDivider()) {
                                    if(next.getUniqueId() == chatline.getUniqueId()) {
                                        continue;
                                    }
                                }/* else if(true || next.getUpdatedCounter() == chatline.getUpdatedCounter()) {
                                    int nextLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(
                                            next.getChatComponent().getFormattedText());
                                    int chatlineLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                                    char endChar = cleanS.charAt(cleanS.length()-1);
                                    int deltaLen = Minecraft.getMinecraft().fontRendererObj
                                            .getStringWidth(s+endChar) - chatlineLen;
                                    StringBuilder sb = new StringBuilder(s.replace("\u00A7r", ""));
                                    if(deltaLen > 0) {
                                        while(chatlineLen < nextLen) {
                                            sb.append(endChar);
                                            chatlineLen += deltaLen;
                                        }
                                    }
                                    s = sb.toString();
                                }*/
                            }
                        }

                        float verticalOffset = drawnLines*lineHeight;
                        if(chatBox.getConfig().alignment.topAligned) {
                            verticalOffset = chatLineCount*lineHeight - lineHeight - drawnLines*lineHeight;
                        }
                        drawnLines++;

                        int top = (int)(bottomY - verticalOffset - lineHeight);
                        int bottom = (int)(bottomY - verticalOffset);
                        GlStateManager.enableDepth();
                        if(opacity > 3) {
                            drawRect(x, top,
                                    x + chatWidth, bottom,
                                    opacity << 24 | rgb);
                        }
                        GlStateManager.disableDepth();

                        GlStateManager.enableBlend();

                        if(textOpacity > 3) {
                            GlStateManager.pushMatrix();
                            int offset = 0;
                            if(chatline.isCentered()) {
                                offset = (int)((chatWidth-sWidth*chatScale)/2);
                            } else if(chatBox.getConfig().alignment.rightAligned) {
                                offset = (int)((chatWidth-sWidth*chatScale));
                            }
                            GlStateManager.translate(x+offset, bottomY-verticalOffset-lineHeight+1, 0);
                            GlStateManager.scale(chatScale, chatScale, 1);

                            boolean specialShadow = chatBox.getConfig().appearance.textShadow == 2;

                            if(specialShadow && textOpacity/4 > 3) {
                                for(int xOff=-2; xOff<=2; xOff++) {
                                    for(int yOff=-2; yOff<=2; yOff++) {
                                        if(xOff*xOff != yOff*yOff) {
                                            Minecraft.getMinecraft().fontRendererObj.drawString(
                                                    StringUtils.cleanColourNotModifiers(s),
                                                    xOff/2f, yOff/2f, (textOpacity/4 << 24), false);
                                        }
                                    }
                                }
                            }
                            Minecraft.getMinecraft().fontRendererObj.drawString(s,
                                    0, 0, 0xffffff | (textOpacity << 24),
                                    chatBox.getConfig().appearance.textShadow == 1);
                            GlStateManager.popMatrix();
                        }

                        if(overlays && mouseY >= top && mouseY < bottom && !chatBox.isEditable()) {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.Icons.COPY);
                            GlStateManager.color(1, 1, 1, 1);
                            int unscaledLineHeight = Math.round(lineHeight/chatScale);
                            float off = (unscaledLineHeight-lineHeight)/2f;
                            GlStateManager.enableDepth();
                            GlStateManager.translate(0, 0, 1);
                            RenderUtils.drawTexturedRect(x+chatWidth-unscaledLineHeight-1, top-off,
                                    unscaledLineHeight, unscaledLineHeight);
                            GlStateManager.translate(0, 0, -1);
                        }
                    }
                }
            }
        }

        return drawnLines;
    }
    
    public void render(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int updateCounter, int x, int bottomY) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int chatWidth = chatBox.getChatWidth(scaledResolution);
        boolean chatOpen = isChatOpen();
        int scrollPos = chatBox.getScrollPos();

        if(!chatOpen) {
            activeGuiElement = null;
        }

        int drawnLines = 0;
        int maxLines = chatLinesWrapped.size();

        boolean hoverHorz = chatOpen && mouseX >= x && mouseX <= x+chatWidth+4;

        float chatScale = chatBox.getChatScale();
        int unscaledLineHeight = 9;
        float lineHeight = unscaledLineHeight*chatScale;
        int chatLineCount = (int)Math.floor(chatBox.getChatHeight(scaledResolution)/lineHeight);
        if (maxLines > 0) {
            drawnLines = renderChat(chatLinesWrapped, mouseX, mouseY, updateCounter, x, bottomY, chatLineCount,
                    scrollPos, chatOpen, lineHeight, chatWidth, chatScale, hoverHorz);

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
            GlStateManager.color(1, 1, 1, 1);
            int top = (int)(bottomY - drawnLines*lineHeight);
            boolean locked = chatBox.isLocked();
            Minecraft.getMinecraft().getTextureManager().bindTexture(locked ? Resources.Icons.LOCK_T : Resources.Icons.LOCK);
            RenderUtils.drawTexturedRect(x+chatWidth+1, top, (int)unscaledLineHeight, (int)unscaledLineHeight);
            if(!locked) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(
                        activeGuiElement instanceof ColourWheel ? Resources.Icons.PAINT_T : Resources.Icons.PAINT);
                RenderUtils.drawTexturedRect(x+chatWidth+1, top+(int)(unscaledLineHeight+1),
                        (int)unscaledLineHeight, (int)unscaledLineHeight);
                Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.Icons.CAMERA);
                RenderUtils.drawTexturedRect(x+chatWidth+1, top+(int)(unscaledLineHeight+1)*2,
                        (int)unscaledLineHeight, (int)unscaledLineHeight);
                Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.Icons.COG);
                RenderUtils.drawTexturedRect(x+chatWidth+1, top+(int)(unscaledLineHeight+1)*3,
                        (int)unscaledLineHeight, (int)unscaledLineHeight);
            }

            long currentTime = System.currentTimeMillis();
            long textPopupDelta = currentTime - textPopupMillis;
            if(textPopup != null && textPopupDelta > 0 && textPopupDelta < 2000) {
                float alpha = 1;
                if(textPopupDelta < 150) {
                    alpha = textPopupDelta/150f;
                    alpha = LerpUtils.sigmoidZeroOne(alpha);
                } else if(textPopupDelta > 1850) {
                    alpha = (2000-textPopupDelta)/150f;
                    alpha = LerpUtils.sigmoidZeroOne(alpha);
                }
                int alphaI = 20+Math.round(235*alpha);

                int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(textPopup);
                RenderUtils.drawFloatingRectWithAlpha(textPopupX, textPopupY-12,
                        stringWidth+4, 12, alphaI, true);
                Minecraft.getMinecraft().fontRendererObj.drawString(textPopup, textPopupX+2, textPopupY-10,
                        0x202020 | (alphaI << 24));
            }

            if(activeGuiElement != null) {
                activeGuiElement.render();
            }
        }
    }

    public boolean keyboardInput() {
        if(chatBox.isEditable()) {
            return false;
        }
        if(activeGuiElement != null) {
            return activeGuiElement.keyboardInput();
        }
        return false;
    }

    public void mouseInput(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int x, int bottomY) {
        if(chatBox.isEditable()) {
            return;
        }
        if(activeGuiElement != null) {
            if(activeGuiElement.mouseInput(mouseX, mouseY)) {
                return;
            }
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
            int chatWidth = chatBox.getChatWidth(scaledResolution);
            int scrollPos = chatBox.getScrollPos();
            int maxLines = chatLinesWrapped.size();
            int drawnLines = 0;
            boolean lastEmpty = false;

            boolean hoverHorz = mouseX >= x && mouseX <= x+chatWidth+4;

            float chatScale = chatBox.getChatScale();
            int unscaledLineHeight = 9;
            float lineHeight = unscaledLineHeight * chatScale;
            int chatLineCount = (int)Math.floor(chatBox.getChatHeight(scaledResolution)/lineHeight);
            if (maxLines > 0) {
                for (int lineIndex = 0; lineIndex < chatLineCount+50; ++lineIndex) {
                    if(drawnLines >= chatLineCount) {
                        break;
                    }
                    if (lineIndex + scrollPos >= chatLinesWrapped.size()) {
                        break;
                    }
                    ExtendedChatLine chatline = chatLinesWrapped.get(lineIndex + scrollPos);

                    if (chatline != null) {
                        String s = chatline.getChatComponent().getFormattedText();
                        String cleanS = StringUtils.cleanColour(s);

                        if(HyChat.getInstance().getChatManager().getConfig().tweaks.smartDividers && chatline.isDivider()) {
                            if (lineIndex + scrollPos + 1 < chatLinesWrapped.size()) {
                                ExtendedChatLine next = chatLinesWrapped.get(lineIndex + scrollPos + 1);
                                if (next.isDivider()) {
                                    if (next.getUniqueId() == chatline.getUniqueId()) {
                                        continue;
                                    }
                                }
                            }
                        }

                        if(cleanS.trim().length() == 0) {
                            if(lastEmpty && HyChat.getInstance().getChatManager().getConfig().tweaks.stackEmptyLines) {
                                continue;
                            }
                            lastEmpty = true;
                        } else {
                            lastEmpty = false;
                        }

                        float verticalOffset = drawnLines * lineHeight;
                        if(chatBox.getConfig().alignment.topAligned) {
                            verticalOffset = chatLineCount*lineHeight - lineHeight - drawnLines*lineHeight;
                        }
                        drawnLines++;

                        int top = (int) (bottomY - verticalOffset - lineHeight);
                        int bottom = (int) (bottomY - verticalOffset);

                        if(hoverHorz && mouseX >= x+chatWidth-unscaledLineHeight-1 &&
                                mouseX <= x+chatWidth-1 && mouseY >= top && mouseY < bottom) {
                            if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                                MiscUtils.copyToClipboard(chatline.getFullLine().getFormattedText());
                                startTextPopup("Copied formatted to clipboard", x+chatWidth-3, top+1);
                            } else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                                screenshotLine(chatline.getFullLine());
                                startTextPopup("Copied SS to clipboard",
                                        x+chatWidth-3, top+1);
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
                lineHeight = 9;
                if(mouseX >= x+chatWidth+1 && mouseX <= x+chatWidth+1+lineHeight) {
                    if(mouseY >= top && mouseY <= top+lineHeight) {
                        chatBox.setLocked(!chatBox.isLocked());
                    } else if(!chatBox.isLocked()) {
                        if(mouseY >= top+(lineHeight+1) && mouseY <= top+lineHeight+(lineHeight+1)) {
                            activeGuiElement = new ColourWheel(mouseX, mouseY, chatBox.getBackgroundColour(),
                                chatBox::setBackgroundColour, () -> this.activeGuiElement = null);
                        } else if(mouseY >= top+(lineHeight+1)*2 && mouseY <= top+lineHeight+(lineHeight+1)*2) {
                            screenshotChat(chatLinesWrapped, scrollPos);
                            startTextPopup("Copied chat SS to clipboard", mouseX, mouseY);
                        } else if(mouseY >= top+(lineHeight+1)*3 && mouseY <= top+lineHeight+(lineHeight+1)*3) {
                            HyChat.getInstance().getChatManager().openEditor(chatBox);
                        }
                    }
                }
            }
        }
    }

    private void screenshotFramebuffer(Framebuffer framebuffer) {
        int w = framebuffer.framebufferWidth;
        int h = framebuffer.framebufferHeight;

        int i = w * h;
        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(i);
        int[] pixelValues = new int[i];

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);

        pixelBuffer.get(pixelValues); //Load buffer into array
        TextureUtil.processPixelValues(pixelValues, w, h); //Flip vertically
        BufferedImage bufferedimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int j = framebuffer.framebufferTextureHeight - framebuffer.framebufferHeight;

        for (int k = j; k < framebuffer.framebufferTextureHeight; ++k) {
            for (int l = 0; l < framebuffer.framebufferWidth; ++l) {
                bufferedimage.setRGB(l, k - j, pixelValues[k * framebuffer.framebufferTextureWidth + l]);
            }
        }

        MiscUtils.copyToClipboard(bufferedimage);
    }

    private Framebuffer createBindFramebuffer(int w, int h) {
        Framebuffer framebuffer = new Framebuffer(w, h, false);
        framebuffer.framebufferColor[0] = 0x36/255f;
        framebuffer.framebufferColor[1] = 0x39/255f;
        framebuffer.framebufferColor[2] = 0x3F/255f;
        framebuffer.framebufferClear();

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        framebuffer.bindFramebuffer(true);

        return framebuffer;
    }

    private void screenshotLine(IChatComponent line) {
        List<ExtendedChatLine> chatLines = new ArrayList<>();

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int w = chatBox.getChatWidth(scaledResolution);

        List<IChatComponent> list = GuiUtilRenderComponents.splitText(line, w,
                Minecraft.getMinecraft().fontRendererObj, false, false);

        for(IChatComponent ichatcomponent : list) {
            chatLines.add(0, new ExtendedChatLine(0, ichatcomponent, line, 0));
        }

        screenshotChat(chatLines, 0);
    }

    private void screenshotChat(List<ExtendedChatLine> chatLinesWrapped, int scrollPos) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int chatWidth = chatBox.getChatWidth(scaledResolution);
        int chatHeight = chatBox.getChatHeight(scaledResolution);
        float chatScale = chatBox.getChatScale();
        int baseScaleFactor = chatScale <= 0.5 ? 1 : 2;
        float chatScaleFactor = baseScaleFactor/chatScale;

        float lineHeight = 9*chatScale;
        /*if(chatLinesWrapped.size()*lineHeight*2 < h*chatScaleFactor) {
            h = (int)Math.ceil(chatLinesWrapped.size()*lineHeight*2);
        }*/
        int maxChatLineCount = (int)Math.floor(chatHeight/lineHeight);
        float maxLineWidth = 10;
        int lines = 0;
        for (int lineIndex = 0; lineIndex < maxChatLineCount; ++lineIndex) {
            if (lineIndex + scrollPos >= chatLinesWrapped.size()) {
                break;
            }
            ChatLine chatline = chatLinesWrapped.get(lineIndex + scrollPos);
            if(StringUtils.cleanColour(chatline.getChatComponent().getUnformattedText()).trim().length() > 0) {
                lines = lineIndex+1;
            }
            maxLineWidth = Math.max(maxLineWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(
                    chatline.getChatComponent().getFormattedText())*chatScale);
        }
        int w = (int)Math.ceil((maxLineWidth+8)*chatScaleFactor);
        int h = (int)Math.ceil((lines*lineHeight+4)*chatScaleFactor);

        Framebuffer framebuffer = createBindFramebuffer(w, h);

        String bg = chatBox.getBackgroundColour();
        chatBox.setBackgroundColour("0:0:54:57:63");
        //chatBox.setBackgroundColour("0:0:0:0:0");
        GlStateManager.translate(0, h-2*chatScaleFactor, 0);
        GlStateManager.scale(chatScaleFactor, chatScaleFactor, 1);
        renderChat(chatLinesWrapped, -1, -1, 0, 4, 0, lines,
                scrollPos, true, lineHeight, chatWidth, chatScale, false);
        chatBox.setBackgroundColour(bg);

        screenshotFramebuffer(framebuffer);

        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
    }

    public IChatComponent getHoveredComponent(List<ExtendedChatLine> chatLinesWrapped, int mouseX, int mouseY, int x, int bottomY) {
        if (!isChatOpen() || chatBox.isEditable()) {
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
