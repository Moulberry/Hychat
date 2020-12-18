package io.github.moulberry.hychat.config.chatbox;

import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.core.config.Config;
import io.github.moulberry.hychat.core.config.annotations.Category;
import io.github.moulberry.hychat.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.hychat.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.hychat.core.config.annotations.ConfigOption;

public class ChatboxConfig extends Config {

    @Expose
    @Category(
            name = "Alignment",
            desc = "Change the alignment of text within the chat box"
    )
    public Alignment alignment = new Alignment();

    @Expose
    @Category(
            name = "Appearance",
            desc = "Change the colour of various elements"
    )
    public Appearance appearance = new Appearance();

    @Expose
    @Category(
            name = "Tweaks",
            desc = "Various modifications to chat messages"
    )
    public Tweaks tweaks = new Tweaks();

    public static class Alignment {
        @Expose
        @ConfigOption(
                name = "Align Right",
                desc = "Chat messages are aligned to the right side of the chat box"
        )
        @ConfigEditorBoolean
        public boolean rightAligned = false;

        @Expose
        @ConfigOption(
                name = "Align Top",
                desc = "Newer chat messages start from the top of the chat box (instead of the bottom)"
        )
        @ConfigEditorBoolean
        public boolean topAligned = false;
    }

    public static class Appearance {
        @Expose
        @ConfigOption(
                name = "Text Shadow",
                desc = "Chat Box: Change the type of shadow effect applied to text"
        )
        @ConfigEditorDropdown(
                values = { "No Shadow", "Shadow", "Full Shadow" }
        )
        public int textShadow = 1;

        @Expose
        @ConfigOption(
                name = "Tab Text Shadow",
                desc = "Tab: Change the type of shadow effect applied to text"
        )
        @ConfigEditorDropdown(
                values = { "No Shadow", "Shadow", "Full Shadow" }
        )
        public int tabTextShadow = 0;
    }

    public static class Tweaks {
        @Expose
        @ConfigOption(
                name = "Compact Chat",
                desc = "Stacks duplicate chat messages, removing the old duplicate"
        )
        @ConfigEditorBoolean
        public boolean compactChat = false;

        @Expose
        @ConfigOption(
                name = "Compact Chat Count",
                desc = "Shows the number of duplicate messages removed by compact chat"
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
        public boolean connectedDividers = false;
    }

}
