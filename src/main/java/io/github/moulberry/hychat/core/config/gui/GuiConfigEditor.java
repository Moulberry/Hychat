package io.github.moulberry.hychat.core.config.gui;

import io.github.moulberry.hychat.core.config.struct.ConfigProcessor;
import io.github.moulberry.hychat.core.util.RenderUtils;
import io.github.moulberry.hychat.core.util.TextRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GuiConfigEditor {

    protected final ConfigProcessor.ProcessedOption option;
    private static final int HEIGHT = 45;

    public GuiConfigEditor(ConfigProcessor.ProcessedOption option) {
        this.option = option;
    }

    public void render(int x, int y, int width) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        RenderUtils.drawFloatingRectDark(x, y, width, HEIGHT, true);
        TextRenderUtils.drawStringCenteredScaledMaxWidth(option.name,
                fr, x+width/6, y+13, true, width/3-10, 0xc0c0c0);
        int maxLines = 3;
        float scale = 1;
        int paraHeight = TextRenderUtils.getParagraphHeight(option.desc, width*2/3-10, -1, 1);
        while(paraHeight >= (int)(12*scale)*maxLines+(int)(8*scale) && maxLines < 5) {
            maxLines++;
            scale -= 2/8f;
            paraHeight = TextRenderUtils.getParagraphHeight(option.desc, width*2/3-10, -1, scale);
        }
        TextRenderUtils.drawTextParagraph(option.desc,
                fr, true, x+5+width/3,
                y+HEIGHT/2-paraHeight/2, width*2/3-10, 0xc0c0c0, maxLines, scale);
    }

    public int getHeight() {
        return HEIGHT;
    }

    public abstract boolean mouseInput(int x, int y, int width, int mouseX, int mouseY);
    public abstract boolean keyboardInput();

}
