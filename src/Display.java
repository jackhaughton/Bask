import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


public class Display {

	private final Frame frame;
	public final BufferedImage im;
	
	public Display(String title, int width, int height) {
		frame=new Frame(title);
		frame.setSize(width,height);
		frame.setMaximumSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		im=new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (frame.isVisible()) {
						synchronized (im) {
							Graphics g=frame.getGraphics();
							g.drawImage(im,0,0,null);
						}
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
	
	public void setVisible(boolean vis) {
		frame.setVisible(vis);
	}
}