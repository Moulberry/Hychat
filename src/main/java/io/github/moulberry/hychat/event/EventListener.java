package io.github.moulberry.hychat.event;

import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.core.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class EventListener {

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        HyChat.getInstance().getChatManager().saveTo(HyChat.getInstance().getChatManagerConfig());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderChat(RenderGameOverlayEvent.Chat event) {
        final ScaledResolution scaledresolution = event.resolution;
        final int scaledWidth = scaledresolution.getScaledWidth();
        final int scaledHeight = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
        int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

        event.setCanceled(true);
        HyChat.getInstance().getChatManager().renderChatBoxes(mouseX, mouseY, event.partialTicks);
    }

    @SubscribeEvent
    public void onRenderGuiChat(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            final int scaledWidth = scaledresolution.getScaledWidth();
            final int scaledHeight = scaledresolution.getScaledHeight();
            int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
            int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

            HyChat.getInstance().getChatManager().renderChatBoxesOverlay(mouseX, mouseY, event.renderPartialTicks);
        } else {
            MiscUtils.resetCursor();
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            if(HyChat.getInstance().getChatManager().keyboardInputChatBoxes()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            final int scaledWidth = scaledresolution.getScaledWidth();
            final int scaledHeight = scaledresolution.getScaledHeight();
            int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
            int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

            HyChat.getInstance().getChatManager().mouseInputChatBoxes(mouseX, mouseY);
            event.setCanceled(true);
        }
    }
}
