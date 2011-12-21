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
		LinkedList<Integer> currentStack=new LinkedList<Integer>();
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
			//TODO: now go through the stack looking for 1:1:3:1:1 patterns
		}
	}
}
