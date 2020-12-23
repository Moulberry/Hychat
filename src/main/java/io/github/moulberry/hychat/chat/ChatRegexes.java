package io.github.moulberry.hychat.chat;

import net.minecraft.util.EnumChatFormatting;
import org.omg.CORBA.Any;

public class ChatRegexes {

    public static final String ANY_COLOUR_REGEX = "(?:\u00A7[a-zA-Z0-9])";
    public static final String ANY_COLOUR_OPT_REGEX = "(?:"+ANY_COLOUR_REGEX+")?";
    public static final String MC_NAME_REGEX = "[a-zA-Z0-9_]+";
    //§dFrom §r§c[§r§fYOUTUBE§r§c] Nullzee§r§7: §r§7moulberry testing new auto-reconnect macro PogU§r
    public static final String HYPIXEL_RANK_PREFIX_REGEX = ANY_COLOUR_REGEX+"*\\[(?:VIP|VIP\u00A7r\u00A76\\+|MVP|MVP(?:\u00A7r)?"+ANY_COLOUR_REGEX+"\\+|MVP(?:\u00A7r)?"+ANY_COLOUR_REGEX+"\\+\\+)"
            +"(?:\u00A7r)?"+ANY_COLOUR_OPT_REGEX+"\\]";
    public static final String HYPIXEL_RANKED_NAME_REGEX =  HYPIXEL_RANK_PREFIX_REGEX+" "+MC_NAME_REGEX;
    public static final String HYPIXEL_NAME_REGEX = "(?:"+HYPIXEL_RANK_PREFIX_REGEX+" "+MC_NAME_REGEX+"|\u00A77"+MC_NAME_REGEX+")";

