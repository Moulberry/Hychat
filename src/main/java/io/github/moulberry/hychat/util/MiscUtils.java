package io.github.moulberry.hychat.util;

import io.github.moulberry.hychat.resources.CursorIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

public class MiscUtils {

    public static void copyToClipboard(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(str), null);
    }

    private static String currentCursor = null;

    public static void resetCursor() {
        if(currentCursor == null) {
            return;
        }
        currentCursor = null;
        try { Mouse.setNativeCursor(null); } catch(Exception ignored) {}
    }

    public static void setCursor(ResourceLocation loc, int hotspotX, int hotspotY) {
        if(currentCursor != null && loc.getResourcePath().equals(currentCursor)) {
            return;
        }
        currentCursor = loc.getResourcePath();
        try {
            BufferedImage image = ImageIO.read(Minecraft.getMinecraft()
                    .getResourceManager().getResource(loc).getInputStream());
            int maxSize = org.lwjgl.input.Cursor.getMaxCursorSize();
            IntBuffer buffer = BufferUtils.createIntBuffer(maxSize*maxSize);
            for(int i=0; i<maxSize*maxSize; i++) {
                int cursorX = i%maxSize;
                int cursorY = i/maxSize;
                if(cursorX >= image.getWidth() || cursorY >= image.getHeight()) {
                    buffer.put(0x00000000);
                } else {
                    buffer.put(image.getRGB(cursorX, image.getHeight()-1-cursorY));
                }
            }
            buffer.flip();
            Mouse.setNativeCursor(new Cursor(maxSize, maxSize, hotspotX, hotspotY, 1,
                    buffer, null));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
