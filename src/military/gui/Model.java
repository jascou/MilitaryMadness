package military.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.GrayFilter;

/**
 * Represents a drawable unit model and its team variants (flipped/color-swapped) and greyscale images.
 * Image loading uses ResourceLoader; missing images are logged and a null image is tolerated.
 */
public class Model {
    private boolean doFlip = true;
    private boolean doColorSwap = true;

    public void setFlipEnabled(boolean value) { this.doFlip = value; }
    public void setColorSwapEnabled(boolean value) { this.doColorSwap = value; }
    private String name;
    private Image image;
    private Image image2;
    private Image greyImage;
    private Image greyImage2;

    public Model(String name) {
        this.name = name;
        image = null;
        loadImage();
    }

    public Image getGreyImage(boolean team) {
        return team ? greyImage : greyImage2;
    }

    public String getName() {
        return name;
    }

    public Image getImage(){
        return getImage(true);
    }
    
    public Image getImage(boolean team) {
        return team ? image : image2;
    }
    
    private void loadImage() {
        try {
            image = military.util.ResourceLoader.loadImage("Resources/" + name + ".gif");
        } catch (IOException ex) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(Model.class);
            logger.info("Image not found: Resources/" + name + ".gif");
        }
        BufferedImage temp = null;
        try {
            temp = military.util.ResourceLoader.loadImage("Resources/" + name + ".gif");
        } catch (IOException ex) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(Model.class);
            logger.info("Image not found (temp): Resources/" + name + ".gif");
        }
        
        class BlueRedSwapFilter extends RGBImageFilter {

            public int filterRGB(int x, int y, int rgb) {
                if (((rgb >> 16) & 0xff) == ((rgb >> 8) & 0xff) && ((rgb >> 16) & 0xff) == (rgb & 0xff)) {
                    return rgb;
                }
                // Fix operator precedence and make intent explicit
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb) & 0xff;
                int newG = (b) / 8; // reduce blue into green channel slightly
                int newB = (r) / 4; // reduce red into blue channel
                int out = (0xff << 24) | (r << 16) | (newG << 8) | (newB);
                return out;
            }
        }
        if (doFlip && temp != null) {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-temp.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image2 = op.filter(temp, null);
        } else if (temp != null) {
            image2 = temp;
        } else {
            image2 = image;
        }
        if (doColorSwap && image2 != null) {
            image2 = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image2.getSource(), new BlueRedSwapFilter()));
        }
        ImageFilter filter = new GrayFilter(true, 35);  
        ImageProducer producer = new FilteredImageSource(image.getSource(), filter);  
        greyImage = Toolkit.getDefaultToolkit().createImage(producer);  
        producer = new FilteredImageSource(image2.getSource(), filter); 
        greyImage2 = Toolkit.getDefaultToolkit().createImage(producer); 
    }
    
    
}
