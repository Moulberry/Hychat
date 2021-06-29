package io.github.moulberry.hychat.util;

import com.mojang.realmsclient.util.Pair;
import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessing {

    private static HashMap<Character, Character> homoglyphMap = null;
    private static HashSet<String> badWordSet = null;
    private static HashMap<Integer, String> badWordReplacements = new HashMap<>();
    private static HashSet<Character> emptyCharSet = null;

    public static IChatComponent cleanChatComponent(IChatComponent chatComponent) {
        IChatComponent newComponent;
        if(chatComponent instanceof ChatComponentText) {
            ChatComponentText text = (ChatComponentText) chatComponent;

            newComponent = new ChatComponentText(cleanText(text.getUnformattedTextForChat()));
            newComponent.setChatStyle(text.getChatStyle().createShallowCopy());

            for(IChatComponent sibling : text.getSiblings()) {
                newComponent.appendSibling(cleanChatComponent(sibling));
            }
        } else if(chatComponent instanceof ChatComponentTranslation) {
            ChatComponentTranslation trans = (ChatComponentTranslation) chatComponent;
            System.out.println(trans);

            Object[] args = trans.getFormatArgs();
            Object[] newArgs = new Object[args.length];
            for(int i=0; i<trans.getFormatArgs().length; i++) {
                if(args[i] instanceof IChatComponent) {
                    newArgs[i] = cleanChatComponent((IChatComponent) args[i]);
                } else {
                    newArgs[i] = args[i];
                }
            }
            newComponent = new ChatComponentTranslation(trans.getKey(), newArgs);

            for(IChatComponent sibling : trans.getSiblings()) {
                newComponent.appendSibling(cleanChatComponent(sibling));
            }
        } else {
            newComponent = chatComponent.createCopy();
        }

        return newComponent;
    }

    public static String cleanText(String str) {
        String cleaned = removeEmptyChars(str);
        GeneralConfig config = HyChat.getInstance().getChatManager().getConfig();
        if (config.tweaks.filterBadWords) {
            StringBuilder builder = new StringBuilder(cleaned);
            HashSet<Pair<Integer, Integer>> foundBadWords = findBadWords(substituteHomoglyphs(cleaned.toString()));
            for (Pair<Integer, Integer> indexes : foundBadWords) {
                builder.replace(indexes.first(), indexes.second(), getBadWordReplacement(indexes.second() - indexes.first()));
            }
            cleaned = builder.toString();
        }
        if (config.tweaks.replaceHomoglyphs) {
            cleaned = substituteHomoglyphs(cleaned);
        }
        return cleaned;
    }

    public static String substituteHomoglyphs(String str) {
        if(homoglyphMap == null) generateHomoglyphMap();

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if(homoglyphMap.containsKey(c)) {
                c = homoglyphMap.get(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String removeEmptyChars(String str) {
        if(emptyCharSet == null) generateEmptyCharSet();

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if(!emptyCharSet.contains(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static HashSet<Pair<Integer, Integer>> findBadWords(String str) {
        if(badWordSet == null) generateBadWordSet();
        HashSet<Pair<Integer, Integer>> found = new HashSet<>();

        for(String filter : badWordSet) {
            Pattern pattern = Pattern.compile("(?i)"+ Pattern.quote(filter));
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                found.add(Pair.of(matcher.start(), matcher.end()));
            }
        }

        return found;
    }

    private static String getBadWordReplacement(int len) {
        if(badWordReplacements.containsKey(len)) {
            return badWordReplacements.get(len);
        }

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<len; i++) sb.append("*");
        String repl = sb.toString();

        badWordReplacements.put(len, repl);
        return repl;
    }

    private static void generateHomoglyphMap() {
        homoglyphMap = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(
                new ResourceLocation("hychat:data/homoglyphs.txt")
        ).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if(!line.isEmpty() && !line.startsWith("#")) {
                    char normalChar = line.charAt(0);
                    for(int i=1; i<line.length(); i++) {
                        char homoglyph = line.charAt(i);
                        if(homoglyph == '\u00A7') {
                            System.out.println("Substituting colour code for " + normalChar + " at index " + i);
                        }
                        homoglyphMap.put(homoglyph, normalChar);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateEmptyCharSet() {
        emptyCharSet = new HashSet<>();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        for(char c=0; c<Character.MAX_VALUE; c++) {
            if(fr.getCharWidth(c) == 0) {
                emptyCharSet.add(c);
            }
        }
    }

    private static void generateBadWordSet() {
        badWordSet = new HashSet<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(
                new ResourceLocation("hychat:data/bad_words.txt")
        ).getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if(!line.isEmpty() && !line.startsWith("#")) {
                    badWordSet.add(line);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
