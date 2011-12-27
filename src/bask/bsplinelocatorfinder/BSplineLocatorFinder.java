package bask.bsplinelocatorfinder;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ListIterator;

import bask.LuminanceImage;

public class BSplineLocatorFinder {

	public static class PotentialLocator {
		public final double start, end;

		public PotentialLocator(double _start, double _end) {
			start=_start; end=_end;
		}
	}
	
	private static class InterestPoint {
		public double t;
		public double x;
		public double gradient;
		
		public InterestPoint(double _t, double _x, double _gradient) { t=_t; x=_x; gradient=_gradient; }
	}
	
	public static PotentialLocator[] findLocators(LuminanceImage im, int index, boolean horizontal) {
		//Form the control point array from the appropriate row or column
		int controlPointCount=horizontal?im.getWidth():im.getHeight();
		Point2D.Double controlPoints[]=new Point2D.Double[controlPointCount];
		for (int i=0; i<controlPointCount; i++) {
			double luminance=horizontal?im.get(i, index):im.get(index, i);
			controlPoints[i]=new Point2D.Double(i, luminance);	
		}
		//Find the zero crossings
		double[] zeroCrossings=BSpline.getSecondDerivativeZeroes(controlPoints,0, controlPoints.length-4);
		ArrayList<InterestPoint> interestPoints=new ArrayList<InterestPoint>();
		ArrayList<Double> differences=new ArrayList<Double>();
		//Set up the initial set of interest points, which are at the zero crossings
		//Also find the average first derivative at those interest points
		double gradientAvg=0;
		for (int i=0; i<zeroCrossings.length; i++) {
			double gradient=BSpline.getSplinePointFirstDerivative(controlPoints, zeroCrossings[i]);
			gradientAvg+=Math.abs(gradient);
			Point2D.Double p=BSpline.getSplinePoint(controlPoints, zeroCrossings[i]);
			interestPoints.add(new InterestPoint(zeroCrossings[i],p.x,gradient));
		}
		gradientAvg/=zeroCrossings.length;
		//Exclude all interest points with first derivative values that are below average
		ListIterator<InterestPoint> ipLi=interestPoints.listIterator();
		while (ipLi.hasNext()) {
			InterestPoint ip=ipLi.next();
			if (Math.abs(ip.gradient)<gradientAvg) ipLi.remove();
		}
		//Set up an array of differences in x between adjacent remaining interest points
		for (int i=0; i<interestPoints.size(); i++) {
			InterestPoint ip=interestPoints.get(i);
			if (i<interestPoints.size()-1) {
				differences.add(interestPoints.get(i+1).x-ip.x);
			}
		}
		//Look for 1:1:3:1:1 ratios in adjacent differences
		ArrayList<PotentialLocator> potentials=new ArrayList<PotentialLocator>();
		final double MAX_ERR=0.75;
		for (int i=0; i<differences.size()-4; i++) {
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
            		if (err>MAX_ERR) continue outer;
				}
				//Found a match
				InterestPoint ip1=interestPoints.get(i);
				InterestPoint ip2=interestPoints.get(i+5);
				potentials.add(new PotentialLocator(ip1.x, ip2.x));
				break;
			}
		}
		return potentials.toArray(new PotentialLocator[potentials.size()]);
	}
}
