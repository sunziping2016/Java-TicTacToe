import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by sun on 5/13/16.
 *
 * Image loader.
 */
public class ImageLoader {
    private static HashMap<String,BufferedImage> cache = new HashMap<>();

    public static BufferedImage getImage(String name) {
        if (!cache.containsKey(name)) {
            try {
                BufferedImage b = ImageIO.read(ImageLoader.class.getResource("/images/" + name));
                cache.put(name, b);
                return b;
            } catch (IOException e) {
                System.out.println("Failed to getImage image resource " + name);
                return null;
            }
        }
        return cache.get(name);
    }

    public static BufferedImage getImageTransparent(String name, float alpha) {
        String hash = name + "#transparent" + alpha;
        if (!cache.containsKey(hash)) {
            BufferedImage image = getImage(name);
            BufferedImage tmpImg = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) tmpImg.getGraphics();
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2d.drawImage(image, 0, 0, null);
            cache.put(hash, tmpImg);
            return tmpImg;
        }
        return cache.get(hash);
    }
}
