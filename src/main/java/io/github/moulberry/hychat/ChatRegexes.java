package io.github.moulberry.hychat;

import net.minecraft.util.EnumChatFormatting;
import org.omg.CORBA.Any;

public class ChatRegexes {

    public static final String ANY_COLOUR_REGEX = "\u00A7[a-zA-Z0-9]";
    public static final String ANY_COLOUR_OPT_REGEX = "(?:"+ANY_COLOUR_REGEX+")?";
    public static final String MC_NAME_REGEX = "[a-zA-Z0-9_]+";
    public static final String HYPIXEL_RANK_PREFIX_REGEX = ANY_COLOUR_REGEX+"*\\[(?:VIP|VIP\u00A7r\u00A76\\+|MVP|MVP\u00A7r"+ANY_COLOUR_REGEX+"\\+|MVP\u00A7r"+ANY_COLOUR_REGEX+"\\+\\+)"
            +"(?:\u00A7r)?"+ANY_COLOUR_OPT_REGEX+"\\]";
    public static final String HYPIXEL_RANKED_NAME_REGEX =  HYPIXEL_RANK_PREFIX_REGEX+" "+MC_NAME_REGEX;
    public static final String HYPIXEL_NAME_REGEX =  "(?:"+HYPIXEL_RANK_PREFIX_REGEX+" "+MC_NAME_REGEX+"|\u00A77"+MC_NAME_REGEX+")";

    public static String substitute(String str) {
        str = str.replace("{ANY_COLOUR}", ANY_COLOUR_REGEX);
        str = str.replace("{ANY_COLOUR_OPT}", ANY_COLOUR_OPT_REGEX);
        str = str.replace("{MC_NAME}", MC_NAME_REGEX);
        str = str.replace("{HYPIXEL_RANK_PREFIX}", HYPIXEL_RANK_PREFIX_REGEX);
        str = str.replace("{HYPIXEL_RANKED_NAME}", HYPIXEL_RANKED_NAME_REGEX);
        str = str.replace("{HYPIXEL_NAME}", HYPIXEL_NAME_REGEX);
        for(EnumChatFormatting formatting : EnumChatFormatting.values()) {
            str = str.replace("{"+formatting.name().toUpperCase()+"}", formatting.toString());
        }
        System.out.println(str);
        return str;
    }

}
