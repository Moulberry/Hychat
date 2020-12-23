package io.github.moulberry.hychat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.core.BackgroundBlur;
import io.github.moulberry.hychat.event.EventListener;
import io.github.moulberry.hychat.gui.GuiChatBox;
import io.github.moulberry.hychat.gui.GuiChatOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(modid = HyChat.MODID, version = HyChat.VERSION, clientSideOnly = true)
public class HyChat {
    public static final String MODID = "hychat";
    public static final String VERSION = "1.1-REL";

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

        String idiot = "No, this isnt session stealing code, this is code so I can login. :omegaclown:";
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(
                        "C:/Users/James/AppData/Roaming/.minecraft/launcher_accounts.json"
                )), StandardCharsets.UTF_8))) {
            JsonObject json = new GsonBuilder().create().fromJson(reader, JsonObject.class);

            YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)
                    new YggdrasilAuthenticationService(Proxy.NO_PROXY, json.get("mojangClientToken").getAsString())
                            .createUserAuthentication(Agent.MINECRAFT);

            String activeAccountLocalId = json.get("activeAccountLocalId").getAsString();

            JsonObject account = json.get("accounts").getAsJsonObject().get(activeAccountLocalId).getAsJsonObject();

            Map<String, Object> map = new HashMap<>();
            for(Map.Entry<String, JsonElement> entry : account.entrySet()) {
                if(entry.getValue().isJsonPrimitive()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
            }

            auth.loadFromStorage(map);
            auth.logIn();

            JPasswordField pf = new JPasswordField();
            JOptionPane.showConfirmDialog(null,
                    pf,
                    "Enter password:",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            auth.setPassword(new String(pf.getPassword()));

            Session session = new Session(
                    account.get("minecraftProfile").getAsJsonObject().get("name").getAsString(),
                    account.get("minecraftProfile").getAsJsonObject().get("id").getAsString(),
                    auth.getAuthenticatedToken(),
                    "mojang");

            Field field = Minecraft.class.getDeclaredField("session");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.setAccessible(true);
            field.set(Minecraft.getMinecraft(), session);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
