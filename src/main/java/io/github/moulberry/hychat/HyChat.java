package io.github.moulberry.hychat;

import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.event.EventListener;
import io.github.moulberry.hychat.gui.GuiChatOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HyChat.MODID, version = HyChat.VERSION, clientSideOnly = true)
public class HyChat {
    public static final String MODID = "hychat";
    public static final String VERSION = "1.1-REL";

    private static HyChat INSTANCE;
    private ChatManager chatManager;
    private GuiChatOverlay chatOverlay;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        chatManager = new ChatManager();
        chatOverlay = new GuiChatOverlay();
        INSTANCE = this;

        MinecraftForge.EVENT_BUS.register(new EventListener());
    }

    public static HyChat getInstance() {
        return INSTANCE;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public GuiChatOverlay getChatOverlay() {
        return chatOverlay;
    }
}
