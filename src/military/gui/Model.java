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
 *
 * @author Nate
 */
public class Model {
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
        InputStream inStream = null;
        try {
//            InputStream inStream = this.getClass().getClassLoader()
//                    .getResourceAsStream(name + ".gif");
            inStream = new FileInputStream("Resources//" + name + ".gif");
            image = ImageIO.read(inStream);
            inStream.close();
        } catch (Exception ex) {
            System.out.println("no Image");
        }
        
        BufferedImage temp = null;
        inStream = null;
        try {
//            InputStream inStream = this.getClass().getClassLoader()
//                    .getResourceAsStream(name + ".gif");
            inStream = new FileInputStream("Resources//" + name + ".gif");
            temp = ImageIO.read(inStream);
            inStream.close();
        } catch (Exception ex) {
            System.out.println("no Image");
        }
        
        class BlueRedSwapFilter extends RGBImageFilter {

            public int filterRGB(int x, int y, int rgb) {
                if (((rgb >> 16) & 0xff) == ((rgb >> 8) & 0xff) && ((rgb >> 16) & 0xff) == ((rgb) & 0xff)) {
                    return rgb;
                }
                return ((((rgb & 0xff00 >> 8) / 8) << 8) | (((rgb & 0xff0000) >> 16) / 4) | ((rgb & 0xff) << 16) | 0xff000000);
            }
        }
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-temp.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image2 = op.filter(temp, null);
        image2 = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image2.getSource(), new BlueRedSwapFilter()));
        ImageFilter filter = new GrayFilter(true, 35);  
        ImageProducer producer = new FilteredImageSource(image.getSource(), filter);  
        greyImage = Toolkit.getDefaultToolkit().createImage(producer);  
        producer = new FilteredImageSource(image2.getSource(), filter); 
        greyImage2 = Toolkit.getDefaultToolkit().createImage(producer); 
    }
    
    
}
