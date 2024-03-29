package io.github.moulberry.hychat.gui;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.Resources;
import io.github.moulberry.hychat.chat.ChatTab;
import io.github.moulberry.hychat.chat.ExtendedChatLine;
import io.github.moulberry.hychat.core.ChromaColour;
import io.github.moulberry.hychat.core.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiChatTabBar {

    @Expose private int selectedTab = 0;
    @Expose private List<ChatTab> tabs;

    private static int SPLIT_Y_THRESHOLD = 20;
    private static int SPLIT_X_THRESHOLD = 30;

    public boolean movingSingleTab = false;
    public boolean addingTabToOtherChatBox = false;

    public GuiChatBox chatBox;
    private int selectedTabX = 0;
    private int selectedTabMouseX = 0;
    private int selectedTabY = 0;
    private int selectedTabMouseY = 0;
    private boolean movingTab = false;

    private GuiChatTabBar() { //Default constructor for Gson
    }

    public GuiChatTabBar(GuiChatBox chatBox, List<ChatTab> tabs) {
        this.chatBox = chatBox;
        this.tabs = tabs;
    }

    public boolean shouldSplitTab(int mouseX, int mouseY) {
        if(!movingTab) return false;

        return Math.abs(selectedTabMouseY - mouseY) > SPLIT_Y_THRESHOLD;
    }

    public void mouseInput(float mouseX, float mouseY, int x, int y) {
        addingTabToOtherChatBox = false;

        if(movingSingleTab && Mouse.getEventButton() != -1) {
            movingSingleTab = false;
        }

        if(chatBox.isEditing()) {
            movingTab = false;
        } else if(Mouse.getEventButton() == 0) {
            movingTab = false;
            if(Mouse.getEventButtonState() && mouseY >= y && mouseY <= y + 13) {
                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

                int tabLeft = x;
                int index = 0;
                for(ChatTab tab : tabs) {
                    int tabWidth = fr.getStringWidth(tab.getTabName()) + 4;

                    if(mouseX >= tabLeft && mouseX <= tabLeft + tabWidth) {
                        chatBox.resetScroll();
                        selectedTab = index;
                        selectedTabX = tabLeft - (int) mouseX;
                        selectedTabY = y - (int) mouseY;
                        selectedTabMouseX = (int) mouseX;
                        selectedTabMouseY = (int) mouseY;
                        if(!chatBox.isLocked()) {
                            movingTab = true;
                        }
                    }

                    index++;
                    tabLeft += tabWidth + 1;
                }
            }
        } else if(!chatBox.showEditOverlay()) {
            movingSingleTab = false;
            addingTabToOtherChatBox = false;
            movingTab = false;
            return;
        } else if(Mouse.getEventButton() != -1 && !Mouse.getEventButtonState() && movingTab) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            GuiChatBox addTabTo = null;
            for(GuiChatBox chatBox : HyChat.getInstance().getChatManager().getAllChatBoxes()) {
                if(chatBox == this.chatBox) continue;

                int chatBoxX = chatBox.getX(scaledResolution);
                int chatBoxY = chatBox.getY(scaledResolution);
                int chatBoxWidth = chatBox.getChatWidth(scaledResolution);
                int chatBoxHeight = chatBox.getChatHeight(scaledResolution);

                if(mouseX > chatBoxX && mouseX < chatBoxX + chatBoxWidth &&
                        mouseY > chatBoxY - chatBoxHeight && mouseY < chatBoxY) {
                    addTabTo = chatBox;
                    break;
                }
            }

            if(addTabTo != null && addTabTo.tabBar != null) {
                addTabTo.tabBar.tabs.add(getSelectedTab());
                tabs.remove(selectedTab).refreshChat(addTabTo.tabBar, addTabTo.getChatWidth(scaledResolution));

                if(tabs.size() == 0) {
                    HyChat.getInstance().getChatManager().removeChatBox(chatBox);
                }

                selectedTab = 0;
            } else if(shouldSplitTab((int)mouseX, (int)mouseY) && tabs.size() > 1) {
                addTabTo = HyChat.getInstance().getChatManager().createNewChatBox(selectedTabX+(int)mouseX,
                        y + ((int)mouseY - selectedTabMouseY), 100, 50, Lists.newArrayList(getSelectedTab()));
                tabs.remove(selectedTab).refreshChat(addTabTo.tabBar, addTabTo.getChatWidth(scaledResolution));
                selectedTab = 0;
            }

            movingTab = false;
        } else if(movingTab && Mouse.getEventButton() == -1 &&
                !Mouse.getEventButtonState() && Mouse.isButtonDown(0)) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            if(tabs.size() == 1 || shouldSplitTab((int)mouseX, (int)mouseY)) {
                for(GuiChatBox chatBox : HyChat.getInstance().getChatManager().getAllChatBoxes()) {
                    if(chatBox == this.chatBox) continue;
                    if(!chatBox.isEditable()) continue;

                    int chatBoxX = chatBox.getX(scaledResolution);

                    int chatBoxY = chatBox.getY(scaledResolution);
                    int chatBoxWidth = chatBox.getChatWidth(scaledResolution);
                    int chatBoxHeight = chatBox.getChatHeight(scaledResolution);

                    if(mouseX > chatBoxX && mouseX < chatBoxX + chatBoxWidth &&
                        mouseY > chatBoxY - chatBoxHeight && mouseY < chatBoxY) {

                        addingTabToOtherChatBox = true;
                    }
                }
            }

            movingSingleTab = tabs.size() == 1 && movingTab;

            if(movingSingleTab) {
                int deltaX = -((int)mouseX - selectedTabMouseX);
                int deltaY = -((int)mouseY - selectedTabMouseY);

                deltaX = chatBox.moveX(-deltaX, chatBox.getChatWidth(scaledResolution), scaledResolution.getScaledWidth());
                deltaY = chatBox.moveY(-deltaY, chatBox.getChatHeight(scaledResolution), scaledResolution.getScaledHeight());

                selectedTabX += deltaX;
                selectedTabY += deltaY;
                selectedTabMouseX += deltaX;
                selectedTabMouseY += deltaY;

                return;
            }

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int tabLeft = x;
            int moveDir = 0;
            for(ChatTab tab : tabs) {
                boolean selected = tab == getSelectedTab();
                int tabWidth = fr.getStringWidth(tab.getTabName())+4;
                if(selected) {
                    if(tabs.size() > selectedTab+1) {
                        int nextWidth = fr.getStringWidth(tabs.get(selectedTab+1).getTabName())+4;

                        if(selectedTabX+(int)mouseX > tabLeft+nextWidth/2) {
                            moveDir = 1;
                        }
                    }
                    break;
                } else if(selectedTab > 0 && tabLeft+tabWidth/2 > selectedTabX+(int)mouseX) {
                    moveDir = -1;
                }
                tabLeft += tabWidth+1;
            }
            if(moveDir > 0) {
                tabs.add(selectedTab+1, tabs.remove(selectedTab));
                selectedTab++;
            } else if(moveDir < 0) {
                tabs.add(selectedTab-1, tabs.remove(selectedTab));
                selectedTab--;
            }
        }
    }

    public int getDynamicTabIndex(int id) {
        if(id == -1) return -1;
        for(int i=0; i<tabs.size(); i++) {
            ChatTab tab = tabs.get(i);
            if(tab.getDynamicId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void addDynamicTab(int id, ChatTab tab) {
        int oldIndex = getDynamicTabIndex(id);
        if(oldIndex != -1) {
            tabs.remove(oldIndex);
        }
        tab.setDynamicId(id);
        tabs.add(tab);
    }

    public void removeDynamicTabs() {
        tabs.removeIf((tab) -> tab.getDynamicId() != -1);
    }

    public List<ChatTab> getTabs() {
        return tabs;
    }

    public ChatTab getSelectedTab() {
        if(tabs.size() == 0) {
            return null;
        } else {
            return tabs.get(Math.max(0, Math.min(tabs.size()-1, selectedTab)));
        }
    }

    public void render(float mouseX, float mouseY, float partialTicks, int x, int y) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LESS);

        boolean shouldSplit = shouldSplitTab((int)mouseX, (int)mouseY) || addingTabToOtherChatBox;
        boolean showMovingTab = movingTab && tabs.size() > 1 && (shouldSplit || Math.abs(mouseX - selectedTabMouseX) > 3);

        if(showMovingTab && !addingTabToOtherChatBox) {
            ChatTab tab = getSelectedTab();
            int tabWidth = fr.getStringWidth(tab.getTabName())+4;
            int tabTextColour = 0xffffffff;
            int tabBackgroundColour = ChromaColour.specialToChromaRGB(chatBox.getBackgroundColour());

            int movingY = y;
            if(shouldSplit) {
                movingY = y + ((int)mouseY - selectedTabMouseY);
            }

            GlStateManager.translate(0, 0, 2f);
            Gui.drawRect(selectedTabX+(int)mouseX, movingY, selectedTabX+(int)mouseX+tabWidth,
                    movingY+13, tabBackgroundColour);
            GlStateManager.translate(0, 0, 1f);
            fr.drawString(tab.getTabName(), selectedTabX+(int)mouseX+2, movingY+3, tabTextColour, false);
            GlStateManager.translate(0, 0, -3f);

            if(shouldSplit) {
                Gui.drawRect(selectedTabX+(int)mouseX, movingY,
                        selectedTabX+(int)mouseX+100, movingY-50, 0x80000000);
            }
        }

        int index = 0;
        int tabLeft = x;
        for(ChatTab tab : tabs) {
            boolean selected = tab == getSelectedTab();

            int tabWidth = fr.getStringWidth(tab.getTabName())+4;

            int defaultTabBackgroundColour = ChromaColour.specialToChromaRGB(chatBox.getBackgroundColour());
            if(!showMovingTab || !selected) {
                if(movingTab && index == selectedTab+1) {
                    int selWidth = fr.getStringWidth(getSelectedTab().getTabName())+4;
                    //tabLeft = Math.max(tabLeft, selectedTabX+(int)mouseX+selWidth+1);
                }
                int tabTop = y+1;
                int tabTextColour = 0xffbbbbbb;
                int tabBackgroundColour = (defaultTabBackgroundColour & 0xFFFFFF) |
                        ((((defaultTabBackgroundColour >> 24) & 0xFF)*3/4) << 24);

                if(selected) {
                    tabTop--;
                    tabTextColour = 0xffffffff;
                    tabBackgroundColour = defaultTabBackgroundColour;
                }

                GlStateManager.enableDepth();
                GlStateManager.depthFunc(GL11.GL_LESS);
                GlStateManager.depthMask(false);
                Gui.drawRect(tabLeft, tabTop, tabLeft+tabWidth, y+13, tabBackgroundColour);

                GlStateManager.translate(0, 0, 1f);

                List<ExtendedChatLine> chatLines = tab.getFullChatLines();
                for(int i=0; i<20 && i<chatLines.size(); i++) {
                    ExtendedChatLine chatLine = chatLines.get(i);
                    if(!HyChat.getInstance().getChatManager().isMessageViewed(chatLine.getUniqueId())) {
                        GlStateManager.depthMask(true);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.Overlay.NOTIF);
                        GlStateManager.color(1, 1, 1, 1);
                        RenderUtils.drawTexturedRect(tabLeft+tabWidth-3, tabTop-2, 4, 4, GL11.GL_LINEAR);
                        GlStateManager.depthMask(false);
                        break;
                    }
                }

                if(chatBox.getConfig().appearance.tabTextShadow == 2) {
                    for(int xOff=-2; xOff<=2; xOff++) {
                        for(int yOff=-2; yOff<=2; yOff++) {
                            if(xOff*xOff != yOff*yOff) {
                                fr.drawString(tab.getTabName(), tabLeft+2+xOff/2f, y+3+yOff/2f,
                                        0x50000000, false);
                            }
                        }
                    }
                }

                fr.drawString(tab.getTabName(), tabLeft+2, y+3, tabTextColour, chatBox.getConfig().appearance.tabTextShadow == 1);
                GlStateManager.translate(0, 0, -1f);
                GlStateManager.depthMask(true);
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
            }

            if(!shouldSplit || !selected) {
                index++;
                tabLeft += tabWidth+1;
            }
        }

        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableDepth();
    }

}
