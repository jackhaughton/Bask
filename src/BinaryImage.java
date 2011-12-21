import java.awt.Image;
import java.awt.image.PixelGrabber;


public class BinaryImage {

	private final int width, height;
	private final boolean[] image;
	
	public BinaryImage(Image im) {
		width=im.getWidth(null);
		height=im.getHeight(null);
		image=new boolean[width*height];
		int[] pixels=new int[width*height];
		PixelGrabber pg=new PixelGrabber(im, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				double luminance=0.2126*((pixels[x+y*width]>>16)/255.0) + 0.7152*((pixels[x+y*width]>>8)/255.0) + 0.0722*(pixels[x+y*width]/255.0);
				image[x+y*width]=luminance>0.5;
			}
		}
		
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public boolean get(int x, int y) { return image[x+width*y]; }
}
