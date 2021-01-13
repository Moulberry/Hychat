package io.github.moulberry.hychat.core.util;

import io.github.moulberry.hychat.HyChat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.datatransfer.SunClipboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("sunapi")
public class MiscUtils {

    public static void copyToClipboard(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(str), null);
    }

    public static void copyToClipboard(BufferedImage bufferedImage) {

        /*BufferedImage premultiplied = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                bufferedImage.getType()
        );

        for(int x=0; x<bufferedImage.getWidth(); x++) {
            for(int y=0; y<bufferedImage.getHeight(); y++) {
                int argb = bufferedImage.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                r = r*a/0xFF;
                g = g*a/0xFF;
                b = b*a/0xFF;

                premultiplied.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }*/

        /*TransferableImage trans = new TransferableImage(premultiplied);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
        if(true) return;*/


        /*try {
            File temp = new File(HyChat.getInstance().getConfigDir(), "temp");
            temp.mkdirs();
            File clipboardImage = new File(temp, "clipboard.png");
            ImageIO.write(bufferedImage, "png", clipboardImage);

            if(SystemUtils.IS_OS_WINDOWS) {
                System.out.println("WINDOWS SS");
                File bin = new File(HyChat.getInstance().getConfigDir(), "bin");
                bin.mkdirs();
                File imgClipWin = new File(bin, "ImgClipWin.exe");
                if(!imgClipWin.exists()) {
                    recursiveDelete(bin);

                    InputStream is = Minecraft.getMinecraft().getResourceManager()
                            .getResource(new ResourceLocation("hychat:bin.zip")).getInputStream();
                    unzip(is, bin);
                }

                Runtime run = Runtime.getRuntime();
                Process pr = run.exec(imgClipWin.getAbsolutePath()+" "+clipboardImage.getAbsolutePath());
                pr.waitFor();
            } else if(SystemUtils.IS_OS_LINUX) {
                System.out.println("LINUX SS");
                Runtime run = Runtime.getRuntime();
                Process pr = run.exec("xclip -selection clip -t image/png "+clipboardImage.getAbsolutePath());
                pr.waitFor();
            } else {
                System.out.println("No OS Detected");
            }
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }*/

        if(SystemUtils.IS_OS_WINDOWS) {
            try {
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                byte hdrSize = 0x28;
                ByteBuffer buffer = ByteBuffer.allocate(hdrSize + width*height*4);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                //Header size
                buffer.putInt(hdrSize);
                //Width
                buffer.putInt(width);
                //Int32 biHeight;
                buffer.putInt(height);
                //Int16 biPlanes;
                buffer.put((byte)1);
                buffer.put((byte)0);
                //Int16 biBitCount;
                buffer.put((byte)32);
                buffer.put((byte)0);
                //Compression
                buffer.putInt(0);
                //Int32 biSizeImage;
                buffer.putInt(width*height*4);

                buffer.putInt(0);
                buffer.putInt(0);
                buffer.putInt(0);
                buffer.putInt(0);

                //Image data
                for(int y=0; y<height; y++) {
                    for(int x=0; x<width; x++) {
                        int argb = bufferedImage.getRGB(x, height - y - 1);
                        if(((argb >> 24) & 0xFF) == 0) {
                            buffer.putInt(0x00000000);
                        } else {
                            buffer.putInt(argb);
                        }
                    }
                }

                buffer.flip();

                byte hdrSizev5 = 0x7C;
                ByteBuffer bufferv5 = ByteBuffer.allocate(hdrSizev5 + width*height*4);
                bufferv5.order(ByteOrder.LITTLE_ENDIAN);
                //Header size
                bufferv5.putInt(hdrSizev5);
                //Width
                bufferv5.putInt(width);
                //Int32 biHeight;
                bufferv5.putInt(height);
                //Int16 biPlanes;
                bufferv5.put((byte)1);
                bufferv5.put((byte)0);
                //Int16 biBitCount;
                bufferv5.put((byte)32);
                bufferv5.put((byte)0);
                //Compression
                bufferv5.putInt(0);
                //Int32 biSizeImage;
                bufferv5.putInt(width*height*4);

                bufferv5.putInt(0);
                bufferv5.putInt(0);
                bufferv5.putInt(0);
                bufferv5.putInt(0);

                bufferv5.order(ByteOrder.BIG_ENDIAN);
                bufferv5.putInt(0xFF000000);
                bufferv5.putInt(0x00FF0000);
                bufferv5.putInt(0x0000FF00);
                bufferv5.putInt(0x000000FF);
                bufferv5.order(ByteOrder.LITTLE_ENDIAN);

                //BGRs
                bufferv5.put((byte)0x42);
                bufferv5.put((byte)0x47);
                bufferv5.put((byte)0x52);
                bufferv5.put((byte)0x73);

                for(int i=bufferv5.position(); i<hdrSizev5; i++) {
                    bufferv5.put((byte)0);
                }

                //Image data
                for(int y=0; y<height; y++) {
                    for(int x=0; x<width; x++) {
                        int argb = bufferedImage.getRGB(x, height - y - 1);

                        int a = (argb >> 24) & 0xFF;
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;

                        r = r*a/0xFF;
                        g = g*a/0xFF;
                        b = b*a/0xFF;

                        bufferv5.putInt((a << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                bufferv5.flip();

                SunClipboard clip = (SunClipboard) Toolkit.getDefaultToolkit().getSystemClipboard();

                DataTransferer dt = DataTransferer.getInstance();
                Field f = dt.getClass().getDeclaredField("CF_DIB");
                f.setAccessible(true);
                long format = f.getLong(null);

                Method openClipboard = clip.getClass().getDeclaredMethod("openClipboard", SunClipboard.class);
                openClipboard.setAccessible(true);
                openClipboard.invoke(clip, clip);

                Method publishClipboardData = clip.getClass().getDeclaredMethod("publishClipboardData",  long.class, byte[].class);
                publishClipboardData.setAccessible(true);

                byte[] arr = buffer.array();
                publishClipboardData.invoke(clip, format, arr);

                Method closeClipboard = clip.getClass().getDeclaredMethod("closeClipboard");
                closeClipboard.setAccessible(true);
                closeClipboard.invoke(clip);

                return;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        TransferableImage trans = new TransferableImage(bufferedImage);
        /*if(Keyboard.isKeyDown(Keyboard.KEY_N)) {

        }  else {
            trans = new TransferableImage(premultiplied);
        }*/
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
        } catch(Exception e2) {
            e2.printStackTrace();
        }

    }

    private static class SimpleTransferable implements Transferable {
        private final DataFlavor flavor;
        private final Object object;

        public SimpleTransferable(DataFlavor flavor, Object object) {
            this.flavor = flavor;
            this.object = object;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return object;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { flavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return this.flavor.equals(flavor);
        }
    }

    private static class TransferableImage extends SimpleTransferable {
        public TransferableImage(BufferedImage image) {
            super(DataFlavor.imageFlavor, image);
        }
    }

    private static void unzip(InputStream src, File dest) {
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(src);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                if(!ze.isDirectory()) {
                    String fileName = ze.getName();
                    File newFile = new File(dest, fileName);
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recursiveDelete(File file) {
        if(file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
            for(File child : file.listFiles()) {
                recursiveDelete(child);
            }
        }
        file.delete();
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
