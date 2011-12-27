package bask;

import java.io.File;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import bask.bsplinelocatorfinder.BSplineLocatorFinder;
import bask.bsplinelocatorfinder.BSplineLocatorFinder.PotentialLocator;
import bask.simplelocatorfinder.LocatorFinder;


import java.io.IOException;

public class Bask {

    public static void main(String[] args) {
        try {
            File f=new File(args[0]);
            BufferedImage bi=ImageIO.read(f);
            Display display=new Display("Bask",bi.getWidth()+10, bi.getHeight()+50);
            display.frame.addWindowListener(new WindowAdapter() {
            	public void windowClosing(WindowEvent e) {
            		System.exit(0);
            	}
			});
            LuminanceImage binIm=new LuminanceImage(bi);
            for (int x=0; x<bi.getWidth(); x++) {
            	System.out.printf("%.4f,",binIm.get(x, 376));
            }
            System.out.println();
            
            /*LocatorFinder lf=new LocatorFinder();
            lf.findLocators(binIm);*/
            
            synchronized(display.im) {
            	Graphics g=display.im.getGraphics();
            	binIm.draw(g, 5, 5);
            	for (int y=0; y<binIm.getHeight(); y++) {
                	PotentialLocator[] locators=BSplineLocatorFinder.findLocators(binIm, y, true);
                	for (PotentialLocator l: locators) {
                		int centreX=(int)((l.start+l.end)/2);
                		g.setColor(Color.GREEN);
                		g.fillRect(centreX+5, y+5, 1, 1);
                	}
                }
            	for (int x=0; x<binIm.getWidth(); x++) {
                	PotentialLocator[] locators=BSplineLocatorFinder.findLocators(binIm, x, false);
                	for (PotentialLocator l: locators) {
                		int centreY=(int)((l.start+l.end)/2);
                		g.setColor(Color.BLUE);
                		g.fillRect(x+5, centreY+5, 1, 1);
                	}
                }
            	/*for (int x=0; x<lf.width; x++) {
            		for (int y=0; y<lf.height; y++) {
            			int flags=lf.flags[x+y*lf.width];
            			if ((flags&LocatorFinder.FLAG_TAGGED)!=0) {
            				g.setColor(Color.RED);
            				g.fillOval(x+5-3, y+5-3, 7, 7);
            			}
            			else if ((flags&(LocatorFinder.FLAG_HORIZ|LocatorFinder.FLAG_VERT))==(LocatorFinder.FLAG_HORIZ|LocatorFinder.FLAG_VERT)) {
            				g.setColor(Color.CYAN);
            				g.fillRect(x+5, y+5, 1, 1);
            			}
            			else if ((flags&LocatorFinder.FLAG_HORIZ)!=0) {
            				g.setColor(Color.BLUE);
            				g.fillRect(x+5, y+5, 1, 1);
            			}
            			else if ((flags&LocatorFinder.FLAG_VERT)!=0) {
            				g.setColor(Color.GREEN);
            				g.fillRect(x+5, y+5, 1, 1);
            			}
            		}
            	}*/
            }
            display.setVisible(true);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

}
