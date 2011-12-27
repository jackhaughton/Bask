package bask;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


public class Display {

	public final Frame frame;
	public final Canvas canv=new Canvas();
	public final BufferedImage im;
	
	public Display(String title, int width, int height) {
		frame=new Frame(title);
		frame.setSize(width,height);
		frame.setMaximumSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		frame.add(canv, BorderLayout.CENTER);
		im=new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (frame.isVisible()) {
						synchronized (im) {
							Graphics g=canv.getGraphics();
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
