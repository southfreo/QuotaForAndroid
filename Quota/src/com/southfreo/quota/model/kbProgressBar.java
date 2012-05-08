package com.southfreo.quota.model;

import java.io.Serializable;

import com.southfreo.quota.utils.Utils;

public class kbProgressBar implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	/**
	 * 
	 */

	public int pid;

	public int type;					// Type of ProgressBAR
	
	// Output Labels
	public String name;				
	public String percentage_msg;
	public String val_msg;
	
	public double percentage;
	
	// Used when Cycle Present
	public double ideal;	

	public boolean used;				// Indicates Value is used or remaining
	
	
	// Internal Stats
	public double value_max;
	public double value_value;
	public double value_used;
	public double value_remains;	
	public double value_pcent;
	public double value_used_perday;
	public double value_remain_perday;

	public String srcValue;
	public String srcMaxValue;
	public int outtype;
	public String outformat;
	
	public cycle cycle_usage;

	public String stat1;
	public String stat2;

	public String stat1long;
	
	public String RemainTxt;
	public String UsedTxt;
	
	public boolean overIdeal() {
		return (percentage>ideal && percentage>0.9 && ideal>0.9);
	}
	
	
	public void UpdateValues() {

		if (!used) {
			// The value is remaining
			if (value_max==0) {
				value_used=0;	
			} else {
				value_used      = value_max-value_value;
			}
			value_remains	= value_value;
		} else {
			value_used      = value_value;
			if (value_max==0) {
				value_remains=0;	
			} else {
				value_remains   = value_max-value_value;
			}
		}
		
		// Show Whatever was loaded...
		this.val_msg = Utils.formatValue(null,value_value,null,outtype,outformat,true);
		
		// Formatted Value
		
		// Percentage Usage
		if (value_max==0) {
			percentage = 0;
		} else {
			percentage = (value_used/value_max)*100;
		}
		
		this.percentage_msg = String.format("%1.0f%%",percentage);
		
		// Create Stat Lines
		
		this.RemainTxt = Utils.formatValue(null,value_remains,null,outtype,outformat,true);
		this.UsedTxt = Utils.formatValue(null,value_used,null,outtype,outformat,true);

		if (cycle_usage!=null && cycle_usage.hasstart && value_max>0) {

			double ideal_day = value_max/(cycle_usage.totaldays);

			// Per Day Usage
			if (cycle_usage.dayssofar==0) {
				value_used_perday=value_used;	
			} else {
				if (value_used>=value_max) {
					value_used_perday = 0;
				} else {
					value_used_perday   = java.lang.Math.abs(value_used/(cycle_usage.dayssofar+1));		// Add 1 for Today
				}
				
			}
			
			if (cycle_usage.daysremaining==0) {
				value_remain_perday = value_remains;
			} else {
				value_remain_perday = java.lang.Math.abs(value_remains/(cycle_usage.daysremaining));
			}
			
			
			// Calculate Ideal Based On Cycle
			ideal = (cycle_usage.dayssofar*ideal_day/value_max)*100;
			
			
			
			this.stat1 = 
			 String.format("Used %s (%s/day) of %s",
			  //name,
			  Utils.formatValue(null,value_used,null,outtype,outformat,true),
			  Utils.formatValue(null,value_used_perday,null,outtype,outformat,true),
			  Utils.formatValue(null,value_max,null,outtype,outformat,false));

			this.stat2 = 
			String.format("Remaining %s (%s/day)",
			 //name,
					Utils.formatValue(null,value_remains,null,outtype,outformat,true),
					Utils.formatValue(null,value_remain_perday,null,outtype,outformat,true));
			
			// Long Version
			this.stat1long = 
			
			String.format("%s/day (%s) Remaining %s/day (%s)",
			 //name,
					Utils.formatValue(null,value_used_perday,null,outtype,outformat,true),
					Utils.formatValue(null,value_max,null,outtype,outformat,false),
					Utils.formatValue(null,value_remain_perday,null,outtype,outformat,true),
					Utils.formatValue(null,value_remains,null,outtype,outformat,true)
			 );
			
			
		} else {
		    ideal=0;
			
			if (value_max==0) {
				this.stat1 = 
				Utils.BlankString(String.format("Used %s",
						Utils.formatValue(null,value_used,null,outtype,outformat,true)));

				this.stat2 = 
					Utils.BlankString(String.format("Remaining %s",
							Utils.formatValue(null,value_remains,null,outtype,outformat,true)));
				
				
				// Long Version
				this.stat1long = 
					Utils.BlankString(String.format("Used %s Remaining %s",
							Utils.formatValue(null,value_used,null,outtype,outformat,true),
							Utils.formatValue(null,value_remains,null,outtype,outformat,true)
				 ));
				
			} else {
				this.stat1 = 
					Utils.BlankString(String.format("Used %S of %s",
						Utils.formatValue(null,value_used,null,outtype,outformat,true),
						Utils.formatValue(null,value_max,null,outtype,outformat,false)));
				
				this.stat2 = 
					Utils.BlankString(String.format("Remaining %s",
							Utils.formatValue(null,value_remains,null,outtype,outformat,true)));

					this.stat1long = 
						Utils.BlankString(String.format("Used %s of %s Remaining %s",
				             Utils.formatValue(null,value_used,null,outtype,outformat,true),
				             Utils.formatValue(null,value_max,null,outtype,outformat,false),
				             Utils.formatValue(null,value_remains,null,outtype,outformat,true)
				 ));
				
			}
			
			
			
		}
	}
	
	
	
}
