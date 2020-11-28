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

    public static final String PARTY_TALK = "{RESET}{BLUE}Party {DARK_GRAY}> (.*)";
    public static final String PARTY_INVITE = "{HYPIXEL_NAME} {RESET}{YELLOW}invited {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to the party! They have {RESET}{RED}" +
            "60 {RESET}{YELLOW}seconds to accept.{RESET}";
    public static final String PARTY_OTHER_LEAVE = "{HYPIXEL_NAME} {RESET}{YELLOW}has left the party.{RESET}";
    public static final String PARTY_OTHER_JOIN = "{HYPIXEL_NAME} {RESET}{YELLOW}joined the party.{RESET}";
    public static final String PARTY_LEAVE = "{YELLOW}You left the party.{RESET}";
    public static final String PARTY_JOIN = "{YELLOW}You have joined {RESET}{HYPIXEL_NAME}'s {RESET}{YELLOW}party!{RESET}";
    public static final String PARTY_DISBANDED = "{RED}The party was disbanded because all invites expired and the party was empty{RESET}";
    public static final String PARTY_INVITE_NOT_ONLINE = "{RED}You cannot invite that player since they're not online.{RESET}";
    public static final String PARTY_HOUSING_WARP = "{YELLOW}The party leader, {HYPIXEL_NAME}{RESET}{YELLOW}, warped you to {HYPIXEL_NAME}{RESET}{YELLOW}'s house.{RESET}";


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
