import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Bask {

    public static void main(String[] args) {
        try {
            File f=new File(args[0]);
            BufferedImage bi=ImageIO.read(f);
            BinaryImage binIm=new BinaryImage(bi);
            System.out.println("Image:");
            System.out.println(binIm.toString());
            LocatorFinder.findLocators(binIm);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

}
