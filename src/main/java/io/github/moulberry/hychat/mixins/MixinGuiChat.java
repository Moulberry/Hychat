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
        GuiChatBox focused = HyChat.getInstance().getChatManager().getFocusedChat();
        if(focused != null) {
            chat.sendChatMessage(focused.sendChatMessage(message));
            return;
        }
        chat.sendChatMessage(message);
    }

}
