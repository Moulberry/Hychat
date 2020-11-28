package io.github.moulberry.hychat;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;
import static io.github.moulberry.hychat.ChatRegexes.*;

public class ChatManager {

    public List<ChatTab> tabs;

    private int selectedTab = 0;

    public ChatManager() {
        this.tabs = new ArrayList<>();
        this.tabs.add(new ChatTab("All"));
        this.tabs.add(new ChatTab("Filtered").withMatch(".*{HYPIXEL_NAME}.*").withIgnore(".*filter.*"));
        this.tabs.add(new ChatTab("Party")
                        .withMatch(PARTY_TALK)
                        .withMatch(PARTY_INVITE)
                        .withMatch(PARTY_OTHER_LEAVE)
                        .withMatch(PARTY_OTHER_JOIN)
                        .withMatch(PARTY_LEAVE)
                        .withMatch(PARTY_JOIN)
                        .withMatch(PARTY_DISBANDED)
                        .withMatch(PARTY_INVITE_NOT_ONLINE)
                        .withMatch(PARTY_HOUSING_WARP)
                        .withMessagePrefix("/pc "));
        this.tabs.add(new ChatTab("Guild")
                .withMatch(RESET.toString()+DARK_GREEN+"Guild > (.*)")
                .withMessagePrefix("/gc "));
        this.tabs.add(new ChatTab("Private")
                .withMatch(RESET.toString()+BLUE+"Party "+DARK_GRAY+"> (.*)")
                .withMatch(RESET.toString()+DARK_GREEN+"Guild > (.*)")
                .matchAny()
                .withMessagePrefix("/gc "));
    }

    public void updateTabs() {
        for(ChatTab tab : tabs) {
            tab.update();
        }
    }

    public ChatTab getSelectedTab() {
        if(tabs.size() == 0) {
            return null;
        } else {
            return tabs.get(Math.max(0, Math.min(tabs.size()-1, selectedTab)));
        }
    }

    public int getSelectedTabIndex() {
        return selectedTab;
    }

    public void setSelectedTabIndex(int selectedTab) {
        this.selectedTab = selectedTab;
    }
}
