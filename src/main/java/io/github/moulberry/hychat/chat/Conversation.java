package io.github.moulberry.hychat.chat;

import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class Conversation {

    public final String recipientUsername;
    private String recipientFancy;
    private List<IChatComponent> to = new ArrayList<>();
    private List<IChatComponent> from = new ArrayList<>();

    public Conversation(String recipientUsername, String recipientFancy) {
        this.recipientUsername = recipientUsername;
        this.recipientFancy = recipientFancy;
    }

    public List<IChatComponent> getTo() {
        return to;
    }

    public List<IChatComponent> getFrom() {
        return from;
    }

    public String getRecipientFancy() {
        return recipientFancy;
    }

    public void setRecipientFancy(String recipientFancy) {
        this.recipientFancy = recipientFancy;
    }

}
