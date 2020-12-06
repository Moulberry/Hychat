package io.github.moulberry.hychat.gui;

import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.chat.ChatTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.util.List;

public class GuiChatTabBar {

    public final GuiChatBox chatBox;

    public GuiChatTabBar(GuiChatBox chatBox) {
        this.chatBox = chatBox;
    }

    public void mouseInput(float mouseX, float mouseY, int x, int y) {
        if(Mouse.getEventButtonState() && mouseY >= y && mouseY <= y+13) {
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            int tabLeft = x;
            int index = 0;
            for(ChatTab tab : chatBox.tabs) {
                int tabWidth = fr.getStringWidth(tab.getTabName())+4;

                if(mouseX >= tabLeft && mouseX <= tabLeft+tabWidth) {
                    chatBox.setSelectedTabIndex(index);
                    return;
                }

                index++;
                tabLeft += tabWidth+1;
            }
        }
    }

    public void render(float mouseX, float mouseY, float partialTicks, int x, int y) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int tabLeft = x;
        for(ChatTab tab : chatBox.tabs) {
            int tabWidth = fr.getStringWidth(tab.getTabName())+4;
            int tabTop = y+1;
            int tabTextColour = 0xff999999;
            int tabBackgroundColour = 0x60000000;

            if(tab == chatBox.getSelectedTab()) {
                tabTop--;
                tabTextColour = 0xffffffff;
                tabBackgroundColour = 0x80000000;
            }

            Gui.drawRect(tabLeft, tabTop, tabLeft+tabWidth, y+13, tabBackgroundColour);
            fr.drawString(tab.getTabName(), tabLeft+2, y+3, tabTextColour, false);

            tabLeft += tabWidth+1;
        }
    }

}
