package io.github.moulberry.hychat.mixins;

import io.github.moulberry.hychat.chat.ChatTab;
import io.github.moulberry.hychat.gui.GuiChatBox;
import io.github.moulberry.hychat.gui.GuiChatOverlay;
import io.github.moulberry.hychat.HyChat;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Inject(method="drawScreen", at=@At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        //HyChat.getInstance().getChatOverlay().renderOverlay(mouseX, mouseY, partialTicks);
    }

    @Inject(method="mouseClicked", at=@At("HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        //HyChat.getInstance().getChatOverlay().mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Redirect(method="drawScreen",
            at=@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiChat;handleComponentHover(" +
                            "Lnet/minecraft/util/IChatComponent;II)V"
            ))
    public void drawScreen_handleComponentHover(GuiChat chat, IChatComponent component, int mouseX, int mouseY) {
    }

    @Redirect(method="keyTyped",
            at=@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiChat;sendChatMessage(Ljava/lang/String;)V"
            ))
    public void keyTyped_sendChatMessage(GuiChat chat, String message) {
        if(message.startsWith("/")) {
            chat.sendChatMessage(message);
            return;
        }
        GuiChatBox focused = HyChat.getInstance().getChatManager().getFocusedChat();
        if(focused != null) {
            ChatTab tab = focused.getSelectedTab();
            if(tab != null && tab.getMessagePrefix() != null) {
                chat.sendChatMessage(tab.getMessagePrefix() + message);
            } else {
                chat.sendChatMessage(message);
            }
        }
    }

}
