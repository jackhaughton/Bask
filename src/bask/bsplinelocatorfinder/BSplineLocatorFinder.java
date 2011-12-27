package bask.bsplinelocatorfinder;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ListIterator;

import bask.LuminanceImage;

public class BSplineLocatorFinder {

	public static class PotentialLocator {
		public final int index;
		public final double start, end;

		public PotentialLocator(int _index, double _start, double _end) {
			index=_index;
			start=_start; end=_end;
		}
	}
	
	private static class InterestPoint {
		public double t;
		public double x;
		public double gradient;
		
		public InterestPoint(double _t, double _x, double _gradient) { t=_t; x=_x; gradient=_gradient; }
	}
	
	public static PotentialLocator[] findLocators(LuminanceImage im, boolean horizontal) {
		int maxIndex=horizontal?im.getHeight():im.getWidth();
		int controlPointCount=horizontal?im.getWidth():im.getHeight();
		ArrayList<ArrayList<InterestPoint>> interestPoints=new ArrayList<ArrayList<InterestPoint>>();
		double gradientAvg=0;
		double minGradient=Double.MAX_VALUE;
		double maxGradient=-Double.MAX_VALUE;
		int gradientCount=0;
		Point2D.Double controlPoints[][]=new Point2D.Double[maxIndex][controlPointCount];
		for (int index=0; index<maxIndex; index++) {
			//Form the control point array from the appropriate row or column
			for (int i=0; i<controlPointCount; i++) {
				double luminance=horizontal?im.get(i, index):im.get(index, i);
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
		gradientThreshold=gradientAvg;
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
			final double MAX_ERR=0.25;
			
			for (int i=0; i<differences.size()-4; i++) {
				//First check that we've got black, white, black, white, black
				double centre1=(interestPointsThisIndex.get(i).t+interestPointsThisIndex.get(i+1).t)/2;
				double centre2=(interestPointsThisIndex.get(i+1).t+interestPointsThisIndex.get(i+2).t)/2;
				double centre3=(interestPointsThisIndex.get(i+2).t+interestPointsThisIndex.get(i+3).t)/2;
				double centre4=(interestPointsThisIndex.get(i+3).t+interestPointsThisIndex.get(i+4).t)/2;
				double centre5=(interestPointsThisIndex.get(i+4).t+interestPointsThisIndex.get(i+5).t)/2;
				double lum1=BSpline.getSplinePoint(controlPoints[index], centre1).y;
				double lum2=BSpline.getSplinePoint(controlPoints[index], centre2).y;
				double lum3=BSpline.getSplinePoint(controlPoints[index], centre3).y;
				double lum4=BSpline.getSplinePoint(controlPoints[index], centre4).y;
				double lum5=BSpline.getSplinePoint(controlPoints[index], centre5).y;
				if (lum1>lum2 || lum2<lum3 || lum3>lum4 || lum4<lum5) continue;
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
						potentials.add(new PotentialLocator(index, ip1.x, ip2.x));
						break;
					}
			}
		}
		return potentials.toArray(new PotentialLocator[potentials.size()]);
	}
}