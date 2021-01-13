package io.github.moulberry.hychat.chat;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

public class ExtendedChatLine extends ChatLine {

    private IChatComponent fullLine;
    private boolean divider = false;
    private boolean centered = false;
    private int uniqueId;

    public ExtendedChatLine(int updateCounter, IChatComponent chatComponent, IChatComponent fullLine, int lineId) {
        super(updateCounter, chatComponent, lineId);
        this.fullLine = fullLine;
    }

    public ExtendedChatLine setDivider(boolean divider) {
        this.divider = divider;
        return this;
    }

    public boolean isDivider() {
        return divider;
    }

    public boolean isCentered() {
        return centered;
    }

    public ExtendedChatLine setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    public ExtendedChatLine setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public IChatComponent getFullLine() {
        return fullLine;
    }
}
