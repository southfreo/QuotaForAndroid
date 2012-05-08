package com.southfreo.quota.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

public class paramgroup implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int pid;
	public String name;
	public String description;
	public ArrayList<parameter> params;
	public String invalidmsg;
	public final static String TAG = "paramgroup";
	
	public paramgroup() {
		
	}
	
	public paramgroup (paramgroup OtherGroup)  {

			pid = OtherGroup.pid;
			description=OtherGroup.description;
			params = new ArrayList<parameter>();
			
			for (int j = 0;j<OtherGroup.params.size();j++) {
				parameter op = OtherGroup.params.get(j);
				
				// Create Local Parameter and Reset
				parameter lp = new parameter();
					
				lp.type = op.type;
				
				// Copy List Values
				if (op.type == parameter.param_textchoice) {
					lp.ListValues = op.ListValues.clone();
				}
				
				lp.defaultvalue = op.defaultvalue;
				lp.escape = op.escape;
				lp.pid = op.pid;
				lp.reset();
				
				params.add(lp);
				
			}
		}

	
	public parameter getParameterByID (int id) {
		
		for (int j = 0;j< params.size();j++) {
			parameter p = params.get(j);
			if (p.pid==id) {
				return p;	
			}
		}
		return null;
	}
	
	public void copyState(paramgroup otherGroup) {
		parameter lp;
		parameter op;
		
		for (int j = 0;j< params.size();j++) {
			lp = params.get(j);
			
			// Try to Get this ID from OtherGroup
			op = otherGroup.getParameterByID(lp.pid);
			if (op==null) {
				Log.e(TAG,"Could not locate paramter %d for copy"+lp.pid);
			} else {
				lp.copyState(op);
			}
		}
	}

	
	public void copyStateCheck(paramgroup otherGroup) {
		parameter lp;
		parameter op;
		
		for (int j = 0;j< params.size();j++) {
			lp = params.get(j);
			
			// Try to Get this ID from OtherGroup
			op = otherGroup.getParameterByID(lp.pid);
			if (op==null) {
				Log.e(TAG,"Could not locate paramter %d for copy"+lp.pid);
			} else {
				// Check if we are copying a secure field to an insecure one
				boolean origEscape=lp.escape;

				if (op.isSecure() && !lp.isSecure()) {
					// Don't copy
				} else {
					// OK to copy
					lp.copyState(op);
					lp.escape=origEscape;
				}
			}
		}
	}
	
	
	
	public void ResetValues() {
		for (int j = 0;j< params.size();j++) {
			parameter p = params.get(j);
			p.reset();
		}
	}
	
	
	public boolean paramExists (int iid) {
		for (int j = 0;j< (params.size());j++) {
			parameter p = params.get(j);
			if (p.pid==iid) {
				return true;	
			}
		}
		return false;
	}
	
	
	public boolean Validate() {
		for (int j = 0;j< params.size();j++) {
			parameter p = params.get(j);
			if (!p.isValid()) {
				invalidmsg = String.format("%s is not valid!",p.Name);
				return false;
			}
		}
		return true;
	}
	
	
	
	
}
