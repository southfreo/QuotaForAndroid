package com.southfreo.quota.widgets;

import com.southfreo.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;



public class IconPreference extends Preference  {

	    private int myImage;  
	    private String myTitle;
	    private Bitmap bm;
	    
	    public IconPreference(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        bm=null;
	        setLayoutResource(R.layout.preference_with_icon);  
	    }  
	  
	    public IconPreference(Context context) {  
	        super(context);  
	        setLayoutResource(R.layout.preference_with_icon);  
	    }  
	    
	    public void setImage(int resid) {
	    	myImage=resid;
	    	bm=null;
	    	notifyChanged();
	    }

	    public void setBitMap(Bitmap bm) {
	    	this.bm=bm;
	    	myImage=0;
	    	notifyChanged();
	    }

	    public void setTitle(String s) {
	    	myTitle = s;
	    	notifyChanged();
	    }
	    
	    @Override  
	    protected void onBindView(View view) {  
	        super.onBindView(view);  
	        final ImageView imageView = (ImageView) view.findViewById(R.id.logo);  
	        final TextView titleView = (TextView)view.findViewById(R.id.title);
	        
	        if (titleView!=null) {
	        	if (myTitle!=null) {
	        		titleView.setText(myTitle);
	        	}
	        }
	        if (imageView!=null) {
	        	if (myImage!=0) {
	        		imageView.setImageResource(myImage);
	        	} else {
	        		if (bm!=null) {
	        			imageView.setImageBitmap(bm);
	        		}
	        	}
	        }
	    }  
	  
	}  
