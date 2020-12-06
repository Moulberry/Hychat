package io.github.moulberry.hychat.mixins;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Invoker("handleComponentClick")
    boolean invokeHandleComponentClick(IChatComponent chatComponent);

    @Invoker("handleComponentHover")
    void invokeHandleComponentHover(IChatComponent chatComponent, int mouseX, int mouseY);

}