    public static final String PARTY_TALK = "{RESET}{BLUE}Party {DARK_GRAY}> (.*)";
    public static final String PARTY_TALK_HYTILS = "{RESET}{BLUE}P {DARK_GRAY}> (.*)";
    public static final String PARTY_TALK_NO_PARTY = "{RED}You are not in a party right now\\.{RESET}";
    public static final String PARTY_TALK_MUTED = "{RED}This party is currently muted\\.{RESET}";
    public static final String PARTY_INVITE = "{HYPIXEL_NAME} {RESET}{YELLOW}invited {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to the party! They have {RESET}{RED}" +
            "60 {RESET}{YELLOW}seconds to accept\\.{RESET}";
    public static final String PARTY_OTHER_LEAVE = "{HYPIXEL_NAME} {RESET}{YELLOW}has left the party\\.{RESET}";
    public static final String PARTY_OTHER_JOIN = "{HYPIXEL_NAME} {RESET}{YELLOW}joined the party\\.{RESET}";
    public static final String PARTY_LEAVE = "{YELLOW}You left the party\\.{RESET}";
    public static final String PARTY_JOIN = "{YELLOW}You have joined {RESET}{HYPIXEL_NAME}'s {RESET}{YELLOW}party!{RESET}";
    public static final String PARTY_DISBANDED = "{RED}The party was disbanded because all invites expired and the party was empty{RESET}";
    public static final String PARTY_INVITE_NOT_ONLINE = "{RED}You cannot invite that player since they're not online\\.{RESET}"; //works
    public static final String PARTY_HOUSING_WARP = "{YELLOW}The party leader, {RESET}{HYPIXEL_NAME}{RESET}{YELLOW}, warped you to {RESET}{HYPIXEL_NAME}{RESET}{YELLOW}'s house\\.{RESET}";//no work :((
    public static final String PARTY_SB_WARP = "{YELLOW}SkyBlock Party Warp {RESET}{GRAY}\\([0-9]+ players?\\){RESET}";
    public static final String PARTY_WARPED = "{GREEN}. {RESET}{HYPIXEL_NAME}{RESET}{WHITE} {RESET}{GREEN}warped to your server{RESET}";//works
    public static final String PARTY_SUMMONED = "{YELLOW}You summoned {RESET}{HYPIXEL_NAME}{RESET}{WHITE} {RESET}{YELLOW}to your server\\.{RESET}"; //works
    public static final String PARTY_WARP_HOUSING = "{YELLOW}The party leader, {RESET}{HYPIXEL_NAME}{RESET}{YELLOW}, warped you to their house\\.{RESET}"; //works
    public static final String PARTY_PRIVATE_ON = "{HYPIXEL_NAME} {RESET}{GREEN}enabled Private Game{RESET}";//works
    public static final String PARTY_PRIVATE_OFF = "{HYPIXEL_NAME} {RESET}{RED}disabled Private Game{RESET}";//works
    public static final String PARTY_MUTE_ON = "{RED}The party is now muted\\. {RESET}";//works
    public static final String PARTY_MUTE_OFF = "{GREEN}The party is no longer muted\\.{RESET}";//works
    public static final String PARTY_NOOFFLINE = "{RED}There are no offline players to remove\\.{RESET}";//works
    public static final String PARTY_KICK = "{HYPIXEL_NAME} {RESET}{YELLOW}has been removed from the party\\.{RESET}";//works
    public static final String PARTY_TRANSFER = "{YELLOW}The party was transferred to {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}by {RESET}{HYPIXEL_NAME}{RESET}";//works
    public static final String PARTY_PROMOTE = "{HYPIXEL_NAME}{RESET}{YELLOW} has promoted {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to Party Leader{RESET}";//works
    public static final String PARTY_PROMOTE_MODERATOR = "{HYPIXEL_NAME}{RESET}{YELLOW} has promoted {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to Party Moderator{RESET}";
    public static final String PARTY_DEMOTE_MODERATOR = "{HYPIXEL_NAME} {RESET}{YELLOW}is now a Party Moderator{RESET}";//works
    public static final String PARTY_DEMOTE_MEMBER = "{HYPIXEL_NAME}{RESET}{YELLOW} has demoted {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}to Party Member{RESET}";//works
    public static final String PARTY_DEMOTE_SELF = "{RED}You can't demote yourself!{RESET}";//works
    public static final String PARTY_LIST_NUM = "{GOLD}Party Members \\([0-9]+\\){RESET}";//works
    public static final String PARTY_LIST_LEADER = "{YELLOW}Party Leader: {RESET}{HYPIXEL_NAME} ?{RESET}{ANY_COLOUR}.{RESET}";//works
    public static final String PARTY_LIST_MEMBERS = "{YELLOW}Party Members: {RESET}(?:{HYPIXEL_NAME}{RESET}{ANY_COLOUR} . {RESET})+";//works
    public static final String PARTY_LIST_MODS = "{YELLOW}Party Moderators: {RESET}(?:{HYPIXEL_NAME}{RESET}{ANY_COLOUR} . {RESET})+";//works
    public static final String PARTY_INVITE_EXPIRE = "{YELLOW}The party invite to {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}has expired{RESET}";//works
    public static final String PARTY_ALLINVITE_OFF = "{HYPIXEL_NAME} {RESET}{RED}disabled All Invite{RESET}";//works
    public static final String PARTY_ALLINVITE_ON = "{HYPIXEL_NAME} {RESET}{GREEN}enabled All Invite{RESET}";//works
    public static final String PARTY_INVITES_OFF = "{RED}You cannot invite that player\\.{RESET}";//works
    public static final String PARTY_INVITE_NOPERMS = "{RED}You are not allowed to invite players\\.{RESET}";//works
    public static final String PARTY_DC_LEADER = "{YELLOW}The party leader, {RESET}{HYPIXEL_NAME} {RESET}{YELLOW}has disconnected, they have {RESET}{RED}5 {RESET}{YELLOW}minutes to rejoin before the party is disbanded\\.{RESET}";
    public static final String PARTY_DC_OTHER = "{HYPIXEL_NAME} {RESET}{YELLOW}has disconnected, they have {RESET}{RED}5 {RESET}{YELLOW}minutes to rejoin before they are removed from the party.{RESET}";

    //§dDungeon Finder §f> §r§r§eYour dungeon group is full!§r§6 Click here to warp to the dungeon!§r

    public static final String[] PARTY = {
            PARTY_TALK, PARTY_INVITE, PARTY_OTHER_LEAVE, PARTY_OTHER_JOIN, PARTY_LEAVE, PARTY_JOIN, PARTY_DISBANDED,
            PARTY_INVITE_NOT_ONLINE, PARTY_HOUSING_WARP, PARTY_SB_WARP, PARTY_WARPED, PARTY_SUMMONED, PARTY_WARP_HOUSING,
            PARTY_PRIVATE_ON, PARTY_PRIVATE_OFF, PARTY_MUTE_ON, PARTY_MUTE_OFF, PARTY_NOOFFLINE, PARTY_KICK,
            PARTY_TRANSFER, PARTY_PROMOTE, PARTY_PROMOTE_MODERATOR, PARTY_DEMOTE_MODERATOR, PARTY_DEMOTE_MEMBER,
            PARTY_DEMOTE_SELF, PARTY_LIST_NUM, PARTY_LIST_LEADER, PARTY_LIST_MEMBERS, PARTY_LIST_MODS,
            PARTY_INVITE_EXPIRE, PARTY_ALLINVITE_OFF, PARTY_ALLINVITE_ON, PARTY_INVITES_OFF, PARTY_INVITE_NOPERMS,
            PARTY_DC_LEADER, PARTY_DC_OTHER, PARTY_TALK_NO_PARTY, PARTY_TALK_MUTED, PARTY_TALK_HYTILS
    };

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
