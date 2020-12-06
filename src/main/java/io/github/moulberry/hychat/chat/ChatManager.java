package io.github.moulberry.hychat.chat;

import io.github.moulberry.hychat.gui.GuiChatBox;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;
import static io.github.moulberry.hychat.chat.ChatRegexes.*;

public class ChatManager {

    private List<GuiChatBox> chatBoxes = new ArrayList<>();
    private GuiChatBox focusedChat = null;

    public ChatManager() {
        List<ChatTab> tabs = new ArrayList<>();
        tabs.add(new ChatTab("All").alwaysMatch());
        tabs.add(new ChatTab("Filtered").withIgnore(".*filter.*"));
        tabs.add(new ChatTab("Party")
                        .withMatches(PARTY)
                        .withMessagePrefix("/pc "));
        tabs.add(new ChatTab("Guild")
                .withMatch(RESET.toString()+DARK_GREEN+"Guild > (.*)")
                .withMatch(RESET.toString()+DARK_GREEN+"G > (.*)")
                .withMessagePrefix("/gc "));
        tabs.add(new ChatTab("Private")
                .withMatches(PARTY)
                .withMatch(RESET.toString()+DARK_GREEN+"Guild > (.*)")
                .withMatch(RESET.toString()+DARK_GREEN+"G > (.*)")
                .withMessagePrefix("/gc "));
        chatBoxes.add(new GuiChatBox(tabs));
    }

    public GuiChatBox getFocusedChat() {
        if(focusedChat != null) {
            return focusedChat;
        }
        if(chatBoxes.size() > 0) {
            return chatBoxes.get(0);
        }
        return null;
    }

    public void renderChatBoxes(int mouseX, int mouseY, float partialTicks) {
        for(GuiChatBox chatBox : chatBoxes) {
            chatBox.render(mouseX, mouseY, partialTicks);
        }
    }

    public void renderChatBoxesOverlay(int mouseX, int mouseY, float partialTicks) {
        for(GuiChatBox chatBox : chatBoxes) {
            chatBox.renderOverlay(mouseX, mouseY, partialTicks);
        }
    }

    public void mouseInputChatBoxes(int mouseX, int mouseY) {
        for(GuiChatBox chatBox : chatBoxes) {
            chatBox.mouseInput(mouseX, mouseY);
        }
        GuiChatBox focusedChat = getFocusedChat();
        if(focusedChat != null) {
            int dWheel = Mouse.getEventDWheel();

            if (dWheel != 0) {
                if (dWheel > 1) {
                    dWheel = 1;
                }

                if (dWheel < -1) {
                    dWheel = -1;
                }

                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    dWheel *= 7;
                }

                focusedChat.scroll(dWheel);
            }
        }
    }

    public void deleteChatLine(int lineId) {
        for(GuiChatBox chatBox : chatBoxes) {
            chatBox.deleteChatLine(lineId);
        }
    }

    public void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean refresh) {
        for(GuiChatBox chatBox : chatBoxes) {
            chatBox.setChatLine(chatComponent, chatLineId, updateCounter, refresh);
        }
    }
}
