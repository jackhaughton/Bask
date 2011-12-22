import java.io.File;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Bask {

    public static void main(String[] args) {
        try {
            File f=new File(args[0]);
            BufferedImage bi=ImageIO.read(f);
            Display display=new Display("Bask",bi.getWidth()*2+15, bi.getHeight()+20);
            display.frame.addWindowListener(new WindowAdapter() {
            	public void windowClosing(WindowEvent e) {
            		System.exit(0);
            	}
			});
            BinaryImage binIm=new BinaryImage(bi);
            
            LocatorFinder lf=new LocatorFinder();
            lf.findLocators(binIm);
            
            synchronized(display.im) {
            	Graphics g=display.im.getGraphics();
            	g.drawImage(bi,5,5,null);
            	binIm.draw(g, bi.getWidth()+10, 5);
            	g.setColor(Color.BLUE);
            	for (Point p: lf.horizCentres) g.fillRect(p.x+5, p.y+5, 1, 1);
            	g.setColor(Color.MAGENTA);
            	for (Point p: lf.vertCentres) g.fillRect(p.x+5, p.y+5, 1, 1);
            	g.setColor(Color.RED);
            	for (Point p: lf.commonCentres) g.fillOval(p.x+5-3, p.y+5-3, 7, 7);
            }
            display.setVisible(true);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

}
