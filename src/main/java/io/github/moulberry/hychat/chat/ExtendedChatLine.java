package io.github.moulberry.hychat.chat;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

public class ExtendedChatLine extends ChatLine {

    private IChatComponent fullLine;

    public ExtendedChatLine(int updateCounter, IChatComponent chatComponent, IChatComponent fullLine, int lineId) {
        super(updateCounter, chatComponent, lineId);
        this.fullLine = fullLine;
    }

    public IChatComponent getFullLine() {
        return fullLine;
    }
}
