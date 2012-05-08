package com.southfreo.quota.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.text.DateFormat;
import android.util.Log;

public class DateUtils {
	   private static final String TAG = "DateUtils";

   public final static long ONE_SECOND = 1000;
   public final static long SECONDS = 60;
   public final static long ONE_MINUTE = ONE_SECOND * 60;
   public final static long MINUTES = 60;
   public static final long ONE_HOUR = 60 * 60 * 1000L;
   public final static long HOURS = 24;
   public final static long ONE_DAY = ONE_HOUR * 24;
   
	public static Date getDateBeforeDays(int days) {  
		long backDateMS = System.currentTimeMillis() - ((long)days) *24*60*60*1000;  
		Date backDate = new Date();  
		backDate.setTime(backDateMS);  
		return backDate;  
		}

	  public static long daysBetween(Date d1, Date d2){
		    return ( (d2.getTime() - d1.getTime()) /
	                  (ONE_HOUR * 24));

//		  return ( (d2.getTime() - d1.getTime() + ONE_HOUR) /
//	                  (ONE_HOUR * 24));
	  }
	  
	  public static String ConvertSecsToHrsMins(String val) {
			
			if (val==null) {
				return null;	
			}
			String value=val;
			value = Utils.RemoveCrap(value);
			if (value.toLowerCase().contains("seconds")) {
				// Convert Value to Minutes
				double secs = Utils.getIntegerFromString(value.replace("seconds", ""));
				return Utils.DecimalTimetoHrsMins(secs/60/60);
			} else {
				return value;	
			}
			
		}
	  
	  public static long secondsBetween(Date d1, Date d2){
		    return (d2.getTime() - d1.getTime()) /1000;
	  }
	  
	  
	  public static String HHMM(int secsIn) {

		  int hours = secsIn / 3600,
		  remainder = secsIn % 3600,
		  minutes = remainder / 60;
		  
		  if (hours==0 && remainder<60) {
			  return remainder+"s";
		  }

		  return (  hours+ "h " +  minutes + "m");
		  
		  //+ ":" + (seconds< 10 ? "0" : "") + seconds );
	  }
	  
	  public static String getAgo (Date fileDate) {
			
			String ago;
			
			if (fileDate==null) {
			   ago = "Never updated";	
			} else {
				double secondsDifference = secondsBetween(fileDate,new Date());
				
				if (secondsDifference>63113852 ) {
					// 2 years ago - Never
					ago = "Never updated";
				} else if (secondsDifference>604800) {
					ago = String.format("%s",DateMiddle(fileDate));
					
				} else if (secondsDifference>(86400*2)) {
					ago = String.format("%d days ago", (int)(secondsDifference/86400));
				} else if (secondsDifference > 3600) {
					ago = String.format("%d hours ago", (int)(secondsDifference/3600));
				} else if (secondsDifference <=10 ){
					ago = String.format("moments ago", (int)(secondsDifference/3600));
				} else if (secondsDifference <=120 ){
					ago = String.format("%d seconds ago", (int)(secondsDifference));
				} else {
					ago = String.format("%d minutes ago", (int)secondsDifference/60);
				}
			}
			
			return ago;
		}
	 
	  
	public static boolean inBetween(Date d,Date start,Date end) {
		return d.after(start) && d.before(end);
	}
	
	
	 /**
	   * converts time (in milliseconds) to human-readable format
	   *  "<dd:>hh:mm:ss"
	   */
	  public static String millisToShortDHMS(long duration) {
	    String res = "";
	    duration /= ONE_SECOND;
	    int seconds = (int) (duration % SECONDS);
	    duration /= SECONDS;
	    int minutes = (int) (duration % MINUTES);
	    duration /= MINUTES;
	    int hours = (int) (duration % HOURS);
	    int days = (int) (duration / HOURS);
	    if (days == 0) {
	      res = String.format("%dh, %dm", hours, minutes);
	    } else {
	      res = String.format("%dd, %dh, %dm", days, hours, minutes);
	    }
	    return res;
	  }
	  
