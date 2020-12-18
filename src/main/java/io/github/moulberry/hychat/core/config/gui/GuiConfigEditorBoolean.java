package io.github.moulberry.hychat.core.config.gui;

import io.github.moulberry.hychat.core.GuiElementBoolean;
import io.github.moulberry.hychat.core.config.struct.ConfigProcessor;
import io.github.moulberry.hychat.core.util.RenderUtils;
import io.github.moulberry.hychat.core.util.TextRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiConfigEditorBoolean extends GuiConfigEditor {

    private final GuiElementBoolean bool;

    public GuiConfigEditorBoolean(ConfigProcessor.ProcessedOption option) {
        super(option);

        bool = new GuiElementBoolean(0, 0, (boolean)option.get(), 10, option::set);
    }

    @Override
    public void render(int x, int y, int width) {
        super.render(x, y, width);
        int height = getHeight();

        bool.x = x+width/6-24;
        bool.y = y+height-7-14;
        bool.render();
    }

    @Override
    public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
        int height = getHeight();
        bool.x = x+width/6-24;
        bool.y = y+height-7-14;
        return bool.mouseInput(mouseX, mouseY);
    }

    @Override
    public boolean keyboardInput() {
        return false;
    }
}
