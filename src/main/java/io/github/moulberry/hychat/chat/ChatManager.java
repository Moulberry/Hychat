package io.github.moulberry.hychat.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.Resources;
import io.github.moulberry.hychat.config.chatbox.GeneralConfig;
import io.github.moulberry.hychat.core.util.RenderUtils;
import io.github.moulberry.hychat.core.util.StringUtils;
import io.github.moulberry.hychat.gui.GuiChatBox;
import io.github.moulberry.hychat.gui.GuiEditConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.EnumChatFormatting.*;
import static io.github.moulberry.hychat.chat.ChatRegexes.*;

public class ChatManager {

    @Expose private GuiChatBox primaryChatBox;
    @Expose private List<GuiChatBox> extraChatBoxes = new ArrayList<>();
    @Expose private final GeneralConfig config = new GeneralConfig();

    private GuiChatBox focusedChat = null;
    private GuiEditConfig editor = null;
    private final HashSet<Integer> unviewedMessages = new HashSet<>();

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public ChatManager() {
        resetPrimaryChatBox();
    }

    private void resetPrimaryChatBox() {
        List<ChatTab> tabs = new ArrayList<>();
        tabs.add(new ChatTab("All").alwaysMatch());
        tabs.add(new ChatTab("Filtered")
                .withIgnore("{RESET}{HYPIXEL_NAME}{WHITE} {GOLD}[a-zA-Z]+ into the lobby!{RESET}")
                .withIgnore("{RESET} (?:{ANY_COLOUR}>){3}{RESET} {HYPIXEL_NAME}{WHITE} {GOLD}[a-zA-Z]+ into the lobby!{RESET} (?:{ANY_COLOUR}<){3}{RESET}")
                .withIgnore("{ANY_COLOUR_OPT}{MC_NAME} {RESET}{WHITE}found a .+ {RESET}{AQUA}Mystery Box{RESET}{WHITE}!{RESET}")
                .withIgnore("{AQUA}\\[Mystery Box\\] {ANY_COLOUR}+{MC_NAME} {WHITE}found a .+"));
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
        primaryChatBox = new GuiChatBox(tabs);
    }

    public void openEditor(GuiChatBox chatBox) {
        editor = new GuiEditConfig(chatBox.getConfig());
    }

    public void saveTo(File file) {
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            primaryChatBox.tabBar.removeDynamicTabs();
            for(GuiChatBox chatBox : extraChatBoxes) {
                chatBox.tabBar.removeDynamicTabs();
            }
            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(gson.toJson(this));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ChatManager loadFrom(File file) {
        try {
            if(file.exists()) {
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8))) {
                    ChatManager chatManager = gson.fromJson(reader, ChatManager.class);

                    chatManager.primaryChatBox.tabBar.chatBox = chatManager.primaryChatBox;
                    chatManager.primaryChatBox.chatArray.chatBox = chatManager.primaryChatBox;
                    for(GuiChatBox box : chatManager.extraChatBoxes) {
                        box.tabBar.chatBox = box;
                        box.chatArray.chatBox = box;
                    }

                    return chatManager;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ChatManager();
    }

    public GuiChatBox getFocusedChat() {
        if(focusedChat != null) {
            return focusedChat;
        }
        if(primaryChatBox == null) {
            resetPrimaryChatBox();
        }
        return primaryChatBox;
    }

    public void renderChatBoxes(int mouseX, int mouseY, float partialTicks) {
        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            editor = null;
        }
        if(editor != null) {
            mouseX = 0;
            mouseY = 0;
        }
        primaryChatBox.render(mouseX, mouseY, partialTicks);
        for(GuiChatBox chatBox : extraChatBoxes) {
            chatBox.render(mouseX, mouseY, partialTicks);
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
    }

    public void renderChatBoxesOverlay(int mouseX, int mouseY, float partialTicks) {
        if(editor != null) {
            editor.render();
            return;
        }

        primaryChatBox.renderOverlay(mouseX, mouseY, partialTicks);
        for(GuiChatBox chatBox : extraChatBoxes) {
            chatBox.renderOverlay(mouseX, mouseY, partialTicks);
        }
        if(!unviewedMessages.isEmpty() && primaryChatBox.getScrollPos() == 0 && primaryChatBox.getSelectedTab().getAlwaysMatch()) {
            unviewedMessages.clear();
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.Overlay.HAMBURGER);
        GlStateManager.color(1, 1, 1, 1);
        RenderUtils.drawTexturedRect(scaledResolution.getScaledWidth()-16, scaledResolution.getScaledHeight()-30, 14, 14);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
    }

    public boolean keyboardInputChatBoxes() {
        if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE &&
            editor != null) {
            editor = null;
            return true;
        }
        if(editor != null && editor.keyboardInput()) {
            return true;
        }
        boolean cancel = primaryChatBox.keyboardInput();
        for(GuiChatBox chatBox : extraChatBoxes) {
            cancel |= chatBox.keyboardInput();
        }
        return cancel;
    }

    public void mouseInputChatBoxes(int mouseX, int mouseY) {
        if(editor != null && editor.mouseInput(mouseX, mouseY)) {
            return;
        }
        primaryChatBox.mouseInput(mouseX, mouseY);
        for(GuiChatBox chatBox : extraChatBoxes) {
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
        primaryChatBox.deleteChatLine(lineId);
        for(GuiChatBox chatBox : extraChatBoxes) {
            chatBox.deleteChatLine(lineId);
        }
    }

    public GeneralConfig getConfig() {
        return config;
    }

    private static final Pattern messagePattern = Pattern.compile(ChatRegexes.substitute(
            "{LIGHT_PURPLE}(?:To|From) {ANY_COLOUR_OPT}({HYPIXEL_NAME}){RESET}{GRAY}: .+{RESET}"));
    public void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean refresh) {
        if(refresh) return;

        Matcher matcher = messagePattern.matcher(chatComponent.getFormattedText());
        if(matcher.matches()) {
            String hypixelName = matcher.group(1);
            String playerName;
            if(hypixelName.contains(" ")) {
                playerName = StringUtils.cleanColour(hypixelName.split(" ")[1]);
            } else {
                playerName = StringUtils.cleanColour(hypixelName);
            }
            if(primaryChatBox.tabBar.getDynamicTabIndex(playerName.hashCode()) == -1) {
                primaryChatBox.tabBar.addDynamicTab(playerName.hashCode(),
                        new ChatTab(playerName)
                                .withMatch("{LIGHT_PURPLE}To {ANY_COLOUR_OPT}"+Pattern.quote(hypixelName)+"{RESET}{GRAY}: .+{RESET}")
                                .withMatch("{LIGHT_PURPLE}From {ANY_COLOUR_OPT}"+Pattern.quote(hypixelName)+"{RESET}{GRAY}: .+{RESET}")
                                .withMessagePrefix("/msg "+playerName+" ")
                );
            }
        }

        int uniqueId = UUID.randomUUID().hashCode();
        unviewedMessages.add(uniqueId);
        primaryChatBox.setChatLine(chatComponent, chatLineId, uniqueId, updateCounter, false);
        for(GuiChatBox chatBox : extraChatBoxes) {
            chatBox.setChatLine(chatComponent, chatLineId, uniqueId, updateCounter, false);
        }
    }

    public boolean isMessageViewed(int uniqueId) {
        return !unviewedMessages.contains(uniqueId);
    }

    public void viewMessage(int uniqueId) {
        unviewedMessages.remove(uniqueId);
    }
}
