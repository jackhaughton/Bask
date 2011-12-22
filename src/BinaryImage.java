import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;


public class BinaryImage {

    private final int width, height;
    private final boolean[] image;
    
    public BinaryImage(BufferedImage im) {
        width=im.getWidth();
        height=im.getHeight();
        image=new boolean[width*height];
        
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int rgb=im.getRGB(x,y);
                double luminance=0.2126*(((rgb>>16)&0xFF)/255.0) + 0.7152*(((rgb>>8)&0xFF)/255.0) + 0.0722*((rgb&0xFF)/255.0);
                image[x+y*width]=luminance>0.5;
            }
        }
        
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public boolean get(int x, int y) { return image[x+width*y]; }
    
    public void draw(Graphics g, int x, int y) {
    	for (int xx=0; xx<width; xx++) {
    		for (int yy=0; yy<height; yy++) {
    			g.setColor(image[xx+yy*width]?Color.white:Color.black);
    			g.fillRect(x+xx, y+yy, 1,1);
    		}
    	}
    }
    public String toString() {
        StringBuilder builder=new StringBuilder();
        
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (get(x,y)) builder.append("O"); else builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
