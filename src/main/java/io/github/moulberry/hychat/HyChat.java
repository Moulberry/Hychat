package io.github.moulberry.hychat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.core.BackgroundBlur;
import io.github.moulberry.hychat.event.EventListener;
import io.github.moulberry.hychat.gui.GuiChatOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.Session;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = HyChat.MODID, version = HyChat.VERSION, clientSideOnly = true)
public class HyChat {
    public static final String MODID = "hychat";
    public static final String VERSION = "@VERSION@";

    private static HyChat INSTANCE;
    private ChatManager chatManager;
    private GuiChatOverlay chatOverlay;
    private File configDir;
    private File chatManagerConfig;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        configDir = new File(event.getModConfigurationDirectory(), "hychat");

        chatManagerConfig = new File(configDir, "chat.json");
        chatManager = ChatManager.loadFrom(chatManagerConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                chatManager.saveTo(chatManagerConfig);
            } catch(Exception e) {}
        }));

        chatOverlay = new GuiChatOverlay();

        MinecraftForge.EVENT_BUS.register(new EventListener());
        BackgroundBlur.registerListener();
    }

    public File getChatManagerConfig() {
        return chatManagerConfig;
    }

    public File getConfigDir() {
        return configDir;
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
