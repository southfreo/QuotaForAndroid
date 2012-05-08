package com.southfreo.quota.activities;

import java.util.Observable;
import java.util.Observer;

import com.markupartist.android.widget.ActionBar.*;

import com.markupartist.android.widget.ActionBar;
import com.southfreo.R;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.extraTableAdapter;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.R.drawable;
import com.southfreo.R.id;
import com.southfreo.R.layout;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.control.ProviderManager.NotifyUpdate;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.widgets.*;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class DetailActivity_ISP extends DetailActivity implements Observer {
	private static final String TAG = "Quota-DetailActivity_ISP";
	
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper viewFlipper;
    private PageControl pageControl;
     
    TextProgressBar expiryp1;
    TextView expiryp2_1;
    TextView expiryp2_2;
    TextView peakLabel;
    TextView peakValue;
    TextView offpeakLabel;
    TextView offpeakValue;
 
    TextView peakLable2;
    TextView peakValue2;
    TextView offpeakLable2;
    TextView offpeakValue2;
       
    TextProgressBar peakBar_gray;
    TextProgressBar peakBar_red;
    
    TextProgressBar offpeakBar_gray;
    TextProgressBar offpeakBar_red;
    
    LinearLayout detailContent;
    
    Drawable redProgress1;
    Drawable redProgress2;
    Drawable grayProgress1;
    Drawable grayProgress2;
    GridView gridview;
    extraTableAdapter edAdapt;
    
    
    private void setupProgress(kbProgressBar pb, int index) {
    	
    	TextProgressBar bar;
    	
        if (pb.overIdeal()) {
        	// Over Usage
        	if (index==1) {
         		peakBar_red.setVisibility(View.VISIBLE);
          		peakBar_gray.setVisibility(View.GONE);
        		bar = peakBar_red;
        	} else {
           		offpeakBar_red.setVisibility(View.VISIBLE);
          		offpeakBar_gray.setVisibility(View.GONE);
        		bar = offpeakBar_red;
        	}
        	bar.setSecondaryProgress((int)pb.percentage);
        	bar.setProgress((int)pb.ideal);
        } else {
        	// Normal
          	if (index==1) {
          		peakBar_gray.setVisibility(View.VISIBLE);
          		peakBar_red.setVisibility(View.GONE);
          		bar = peakBar_gray;
        	} else {
        		offpeakBar_gray.setVisibility(View.VISIBLE);
          		offpeakBar_red.setVisibility(View.GONE);
          		bar = offpeakBar_gray;
        	}
          	bar.setSecondaryProgress((int)pb.ideal);
          	bar.setProgress((int)pb.percentage);
        }
        
        bar.setMax(100);
        bar.setText(pb.percentage_msg);  
    
    }
    
    
    protected void  updateDetail() {
        
    	super.updateDetail();
    	
    	try {
    	   	
        	//Peak Progress
        	if (thisProvider.progress !=null && thisProvider.progress.size()>0) {
            	kbProgressBar pb1 = thisProvider.progress.get(0);
            	peakLabel.setText(pb1.name);
            	peakValue.setText(pb1.val_msg);
            	setupProgress(pb1,1);
                
            	peakLable2.setText(pb1.stat1);
            	peakValue2.setText(pb1.stat2);
        	}
        	
        	if (thisProvider.progress !=null && thisProvider.progress.size()==2) {
        	   	// OffPeak (Check to Turn off)
            	kbProgressBar pb2 = thisProvider.progress.get(1);
               	offpeakLabel.setText(pb2.name);
            	offpeakValue.setText(pb2.val_msg);
           
            	setupProgress(pb2,2);
            	offpeakLable2.setText(pb2.stat1);
            	offpeakValue2.setText(pb2.stat2);
         	}
        
        	if (thisProvider.current_cycle!=null) {
              	int cyclepercent = 0;
              	 
        		cyclepercent = (int)thisProvider.current_cycle.dayspercentage;
            	// Days Progress Bar
            	expiryp1.setText(thisProvider.current_cycle.getCycleText());  
                expiryp1.setProgress(cyclepercent);  
                
                // Days Progress Alternate
                expiryp2_1.setText(thisProvider.current_cycle.getCycleTextAlternate());
                expiryp2_2.setText(thisProvider.current_cycle.getCycleTextAlternate2());
        	}
        	
        	// Update Extra table
            if (gridview!=null) {
               	edAdapt.mData = thisProvider.extras;
            	edAdapt.notifyDataSetChanged();
            }
        	
    	} catch (Exception e) {
    		Log.e(TAG,"Problem displaying detail"+e.toString());
    	}
        
    }
	

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       	super.onCreate(savedInstanceState,R.layout.activity_detailview_isp);

        
        // Setup Bar 1
        peakLabel = (TextView) findViewById(R.id.plabel);
        peakValue = (TextView) findViewById(R.id.pvalue);
        
        peakBar_gray= (TextProgressBar) findViewById(R.id.bar_1_gray);
        peakBar_red = (TextProgressBar) findViewById(R.id.bar_1_red);
        
        
        //peakBar.setProgressDrawable(grayProgress.getProgressDrawable());  

        peakLable2 = (TextView) findViewById(R.id.plabel_p2);
        peakValue2 = (TextView) findViewById(R.id.pvalue_p2);
        
        
        // Setup Bar 2
        offpeakLabel = (TextView) findViewById(R.id.plabel2);
        offpeakValue = (TextView) findViewById(R.id.pvalue2);

        offpeakBar_gray = (TextProgressBar) findViewById(R.id.bar_2_gray);
        offpeakBar_red = (TextProgressBar) findViewById(R.id.bar_2_red);
        
        //offpeakBar.setProgressDrawable(grayProgress.getProgressDrawable());  
       
        offpeakLable2 = (TextView) findViewById(R.id.plabel2_p2);
        offpeakValue2 = (TextView) findViewById(R.id.pvalue2_p2);
        
        // Expiry
        expiryp1 = (TextProgressBar) findViewById(R.id.bar_3_blue);
        expiryp1.setMax(100);
        
        expiryp2_1 = (TextView)findViewById(R.id.plabel3_p2);
        expiryp2_2 = (TextView)findViewById(R.id.pvalue3_p2);
        
        
        
        // Page Control
        pageControl = (PageControl)findViewById(R.id.pagecontrol);
        
        pageControl.setNoPages(2);
        pageControl.setCurrentPage(0);
        
        // View Flipper
        viewFlipper = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        viewFlipper.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });      	
       	
        // Extras Table - Portrait Only
        gridview = (GridView) findViewById(R.id.gridview);
        if (gridview!=null) {
            gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));   
        	edAdapt = new extraTableAdapter(this);
        	gridview.setAdapter(edAdapt);
        }
  
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	viewFlipper.setInAnimation(slideLeftIn);
                    viewFlipper.setOutAnimation(slideLeftOut);
                	viewFlipper.showNext();
                	pageControl.nextPage();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	viewFlipper.setInAnimation(slideRightIn);
                    viewFlipper.setOutAnimation(slideRightOut);
                	viewFlipper.showPrevious();
                	pageControl.previousPage();
                                   }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    if (gestureDetector.onTouchEvent(event))
    	return true;
    else
    	return false;
    }
   
}