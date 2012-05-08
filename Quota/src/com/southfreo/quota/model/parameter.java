package com.southfreo.quota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import android.util.Log;

import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Utils;

public class parameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public final static int NUMBER_BLANK =-9999;
	
	public final String TAG = "parameter";
	

	public final static int param_textchoice=0;				// Displays a List of Values, Only 1 Can be choosen
	public final static int param_currency=1;				// Display number with Decimal keyboard
	public final static int param_decimal=2;				// Displays number with Decimal keyboard
	public final static int param_number=3;					// Normal Number, no decimals
	public final static int param_text=4;					// Simple text Value
	public final static int param_dateval=5;				// Date Value with Selector
	public final static int param_textchoicemulti=6;		// Displays a List of Values, Multiples Allowed
	public final static int param_dictionary=8;				// Displays Spinner with Multiple Values
	public final static int param_pinnumber=9;				// Displays Number, hidden display
	public final static int param_password=10;				// Displays Text, hidden display
	public final static int param_range=11;					// Displays Number with Spinner selector
	public final static int param_numberstr=12;				// Treated as String, Shown as Number
	public final static int param_information=13;			// Information text only
	public final static int param_selector=14;				// Execute the selector
	public final static int param_seconds=15;				// Execute the selector

	public int pid;										// ID must be unique
	public String Name;									// Name of Entry
	public String ExtraInfo;							// Used by ChoiceDict to show more Info
	
	public String Value;								// String Value - used for persistence to disk

	public String defaultvalue;							// Default Value
	
	public Object refobject;							// Reference Object
	
	// State
	public String CurrentSelectedValues[];				// Internal selected values
	public double numberval;							// Internal Number Value
	public parameter currentparameter;					// Used by Dictionary
	public Date   DateValue;							// Internal Value as Date

	public String description;							// Description Shown
	public String valid;								// Validation String
	//public ArrayList<String>  ListValues;				// Choices
	public String ListValues[];							// Choices
	public String icon;									// Used for ChoiceDict 
	public String formatString;							// How to Format String
	
	public Hashtable<String,Object> DictValues;			// List of Available Options
	public String blankmsg;								// Shows this when blank
	public int type;									// Type of Parameter
	public Boolean optional;							// Optional
	public int startrange;								// Used for ranges, Internal
	public int endrange;								// Used for ranges, Internal
	
	public boolean confirmexistingvalue;			// Used for Pin Entry
	
	public boolean passesvalidation;
	public boolean escape;
	
	// Constructor
	public parameter() {
		//CurrentSelectedValues = new ArrayList<String>();
	}
	
	
	public void setValidationString(String v) {
		valid=v;
		if (type==param_range && valid!=null) {
			String rn[] = valid.split("\\.\\.");
			if (rn.length!=2) {
				Log.e(TAG,String.format("Internal error with range string [%s]",valid));	
			}
			startrange = Utils.getIntegerFromString(rn[0]);
			endrange = Utils.getIntegerFromString(rn[1]);	
		}
	}
	
	public boolean useNumberEntry() {
		
		boolean isIt= (type==param_numberstr ||
				type==param_pinnumber ||
				type==param_range ||
				type==param_decimal ||
				type==param_currency ||
				type==param_number ||
				type==param_seconds);
		
		//Log.i(TAG,"param"+Name+" NumberEntry"+isIt);
		
		return isIt;
		
	}

	public boolean isNumberZero() {
		return (type==param_number ||
				type==param_range);
	}

	public boolean isSecure() {
		return (type==param_pinnumber ||
				type==param_password);
	}

	public boolean isNumber() {
		  
		return (type==param_decimal  || 
				type==param_currency ||
				type==param_number ||
				type==param_range ||
				type==param_seconds);
	}
	
	
	public boolean isText() {
		return (type==param_textchoice ||
				type==param_textchoicemulti ||
				type==param_text ||
				type==param_text ||
				type==param_password ||
				type==param_dictionary ||
				type==param_selector ||
				type==param_numberstr ||
				type==param_information ||
				type==param_pinnumber );
	}
				

	public boolean isDate() {
		return (type==param_dateval);
	}
	
	public void reset() {
	  	
		CurrentSelectedValues=null;
		numberval=NUMBER_BLANK;
		currentparameter=null;
		DateValue=null;
		Value="";
	}
	
	public void copyState(parameter source) {
		reset();
		
		if (source.CurrentSelectedValues!=null){
			CurrentSelectedValues = source.CurrentSelectedValues.clone();
		}
		
		numberval 	 = source.numberval;
		if (source.DateValue!=null) {
			DateValue 	 = (Date)source.DateValue.clone();
		}
		if (source.Value!=null) {
			Value 	  	 = new String(source.Value);
		}
		escape    	 = source.escape;
		if (source.defaultvalue!=null) {
			defaultvalue = new String(source.defaultvalue);
		}
		
	}

	
	public boolean isBlank() {

		
		if (type==param_textchoice) {
			return (numberval==NUMBER_BLANK);
		}
		
		if (type==param_textchoicemulti) {
		    return 	(CurrentSelectedValues==null || (CurrentSelectedValues.length==0));
		}
		
		
		if (isText()) {
			return (Value==null || Value.length()==0);
		}
		
		if (isNumber()) {
			return (numberval==NUMBER_BLANK);
		}
		if (isDate()) {
		    return DateValue==null;	
		}
		return false;
	}
	
	public void setNumberString(String val) {
		if (Utils.isBlank(val)) {
			numberval=NUMBER_BLANK;	
		} else {
			numberval=Utils.getDoubleFromString(val);	
		}
	}
	
	public void setTextValue(String val) {
		if (isNumber()) {
			setNumberString(val);	
		} else {
		    Value = val;	
		}
	}
	
	
	public void checkDefault() {
		if (isBlank() || (type==param_textchoicemulti)) {
			if (defaultvalue!=null && defaultvalue.length()>0) {
				// Set Default
				if (isText()) {
					if (type==param_textchoice) {
					    numberval = Utils.getIntegerFromString(defaultvalue);
					} else if (type==param_textchoicemulti) {
						CurrentSelectedValues = defaultvalue.split("\\|");
					}else {
						this.Value = defaultvalue;	
					}
				} else if (isNumber()) {
					setNumberString(defaultvalue);
				}
			}
		}
	}
	
	public String  CurrentValueWithEscape() {
		if (isBlank()) {
			return "";
		} else {
			String v = CurrentValueAsInternalString();
			if (escape) {
				return Utils.EscapeStringHTML(v);	
			} else {
				return v;	
			}
		}
	}
	
	public String CurrentValueAsString() {
		return Value;	
	}
	
	public boolean isSelected (String name)  {
		for (int i=0;i<this.CurrentSelectedValues.length;i++) {
		   	if (CurrentSelectedValues[i].equalsIgnoreCase(name)) {
				return true;	
			}
		}
		return false;
	}
	
	public boolean isSelectedVal (int no)  {
		// Get the List value
		if (ListValues==null) {
			return false;	
		}
		if (no>this.ListValues.length) {
			return false;
		}
		String name = ListValues[no];
		return isSelected(name);
	}
	
	public String CurrentValueAsInternalString() {
		
		switch (type) {
			
			// Numbers
			case param_decimal:
			{
				return String.format("%g",numberval);
			}
			case param_currency:	
			{
				return String.format("%.2f",numberval);
			}
			case param_number:
			case param_seconds:
			case param_range:{
				return String.format("%.0f",numberval);
			}
				
			case param_textchoice:
			case param_textchoicemulti:
			{
				// Convert Selected values to a string
				return CurrentValueAsFormattedString();
			}
				
			case param_dictionary: {
				// Get Dictionary Value
				break;
			}
				
			case param_dateval: {
			    return DateUtils.DateInternal(DateValue);
			}
				
			case param_text:
			case param_password:
			case param_pinnumber: 
			case param_numberstr:
			{
			    return Value;	
			}
			
				
			default: {
				return "CVAS Unknown!";
			}
		}
		return "CVAS Unknown!";
	}

	
	public boolean isValid() {
		Log.i(TAG,String.format("Validating %s Valid:[%s] Value:%s",Name,valid,Value));
		
		checkDefault();
		
		// Nothing Specified
		
		if (isBlank() && optional) {
			return true;	
		}
		
		// Don't Validate these Types
		if (type==param_dictionary) {
			return true;	
		}
		
		if (type==param_selector) {
			return true;	
		}
		
		// Range Value should not be blank if not Optional
		if ( (type==param_range || type==param_textchoice || type==param_textchoicemulti) 
			  && !isBlank())   {
			return true;	
		}
		
		
		if (isBlank() && !optional) {
			return false;	
		}
		
		String val=CurrentValueAsInternalString();
		
		// Hack as Android XML Parser seems to be removing "\" on attributes for some reason
		if (type==param_currency || type==param_decimal) {
			return Utils.isNumber(val);
		}
		
		
		boolean match=val.matches(valid);
		
		passesvalidation=match;
		
		return match;
	}
	

	public boolean isValid (String proposed) {
		// Nothing Specified
		
		if ( Utils.isBlank(proposed)  && optional ) {
			return true;	
		}
		
		if (type==param_currency) {
			return Utils.isNumber(proposed);
		}
		
		// Check Range Parameter (Additional as entry is a text box)
		if (type==param_range) {
			int no=Utils.getIntegerFromString(proposed);
			return !(no<startrange || no> endrange);
		}

		
		return proposed.matches(valid);
	}
	
	public String CurrentValueAsFormattedString() {

		String fsdefault=null;
		
		try {
			// Check/Setup Default Value
			checkDefault();
			
			
			if (isNumber()) {
				
				if (numberval==NUMBER_BLANK) {
					if (blankmsg!=null) {
						return blankmsg;
					} else {
						return "";	
					}
				}
				
				if (isSecure()) {
					return "Hidden";
				} 
				
				if (type==param_currency) {
					return Utils.currencyvalue(numberval,2);	
				}
				
				if (type==param_seconds) {
					return DateUtils.HHMM((int)numberval);
				}
				
				// Add Currency and Interest Formatters..
				if (isNumberZero()) {
					fsdefault="%.0f";
				} else {
					if (type==param_decimal) {
						fsdefault="%g";
					} else {
						fsdefault="%.2f";
					}
				}
				
				
				String fs=formatString.toUpperCase();
				int fv=Utils.E_FORMAT_NUMBER;
				double val=numberval;
				
				if (formatString!=null) {
					// Check if it's Special
					if (fs.equalsIgnoreCase("DAYVALUE")) {
						fv=Utils.E_FORMAT_DAYMONTH;
					} else if (fs.equalsIgnoreCase("DATA")) {
						fv=Utils.E_FORMAT_DATA;
						val *=1073741.824;	
					} else if (fs.equalsIgnoreCase("DATASIMPLE")) {
						fv=Utils.E_FORMAT_DATA_SIMPLE;
						val *=1000000;	
					}
					
					fsdefault=formatString;	
				}
				//
				// Add Optional Decimal Places to Parameter
				//
				return Utils.formatValue(null,val,null,fv,fsdefault,false);
				
			} else if (isDate()) {
				if (DateValue==null) {
					return blankmsg;
				} else {
					String df=(formatString==null?Utils.INTERNAL_DATE_FORMAT:formatString);
					return Utils.formatValue(null,0,DateValue,Utils.E_FORMAT_DATE,df,false);	
				}
				
			} else if (isText()) {
				
				
				String ev=Value;
				
				if (isBlank() && (type!=param_textchoicemulti)) {
					if (blankmsg!=null) {
						return blankmsg;
					} else {
						ev = "";	
					}
				}
				
				if (isSecure()) {
					return "Hidden";
				} 
				
				if (type==param_textchoice) {
					if (ListValues!=null && ListValues.length>numberval) {
						ev = ListValues[(int)numberval];
					} else {
						ev="";	
					}
				}
				if (type==param_textchoicemulti) {
					if (CurrentSelectedValues!=null) {
						ev = String.format("%d selected",CurrentSelectedValues.length);
					} else {
						ev="";	
					}
				}
				if (type==param_dictionary) {
					ev = currentparameter.Name;
				}
				
				String df=formatString==null?"%s":formatString;
				return Utils.formatValue(ev,0,null,Utils.E_FORMAT_STRING,df,false);
				
			} else {
				return "Unknown!!";	
			}
		}
		catch (Exception e) {
			return "?";
		}
	}

	
	
	}
