package io.github.moulberry.hychat.chat;

import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.core.util.StringUtils;
import io.github.moulberry.hychat.gui.GuiChatTabBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChatTab {

    @Expose private boolean matchAny = true;
    @Expose private final String tabName;
    @Expose private String messagePrefix;
    @Expose private boolean alwaysMatch = false;
    @Expose private final List<String> regexMatchS = new ArrayList<>();
    @Expose private final List<String> regexIgnoreS = new ArrayList<>();

    private boolean recompileRegex = true;
    private List<Pattern> regexMatch = null;
    private List<Pattern> regexIgnore = null;

    private int dynamicId = -1;

    private List<ExtendedChatLine> chatLines = new ArrayList<>();
    private List<ExtendedChatLine> chatLinesWrapped = new ArrayList<>();

    //Compact chat stuff
    private long lastCleanMillis = 0;
    private final HashMap<Integer, ChatEntry> chatMessageMap = new HashMap<>();
    public final HashMap<Integer, Set<ExtendedChatLine>> messagesForHash = new HashMap<>();

    public static class ChatEntry {
        int messageCount;
        long lastSeenMessageMillis;

        public ChatEntry(int messageCount, long lastSeenMessageMillis) {
            this.messageCount = messageCount;
            this.lastSeenMessageMillis = lastSeenMessageMillis;
        }
    }

    public static class ChatComponentIgnored extends ChatComponentText {
        public ChatComponentIgnored(String msg) {
            super(msg);
        }
    }

    private ChatTab() { //Default constructor for Gson
        this.tabName = null;
    }

    public ChatTab(String tabName) {
        this.tabName = tabName;
    }

    public int getDynamicId() {
        return dynamicId;
    }

    public void setDynamicId(int dynamicId) {
        this.dynamicId = dynamicId;
    }

    public List<ExtendedChatLine> getChatLines() {
        return Collections.unmodifiableList(chatLinesWrapped);
    }

    public List<ExtendedChatLine> getFullChatLines() {
        return Collections.unmodifiableList(chatLines);
    }

    public String getTabName() {
        return tabName;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public boolean getAlwaysMatch() {
        return alwaysMatch || regexMatchS.isEmpty();
    }

    public ChatTab alwaysMatch() {
        alwaysMatch = true;
        return this;
    }

    public ChatTab withMatch(String match) {
        regexMatchS.add(ChatRegexes.substitute(match));
        recompileRegex = true;
        return this;
    }

    public ChatTab withMatches(String[] matches) {
        for(String match : matches) {
            withMatch(match);
        }
        return this;
    }

    public ChatTab withIgnore(String ignore) {
        regexIgnoreS.add(ChatRegexes.substitute(ignore));
        recompileRegex = true;
        return this;
    }

    public ChatTab withMessagePrefix(String prefix) {
        messagePrefix = prefix;
        return this;
    }

    public ChatTab matchAll() {
        matchAny = false;
        return this;
    }

    public ChatTab matchAny() {
        matchAny = true;
        return this;
    }

    private void compileRegexes() {
        regexMatch = new ArrayList<>();
        regexIgnore = new ArrayList<>();
        for(String match : regexMatchS) {
            try {
                regexMatch.add(Pattern.compile(match));
            } catch(PatternSyntaxException ignored) {}
        }
        for(String ignore : regexIgnoreS) {
            try {
                regexIgnore.add(Pattern.compile(ignore));
            } catch(PatternSyntaxException ignored) {}
        }
        recompileRegex = false;
    }

    public boolean isDivider(String clean) {
        boolean divider = true;
        if(clean.length() < 5) {
            divider = false;
        } else {
            for(int i=0; i<clean.length(); i++) {
                char c = clean.charAt(i);
                if(c != '-' && c != '=' && c != '\u25AC') {
                    divider = false;
                    break;
                }
            }
        }
        return divider;
    }

    public int setChatLine(GuiChatTabBar bar, IChatComponent chatComponent, int chatLineId, int uniqueId,
                           int updateCounter, boolean refresh, int scaledWidth) {
        String clean = StringUtils.cleanColour(chatComponent.getUnformattedText()).trim();
        boolean divider = isDivider(clean);

        if(!refresh) {
            chatComponent = processChatComponent(chatComponent);
            if(chatComponent == null) {
                return 0;
            }
        }

        if (!refresh && chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }

        long currentTime = System.currentTimeMillis();
        if(currentTime - lastCleanMillis > 300*1000 && !chatMessageMap.isEmpty()) {
            lastCleanMillis = currentTime;
            cleanCompactChatMaps();
        }

        int currentMessageHash = -1;
        if (!clean.isEmpty() && !divider) {
            currentMessageHash = getChatComponentHash(chatComponent);

            if(!refresh) {
                if (!chatMessageMap.containsKey(currentMessageHash)) {
                    chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
                } else {
                    ChatEntry entry = chatMessageMap.get(currentMessageHash);
                    if (currentTime - entry.lastSeenMessageMillis > 120*1000) { //Don't compact messages from more than (x=120s) ago, add config option if you want
                        chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
                    } else if (HyChat.getInstance().getChatManager().getConfig().tweaks.compactChat) {
                        boolean deleted = deleteMessageByHash(currentMessageHash);
                        if (!deleted) { //deleteMessageByHash only searches the last (x=100) messages. If nothing was removed, reset counter
                            chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
                        } else {
                            entry.messageCount++;
                            entry.lastSeenMessageMillis = currentTime;
                            if (HyChat.getInstance().getChatManager().getConfig().tweaks.compactChatCount) {
                                chatComponent.appendSibling(new ChatComponentIgnored(EnumChatFormatting.GRAY + " (" + entry.messageCount + ")"));
                            }
                        }
                    } else {
                        entry.messageCount++;
                        entry.lastSeenMessageMillis = currentTime;
                    }
                }
            }
        }

        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, scaledWidth,
                Minecraft.getMinecraft().fontRendererObj, false, false);

        int addedLines = 0;
        boolean lastDividerPartial = false;
        for(IChatComponent ichatcomponent : list) {
            if(HyChat.getInstance().getChatManager().getConfig().tweaks.smartDividers && !divider) {
                String cleanWrap = StringUtils.cleanColour(ichatcomponent.getUnformattedText());

                boolean thisDivider = true;
                for(int i=0; i<cleanWrap.length(); i++) {
                    char c = cleanWrap.charAt(i);
                    if(c != '-' && c != '=' && c != '\u25AC') {
                        thisDivider = false;
                        break;
                    }
                }
                if(lastDividerPartial && thisDivider) {
                    continue;
                }
                if(cleanWrap.length() >= 5) {
                    lastDividerPartial = thisDivider;
                }
            }

            addedLines++;
            ExtendedChatLine line = new ExtendedChatLine(updateCounter, ichatcomponent,
                    chatComponent, chatLineId).setDivider(divider).setUniqueId(uniqueId);
            this.chatLinesWrapped.add(0, line);
            if(currentMessageHash != -1) {
                messagesForHash.computeIfAbsent(currentMessageHash, k->new HashSet<>()).add(line);
            }
        }

        while(chatLinesWrapped.size() > 1000) {
            chatLinesWrapped.remove(this.chatLinesWrapped.size() - 1);
        }

        if (!refresh) {
            ExtendedChatLine line = new ExtendedChatLine(updateCounter, chatComponent,
                    chatComponent, chatLineId).setDivider(divider).setUniqueId(uniqueId);
            this.chatLines.add(0, line);
            if(currentMessageHash != -1) {
                messagesForHash.computeIfAbsent(currentMessageHash, k->new HashSet<>()).add(line);
            }

            while (this.chatLines.size() > 1000) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }

        return addedLines;
    }

    public void deleteChatLine(int lineId) {
        deleteChatLine(chatLines.iterator(), lineId, true);
        deleteChatLine(chatLinesWrapped.iterator(), lineId, false);
    }

    public void cleanCompactChatMaps() {
        chatMessageMap.entrySet().removeIf((entry) -> {
            if(entry.getValue().lastSeenMessageMillis > 120*1000) {
                messagesForHash.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void refreshChat(GuiChatTabBar bar, int scaledWidth) {
        chatLinesWrapped.clear();
        chatMessageMap.clear();
        messagesForHash.clear();
        for(int i=chatLines.size()-1; i>=0; i--) {
            ExtendedChatLine chatLine = chatLines.get(i);
            setChatLine(bar, chatLine.getChatComponent(), chatLine.getChatLineID(), chatLine.getUniqueId(),
                    chatLine.getUpdatedCounter(), true, scaledWidth);
        }
    }

    private void deleteChatLine(Iterator<ExtendedChatLine> iterator, int lineId, boolean all) {
        while(iterator.hasNext()) {
            if(iterator.next().getChatLineID() == lineId) {
                iterator.remove();
                if(!all) {
                    break;
                }
            }
        }
    }

    public IChatComponent processChatComponent(IChatComponent chatComponent) {
        if(alwaysMatch) {
            return chatComponent.createCopy();
        }

        if(regexMatch == null || regexIgnore == null || recompileRegex) {
            compileRegexes();
        }

        String lineString = chatComponent.getFormattedText();

        for(Pattern ignore : regexIgnore) {
            if(ignore.matcher(lineString).matches()) {
                return null;
            }
        }

        int substringStart = -1;

        boolean doesLineMatch = !matchAny || regexMatch.isEmpty();
        for(Pattern match : regexMatch) {
            //If matching all, if the line does not match then it sets doesLineMatch to false
            //If matching any, if the line does match then it sets doesLineMatch to true
            Matcher matcher = match.matcher(lineString);
            if(matcher.matches()) {
                if(substringStart == -1 && matcher.groupCount() == 1) {
                    substringStart = matcher.start(1);
                }
                if(matchAny) {
                    doesLineMatch = true;
                    break;
                }
            } else if(!matchAny) {
                doesLineMatch = false;
                break;
            }
        }

        if(doesLineMatch) {
            IChatComponent component = chatComponent.createCopy();

            if(substringStart > 0 && component instanceof ChatComponentText) {
                ChatComponentText componentText = (ChatComponentText) component;
                component = cutCharactersFromText(substringStart, componentText);
            }

            return component;
        }

        return null;
    }

    private boolean deleteMessageByHash(int hashCode) {
        if(!messagesForHash.containsKey(hashCode) || messagesForHash.get(hashCode).isEmpty()) {
            return false;
        }

        Set<ExtendedChatLine> toRemove = messagesForHash.get(hashCode);
        messagesForHash.remove(hashCode);

        int normalSearchLen = 100;
        int wrappedSearchLen = 300;

        boolean removedMessage = false;
        {
            for(int i=0; i<chatLines.size() && i<normalSearchLen; i++) {
                ExtendedChatLine line = chatLines.get(i);
                if(toRemove.contains(line)) {
                    removedMessage = true;
                    chatLines.remove(i);
                    i--;

                    if(i < 0 || i >= chatLines.size()) {
                        continue;
                    }

                    ExtendedChatLine prevLine = chatLines.get(i);
                    if(isDivider(StringUtils.cleanColour(prevLine.getChatComponent().getUnformattedText())) &&
                            Math.abs(line.getUpdatedCounter() - prevLine.getUpdatedCounter()) <= 2) {
                        chatLines.remove(i);
                    }

                    if(i >= chatLines.size()) {
                        continue;
                    }

                    ExtendedChatLine nextLine = chatLines.get(i);
                    if(isDivider(StringUtils.cleanColour(nextLine.getChatComponent().getUnformattedText())) &&
                            Math.abs(line.getUpdatedCounter() - nextLine.getUpdatedCounter()) <= 2) {
                        chatLines.remove(i);
                    }
                    i--;
                } else if(HyChat.getInstance().getChatManager().getConfig().tweaks.consecutiveCompactChat) {
                    break;
                }
            }
        }
        if(!removedMessage) {
            return false;
        }
        for(int i=0; i<chatLinesWrapped.size() && i<wrappedSearchLen; i++) {
            ExtendedChatLine line = chatLinesWrapped.get(i);
            if(toRemove.contains(line)) {
                chatLinesWrapped.remove(i);
                i--;

                if(i < 0 || i >= chatLinesWrapped.size()) {
                    continue;
                }

                ExtendedChatLine prevLine = chatLinesWrapped.get(i);
                if(isDivider(StringUtils.cleanColour(prevLine.getChatComponent().getUnformattedText())) &&
                        Math.abs(line.getUpdatedCounter() - prevLine.getUpdatedCounter()) <= 2) {
                    chatLinesWrapped.remove(i);
                }

                if(i >= chatLinesWrapped.size()) {
                    continue;
                }

                ExtendedChatLine nextLine = chatLinesWrapped.get(i);
                if(isDivider(StringUtils.cleanColour(nextLine.getChatComponent().getUnformattedText())) &&
                        Math.abs(line.getUpdatedCounter() - nextLine.getUpdatedCounter()) <= 2) {
                    chatLinesWrapped.remove(i);
                }
                i--;
            } else if(HyChat.getInstance().getChatManager().getConfig().tweaks.consecutiveCompactChat) {
                break;
            }
        }
        return true;
    }

    public static int getChatStyleHash(ChatStyle style) {
        HoverEvent hover = style.getChatHoverEvent();
        HoverEvent.Action hoverAction = null;
        int hoverChatHash = 0;
        if(hover != null) {
            hoverAction = hover.getAction();
            hoverChatHash = getChatComponentHash(hover.getValue());
        }

        return Objects.hash(style.getColor(),
                style.getBold(),
                style.getItalic(),
                style.getUnderlined(),
                style.getStrikethrough(),
                style.getObfuscated(),
                hoverAction,
                hoverChatHash,
                style.getChatClickEvent(),
                style.getInsertion());
    }

    public static int getChatComponentHash(IChatComponent chatComponent) {
        List<Integer> siblingHashes = new ArrayList<>();
        for(IChatComponent sibling : chatComponent.getSiblings()) {
            if(!(sibling instanceof ChatComponentIgnored) &&
                    sibling instanceof ChatComponentStyle) {
                siblingHashes.add(getChatComponentHash(sibling));
            }
        }

        if(chatComponent instanceof ChatComponentIgnored) {
            return Objects.hash(siblingHashes);
        }

        /*if(chatComponent instanceof ChatComponentIgnored) {
            if(chatComponent.getSiblings().isEmpty()) {
                return 0;
            } else {
                return Objects.hash(siblingHashes);
            }
        }*/

        return Objects.hash(chatComponent.getUnformattedTextForChat(),
                siblingHashes,
                getChatStyleHash(chatComponent.getChatStyle()));
    }

    public ChatComponentText cutCharactersFromText(int characters, ChatComponentText text) {
        //Cut characters from style
        ChatStyle style = text.getChatStyle().createShallowCopy();
        while(characters >= 2) {
            if (style.getColor() != null) {
                style.setColor(null);
                characters -= 2;
                continue;
            }

            if (style.getBold()) {
                style.setBold(false);
                characters -= 2;
                continue;
            }

            if (style.getItalic()) {
                style.setItalic(false);
                characters -= 2;
                continue;
            }

            if (style.getUnderlined()) {
                style.setUnderlined(false);
                characters -= 2;
                continue;
            }

            if (style.getObfuscated()) {
                style.setObfuscated(false);
                characters -= 2;
                continue;
            }

            if (style.getStrikethrough()) {
                style.setStrikethrough(false);
                characters -= 2;
                continue;
            }

            break;
        }
        //We already cut enough characters, return
        if(((style.getColor() != null || style.getBold() || style.getItalic() || style.getUnderlined() || style.getObfuscated() || style.getStrikethrough())
                && characters < 2) || characters == 0) {
            ChatComponentText textCopy = text.createCopy();
            textCopy.setChatStyle(style);
            return textCopy;
        }
        //Length of main text is more than the amount of characters we need to cut, so we don't need to mess with siblings
        if(text.getUnformattedTextForChat().length() > characters) {
            ChatComponentText cut = new ChatComponentText(text.getUnformattedTextForChat().substring(characters));
            cut.setChatStyle(style);
            for(IChatComponent sibling : text.getSiblings()) {
                cut.appendSibling(sibling);
            }
            return cut;
        } else {
            //Cut off all the text from the main chatcomponent and then cut the remaining characters from the siblings
            int remaining = characters - (text.getUnformattedTextForChat().length()+2);
            ChatComponentText cut = new ChatComponentText("");
            for(IChatComponent sibling : text.getSiblings()) {
                if(remaining > 0 && sibling instanceof ChatComponentText) {
                    int siblingLength = sibling.getUnformattedTextForChat().length();
                    //Cut sibling if its length is greater than remaining
                    if(siblingLength > remaining) {
                        cut.appendSibling(cutCharactersFromText(remaining, (ChatComponentText) sibling));
                    }
                    remaining -= siblingLength + 2;
                } else {
                    cut.appendSibling(sibling);
                }
            }
            return cut;
        }
    }
}
