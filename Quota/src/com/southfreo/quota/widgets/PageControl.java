package com.southfreo.quota.widgets;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.widget.ProgressBar;

public class PageControl extends View {
    private int noPages;
    private int currentPage;
	
	
    public PageControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		noPages=0;
		currentPage=0;
		
	}

    
    
    @Override  
    protected synchronized void onDraw(Canvas canvas) {  
        //   
    	
        super.onDraw(canvas);  
        
        
        if (noPages==0) {
        	return;
        }
        
    	// Center
     	int cx = getWidth()-getPaddingLeft()-getPaddingRight();
     	int ch = getHeight()-getPaddingTop()-getPaddingBottom();
     	
     	int cy = (ch/2);
     	int rc = ch/3;
     	int wc = rc*3;
     	
     	cx = (cx /2);
     	
        Paint border = new Paint();
        border.setAntiAlias(true);
        border.setStyle(Style.FILL);
        border.setStrokeWidth(1.5f);
        border.setColor(0xFF3e3f51);//0xff666666
     
        
        if (noPages==2) {
        	cx = cx - (wc/2);
        } else {
            cx = cx - (noPages/2)*wc;
        }
        
        
        
        for (int i=0;i<noPages;i++) {
        	if (i==currentPage) {
        	       border.setColor(0xFFFFFFFF);//0xff666666
        	       
        	} else {
        	       border.setColor(0xFF3e3f51);//0xff666666
        	}
            canvas.drawCircle(cx, cy, rc, border);
            cx = cx + wc;
        }
    
 		
    }



    
    
	public void setNoPages(int noPages) {
		this.noPages = noPages;
	}


	public int getNoPages() {
		return noPages;
	}


	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}


	public int getCurrentPage() {
		return currentPage;
	}  
	
    public void nextPage() {
       	currentPage = currentPage + 1;
       	if (currentPage==noPages) {
       		currentPage=0;
       	}
     	this.invalidate();
    }
    
    public void previousPage() {
    	if (currentPage==0) {
    	   currentPage=noPages-1;	
    	} else {
    	   	currentPage = currentPage - 1;
    	}
      	this.invalidate();
      	 
    }
    

}
