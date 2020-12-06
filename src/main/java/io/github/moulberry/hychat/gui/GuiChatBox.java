package io.github.moulberry.hychat.gui;

import com.google.common.collect.Queues;
import io.github.moulberry.hychat.mixins.GuiScreenAccessor;
import io.github.moulberry.hychat.chat.ChatTab;
import io.github.moulberry.hychat.resources.CursorIcons;
import io.github.moulberry.hychat.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class GuiChatBox {

    private static final int LEFT = 0b1000;
    private static final int RIGHT = 0b0100;
    private static final int UP = 0b0010;
    private static final int DOWN = 0b0001;

    private static final int EW = 0b00;
    private static final int NS = 0b01;
    private static final int NWSE = 0b10;
    private static final int NESW = 0b11;
    private static final int MOVE = 0b100;

    private final GuiChatTabBar tabBar;
    private final GuiChatArray chatArray;

    public List<ChatTab> tabs;
    private int selectedTab = 0;

    private int scrollPos = 0;

    private int x = 2;
    private int y = -28;
    private int width = 320;
    private int height = 180;

    private int grabbedSide = -1;
    private int grabbedMouseX = -1;
    private int grabbedMouseY = -1;

    boolean locked = false;

    public GuiChatBox(List<ChatTab> tabs) {
        this.tabs = tabs;
        this.tabBar = new GuiChatTabBar(this);
        this.chatArray = new GuiChatArray(this);
    }

    public void deleteChatLine(int lineId) {
        for(ChatTab tab : tabs) {
            tab.deleteChatLine(lineId);
        }
    }

    public void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean refresh) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int scaledWidth =  MathHelper.floor_float((float)getChatWidth(scaledResolution) / getChatScale());
        for(ChatTab tab : tabs) {
            boolean updated =tab.setChatLine(chatComponent, chatLineId, updateCounter, refresh, scaledWidth);
            if(updated && tab == getSelectedTab()) {
                resetScroll();
            }
        }
    }

    public void refreshChat() {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int scaledWidth =  MathHelper.floor_float((float)getChatWidth(scaledResolution) / getChatScale());
        for(ChatTab tab : tabs) {
            tab.refreshChat(scaledWidth);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ChatTab getSelectedTab() {
        if(tabs.size() == 0) {
            return null;
        } else {
            return tabs.get(Math.max(0, Math.min(tabs.size()-1, selectedTab)));
        }
    }

    public void scroll(int scroll) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        this.scrollPos += scroll;
        int i = getSelectedTab().getChatLines().size();

        if (this.scrollPos > i - getChatHeight(scaledResolution)/9) {
            this.scrollPos = i - getChatHeight(scaledResolution)/9;
        }

        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
        }
    }

    public void resetScroll() {
        this.scrollPos = 0;
    }

    public int getScrollPos() {
        return scrollPos;
    }

    public int getSelectedTabIndex() {
        return selectedTab;
    }

    public void setSelectedTabIndex(int selectedTab) {
        if(this.selectedTab != selectedTab) {
            resetScroll();
            this.selectedTab = selectedTab;
        }
    }

    public int getChatWidth(ScaledResolution scaledResolution) {
        return Math.min(scaledResolution.getScaledWidth()-6, width);
    }

    public int getChatHeight(ScaledResolution scaledResolution) {
        return Math.min(scaledResolution.getScaledHeight()-32, height);
    }

    public int getX(ScaledResolution scaledResolution) {
        int realX = x;
        if(x < 0) {
            realX += scaledResolution.getScaledWidth();
        }
        if(realX < 2) {
            return 2;
        }
        int width = getChatWidth(scaledResolution);
        if(realX+width > scaledResolution.getScaledWidth()-2) {
            return scaledResolution.getScaledWidth()-2-width;
        }
        return realX;
    }

    public int getY(ScaledResolution scaledResolution) {
        int realY = y;
        if(y < 0) {
            realY += scaledResolution.getScaledHeight();
        }
        if(realY > scaledResolution.getScaledHeight()-28) {
            return scaledResolution.getScaledHeight()-28;
        }
        int height = getChatHeight(scaledResolution);
        if(realY-height < 2) {
            return 2+height;
        }
        return realY;
    }

    public float getChatScale() {
        return Minecraft.getMinecraft().gameSettings.chatScale;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int x = getX(scaledResolution);
        int y = getY(scaledResolution);
        int width = getChatWidth(scaledResolution);
        int height = getChatHeight(scaledResolution);
        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            tabBar.render(mouseX, mouseY, partialTicks, x, y);
        }
        if(isEditing()) {
            Gui.drawRect(x, y-height, x+width, y, 0xb0404040);
        }
        chatArray.render(getSelectedTab().getChatLines(), mouseX, mouseY,
                Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), x, y);
    }

    public int getGrab(int mouseX, int mouseY, ScaledResolution scaledResolution) {
        if(locked) {
            return -1;
        }

        int x = getX(scaledResolution);
        int y = getY(scaledResolution);
        int width = getChatWidth(scaledResolution);
        int height = getChatHeight(scaledResolution);

        int grabRadius = 4;

        if(mouseX < x-grabRadius || mouseX > x+width+grabRadius ||
                mouseY < y-height-grabRadius || mouseY > y+grabRadius) {
            return -1;
        }

        int grabbedSide = 0;
        if(Math.abs(mouseX - x) <= grabRadius) {
            grabbedSide |= LEFT;
        }
        if(Math.abs(mouseX - (x + width)) <= grabRadius) {
            grabbedSide |= RIGHT;
        }
        if(Math.abs(mouseY - (y - height)) <= grabRadius) {
            grabbedSide |= UP;
        }
        if(Math.abs(mouseY - y) <= grabRadius) {
            grabbedSide |= DOWN;
        }
        if(grabbedSide != 0) {
            return grabbedSide;
        }

        if(mouseX > x && mouseX < x+width &&
            mouseY > y-height && mouseY < y) {
            return LEFT | RIGHT | UP | DOWN;
        }

        return -1;
    }

    public boolean isEditing() {
        return !locked && Minecraft.getMinecraft().currentScreen instanceof GuiChat &&
                Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
    }

    public int getGrabState(int side) {
        boolean left = (side & LEFT) != 0;
        boolean right = (side & RIGHT) != 0;
        boolean up = (side & UP) != 0;
        boolean down = (side & DOWN) != 0;

        if(left && right && up && down) {
            return MOVE;
        }
        if(!left && !right && !up && !down) {
            return -1;
        } else if(left == !right && !up && !down) {
            return EW;
        } else if(!left && !right && up == !down) {
            return NS;
        } else if(left == !right && up == !down) {
            if(left == up) {
                return NWSE;
            } else {
                return NESW;
            }
        }
        return -1;
    }

    public void renderOverlay(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        //int x = getX(scaledResolution);
        int y = getY(scaledResolution);
        //int width = getChatWidth(scaledResolution);
        //int height = getChatHeight(scaledResolution);

        boolean isEditing = isEditing();

        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            IChatComponent ichatcomponent = chatArray.getHoveredComponent(getSelectedTab().getChatLines(),
                    Mouse.getX(), Mouse.getY(), 2, y);

            if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
                ((GuiScreenAccessor)Minecraft.getMinecraft().currentScreen).invokeHandleComponentHover(ichatcomponent, mouseX, mouseY);
            }
        }

        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat && isEditing) {
            int grabbedSide = this.grabbedSide;
            if(grabbedSide == -1) {
                grabbedSide = getGrab(mouseX, mouseY, scaledResolution);
            }
            switch(getGrabState(grabbedSide)) {
                case MOVE:
                    MiscUtils.setCursor(CursorIcons.MOVE, 11, 11); break;
                case EW:
                    MiscUtils.setCursor(CursorIcons.EW, 11, 4); break;
                case NS:
                    MiscUtils.setCursor(CursorIcons.NS, 4, 11); break;
                case NESW:
                    MiscUtils.setCursor(CursorIcons.NESW, 11, 11); break;
                case NWSE:
                    MiscUtils.setCursor(CursorIcons.NWSE, 11, 11); break;
                default:
                    MiscUtils.resetCursor();
            }
        } else {
            MiscUtils.resetCursor();
        }
    }

    public int moveX(int deltaX, int width, int screenWidth) {
        boolean wasPositiveX = this.x >= 0;
        this.x += deltaX;

        if(wasPositiveX) {
            if(this.x < 2) {
                deltaX += 2-this.x;
                this.x = 2;
            }
            if(this.x > screenWidth-2) {
                deltaX += screenWidth-2-this.x;
                this.x = screenWidth-2;
            }
        } else {
            if(this.x+width > -2) {
                deltaX += -2-width-this.x;
                this.x = -2-width;
            }
            if(this.x+screenWidth < 2) {
                deltaX += 2-screenWidth-this.x;
                this.x = 2-screenWidth;
            }
        }

        if(this.x >= 0 && this.x+width/2 > screenWidth/2) {
            this.x -= screenWidth;
        }
        if(this.x < 0 && this.x+width/2 <= -screenWidth/2) {
            this.x += screenWidth;
        }
        return deltaX;
    }

    public int moveY(int deltaY, int height, int screenHeight) {
        boolean wasPositiveY = this.y >= 0;
        this.y += deltaY;

        if(wasPositiveY) {
            if(this.y-height < 2) {
                deltaY += 2+height-this.y;
                this.y = 2+height;
            }
            if(this.y > screenHeight-28) {
                deltaY += screenHeight-28-this.y;
                this.y = screenHeight-28;
            }
        } else {
            if(this.y > -28) {
                deltaY += -28-this.y;
                this.y = -28;
            }
            if(this.y+screenHeight < 2) {
                deltaY += 2-screenHeight-this.y;
                this.y = 2-screenHeight;
            }
        }

        if(this.y >= 0 && this.y-height/2 > screenHeight/2) {
            this.y -= screenHeight;
        }
        if(this.y < 0 && this.y-height/2 <= -screenHeight/2) {
            this.y += screenHeight;
        }
        return deltaY;
    }



    public void mouseInput(int mouseX, int mouseY) {
        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int x = getX(scaledResolution);
        int y = getY(scaledResolution);
        int width = getChatWidth(scaledResolution);
        int height = getChatHeight(scaledResolution);
        tabBar.mouseInput(mouseX, mouseY, x, y);
        chatArray.mouseInput(getSelectedTab().getChatLines(), mouseX, mouseY, x, y);

        int button = Mouse.getEventButton();
        if(button == 0) { //left click
            if(Mouse.getEventButtonState() && isEditing()) {
                grabbedSide = getGrab(mouseX, mouseY, scaledResolution);
                grabbedMouseX = mouseX;
                grabbedMouseY = mouseY;
            } else {
                grabbedSide = -1;
            }
        }
        if(grabbedSide >= 0 && !Mouse.getEventButtonState() && button == -1 && Mouse.isButtonDown(0)) { //Mouse move
            if(grabbedSide == (LEFT | RIGHT | UP | DOWN)) {
                grabbedMouseX += moveX(mouseX - grabbedMouseX, width, scaledResolution.getScaledWidth());
                grabbedMouseY += moveY(mouseY - grabbedMouseY, height, scaledResolution.getScaledHeight());
            } else {
                if((grabbedSide & RIGHT) != 0) {
                    this.width += mouseX - grabbedMouseX;
                    grabbedMouseX = mouseX;
                    if(this.width < 70) {
                        grabbedMouseX += 70 - this.width;
                        this.width = 70;
                    }
                    refreshChat();
                } else if((grabbedSide & LEFT) != 0) {
                    this.width -= mouseX - grabbedMouseX;
                    if(this.width < 70) {
                        this.width = 70;
                    }
                    width = getChatWidth(scaledResolution);
                    moveX(mouseX - grabbedMouseX, width, scaledResolution.getScaledWidth());
                    grabbedMouseX = mouseX;
                    refreshChat();
                }
                if((grabbedSide & DOWN) != 0) {
                    this.height += mouseY - grabbedMouseY;
                    if(this.height < 100) {
                        this.height = 100;
                    }
                    height = getChatHeight(scaledResolution);
                    moveY(mouseY - grabbedMouseY, height, scaledResolution.getScaledHeight());
                    grabbedMouseY = mouseY;
                    refreshChat();
                } else if((grabbedSide & UP) != 0) {
                    this.height -= mouseY - grabbedMouseY;
                    grabbedMouseY = mouseY;
                    if(this.height < 100) {
                        grabbedMouseY += 100 - this.height;
                        this.height = 100;
                    }
                    refreshChat();
                }
            }

        }
    }

    
}
