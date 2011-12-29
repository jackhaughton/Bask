package bask;

import java.io.File;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import bask.bsplinelocatorfinder.BSplineLocatorFinder;
import bask.bsplinelocatorfinder.BSplineLocatorFinder.PotentialLocator;
import bask.bsplinelocatorfinder.BSplineLocatorFinder.ScanDirection;
import bask.simplelocatorfinder.LocatorFinder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Bask {

	public static void main(String[] args) {
		try {
			File f=new File(args[0]);
			BufferedImage bi=ImageIO.read(f);
			final Display display=new Display("Bask",bi.getWidth()+10, bi.getHeight()+50);
			display.frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			display.canv.addMouseMotionListener(new MouseAdapter() {
				public void mouseMoved(MouseEvent e) {
					display.frame.setTitle("Bask "+(e.getX()-5)+","+(e.getY()-5));
				}
			});

			LuminanceImage binIm=new LuminanceImage(bi);
			HashMap<Point,ArrayList<PotentialLocator>> locatorCentres=new HashMap<Point, ArrayList<PotentialLocator>>();
			boolean[][] markers=new boolean[bi.getWidth()][bi.getHeight()];
			/*for (int x=0; x<bi.getWidth(); x++) {
            	System.out.printf("%.4f,",binIm.get(x, 376));
            }
            System.out.println();*/

			/*LocatorFinder lf=new LocatorFinder();
            lf.findLocators(binIm);*/

			synchronized(display.im) {
				Graphics g=display.im.getGraphics();
				binIm.draw(g, 5, 5);
				PotentialLocator[] locatorsH=BSplineLocatorFinder.findLocators(binIm, ScanDirection.HORIZONTAL);
				PotentialLocator[] locatorsV=BSplineLocatorFinder.findLocators(binIm, ScanDirection.VERTICAL);
				PotentialLocator[] locatorsD1=BSplineLocatorFinder.findLocators(binIm, ScanDirection.DIAGONAL1);
				PotentialLocator[] locatorsD2=BSplineLocatorFinder.findLocators(binIm, ScanDirection.DIAGONAL2);
				for (PotentialLocator l: locatorsH) {
					g.setColor(Color.GREEN);
					g.drawLine(l.startX+5, l.startY+5, l.endX+5, l.endY+5);
				}
				for (PotentialLocator l: locatorsV) {
					g.setColor(Color.BLUE);
					g.drawLine(l.startX+5, l.startY+5, l.endX+5, l.endY+5);
				}
				for (PotentialLocator l: locatorsD1) {
					g.setColor(Color.CYAN);
					g.drawLine(l.startX+5, l.startY+5, l.endX+5, l.endY+5);
				}
				for (PotentialLocator l: locatorsD2) {
					g.setColor(Color.MAGENTA);
					g.drawLine(l.startX+5, l.startY+5, l.endX+5, l.endY+5);
				}
				PotentialLocator[][] allLocators=new PotentialLocator[][] { locatorsH, locatorsV, locatorsD1, locatorsD2 };
				for (int i=0; i<3; i++) {
					for (int a=0; a<allLocators[i].length; a++) {
						PotentialLocator locator=allLocators[i][a];
						Point centre=locator.getCentre();
						ArrayList<PotentialLocator> locators=locatorCentres.get(centre);
						if (locators==null) {
							locators=new ArrayList<PotentialLocator>();
							locatorCentres.put(centre, locators);
						}
						locators.add(locator);
						/*PotentialLocator locator1=allLocators[i][a];
            			double locator1LengthSquared=(locator1.endX-locator1.startX)*(locator1.endX-locator1.startX)+(locator1.endY-locator1.startY)*(locator1.endY-locator1.startY);
            			for (int j=i+1; j<4; j++) {
            				for (int b=0; b<allLocators[j].length; b++) {
            					PotentialLocator locator2=allLocators[j][b];
            					double locator2LengthSquared=(locator2.endX-locator2.startX)*(locator2.endX-locator2.startX)+(locator2.endY-locator2.startY)*(locator2.endY-locator2.startY);
            					double lengthRatio=locator1LengthSquared/locator2LengthSquared;
            					if (lengthRatio<0.5 || lengthRatio>2) continue;
            					double t2=(locator2.startY-locator1.startY)*(locator2.endX-locator1.startX)+(locator1.startX-locator2.startX)*(locator1.endY-locator1.startY);
            					t2/=(locator2.endX-locator2.startX)*(locator1.endY-locator1.startY)+(locator2.startY-locator2.endY)*(locator1.endX-locator1.startX);
            					double t1=t2*(locator2.endX-locator2.startX)+locator2.startX-locator1.startX;
            					t1/=locator1.endX-locator1.startX;
            					if (t1>0.25 && t1<0.75 && t2>0.25 && t2<0.75) {

            						int centreX=(int)((locator1.startX+locator1.endX)/2.0);
            						int centreY=(int)((locator1.startY+locator1.endY)/2.0);
            						pixelVotes[centreX][centreY]++;
            						if (pixelVotes[centreX][centreY]>maxPixelVote) maxPixelVote=pixelVotes[centreX][centreY];
            					}
            				}
            			}*/
					}
				}
				//Cluster the votes. All adjoining ones get added to the centre of the cluster.
				/*for (int x=0; x<binIm.getWidth(); x++) {
            		for (int y=0; y<binIm.getHeight(); y++) {
            			if (pixelVotes[x][y]>0) {
            				cluster(pixelVotes,x,y);
            			}
            		}
            	}
            	double averageVotes=0;
            	int voteCount=0;
            	for (int x=0; x<binIm.getWidth(); x++) {
            		for (int y=0; y<binIm.getHeight(); y++) {
            			if (pixelVotes[x][y]>0) {
            				System.out.println("("+x+","+y+") "+pixelVotes[x][y]);
            				g.setColor(Color.RED);
            				g.drawString(Integer.toString(pixelVotes[x][y]), x+20, y+20);
            				averageVotes+=pixelVotes[x][y];
            				voteCount++;
            			}
            		}
            	}
            	averageVotes/=voteCount;*/
				Point p=new Point();
				HashMap<ScanDirection, Integer> directionCounts=new HashMap<ScanDirection, Integer>();
				for (int x=0; x<binIm.getWidth(); x++) {
					for (int y=0; y<binIm.getHeight(); y++) {
						for (int xx=-1; xx<=1; xx++) {
							for (int yy=-1; yy<=1; yy++) {
								p.x=x+xx; p.y=y+yy;
								ArrayList<PotentialLocator> locators=locatorCentres.get(p);
								if (locators==null) continue;
								for (PotentialLocator locator: locators) {
									Integer i=directionCounts.get(locator.direction);
									if (i==null) directionCounts.put(locator.direction, 1);
									else directionCounts.put(locator.direction, i+1);
								}		
							}
						}
						if (directionCounts.size()>2) markers[x][y]=true;

						directionCounts.clear();
					}
				}
				//Cluster the votes. All adjoining ones get added to the centre of the cluster.
				for (int x=0; x<binIm.getWidth(); x++) {
            		for (int y=0; y<binIm.getHeight(); y++) {
            			if (markers[x][y]) {
            				cluster(markers,x,y);
            			}
            		}
            	}
				for (int x=0; x<binIm.getWidth(); x++) {
					for (int y=0; y<binIm.getHeight(); y++) {
						if (markers[x][y]) {
							g.setColor(Color.RED);
							g.fillOval(x+5-2, y+5-2, 5, 5);
						}
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

	static void cluster(boolean[][] pixelVotes, int x, int y) {
		Stack<Point> notDone=new Stack<Point>();
		Stack<Point> done=new Stack<Point>();
		notDone.push(new Point(x,y));
		int centreX=0, centreY=0;
		int total=0;
		while (!notDone.isEmpty()) {
			Point p=notDone.pop();
			done.push(p);
			centreX+=p.x;
			centreY+=p.y;
			total++;
			for (int xx=-1; xx<=1; xx++) {
				for (int yy=-1; yy<=1; yy++) {
					if (xx==0 && yy==0) continue;
					Point p2=new Point(p.x+xx, p.y+yy);
					if (p2.x<0 || p2.x>pixelVotes.length || p2.y<0 || p2.y>pixelVotes[0].length) continue;
					if (done.contains(p2)) continue;
					if (!pixelVotes[p2.x][p2.y]) continue;
					notDone.push(p2);	
				}
			}

		}
		centreX=(int)Math.round((1.0*centreX)/total);
		centreY=(int)Math.round((1.0*centreY)/total);

		for (Point p: done) {
			pixelVotes[p.x][p.y]=false;
		}
		pixelVotes[centreX][centreY]=true;
	}
}