	  /**
	   * converts time (in milliseconds) to human-readable format
	   *  "<w> days, <x> hours, <y> minutes and (z) seconds"
	   */
	  public static String millisToLongDHMS(long duration) {
	    StringBuffer res = new StringBuffer();
	    long temp = 0;
	    if (duration >= ONE_SECOND) {
	      temp = duration / ONE_DAY;
	      if (temp > 0) {
	        duration -= temp * ONE_DAY;
	        res.append(temp).append(" day").append(temp > 1 ? "s" : "")
	           .append(duration >= ONE_MINUTE ? ", " : "");
	      }

	      temp = duration / ONE_HOUR;
	      if (temp > 0) {
	        duration -= temp * ONE_HOUR;
	        res.append(temp).append(" hour").append(temp > 1 ? "s" : "")
	           .append(duration >= ONE_MINUTE ? ", " : "");
	      }

	      temp = duration / ONE_MINUTE;
	      if (temp > 0) {
	        duration -= temp * ONE_MINUTE;
	        res.append(temp).append(" minute").append(temp > 1 ? "s" : "");
	      }

	      if (!res.toString().equals("") && duration >= ONE_SECOND) {
	        res.append(" and ");
	      }

	      temp = duration / ONE_SECOND;
	      if (temp > 0) {
	        res.append(temp).append(" second").append(temp > 1 ? "s" : "");
	      }
	      return res.toString();
	    } else {
	      return "0 second";
	    }
	  }
	  
	
	public static Date getDateAfterDays(int days) {  
		long backDateMS = System.currentTimeMillis() + ((long)days) *24*60*60*1000;  
		Date backDate = new Date();  
		backDate.setTime(backDateMS);  
		return backDate;  
		} 
	
    public static Date Midnight(Date d) {
        return Midnight(d, TimeZone.getDefault());
    }    
 
    public static int Today() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_MONTH);	
    }
    
    public static String DateFormat(Date d,String sFormat) {
        if (d==null) return "????";
        SimpleDateFormat formatter = new SimpleDateFormat(sFormat);
    	return formatter.format(d);
    }
 
    public static String DateTime(Date d) {
        if (d==null) return "????";
              	 return DateUtils.DateFormat(d, "hh:mma");
      }
    
    public static String DateUpTime(Date d) {
        if (d==null) return "????";
   	 return DateUtils.DateFormat(d, "dd MMM - hh:mma");
   }
    
    public static String DateAbr(Date d) {
        if (d==null) return "????";
        return DateUtils.DateFormat(d, "dd MMM");
    }
    
    public static String agoDate(Date d) {
        if (d==null) return "????";
        return DateUtils.DateFormat(d, "hh:mma");
    }
    
    public static String DateLong(Date d) {
        if (d==null) return "????";
        return DateUtils.DateFormat(d, "dd/MMM/yyyy hh:mma");
      }
    

    public static String DateMiddle(Date d) {
        if (d==null) return "????";
        return DateUtils.DateFormat(d, "dd MMM yy");
   }
    
    public static String DateInternal(Date d) {
     if (d==null) return "????";
   	 return DateUtils.DateFormat(d, Utils.INTERNAL_DATE_FORMAT);
    }
    
    // Locale Functions
    public static String DateShort(Date d) {
    	if (d==null) return "????";
    	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    	return df.format(d);
    }
   
    public static long DateMills() {
    	return new Date().getTime();
    }
    
    
    public static Date DateFromString(String sdate,String aFormat)  {
    	
    	String sFormat = aFormat;
    	
    	if (sdate.toLowerCase().contains("today")) {
    		return new Date();
    	}
       	if (sdate.toLowerCase().contains("tomorrow")) {
       	    return  getDateAfterDays(1);  	
       	}
       	
       	// C = Java Tweaks
       	sFormat = sFormat.replace("hh:mma", "hh:mm a");
       	sFormat = sFormat.replace("cccc", "EEEE");
       	
    	try {
     	   SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
     	   sdf.setLenient(true);
     	   Date d = sdf.parse(sdate);
    	   Calendar cal = Calendar.getInstance();
    	   cal.setTime(d);
    	   return cal.getTime();
    	}
    	catch (Exception e) {
    		  Log.e( TAG, "DateFromString: Problem parsing " + sdate + " format [" + sFormat + "]"); 
    		  return null;
    	}
    	
    }
    
    public static Date DateFromStringTZ(String sdate,String sFormat,String tz)  {
    	try {
    	   TimeZone tzone;
    	   
    	   tzone = TimeZone.getTimeZone(tz);	
 
    	   SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
    	   sdf.setTimeZone(tzone);
     	   Date d = sdf.parse(sdate);
     	   
    	   Calendar cal = Calendar.getInstance();
    	   cal.setTime(d);
    	   return cal.getTime();
    	}
    	catch (Exception e) {
    		  Log.e( TAG, "DateFromString: Problem parsing " + sdate + " format [" + sFormat + "]"); 
    		  return null;
    	}
    	
    }
    
    public static Date Midnight(Date d, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        return c.getTime();
    }
    
    public static Date EndOfToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 0);
        
        return c.getTime();
    }
    
    public static Date MidnightToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        return c.getTime();
    }
    
}
