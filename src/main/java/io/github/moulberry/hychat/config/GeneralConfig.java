package io.github.moulberry.hychat.config;

import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.core.config.Config;
import io.github.moulberry.hychat.core.config.annotations.Category;
import io.github.moulberry.hychat.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.hychat.core.config.annotations.ConfigOption;

public class GeneralConfig extends Config {

    @Expose
    @Category(
            name = "Tweaks",
            desc = "Various modifications to chat messages"
    )
    public Tweaks tweaks = new Tweaks();

    public static class Tweaks {
        @Expose
        @ConfigOption(
                name = "Compact Chat",
                desc = "Stacks duplicate chat messages, removing the old duplicate",
                subcategoryId = 1
        )
        @ConfigEditorBoolean
        public boolean compactChat = true;

        @Expose
        @ConfigOption(
                name = "Consecutive Compact Chat",
                desc = "Only compacts chat messages if they are in a row",
                subcategoryId = 1
        )
        @ConfigEditorBoolean
        public boolean consecutiveCompactChat = false;

        @Expose
        @ConfigOption(
                name = "Compact Chat Count",
                desc = "Shows the number of duplicate messages removed by compact chat",
                subcategoryId = 1
        )
        @ConfigEditorBoolean
        public boolean compactChatCount = true;

        @Expose
        @ConfigOption(
                name = "Fix Centered Text",
                desc = "Fixes text which should be centered on Hypixel, but isn't because of the chat size"
        )
        @ConfigEditorBoolean
        public boolean fixCenteredText = true;

        @Expose
        @ConfigOption(
                name = "Smart Dividers",
                desc = "Makes chat dividers (eg. -------) fit within the chat box properly"
        )
        @ConfigEditorBoolean
        public boolean smartDividers = true;

        @Expose
        @ConfigOption(
                name = "Connect Dividers",
                desc = "Removes the gap between chat dividers (-------)"
        )
        @ConfigEditorBoolean
        public boolean connectedDividers = true;

        @Expose
        @ConfigOption(
                name = "Stack Empty Lines",
                desc = "Removes consecutive empty lines"
        )
        @ConfigEditorBoolean
        public boolean stackEmptyLines = true;

        @Expose
        @ConfigOption(
                name = "Filter bad words",
                desc = "Replaces bad words in messages with *s"
        )
        @ConfigEditorBoolean
        public boolean filterBadWords = true;

        @Expose
        @ConfigOption(
                name = "Replace homoglyphs",
                desc = "Replaces characters that look similar to the real ones with normal ascii characters"
        )
        @ConfigEditorBoolean
        public boolean replaceHomoglyphs = true;
    }

}
