package io.github.moulberry.hychat;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        HyChat.getInstance().getChatManager().updateTabs();
    }
}
