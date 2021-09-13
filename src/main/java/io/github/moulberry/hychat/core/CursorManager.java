package io.github.moulberry.hychat.core;

import io.github.moulberry.hychat.core.util.MiscUtils;
import net.minecraft.util.ResourceLocation;

public class CursorManager {

    private static ResourceLocation activeCursor;
    private static int activeHotspotX;
    private static int activeHotspotY;

    public static void setCursor(ResourceLocation cursor, int hotspotX, int hotspotY) {
        activeCursor = cursor;
        activeHotspotX = hotspotX;
        activeHotspotY = hotspotY;
    }

    public static void reset() {
        activeCursor = null;
    }

    public static void updateCursor() {
        if(activeCursor == null) {
            MiscUtils.resetCursor();
        } else {
            MiscUtils.setCursor(activeCursor, activeHotspotX, activeHotspotY);
        }
    }

}
