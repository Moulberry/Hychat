package io.github.moulberry.hychat.mixins;

import io.github.moulberry.hychat.HyChat;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Inject(method="deleteChatLine", at=@At("RETURN"))
    public void deleteChatLine(int lineId, CallbackInfo ci) {
        HyChat.getInstance().getChatManager().deleteChatLine(lineId);
    }

    @Inject(method="setChatLine", at=@At("RETURN"))
    public void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean refresh, CallbackInfo ci) {
        HyChat.getInstance().getChatManager().setChatLine(chatComponent, chatLineId, updateCounter, refresh);
    }

}
