package com.southfreo.quota.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.southfreo.R;
import com.southfreo.quota.control.SlotManager;

public class PinEntry extends Activity {

	   public static int 	MODE_CREATE   = 0;
	   public static int 	MODE_VALIDATE = 1;
	   public static int 	MODE_CHANGE   = 2;
	   public static String MODE_OP = "Mode";

	   private static int SUBMODE_ENTERPIN    = 1;
	   private static int SUBMODE_VALIDATEPIN = 2;
	   private static int SUBMODE_CONFIRMNEW = 3;
	   

	   private static final String TAG = "Quota-PinEntry";
	   private Button butOK;
	   private Button butCancel;
	   private EditText pinTxt;
	   private int pinmode;
	   private int pin_submode;
	   private TextView header;
	   private String cpin;
	   private InputMethodManager mgr;
	   
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.pinentry);
	        
	        // Screen Controls
	        butOK = (Button) findViewById(R.id.okbutt);
	        //butCancel = (Button) findViewById(R.id.cancelbutt);
	        pinTxt = (EditText)findViewById(R.id.pinDisplay);
	        header = (TextView)findViewById(R.id.headertxt);
	        
	        
	        butOK.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	CheckPin();
	            }
	          });
	        
	        
	        // What mode are we in
	        pinmode 	= getIntent().getIntExtra(MODE_OP, MODE_CREATE);

	        if (pinmode==MODE_CREATE) {
		        pin_submode = SUBMODE_ENTERPIN;
	        } else if (pinmode==MODE_VALIDATE) {
	        	cpin=SlotManager.getInstance().pinCode;
	        	pin_submode = SUBMODE_VALIDATEPIN;
	        } else if (pinmode==MODE_CHANGE) {
	        	cpin=SlotManager.getInstance().pinCode;
	        	pin_submode = SUBMODE_VALIDATEPIN;
	        }
	        	
	        // Setup PIN
	        pinTxt.setSingleLine();
  			pinTxt.setInputType(InputType.TYPE_CLASS_PHONE);
            PasswordTransformationMethod transMethod = new PasswordTransformationMethod();
            pinTxt.setTransformationMethod(transMethod);
	        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  			
	        pinTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() { 
	            @Override 
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { 
	                if (actionId == EditorInfo.IME_ACTION_DONE) { 
	                	CheckPin();
	                } 
	                return false; 
	            } 
	        });
	        
	    }
	    
	    public void CheckPin() {
        	// MODE CREATE
        	if (pinmode==MODE_CREATE && pin_submode==SUBMODE_ENTERPIN) {
        		// Initial PIN entered - Validate
        		if (pinTxt.getText().length()<4) {
            		header.setText("PIN must be at least 4 characters");
            		pinTxt.setText("");
            		pinTxt.requestFocus();	                			
        		} else {
            		cpin = pinTxt.getText().toString();
            		header.setText("Confirm new PIN");
            		pinTxt.setText("");
            		pinTxt.requestFocus();	                			
            		pin_submode=SUBMODE_VALIDATEPIN;
        		}
        	} else if (pinmode==MODE_CREATE && pin_submode==SUBMODE_VALIDATEPIN) {
        		// Entered and Validated - Now Save PIN and Exit
        		if (cpin.equalsIgnoreCase(pinTxt.getText().toString())) {
        			// All Good Save PIN
        			SlotManager.getInstance().SavePin(cpin);
            		finish();
        		} else {
            		header.setText("Incorrect - Confirm new PIN");
            		pinTxt.setText("");
        		}
        	// MODE CHANGE
        	} else if (pinmode==MODE_CHANGE && pin_submode==SUBMODE_VALIDATEPIN) {
        		if (cpin.equalsIgnoreCase(pinTxt.getText().toString())) {
        			// Confirmed PIN - Enter new PIN
            		header.setText("Enter new PIN (blank to remove)");
        			pinTxt.setText("");
        			pin_submode=SUBMODE_ENTERPIN;
        		} else {
            		header.setText("Incorrect PIN - Retry");
            		pinTxt.setText("");
            		pinTxt.requestFocus();
        		}
        	} else if (pinmode==MODE_CHANGE && pin_submode==SUBMODE_ENTERPIN) {
        		cpin = pinTxt.getText().toString();
        		header.setText("Confirm new PIN");
        		pinTxt.setText("");
        		pinTxt.requestFocus();
        		pin_submode=SUBMODE_CONFIRMNEW;
        	} else if (pinmode==MODE_CHANGE && pin_submode==SUBMODE_CONFIRMNEW) {
        		if (cpin.equalsIgnoreCase(pinTxt.getText().toString())) {
        			// All Good Save PIN
        			SlotManager.getInstance().SavePin(cpin);
        	        mgr.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            		finish();
        		} else {
            		header.setText("Incorrect - Confirm new PIN");
            		pinTxt.setText("");
        		}
        	// MODE VALIDATE
        	} else if (pinmode==MODE_VALIDATE && pin_submode==SUBMODE_VALIDATEPIN) {
        		if (cpin.equalsIgnoreCase(pinTxt.getText().toString())) {
        			SlotManager.getInstance().lastpinCheck  = new Date();
        			//SlotManager.getInstance().backgroundapp = false;
        	        mgr.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            		finish();
        		} else {
            		header.setText("Incorrect - Retry");
            		pinTxt.setText("");
            		pinTxt.requestFocus();
        		}
        	}	
	    }

	    public boolean onKeyDown(int keyCode, KeyEvent event)  {
	       
	    	if (  keyCode == KeyEvent.KEYCODE_BACK
	              && event.getRepeatCount() == 0) {
	    		// Allow Cancel when setting up
	    		if (pinmode==MODE_CREATE) {
	                setResult(RESULT_CANCELED);
	                finish(); 	    			
	    		}
	    		
	        	// Cancel PIN Entry
	        }
	        // Validate PIN
	        
	        return true;
	    }
	    
	    @Override 
	    protected void onResume(){
	        super.onResume();
	    }
	    
}
