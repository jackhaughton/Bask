import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;


public class LocatorFinder {

	public static Point[] findLocators(BinaryImage bi) {
		ArrayList<Point> horizCentres=new ArrayList<Point>();
		//Run findPotentialLocators in both directions and correlate the points
	}
	
	private static void findPotentialLocators(BinaryImage bi, ArrayList<Point> results, boolean horizontal) {
		int w=bi.getWidth();
		int h=bi.getHeight();
		ArrayList<Integer> currentStack=new ArrayList<Integer>();
		for (int a=0; a<(horizontal?h:w); a++) {
			currentStack.clear();
			boolean startColour=horizontal?bi.get(0,a):bi.get(a,0);
			boolean lastColour=startColour;
			int startPoint=0;
			for (int b=1; b<(horizontal?w:h); b++) {
				boolean currentColour=horizontal?bi.get(b,a):bi.get(a,b);
				if (currentColour!=lastColour) {
					currentStack.add(b-startPoint);
					startPoint=b;
				}
			}
			currentStack.add((horizontal?w:h)-startPoint);
			
			//now go through the stack looking for 1:1:3:1:1 patterns
			final double MAX_ERR=0.1;
			startPoint=currentStack.get(0);
			for (int i=1; i<currentStack.size()-5; i++) {
				double base=currentStack.get(i);
				double err1=Math.abs((currentStack.get(i+1)/base)-1);
				double err2=Math.abs((currentStack.get(i+2)/base)-3);
				double err3=Math.abs((currentStack.get(i+3)/base)-1);
				double err4=Math.abs((currentStack.get(i+4)/base)-1);
				if (err1>MAX_ERR || err2>MAX_ERR || err3>MAX_ERR || err4>MAX_ERR) continue;
				//If we get here, we've got a potential
				//TODO: add it to the results list
			}
		}
	}
}
