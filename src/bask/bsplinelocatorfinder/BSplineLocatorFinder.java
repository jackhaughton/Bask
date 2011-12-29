package bask.bsplinelocatorfinder;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ListIterator;

import bask.LuminanceImage;

public class BSplineLocatorFinder {

	public static class PotentialLocator {
		public final int startX, endX, startY, endY;
		public final ScanDirection direction;

		public PotentialLocator(int _startX, int _endX, int _startY, int _endY, ScanDirection _direction) {
			startX=_startX; endX=_endX;
			startY=_startY; endY=_endY;
			direction=_direction;
		}
		
		public double getSquaredLength() {
			return (endX-startX)*(endX-startX)+(endY-startY)*(endY-startY);
		}
		
		public Point getCentre() {
			return new Point((int)Math.round((startX+endX)/2.0), (int)Math.round((startY+endY)/2.0));
		}
	}
	
	private static class InterestPoint {
		public double t;
		public double x;
		public double gradient;
		
		public InterestPoint(double _t, double _x, double _gradient) { t=_t; x=_x; gradient=_gradient; }
	}
	
	public static enum ScanDirection {
		HORIZONTAL, VERTICAL,
		DIAGONAL1, DIAGONAL2
	}
	
	public static PotentialLocator[] findLocators(LuminanceImage im, ScanDirection direction) {
		int imWidth=im.getWidth(), imHeight=im.getHeight();
		int maxIndex;
		switch (direction) {
		case HORIZONTAL: maxIndex=imHeight; break;
		case VERTICAL: maxIndex=imWidth; break;
		case DIAGONAL1: maxIndex=imWidth+imHeight-1; break;
		case DIAGONAL2: maxIndex=imWidth+imHeight-1; break;
		default: throw new IllegalArgumentException();
		}
		ArrayList<ArrayList<InterestPoint>> interestPoints=new ArrayList<ArrayList<InterestPoint>>();
		double gradientAvg=0;
		double minGradient=Double.MAX_VALUE;
		double maxGradient=-Double.MAX_VALUE;
		int gradientCount=0;
		Point2D.Double controlPoints[][]=new Point2D.Double[maxIndex][];
		for (int index=0; index<maxIndex; index++) {
			int controlPointCount;
			switch (direction) {
			case HORIZONTAL: controlPointCount=imWidth; break;
			case VERTICAL: controlPointCount=imHeight; break;
			case DIAGONAL1:
			case DIAGONAL2:
				if (imWidth<imHeight) {
					if (index<imWidth) controlPointCount=1+index;
					else if (index<imHeight) controlPointCount=imWidth;
					else controlPointCount=imHeight+imWidth-(1+index);
				}
				else {
					if (index<imHeight) controlPointCount=1+index;
					else if (index<imWidth) controlPointCount=imHeight;
					else controlPointCount=imHeight+imWidth-(1+index);
				}
				break;
			default: throw new IllegalArgumentException();
			}
			controlPoints[index]=new Point2D.Double[controlPointCount];
			//Form the control point array from the appropriate row or column
			for (int i=0; i<controlPointCount; i++) {
				
				Point p=getPixelPoint(index, i, imWidth, direction);
				double luminance=im.get(p.x,p.y);
				controlPoints[index][i]=new Point2D.Double(i, luminance);	
			}
			//Find the zero crossings
			double[] zeroCrossings=BSpline.getSecondDerivativeZeroes(controlPoints[index],0, controlPoints[index].length-4);
			ArrayList<InterestPoint> interestPointsThisIndex=new ArrayList<InterestPoint>();
			interestPoints.add(interestPointsThisIndex);
			//Set up the initial set of interest points, which are at the zero crossings
			
			for (int i=0; i<zeroCrossings.length; i++) {
				double gradient=BSpline.getSplinePointFirstDerivative(controlPoints[index], zeroCrossings[i]);
				gradientAvg+=Math.abs(gradient);
				gradientCount++;
				if (gradient<minGradient) minGradient=gradient;
				if (gradient>maxGradient) maxGradient=gradient;
				Point2D.Double p=BSpline.getSplinePoint(controlPoints[index], zeroCrossings[i]);
				interestPointsThisIndex.add(new InterestPoint(zeroCrossings[i],p.x,gradient));
			}

		}
		gradientAvg/=gradientCount;
		double gradientThreshold=minGradient+0.75*(maxGradient-minGradient);
		//gradientThreshold=gradientAvg;
		for (ArrayList<InterestPoint> interestPointList: interestPoints) {
			//Exclude all interest points with first derivative values that are below average
			ListIterator<InterestPoint> ipLi=interestPointList.listIterator();
			while (ipLi.hasNext()) {
				InterestPoint ip=ipLi.next();
				if (Math.abs(ip.gradient)<gradientThreshold) ipLi.remove();
			}
		}

		ArrayList<PotentialLocator> potentials=new ArrayList<PotentialLocator>();
		for (int index=0; index<maxIndex; index++) {
			ArrayList<InterestPoint> interestPointsThisIndex=interestPoints.get(index);
			//Set up an array of differences in x between adjacent remaining interest points
			ArrayList<Double> differences=new ArrayList<Double>();
			for (int i=0; i<interestPointsThisIndex.size()-1; i++) {
				InterestPoint ip=interestPointsThisIndex.get(i);
				differences.add(interestPointsThisIndex.get(i+1).x-ip.x);
			}
			//Look for 1:1:3:1:1 ratios in adjacent differences
			final double MAX_ERR=0.5;
			
			for (int i=0; i<differences.size()-4; i++) {
				//First check that we've got black, white, black, white, black
				double gradient2=interestPointsThisIndex.get(i+1).gradient;
				double gradient3=interestPointsThisIndex.get(i+2).gradient;
				double gradient4=interestPointsThisIndex.get(i+3).gradient;
				double gradient5=interestPointsThisIndex.get(i+4).gradient;
				if (gradient2<0 || gradient3>0 || gradient4<0 || gradient5>0) continue;
				outer:
					for (int j=0; j<5; j++) {
						double base=differences.get(i+j);
						for (int k=0; k<5; k++) {
							if (j==k) continue;
							double ratio=differences.get(i+k)/base;
							double target=1;
							if (j==2) {
								if (k!=2) target=1/3.0;
							}
							else {
								if (k==2) target=3;
							}
							double err=Math.abs(ratio-target);
							if (err>MAX_ERR*target) continue outer;
						}
						//Found a match
						InterestPoint ip1=interestPointsThisIndex.get(i);
						InterestPoint ip2=interestPointsThisIndex.get(i+5);
						Point p1=getPixelPoint(index, (int)Math.round(ip1.x), imWidth, direction);
						Point p2=getPixelPoint(index, (int)Math.round(ip2.x), imWidth, direction);
						potentials.add(new PotentialLocator(p1.x ,p2.x, p1.y, p2.y, direction));
						break;
					}
			}
		}
		return potentials.toArray(new PotentialLocator[potentials.size()]);
	}
	
	private static Point getPixelPoint(int index, int i, int imWidth, ScanDirection direction) {
		switch (direction) {
		case HORIZONTAL: return new Point(i, index);
		case VERTICAL: return new Point(index, i);
		case DIAGONAL1: 
			if (index<imWidth) return new Point(i+imWidth-(1+index), i);
			else return new Point(i, i+1+index-imWidth);
		case DIAGONAL2: 
			if (index<imWidth) return new Point(index-i, i);
			else return new Point(imWidth-i-1, i+1+index-imWidth);
		default: throw new IllegalArgumentException();
		}
	}
}