package io.github.moulberry.hychat.mixins;

import io.github.moulberry.hychat.ChatManager;
import io.github.moulberry.hychat.HyChat;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Redirect(method="drawChat",
            at=@At(
                    value="FIELD",
                    target="Lnet/minecraft/client/gui/GuiNewChat;field_146253_i:Ljava/util/List;",
                    opcode = Opcodes.GETFIELD
            ))
    public List modifyChatLines_drawChat(GuiNewChat chat) {
        return HyChat.getInstance().getChatManager().getSelectedTab().getChatLines();
    }

    @Redirect(method="getChatComponent",
            at=@At(
                    value="FIELD",
                    target="Lnet/minecraft/client/gui/GuiNewChat;field_146253_i:Ljava/util/List;",
                    opcode = Opcodes.GETFIELD
            ))
    public List modifyChatLines_getChatComponent(GuiNewChat chat) {
        return HyChat.getInstance().getChatManager().getSelectedTab().getChatLines();
    }

    @Redirect(method="scroll",
            at=@At(
                    value="FIELD",
                    target="Lnet/minecraft/client/gui/GuiNewChat;field_146253_i:Ljava/util/List;",
                    opcode = Opcodes.GETFIELD
            ))
    public List modifyChatLines_scroll(GuiNewChat chat) {
        return HyChat.getInstance().getChatManager().getSelectedTab().getChatLines();
    }

}
