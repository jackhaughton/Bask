import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;


public class LocatorFinder {

	public static final int FLAG_HORIZ=1;
	public static final int FLAG_VERT=2;
	public static final int FLAG_TAGGED=4;
	public int width, height;
	public int[] flags;
    
    public Point[] findLocators(BinaryImage bi) {
        width=bi.getWidth();
        height=bi.getHeight();
        flags=new int[width*height];
        //Run findPotentialLocators in both directions and correlate the points
        findPotentialLocators(bi,true);
        findPotentialLocators(bi,false);
        for (int x=1; x<width-1; x++) {
        	for (int y=1; y<height-1; y++) {
        		int horizCount=0, vertCount=0;
        		for (int xx=-1; xx<=1; xx++) {
        			for (int yy=-1; yy<=1; yy++) {
        				int flag=flags[x+xx+(y+yy)*width];
        				if ((flag&FLAG_HORIZ)!=0) horizCount++;
        				if ((flag&FLAG_VERT)!=0) vertCount++;
        			}
        		}
        		if (horizCount>1 && vertCount>1) flags[x+y*width]|=FLAG_TAGGED;
        	}
        }
        return null;
    }
    
    private void findPotentialLocators(BinaryImage bi, boolean horizontal) {
    	int flag=horizontal?FLAG_HORIZ:FLAG_VERT;
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
                    lastColour=currentColour;
                }
            }
            currentStack.add((horizontal?w:h)-startPoint);
            
            for (Integer val: currentStack) {
                System.out.print("["+val+"]");
            }
            System.out.println();
            
            //now go through the stack looking for 1:1:3:1:1 patterns
            final double MAX_ERR=0.5;
            startPoint=currentStack.get(0);
            for (int i=1; i<currentStack.size()-5; i++) {
                int cStartPoint=startPoint;
                double base=currentStack.get(i);
                double err1=Math.abs((currentStack.get(i+1)/base)-1);
                double err2=Math.abs((currentStack.get(i+2)/base)-3);
                double err3=Math.abs((currentStack.get(i+3)/base)-1);
                double err4=Math.abs((currentStack.get(i+4)/base)-1);
                startPoint+=currentStack.get(i);
                if (err1>MAX_ERR || err2>MAX_ERR || err3>MAX_ERR || err4>MAX_ERR) continue;
                //If we get here, we've got a potential
                int bCentre=cStartPoint+currentStack.get(i)+currentStack.get(i+1)+currentStack.get(i+2)/2;
                int point;
                if (horizontal) point=bCentre+a*width;
                else point=a+bCentre*width;
                flags[point]|=flag;
            }
        }
    }
}
