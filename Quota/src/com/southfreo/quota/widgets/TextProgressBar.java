package com.southfreo.quota.widgets;

import com.southfreo.quota.control.UIManager;

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar {
    private String text;  
    private Paint textPaint;  
  
//    public TextProgressBar(Context context) {  
//        super(context);  
//        text = "HP";  
//        textPaint = new Paint();  
//        textPaint.setColor(Color.BLACK);  
//        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//    }  
  
    
    
    public TextProgressBar(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        textPaint = new Paint();  
        textPaint.setColor(Color.WHITE); 
        Typeface typeArial;
        
        typeArial = Typeface.create("arial", Typeface.BOLD);
         
        textPaint.setTypeface(typeArial);
        
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //textPaint.setFont
    }  
  
/*    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        text = "HP";  
        textPaint = new Paint();  
        textPaint.setColor(Color.BLACK);  
    }*/  
  
    @Override  
    protected synchronized void onDraw(Canvas canvas) {  
        // First draw the regular progress bar, then custom draw our text  

    	RectF r = new RectF(getPaddingLeft(), getPaddingTop(), getWidth()-getPaddingLeft(), getHeight());

    	
    	// Border
        Paint border = new Paint();
        border.setAntiAlias(true);
        border.setStyle(Style.STROKE);
        border.setStrokeWidth(1.5f);
        border.setColor(0xFF3e3f51);//0xff666666
     
        canvas.drawRoundRect(r, 5, 5, border);
      
        // Ticks
        float numBars = getWidth()/10;
        
        canvas.save();
        canvas.clipRect(r);
        for (int i = 0; i < numBars; i++) {
        	float xpos = i*numBars;
        	if (i<10) {
            	canvas.drawLine(xpos,0.0f,xpos,getHeight(),border);
        	}
        }
        canvas.restore();
        
        super.onDraw(canvas);  
        
        // Text Overlay
        Rect bounds = new Rect(); 
        int nudgey=0;
        if (getHeight()<30) {
        	textPaint.setTextSize(UIManager.getInstance().getDipSize(10));
        } else {
        	nudgey=2;
           	textPaint.setTextSize(UIManager.getInstance().getDipSize(14));
        }
        
        if (text!=null) {
            textPaint.getTextBounds(text, 0, text.length(), bounds);  
            
            int wid = getWidth()-getPaddingLeft()-getPaddingRight();
            int hei = getHeight();//-getPaddingTop()-getPaddingBottom();
            
            int x = wid / 2 - bounds.centerX();  
            int y = hei / 2 - bounds.centerY();  
            canvas.drawText(text, x, y+nudgey, textPaint);  
        }
 		
    }  
  
    
    public synchronized void setText(String text) {  
        this.text = text;  
        drawableStateChanged();  
    }  
  
    public void setTextColor(int color) {  
        textPaint.setColor(color);  
        drawableStateChanged();  
    }  
}
