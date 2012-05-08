package com.southfreo.quota.widgets;

import com.southfreo.R;
import com.southfreo.quota.model.parameter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;



public class ListPreferenceValueNoParam extends ListPreference  {

	    protected TextView mValueText;  
	    protected String  currentValue;
	    public parameter param;
	    
	    public ListPreferenceValueNoParam(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        setLayoutResource(R.layout.preference_with_value);  
	    }  
	  
	    public ListPreferenceValueNoParam(Context context) {  
	        super(context);  
	        setLayoutResource(R.layout.preference_with_value);  
	    }  
	  
	    protected void updateValue() {
	        if (mValueText != null) {  
	        	if (currentValue!=null) {
	        		// Update Parameter Value
	        		if (currentValue!=null) {
		        		mValueText.setText(currentValue);  
	        		}
	        		
	        		notifyChanged();
	        	}
	        }  
	    }
	    
	    @Override  
	    protected void onBindView(View view) {  
	        super.onBindView(view);  
	        mValueText = (TextView) view.findViewById(R.id.preference_value);  
	        updateValue();
	    }  
	  
	    @Override
	    public void setValue(String value) {
	    	super.setValue(value);
	    	currentValue = value;
	    	updateValue();
	    }
	  
	}  
