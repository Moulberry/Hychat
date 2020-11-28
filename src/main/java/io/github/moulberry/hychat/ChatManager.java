package io.github.moulberry.hychat;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

public class ChatManager {

    public List<ChatTab> tabs;

    private int selectedTab = 0;

    public ChatManager() {
        this.tabs = new ArrayList<>();
        this.tabs.add(new ChatTab("All"));
        this.tabs.add(new ChatTab("Filtered").withMatch(".*{HYPIXEL_NAME}.*").withIgnore(".*filter.*"));
        this.tabs.add(new ChatTab("Party")
                        .withMatch("{RESET}{BLUE}Party {DARK_GRAY}> (.*)")
                        .withMatch("{HYPIXEL_NAME} {RESET}{YELLOW}invited {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to the party! They have {RESET}{RED}" +
                                "60 {RESET}{YELLOW}seconds to accept.{RESET}")
                        .withMatch("{HYPIXEL_NAME} {RESET}{YELLOW}has left the party.{RESET}")
                        .withMatch("{HYPIXEL_NAME} {RESET}{YELLOW}joined the party.{RESET}")
                        .withMatch("{YELLOW}You left the party.{RESET}")
                        .withMatch("{YELLOW}You have joined {RESET}{HYPIXEL_NAME}'s {RESET}{YELLOW}party!{RESET}")
                        .withMatch("{RED}The party was disbanded because all invites expired and the party was empty{RESET}")
                        .withMatch("{RED}You cannot invite that player since they're not online.{RESET}")
                        .withMatch("{YELLOW}The party leader, {HYPIXEL_NAME}{RESET}{YELLOW}, warped you to {HYPIXEL_NAME}{RESET}{YELLOW}'s house.{RESET}")
                        /*.withMatch(".*"+YELLOW+"invited.*")*/
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
