package io.github.moulberry.hychat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class HookGuiChat {

    public static void renderChatBar(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int tabLeft = 2;
        for(ChatTab tab : HyChat.getInstance().getChatManager().tabs) {
            int tabWidth = fr.getStringWidth(tab.getTabName())+4;
            int tabTop = height - 27;
            int tabTextColour = 0xff999999;
            int tabBackgroundColour = 0x60000000;

            if(tab == HyChat.getInstance().getChatManager().getSelectedTab()) {
                tabTop--;
                tabTextColour = 0xffffffff;
                tabBackgroundColour = 0x80000000;
            }

            Gui.drawRect(tabLeft, tabTop, tabLeft+tabWidth, height - 15, tabBackgroundColour);
            fr.drawString(tab.getTabName(), tabLeft+2, height - 25, tabTextColour, false);

            tabLeft += tabWidth+1;
        }
    }

    public static void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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


    }

}
