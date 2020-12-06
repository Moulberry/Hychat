package io.github.moulberry.hychat.util;

import com.google.common.collect.Sets;

import java.util.Set;

public class StringUtils {

    public static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

    public static String cleanColour(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }

}
