package com.southfreo.quota.model;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Utils;

public class datakey {
	
	public final static String TAG = "Quota-datakey";
	
	public int myid;
	public String name;
	public String type;		
	public String src;
	public int srcid;
	public String find;	
	public String start;
	public String end;
	public String _text;
	public String extract;
	public String format;
	public String defaultval;
	public String prefix;
	public String postfix;
	
	public int outputtype;
	public String outputformat;
	public Date   dateval;
	public String condition;
	public int  subkey;
	public ArrayList<?> strArray;
	public int pos;
	public double dvalue;
	
	// Extra Text Processing
	public boolean trimspace;
	public boolean removehtml;
	public boolean escape;
	public boolean javascript;
	
	public String removechars;
	public String replacechars;

	public String InternalText() {
		
		// Return Formatted text
		if (type.equalsIgnoreCase("NUMBER")) {
			return String.format("%.4f",dvalue);
		} else if ((type.equalsIgnoreCase("STRING"))) {
			return _text;
		} else if (type.equalsIgnoreCase("DATE")) {
			return DateUtils.DateInternal(dateval);
		}
		Log.e(TAG, String.format("XmlDataKey: ERROR: Unknown type [%s] in text",type));
		return null;
	}

	
	public String OutputText() {
		int def_output=0;
		
		// Return Formatted text
		if (type.equalsIgnoreCase("NUMBER")) {
			if (outputtype==0) {
				def_output=Utils.E_FORMAT_NUMBER;
			} else {
			    def_output=outputtype;	
			}
			if (!Utils.isBlank(outputformat)) {
				return Utils.formatValue(null,dvalue,null,def_output,outputformat,true);
			} else {
				return Utils.formatValue(null,dvalue,null,def_output,outputformat,false);
			}
		} else if ((type.equalsIgnoreCase("STRING")) || Utils.isBlank(type)) {
			if (outputtype==0) {
				def_output=Utils.E_FORMAT_STRING;
			} else {
			    def_output=outputtype;	
			}
			return Utils.formatValue(_text,0,null,def_output,outputformat,false);
		} else if (type.equalsIgnoreCase("DATE")) {
			if (outputtype==0) {
				def_output=Utils.E_FORMAT_DATE;
			} else {
			    def_output=outputtype;	
			}
			if (Utils.isBlank(outputformat)) {
				return Utils.formatValue(null,0,dateval,def_output,Utils.INTERNAL_DATE_FORMAT,false);
			} else {
				return Utils.formatValue(null,0,dateval,def_output,outputformat,false);
			}
		}
		return "?";
	}
	
	
	public boolean isEmpty() {
		return (Utils.isBlank(_text));	
	}
	
	public void setTextValue (String val) {
		// Dealloc
		
		_text = Utils.BlankString(val);
		
		// Check Extra Tags
		if (removehtml) {
			_text = Utils.RemoveCrap(_text);
		}

		if (trimspace) {
			_text = Utils.removeWhitespaceNewLine(_text);
		}
		
		if (!Utils.isBlank(replacechars)) {
			_text = Utils.ReplaceStringsFromString(_text, replacechars, "\\|\\|");
		}
		
		
		if (!Utils.isBlank(removechars)) {
			_text = Utils.RemoveStringsFromString(_text, removechars, "\\|\\|");
		}
		
		
		
		
		if (escape) {
			_text = Utils.EscapeStringHTML(_text);	
		}
		
		if (!Utils.isBlank(prefix)) {
			_text =  prefix+_text;
		}

		if (!Utils.isBlank(postfix)) {
			_text =  _text+postfix;
		}
		
		// Check for Null
		if (javascript) {
						  
			_text = String.format("%s\n%s\n%s\n%s\n%s\n%s\n",
						  "<html><script type=\"text/javascript\">",
						  "<!--",
						  _text,
						  "-->",
						  "</script></html>",
						  "");	
		}
		
		
		// Set Double value based on type
		if ((type.equalsIgnoreCase("NUMBER"))) {
			// Check Parse Format
			if (!Utils.isBlank(format) && format.equalsIgnoreCase("MB")) {
				dvalue = Utils.getMBVal(_text);	
			} else if (!Utils.isBlank(format) && format.equalsIgnoreCase("time")) {
				dvalue = Utils.getHrsFromTime(_text);
			} else {
				dvalue = Utils.getDoubleFromString(_text);	
			}
		} else if ((type.equalsIgnoreCase("STRING"))) {
			dvalue = 0;
		} else if ((type.equalsIgnoreCase("DATE"))) {
			if (Utils.isBlank(format)) {
				Log.e(TAG,"No parseformat specified");
			} else {
				// Extract Date Using Format
				dateval = DateUtils.DateFromString(_text, format);  
			}
		} else {
			Log.e(TAG,"Unknown datakey type");
	  	}
		Log.i(TAG,"XML DataKey"+toString());
	}
	
	
	public void setTextValueSum(String val) {
		if (Utils.isBlank(_text)) {
			dvalue=0;
		}
		double cval = dvalue;
		setTextValue(val);
		dvalue += cval;
	}
	
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		
		buf.append("DataKey\nName :"+name+"\n");
		buf.append("Type   "+type+"\n");
		
		if (!Utils.isBlank(_text)) {
	 		if (_text.length()>2000) {
	 			buf.append("_Text  ( > 2K) \n");
	 		} else {
	 			buf.append("_Text  "+Utils.BlankString(_text)+"\n");
	 			buf.append("OText  "+OutputText()+"\n");
	 			buf.append("IText  "+InternalText()+"\n");
	 		}
		}
		buf.append("Find   "+Utils.BlankString(find)+"\n");
		buf.append("Start  "+Utils.BlankString(start)+"\n");
		buf.append("End    "+Utils.BlankString(end)+"\n");
		buf.append("Pos    "+pos+"\n");
		buf.append("Double "+dvalue+"\n");
		buf.append("Date   "+dateval+"\n");
		buf.append("Format "+Utils.BlankString(format)+"\n");

		// Dump Array
		if (strArray!=null) {
			for (int i=0;i<strArray.size();i++) {
				buf.append(String.format("  Array [%d] = [%s]\n",i,strArray.get(i).toString()));
			}
		}
		buf.append("\n\n");

		return buf.toString();
	}
}

