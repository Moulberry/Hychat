package io.github.moulberry.hychat;

import com.google.common.collect.Lists;
import com.sun.javafx.UnmodifiableArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChatTab {

    private boolean matchAny = true;
    private final String tabName;
    private String messagePrefix;
    private final List<String> regexMatch = new ArrayList<>();
    private final List<String> regexIgnore = new ArrayList<>();

    private List<ChatLine> chatLines = new ArrayList<>();

    public ChatTab(String tabName) {
        this.tabName = tabName;
    }

    public List<ChatLine> getChatLines() {
        return Collections.unmodifiableList(chatLines);
    }

    public String getTabName() {
        return tabName;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public ChatTab withMatch(String match) {
        regexMatch.add(ChatRegexes.substitute(match));
        return this;
    }

    public ChatTab withIgnore(String ignore) {
        regexIgnore.add(ChatRegexes.substitute(ignore));
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

    public void update() {
        if(Minecraft.getMinecraft().ingameGUI == null) {
            return;
        }
        GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        if(chat == null) {
            return;
        }
        List<ChatLine> lines = chat.chatLines;
        chatLines.clear();

        List<Pattern> matches = new ArrayList<>();
        List<Pattern> ignores = new ArrayList<>();
        for(String match : regexMatch) {
            try {
                matches.add(Pattern.compile(match));
            } catch(PatternSyntaxException ignored) {}
        }
        for(String ignore : regexIgnore) {
            try {
                ignores.add(Pattern.compile(ignore));
            } catch(PatternSyntaxException ignored) {}
        }

        out:
        for(ChatLine line : lines) {
            String lineString = line.getChatComponent().getFormattedText();

            for(Pattern ignore : ignores) {
                if(ignore.matcher(lineString).matches()) {
                    continue out;
                }
            }

            int substringStart = -1;

            boolean doesLineMatch = !matchAny || matches.isEmpty();
            for(Pattern match : matches) {
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
                IChatComponent component = line.getChatComponent().createCopy();

                if(substringStart > 0 && component instanceof ChatComponentText) {
                    ChatComponentText componentText = (ChatComponentText) component;
                    component = cutCharactersFromText(substringStart, componentText);
                }

                int i = MathHelper.floor_float((float)chat.getChatWidth() / chat.getChatScale());
                List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(component, i,
                        Minecraft.getMinecraft().fontRendererObj, false, false);

                for(int index=list.size()-1; index>=0; index--) {
                    chatLines.add(new ChatLine(line.getUpdatedCounter(), list.get(index), line.getChatLineID()));
                }
            }
        }
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
