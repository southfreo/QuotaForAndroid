package com.southfreo.quota.widgets;

import com.southfreo.R;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.utils.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;



public class TextParamValidate extends EditTextPreference  {

	    private TextView mValueText;  
	    private TextView mTitleView;
	    public parameter param;
	    
	    public TextParamValidate(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        setLayoutResource(R.layout.preference_with_value);  
	    }  
	  
	    public TextParamValidate(Context context) {  
	        super(context);  
	        setLayoutResource(R.layout.preference_with_value);  
	    }  
	  
	    private boolean updateValue(boolean validate,String newValue) {
	    	UIManager ui = UIManager.getInstance();
	    	
	        if (mValueText != null) {  
	        	if (newValue!=null) {
	        		// Set the Parameter Value
	        		if (param!=null) {
		        		// Validate Parameter
	        			if (validate && (!param.isValid(newValue))) {
	        				ui.MsgBoxInfo(getContext(),param.Name, "Entry is not valid");
	        				return false;
	        			} else {
		        			param.setTextValue(newValue);
				            mValueText.setText(param.CurrentValueAsFormattedString());  
				            notifyChanged();
	        			}
	        		} else if (newValue!=null) {
			            // Not based on a parameter
	        			mValueText.setText(newValue);  
	        			notifyChanged();	        		}
	        	}
	        }  
	        return true;
	    }
	    
	    
	    @Override  
	    protected void onBindView(View view) {  
	        super.onBindView(view);  
	        mValueText = (TextView) view.findViewById(R.id.preference_value);  
//	        mTitleView = (TextView) view.findViewById(android.R.id.title);
//	        if (mTitleView!=null) {
//	        	if (param!=null && param.optional) {
//	        		//Typeface t = mTitleView.getTypeface();
//	        		//t.
//	        	   mTitleView.setTypeface(null, Typeface.ITALIC);
//	        	}
//	        }
	        updateValue(false,getText());
	    }  
	  
//	    public void setTitle(String text) {
//	    	super.setTitle(text);
//	    }
	    
	    @Override  
	    public void setText(String text) {
	    	// Validate
	    	if (updateValue(true,text)) {
		        super.setText(text);
	    	}
	    }  
	  
	}  
