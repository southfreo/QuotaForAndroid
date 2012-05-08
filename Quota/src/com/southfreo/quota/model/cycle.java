package com.southfreo.quota.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Utils;

public class cycle  implements Serializable {
	private static final long serialVersionUID = 1;

	public int typecycle;
	
	// Flags Control Output
	public boolean hasstart;
	public boolean includeend;
	
	// Period/Calendar Variables
	public Date startDate;
	public Date endDate;
	public Date nextPeriodDate;
	
	public int startday;
	public int dayssofar;
	public int daysremaining;
	public int totaldays;
	
	public double dayspercentage;
	
	// XML Loading Keys
	public int pid;
	public String srcStartDate;
	public String srcEndDate;
	public String srcStartDay;

	
	   public void initWithDates(Date startDate,Date endDate, boolean includeEnd) {

		   this.startDate = startDate;
		   this.endDate = endDate;
		   this.includeend = includeEnd;
		   
		   if (includeend) {
			    // The end Date Really is the End Date
			   Calendar c = Calendar.getInstance();
			   c.setTime(endDate);
			   c.add(Calendar.DATE,1);
			   this.nextPeriodDate = c.getTime();
		   } else {
			    // The End Date is really start of Next Period and Real end date is -1
			   this.nextPeriodDate = this.endDate;
			   Calendar c = Calendar.getInstance();
			   c.setTime(endDate);
			   c.add(Calendar.DATE,-1);
			   this.endDate = c.getTime();
		   }
		   // Start Day
		   Calendar c = Calendar.getInstance();
		   c.setTime(startDate);
		   startday = c.get(Calendar.DAY_OF_MONTH);
		   
		   // Check +1
		   daysremaining = (int)DateUtils.daysBetween(DateUtils.MidnightToday(), endDate)+1;
		   dayssofar = (int)DateUtils.daysBetween(startDate,DateUtils.EndOfToday() );
			
		   this.totaldays = dayssofar+daysremaining;
		   dayspercentage = (double)dayssofar/(double)totaldays*100;
		   
	   }
	   

	   public void initWithEndOnly(String sdate, String format) {
		   
		   endDate = DateUtils.DateFromString(sdate, format);
		   hasstart=false;
		   daysremaining = (int)DateUtils.daysBetween(DateUtils.MidnightToday(), endDate);
		   this.totaldays=daysremaining;
		   dayssofar = 0;
		   
	   }
	   
	   public void initWithEndDate(String sdate, String format) {
		   
		   endDate = DateUtils.DateFromString(sdate, format);
		   if (endDate!=null) {
			   Calendar c = Calendar.getInstance();
			   c.setTime(endDate);
			   c.add(Calendar.DATE,1);
			   startDate = c.getTime();
			   // Get Day
			   int d = c.get(Calendar.DAY_OF_MONTH);
			   this.initWithAnniversaryDay(d);
		   }
	   }

	   public void initWithStartDate(String sdate, String format) {
		   
		   startDate = DateUtils.DateFromString(sdate, format);
		   if (startDate!=null) {
			   Calendar c = Calendar.getInstance();
			   c.setTime(startDate);
			   startDate = c.getTime();
			   // Get Day
			   int d = c.get(Calendar.DAY_OF_MONTH);
			   this.initWithAnniversaryDay(d);
		   }
	   }

	   public void initWithStringDates(String sdate, String endDate,String format, boolean endIncluded) {

		   
		   if (Utils.isBlank(sdate)) {
			   
			   if (!Utils.isBlank(endDate)) {
				  this.initWithEndOnly(endDate, format);
			   }
			   
		   } else {
			   Date sd = DateUtils.DateFromString(sdate, format);
			   Date ed = DateUtils.DateFromString(endDate, format);
			   
			   if (sd!=null && ed!=null) {
				  this.initWithDates(sd, sd, endIncluded);   
			   }
		   }
		   
	   }
	   
	   
	   
	   
	// InitWithAniversaryDay
	   public void initWithAnniversaryDay(int start) {
		   
		   // Possibly check timezone
		   
		   Calendar c = Calendar.getInstance();
		   
		   c.set(Calendar.DAY_OF_MONTH,start);
		   
		   int today=DateUtils.Today();

		   if (today>=start) {
			   // Add 1 month
			   c.add(Calendar.MONTH,1);
			   // Subtract 1 from Start Date
		   }

		   //  End Date
		   c.add(Calendar.DATE, -1);
		   endDate = c.getTime();
		   
		   // Find Start Date
		   c.add(Calendar.DATE,1);
		   c.add(Calendar.MONTH, -1);
		   startDate = c.getTime();
		   
		   hasstart=true;
		   this.initWithDates(startDate,endDate,true);
		   
	   }




	   public String getCycleText() {
			
			if (!hasstart) {
				if (daysremaining<0) {
					return String.format("Expired %d days ago (%s)",java.lang.Math.abs(daysremaining),DateUtils.DateAbr(nextPeriodDate));
					
				} else {
					return  String.format("Expires in %d Days (%s)",daysremaining,DateUtils.DateShort(endDate));
				}
				
			} else {
				if (daysremaining==1) {
					return String.format("Today is the last day of cycle");	
				} else {
					if (daysremaining<0) {
						return String.format("Expired %d days ago (%s)",java.lang.Math.abs(daysremaining),DateUtils.DateAbr(nextPeriodDate));
					} else {
						return String.format("Reset in %d days (%s)",
								daysremaining,DateUtils.DateAbr(nextPeriodDate));
					}
					
				}
			}
		}

	   

	   public String  getCycleTextAlternate() {
			if (!hasstart) {
				return this.getCycleText();
			} else {
				return String.format("Today is day %d of %d",
						dayssofar+1,				
						totaldays
						);
			}
		}

	   
		public String getCycleTextAlternate2() {
			if (!hasstart) {
				return "";
			} else {
				return String.format("%s until %s",
						DateUtils.DateMiddle(startDate),
						DateUtils.DateMiddle(endDate)
						);
			}
		}
	
	
}
