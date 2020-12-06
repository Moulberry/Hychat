package io.github.moulberry.hychat.gui;

import io.github.moulberry.hychat.chat.ChatTab;
import io.github.moulberry.hychat.HyChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class GuiChatOverlay {

    /*public void renderOverlay(int mouseX, int mouseY, float partialTicks) {
        renderChatBar(mouseX, mouseY, partialTicks);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int tabTop = height - 27;
        if(mouseY >= tabTop && mouseY <= height - 15) {
            int tabLeft = 2;
            int index = 0;
            for(ChatTab tab : HyChat.getInstance().getChatManager().tabs) {
                int tabWidth = fr.getStringWidth(tab.getTabName())+4;

                if(mouseX >= tabLeft && mouseX <= tabLeft+tabWidth) {
                    HyChat.getInstance().getChatManager().setSelectedTabIndex(index);
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().resetScroll();
                    return;
                }

                tabLeft += tabWidth+1;
                index++;
            }
        }
    }*/

}
