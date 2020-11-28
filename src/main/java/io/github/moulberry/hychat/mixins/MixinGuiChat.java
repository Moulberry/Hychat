package io.github.moulberry.hychat.mixins;

import io.github.moulberry.hychat.HookGuiChat;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Inject(method="drawScreen", at=@At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        HookGuiChat.renderChatBar(mouseX, mouseY, partialTicks);
    }

    @Inject(method="mouseClicked", at=@At("HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        HookGuiChat.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
