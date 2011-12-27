package bask.bsplinelocatorfinder;

import java.awt.geom.Point2D;
import java.util.ArrayList;


public class BSpline {
	
	static final double[][] B_SPLINE_MATRIX=new double[][] {
		new double[] { 1/6.0, 4/6.0, 1/6.0, 0/6.0 },
		new double[] { -3/6.0, 0/6.0, 3/6.0, 0/6.0 },
		new double[] { 3/6.0, -6/6.0, 3/6.0, 0/6.0 },
		new double[] { -1/6.0, 3/6.0, -3/6.0, 1/6.0 }
	};
	
	static final double[][] B_SPLINE_MATRIX_FIRST_DERIVATIVE=getDerivative(B_SPLINE_MATRIX);
	static final double[][] B_SPLINE_MATRIX_SECOND_DERIVATIVE=getDerivative(B_SPLINE_MATRIX_FIRST_DERIVATIVE);
	static final double[][] B_SPLINE_MATRIX_THIRD_DERIVATIVE=getDerivative(B_SPLINE_MATRIX_SECOND_DERIVATIVE);
	
	private static double[][] getDerivative(double[][] base) {
		return new double[][] {
				new double[] { base[1][0], base[1][1], base[1][2], base[1][3] },
				new double[] { base[2][0]*2, base[2][1]*2, base[2][2]*2, base[2][3]*2 },
				new double[] { base[3][0]*3, base[3][1]*3, base[3][2]*3, base[3][3]*3 },
				new double[] { 0,0,0,0 }
		};
	}
	public static Point2D.Double getSplinePoint(Point2D.Double[] controlPoints, double t) {
		int intT=(int)t;
		double fractionalT=t-intT;
		Point2D.Double p=new Point2D.Double(0,0);
		double tToPower=1.0;
		for (int power=0; power<4; power++) {
			double[] matrixRow=B_SPLINE_MATRIX[power];
			for (int point=0; point<4; point++) {
				p.x+=matrixRow[point]*tToPower*controlPoints[point+intT].x;
				p.y+=matrixRow[point]*tToPower*controlPoints[point+intT].y;
			}
			tToPower*=fractionalT;
		}
		return p;
	}
	
	public static double getSplinePointFirstDerivative(Point2D.Double[] controlPoints, double t) {
		int intT=(int)t;
		double fractionalT=t-intT;
		Point2D.Double p=new Point2D.Double(0,0);
		double tToPower=1.0;
		for (int power=0; power<3; power++) {
			double[] matrixRow=B_SPLINE_MATRIX_FIRST_DERIVATIVE[power];
			for (int point=0; point<4; point++) {
				p.x+=matrixRow[point]*tToPower*controlPoints[point+intT].x;
				p.y+=matrixRow[point]*tToPower*controlPoints[point+intT].y;
			}
			tToPower*=fractionalT;
		}
		return p.y/p.x;
	}
	
	public static double getSplinePointSecondDerivative(Point2D.Double[] controlPoints, double t) {
		int intT=(int)t;
		double fractionalT=t-intT;
		Point2D.Double pFirst=new Point2D.Double(0,0);
		Point2D.Double pSecond=new Point2D.Double(0,0);
		double tToPower=1.0;
		for (int power=0; power<3; power++) {
			double[] matrixRow1st=B_SPLINE_MATRIX_FIRST_DERIVATIVE[power];
			double[] matrixRow2nd=B_SPLINE_MATRIX_SECOND_DERIVATIVE[power];
			for (int point=0; point<4; point++) {
				pFirst.x+=matrixRow1st[point]*tToPower*controlPoints[point+intT].x;
				pFirst.y+=matrixRow1st[point]*tToPower*controlPoints[point+intT].y;
				pSecond.x+=matrixRow2nd[point]*tToPower*controlPoints[point+intT].x;
				pSecond.y+=matrixRow2nd[point]*tToPower*controlPoints[point+intT].y;
			}
			tToPower*=fractionalT;
		}
		
		return (pFirst.x*pSecond.y-pSecond.x*pFirst.y)/(pFirst.x*pFirst.x*pFirst.x);
	}
	
	public static double getSplinePointThirdDerivative(Point2D.Double[] controlPoints, double t) {
		int intT=(int)t;
		double fractionalT=t-intT;
		Point2D.Double pFirst=new Point2D.Double(0,0);
		Point2D.Double pSecond=new Point2D.Double(0,0);
		Point2D.Double pThird=new Point2D.Double(0,0);
		double tToPower=1.0;
		for (int power=0; power<3; power++) {
			double[] matrixRow1st=B_SPLINE_MATRIX_FIRST_DERIVATIVE[power];
			double[] matrixRow2nd=B_SPLINE_MATRIX_SECOND_DERIVATIVE[power];
			double[] matrixRow3rd=B_SPLINE_MATRIX_THIRD_DERIVATIVE[power];
			for (int point=0; point<4; point++) {
				pFirst.x+=matrixRow1st[point]*tToPower*controlPoints[point+intT].x;
				pFirst.y+=matrixRow1st[point]*tToPower*controlPoints[point+intT].y;
				pSecond.x+=matrixRow2nd[point]*tToPower*controlPoints[point+intT].x;
				pSecond.y+=matrixRow2nd[point]*tToPower*controlPoints[point+intT].y;
				pThird.x+=matrixRow3rd[point]*tToPower*controlPoints[point+intT].x;
				pThird.y+=matrixRow3rd[point]*tToPower*controlPoints[point+intT].y;
			}
			tToPower*=fractionalT;
		}
		
		return (pFirst.x*pFirst.x*pThird.y-pThird.x*pFirst.y*pFirst.x-3*pSecond.x*pSecond.y*pFirst.x+3*pSecond.x*pSecond.x*pFirst.y)/
				(pFirst.x*pFirst.x*pFirst.x*pFirst.x*pFirst.x);
	}
	
	public static double[] getSecondDerivativeZeroes(Point2D.Double[] controlPoints, int tStart, int tEnd) {
		ArrayList<Double> zeroCrossingsT=new ArrayList<Double>();
		//Work section by section
		for (int t=tStart; t<tEnd; t++) {
			//First get the start and end points (we just need the x component of these)
			double startPointX=getSplinePoint(controlPoints, t).x;
			double endPointX=getSplinePoint(controlPoints, t+1).x;
			//Now get the start y point
			double startPointY=getSplinePointSecondDerivative(controlPoints, t);
			//Now the gradient
			double gradient=getSplinePointThirdDerivative(controlPoints, t+0.5);
			if (Math.abs(gradient)<0.000001) continue;
			//Calculate the constant part (equation of the form y=mx+c, so c=y-mx)
			double constantPart=startPointY-gradient*startPointX;
			//Find the zero crossing at x=-c/m
			double zeroCrossingX=-constantPart/gradient;
			//If it's outside the range, skip
			if (zeroCrossingX<startPointX || zeroCrossingX>endPointX) continue;
			//If we get here it's valid
			zeroCrossingsT.add((zeroCrossingX-startPointX)/(endPointX-startPointX)+t);
		}
		double[] result=new double[zeroCrossingsT.size()];
		for (int i=0; i<result.length; i++) result[i]=zeroCrossingsT.get(i);
		return result;
	}
}
