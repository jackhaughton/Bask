package bask.simplelocatorfinder;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import bask.LuminanceImage;


public class LocatorFinder {

	public static final int FLAG_HORIZ=1;
	public static final int FLAG_VERT=2;
	public static final int FLAG_TAGGED=4;
	public int width, height;
	public int[] flags;
    
    public Point[] findLocators(LuminanceImage bi) {
        width=bi.getWidth();
        height=bi.getHeight();
        flags=new int[width*height];
        //Run findPotentialLocators in both directions and correlate the points
        findPotentialLocators(bi,true);
        findPotentialLocators(bi,false);
        for (int x=1; x<width-1; x++) {
        	for (int y=1; y<height-1; y++) {
//        		int centralFlag=flags[x+y*width];
        		/*if ((centralFlag&(FLAG_HORIZ|FLAG_VERT))==0)
        			continue;*/
        		/*if ((centralFlag&(FLAG_HORIZ|FLAG_VERT))==(FLAG_HORIZ|FLAG_VERT))
        			{ flags[x+y*width]|=FLAG_TAGGED; continue; }*/
        		int horizCount=0, vertCount=0;
        		for (int xx=-1; xx<=1; xx++) {
        			for (int yy=-1; yy<=1; yy++) {
        				int flag=flags[x+xx+(y+yy)*width];
        				if ((flag&FLAG_HORIZ)!=0) horizCount++;
        				if ((flag&FLAG_VERT)!=0) vertCount++;
        			}
        		}
        		if (horizCount>0 && vertCount>0) flags[x+y*width]|=FLAG_TAGGED;
        	}
        }
        return null;
    }
    
    private void findPotentialLocators(LuminanceImage bi, boolean horizontal) {
    	int flag=horizontal?FLAG_HORIZ:FLAG_VERT;
        int w=bi.getWidth();
        int h=bi.getHeight();
        ArrayList<Integer> currentStack=new ArrayList<Integer>();
        for (int a=0; a<(horizontal?h:w); a++) {
            currentStack.clear();
            double startColour=horizontal?bi.get(0,a):bi.get(a,0);
            boolean lastColour=startColour>0.5;
            int startPoint=0;
            for (int b=1; b<(horizontal?w:h); b++) {
            	double currentColour=horizontal?bi.get(b,a):bi.get(a,b);
                if ((currentColour>0.5)!=lastColour) {
                    currentStack.add(b-startPoint);
                    startPoint=b;
                    lastColour=currentColour>0.5;
                }
            }
            currentStack.add((horizontal?w:h)-startPoint);
            
            /*for (Integer val: currentStack) {
                System.out.print("["+val+"]");
            }
            System.out.println();*/
            
            //now go through the stack looking for 1:1:3:1:1 patterns
            final double MAX_ERR=0.3;
            int[] window=new int[5];
            startPoint=currentStack.get(0);
            for (int i=1; i<currentStack.size()-5; i++) {
            	int cStartPoint=startPoint;
            	startPoint+=currentStack.get(i);
            	int totalWidth=0;
            	for (int j=0; j<5; j++) { window[j]=currentStack.get(i+j); totalWidth+=window[j]; }
            	if (totalWidth<10) continue;
                boolean isCandidate=false;
                outer:
                for (int j=0; j<5; j++) {
                	double base=window[j];
                	for (int k=0; k<5; k++) {
                		if (j==k) continue;
                		double target=1;
                		if (j==2) {
                			if (k!=2) target=1/3.0;
                		}
                		else {
                			if (k==2) target=3;
                		}
                		double err=Math.abs((window[k]/base)-target);
                		if (err>MAX_ERR) continue outer; 
                	}
                	isCandidate=true;
                	break;
                }
                if (!isCandidate) continue;
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
