package io.github.moulberry.hychat.chat;

import io.github.moulberry.hychat.gui.GuiChatBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChatTab {

    private boolean matchAny = true;
    private final String tabName;
    private String messagePrefix;
    private boolean alwaysMatch = false;
    private final List<String> regexMatchS = new ArrayList<>();
    private final List<String> regexIgnoreS = new ArrayList<>();

    private boolean recompileRegex = true;
    private List<Pattern> regexMatch = null;
    private List<Pattern> regexIgnore = null;

    private List<ExtendedChatLine> chatLines = new ArrayList<>();
    private List<ExtendedChatLine> chatLinesWrapped = new ArrayList<>();

    public ChatTab(String tabName) {
        this.tabName = tabName;
    }

    public List<ExtendedChatLine> getChatLines() {
        return Collections.unmodifiableList(chatLinesWrapped);
    }

    public String getTabName() {
        return tabName;
    }

    public String getMessagePrefix() {
        return messagePrefix;
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

    public boolean setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean refresh, int scaledWidth) {
        if(!refresh) {
            chatComponent = processChatComponent(chatComponent);
            if(chatComponent == null) {
                return false;
            }
        }

        if (!refresh && chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }

        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(chatComponent, scaledWidth,
                Minecraft.getMinecraft().fontRendererObj, false, false);

        for(IChatComponent ichatcomponent : list) {
            this.chatLinesWrapped.add(0, new ExtendedChatLine(updateCounter, ichatcomponent, chatComponent, chatLineId));
        }

        while(chatLinesWrapped.size() > 1000) {
            chatLinesWrapped.remove(this.chatLinesWrapped.size() - 1);
        }

        if (!refresh) {
            this.chatLines.add(0, new ExtendedChatLine(updateCounter, chatComponent, chatComponent, chatLineId));

            while (this.chatLines.size() > 1000) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }

        return true;
    }

    public void deleteChatLine(int lineId) {
        deleteChatLine(chatLines.iterator(), lineId, true);
        deleteChatLine(chatLinesWrapped.iterator(), lineId, false);
    }

    public void refreshChat(int scaledWidth) {
        chatLinesWrapped.clear();
        for(int i=chatLines.size()-1; i>=0; i--) {
            ChatLine chatLine = chatLines.get(i);
            setChatLine(chatLine.getChatComponent(), chatLine.getChatLineID(),
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
            return chatComponent;
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
