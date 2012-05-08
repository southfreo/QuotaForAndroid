package com.southfreo.quota.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Utils;

public class Provider  implements Comparable<Provider>, Serializable {
	
	// Parameter Group
	public static final String TAG = "Provider";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int PROVIDER_NOTSETUP = -1;
	
	public static int method_code = 1;
	public static int method_xml = 2;
	public static int method_codeparser = 3;
	public static int URL_CACHEINTERVAL = 900;  // 15 Minutes
	
	public static int provider_status_idle=0;
	public static int provider_status_loading=1;
	public static int provider_status_parsing=2;
	
	// Display Type
	public static int  kDisplayType_ISPMOBILE=0;
	public static int  kDisplayType_WEATHER=1;
	public static int  kDisplayType_ACCOUNT=2;			
	public static int  kDisplayType_PHOTO=3;
	public static int  kDisplayType_UNUSED=4;
	
	// Failures
	public static int fail_authentication=0;
	public static int fail_parser=1;
	public static int fail_connection=2;
	public static int fail_nousagereturned=3;
	public static int fail_internalerror=4;
	public static int fail_incorrectsetup=5;
	public static int fail_accountissue=6;
	public static int fail_xmlerror=7;
	public static int fail_xmlparsing=8;
	public static int fail_nocachedata=9;
	public static int fail_noconnection=10;
	
	// Usage Values
	
	// Persistent State Values
	public int disk_isp;
	public String disk_planname;
	public boolean disk_manualrefresh;
	public paramgroup disk_parameters;
	public Hashtable<String,Object> disk_alerts;
	

	public String supportUrl;
	public String providerUrl;
	public boolean debugprovider;
	public int paramgroupid;
	public int disk_display_type;
	public int parse_method;
	public String providerName;
	public String providertype;
	public String xmlVersion;
	public String xmlAuthor;
	public String xmlDescription;
	public int cacheseconds;
	public String icon;
	public boolean secure;
	public Provider provider_definition;
	
	// Cache
	public double cacheage;
	public Date cacheFileDate;
		
	// loading
	public int loadstatus;
	public boolean loadsuccess;
	public String loadmsg;
	public int failurereason;
	public String loaderrormsg;
	
	// Internal
	public int slotnumber;
	public boolean isvalid;
	public cycle current_cycle;
	public String xmlCookie;
	
	// ISP Data
	public ArrayList<kbProgressBar> progress;
	public ArrayList<extradata> extras;

	// Account Data
	public account accountdata;
	
	public int currentURL;
	
	public ArrayList<urlinfo> urls;
	public ArrayList<datakey> datakeys;
	public ArrayList<condition> conditions;
	
	// Question in progress
	public boolean questioninProgress;
	public boolean questionAnswered;
	public int questionanswer;
	public String[] questionchoices;
	
	// 
	public int loadedFrom;			// 0 - Internal, 1 - User
	
	// Widget
	public int widgetid;
	
	public Provider() {
		// Default Values
	}

	
	public boolean hasData() {
		return cacheFileDate!=null;
	}
	
	
	public boolean notSetup() {
		return disk_isp==PROVIDER_NOTSETUP;
	}
	
	public boolean isSetup() {
		return !notSetup();
	}
	
	public String AccountSummaryText() {
		String val="";
		
		if (accountdata!=null) {
			if (accountdata.isRssFeed()) {
				if (accountdata.rssData!=null && accountdata.rssData.size()>0) {
					val = accountdata.rssData.size()+" items";
				} else {
					val = "No items";
				}
			} else {
				val = Utils.BlankString(accountdata.bal1value);
			}
		}
		return val;
	}
	
	
	public void Reset() {
		disk_isp = PROVIDER_NOTSETUP;
		provider_definition=null;
		
		disk_planname="";
		disk_manualrefresh=false;

		// Reset other Values here
		loadstatus = provider_status_idle;
		loadsuccess=true;
		
	}
	
	
	public datakey getDataKeyForID (int xID) {
		
		for (int i=0;i< datakeys.size();i++) {
			datakey dk = datakeys.get(i);
			if (dk.myid==xID) {
				return dk;	
			}
		}
		return null;
	}
	
	public datakey getDataKeyForName (String name) {
		
		if (Utils.isBlank(name)) {
			return null;	
		}
		
		String mkey = name;
		
		mkey = mkey.replace("_I_", "");
		
		String ar = Utils.RegEx(name, "[.*?]", 0);
		if (ar!=null) {
			mkey = mkey.replace(ar,"");
		}
		
		String ar2 = Utils.RegEx(name, "\\{.*?\\}", 0);
		if (ar2!=null) {
			mkey = mkey.replace(ar2, name);
		}
		
		for (int i=0;i<datakeys.size();i++) {
			datakey dk = datakeys.get(i);
			if (dk.name.equalsIgnoreCase(mkey)) {
				return dk;	
			}
		}
		return null;
	}
	
	public condition getConditionForID (String xmlId) {
		
		condition rc=null;
		
		if (conditions!=null) {
			int i=0;
			for (i=0;i<conditions.size();i++) {
				// Get Provider Key
				condition c = conditions.get(i);
				// Found Condition
				if (c.xmlid.equalsIgnoreCase(xmlId)) {
					return c;
				}
			}
		}
		return rc;
	}
	
	
	public String ExtraInfo() {

		String method="";
		if (parse_method==method_xml) {
			   method="x";	
			} else if (parse_method==method_codeparser) {
			   method="xc";	
			} else {
				method="c";	
			}
		
		// Cache in Minutes
		//String cache = "  Updates: "+DateUtils.HHMM(cacheseconds);
		//Utils.isBlank(xmlAuthor)?"":xmlAuthor
		return String.format("Version: %s%s", xmlVersion,method);
	}
	
	public String lastUpdatedWidget() {
		return "Updated: "+ DateUtils.agoDate(cacheFileDate);
	}
	
	public String lastUpdated() {
		return DateUtils.getAgo(cacheFileDate);
	}
	
	public kbProgressBar pbar1() {
		if (progress==null) return null;
		if (progress.size()<1) return null;
		return progress.get(0);
	}

	public kbProgressBar pbar2() {
		if (progress==null) return null;
		if (progress.size()<2) return null;
		return progress.get(1);
	}
	

	public boolean p1OverIdeal() {
   	 	kbProgressBar pb = pbar1();
   	 	if (pb==null) return false;
		return (pb.overIdeal());
	}

	private String percMsg(kbProgressBar pb,int perc) {
   	 	if (pb==null) return null;
   	 	if ((int)pb.percentage>perc) {
   	 		return String.format("%s Alert! %d%% > %d%%",pb.name,(int)pb.percentage,perc);
   	 	}
   	 	return null;
	}
	
	public String p1OverPercent(int perc) {
 		return percMsg(pbar1(),perc);
	}

	public String p2OverPercent(int perc) {
 		return percMsg(pbar2(),perc);
	}

	
	public void setPrepayMoney(String edate,String format,double credit,double creditMax) {
		
	  	current_cycle = new cycle();
	  	
    	current_cycle.initWithEndOnly(edate, format);
    	current_cycle.hasstart=false;
    	

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Credit";
    	pb.value_max = creditMax;
    	pb.value_value = credit;
    	pb.used = false;
    	pb.outtype = Utils.E_FORMAT_CURRENCY;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
    
	}
	

	public void setPrepayData(String edate,String format,double credit,double creditMax,double data,double datamax) {
		
	  	current_cycle = new cycle();
	  	
    	current_cycle.initWithEndOnly(edate, format);
    	current_cycle.hasstart=false;
    	

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Credit";
    	pb.value_max = creditMax;
    	pb.value_value = credit;
    	pb.used = false;
    	pb.outtype = Utils.E_FORMAT_CURRENCY;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
    	
     	kbProgressBar pb2 = new kbProgressBar();
       	progress.add(pb2);
 
       	pb2.pid = 1;
       	pb2.type=0;
       	pb2.name = "Data";
       	pb2.value_max = datamax;
       	pb2.value_value = data;
       	pb2.used = false;
       	pb2.outtype = Utils.E_FORMAT_DATA_MB;
       	pb2.cycle_usage = current_cycle;
    	pb2.UpdateValues();
 
	}

	public void setPrepayDataOnly3Mob(String edate,String format,double data,double datamax) {
		
	  	current_cycle = new cycle();
	  	
	  	try {
	    	current_cycle.initWithEndOnly(edate, format);
	    	current_cycle.hasstart=false;
	  	} catch (Exception e) {
	  		// Bad bad
	  	}
    	

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Credit";
    	pb.value_max = datamax;
    	pb.value_value = data;
    	pb.used = false;
    	pb.outtype = Utils.E_FORMAT_DATA_MB;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
    	
     	kbProgressBar pb2 = new kbProgressBar();
       	progress.add(pb2);
 
	}

	public void setProgressPeak(int sday,double pv,double pmx) {
		
	  	current_cycle = new cycle();
	  	
    	current_cycle.initWithAnniversaryDay(sday);

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Usage";
    	pb.value_max = pmx;
    	pb.value_value = pv;
    	pb.used = true;
    	pb.outtype = Utils.E_FORMAT_DATA_MB;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
 	}
	
	public void setProgressPeakStart(String sdate,double pv,double pmx) {
		
	  	current_cycle = new cycle();
	  	
    	current_cycle.initWithStartDate(sdate, "yyyy-MM-dd");

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Usage";
    	pb.value_max = pmx;
    	pb.value_value = pv;
    	pb.used = true;
    	pb.outtype = Utils.E_FORMAT_DATA_MB;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
 	}
	
	public void setProgressPeak(String sdate,double pv,double pmx) {
		
	  	current_cycle = new cycle();
	  	
    	current_cycle.initWithEndDate(sdate, "yyyy-MM-dd");

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Usage";
    	pb.value_max = pmx;
    	pb.value_value = pv;
    	pb.used = true;
    	pb.outtype = Utils.E_FORMAT_DATA_MB;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
 	}
	
    public void setPostpayPeakOffPeak(int sd,double pv,double pmx,double opv,double opm) {
		
	  	current_cycle = new cycle();
    	current_cycle.initWithAnniversaryDay(sd);

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Credit";
    	pb.value_max = pmx;
    	pb.value_value = pv;
    	pb.used = true;
    	pb.outtype = Utils.E_FORMAT_CURRENCY;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
    	
    	// 2 
       	kbProgressBar pb2 = new kbProgressBar();
       	progress.add(pb2);
 
       	pb2.pid = 1;
       	pb2.type=0;
       	pb2.name = "Data";
       	pb2.value_max = opm;
       	pb2.value_value = opv;
       	pb2.used = true;
       	pb2.outtype = Utils.E_FORMAT_DATA_MB;
       	pb2.cycle_usage = current_cycle;
       	pb2.UpdateValues();
	}

    
    
	public void setProgressPeakOffPeak(int sd,double pv,double pmx,double opv,double opm) {
		
	  	current_cycle = new cycle();
    	current_cycle.initWithAnniversaryDay(sd);

    	progress = new ArrayList<kbProgressBar>();
	  	
    	kbProgressBar pb = new kbProgressBar();
       	progress.add(pb);
        
       	pb.pid = 0;
    	pb.type=0;
    	pb.name = "Peak";
    	pb.value_max = pmx;
    	pb.value_value = pv;
    	pb.used = true;
    	pb.outtype = Utils.E_FORMAT_DATA_MB;
    	pb.cycle_usage = current_cycle;
    	pb.UpdateValues();
    	
    	// 2 
       	kbProgressBar pb2 = new kbProgressBar();
       	progress.add(pb2);
 
       	pb2.pid = 1;
       	pb2.type=0;
       	pb2.name = "OffPeak";
       	pb2.value_max = opm;
       	pb2.value_value = opv;
       	pb2.used = true;
       	pb2.outtype = Utils.E_FORMAT_DATA_MB;
       	pb2.cycle_usage = current_cycle;
       	pb2.UpdateValues();
	}
	
	
	public boolean p2OverIdeal() {
   	 	kbProgressBar pb = pbar2();
   	 	if (pb==null) return false;
		return (pb.overIdeal());
	}
		
		
	public String  getFailureCode() {

		String fr = "Unknown";	

		if (!loadsuccess) {
			if (failurereason==fail_parser) {
				fr = "Parsing problem";	
			} else if (failurereason==fail_authentication) {
				fr = "Authentication problem";	
			} else if (failurereason==fail_nousagereturned) {
				fr = "No data returned";	
			} else if (failurereason==fail_internalerror) {
				fr = "Internal problem";	
			} else if (failurereason==fail_connection) {
				fr = "Connection problem";	
			} else if (failurereason==fail_incorrectsetup) {
				fr = "Settings error";	
			} else if (failurereason==fail_accountissue) {
			    fr = "Account issue";	
			} else if (failurereason==fail_xmlerror) {
			    fr = "XML Config error";	
			} else if (failurereason==fail_xmlparsing) {
			    fr = "XML parser error";	
			} else if (failurereason==fail_nocachedata) {
			    fr = "Provider update required";	
			} 
		} else {
			// Check if Cache Out of Date
			if (!hasData()) {
			   fr = "Provider update required";	
			} else {
				fr = DateUtils.getAgo(cacheFileDate);	
			}
		}
		
		return fr;
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void clearURLs(boolean initTemplate) {
		if (urls!=null) {
			urls.clear();
		} else {
			currentURL = 0;
			urls = new ArrayList<urlinfo>();
		}
		if (initTemplate) {
			urls = (ArrayList<urlinfo>)provider_definition.urls.clone();
		}
	}

	public void clearProgress() {
		if (progress!=null) {
			progress.clear();
		} else {
			progress = new ArrayList<kbProgressBar>();
		}
	}
	
	
	public void clearDataKeys() {
		if (datakeys!=null) {
			datakeys.clear();
		} else {
			   datakeys = new ArrayList<datakey>();
		}
	}
		
	public void clearExtras() {
		if (extras!=null) {
			extras.clear();
		} else {
			extras = new ArrayList<extradata>();
		}
	}
	
	
	public parameter getParameterByID (int i) {
		if (disk_parameters==null) {
			Log.e(TAG,"getParameterByID "+i);
			return null;
		} else {
			return (disk_parameters.getParameterByID(i));
		}
	}
	
	
	public void addExtra(int type,String name,String value) {
		if (extras==null) {
			extras = new ArrayList<extradata>();
		}
		extradata ed = new extradata();
		ed.type=type;
		ed.name= name;
      	ed.value = Utils.BlankString(value);
      	extras.add(ed);
 	}
	
	
	public void updateCacheFileDate() 
	{
		if (cacheFileDate==null) {
			// Never Updated
			cacheage = Double.MAX_VALUE;
		} else {
			// Calculate Cache Age in seconds from Now
			cacheage =  DateUtils.secondsBetween(cacheFileDate, new Date());
		}
	}
	
	public int CacheSeconds() {
		parameter p = this.getParameterByID(ProviderManager.CACHE_PERIOD_PARAMETER);
		if (p==null) {
			return provider_definition.cacheseconds;
		} else {
			int v = (int)p.numberval;
			if (v>0) {
				return v;
			} else {
				return 900;
			}
		}
	}
	
	public boolean hasCacheExpired() {
		updateCacheFileDate();
		return cacheage>CacheSeconds();
	}
	
	public boolean hasExtras() {
		if (this.extras!=null && this.extras.size()>0) {
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(Provider another) {
		// TODO Auto-generated method stub
		return this.providerName.compareToIgnoreCase(another.providerName);  
	}
	
	
	   public void writeCachedData(ObjectOutputStream oos) throws IOException {
		
		   oos.writeObject(new Date());
		   
			 if (provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
				 oos.writeObject(this.progress);
				 oos.writeObject(this.extras);
				 oos.writeObject(this.current_cycle);
				 
			 } else if (provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
				 oos.writeObject(this.accountdata);
			 }
	   }
	   
	   
	    @SuppressWarnings("unchecked")
		public Provider readCachedData(ObjectInputStream ois) throws IOException,ClassNotFoundException {
	    	try {
	    		Provider p = new Provider();
				Date lc = (Date)ois.readObject();
	    		
			 if (provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
		    	 // Read ISP Data
				 p.progress = (ArrayList)ois.readObject();
		    	 p.extras = (ArrayList)ois.readObject();
		    	 p.current_cycle = (cycle)ois.readObject();
		    	 // Attach to current Provider
		    	 this.progress=p.progress;
		    	 this.extras=p.extras;
		    	 this.current_cycle=p.current_cycle;
		    	 
			 } else if (provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
				 // Read Account Data
				 p.accountdata = (account)ois.readObject();
				 
				 // Copy
				 this.accountdata=p.accountdata;
				 
				 
			 }
	    	 this.cacheFileDate=lc;
			 
			 return p;
			 
	    	} catch (Exception e) {
	    		Log.e(TAG,"Could not read Provider Cached data");
	    		// Ignore Caching problem
	    		return null;
	    	}
	    }
	    
		// Persist Data
	   private void writeObject(ObjectOutputStream oos) throws IOException {
	        
		    oos.writeInt(disk_isp);
		    oos.writeObject(disk_planname);
		    oos.writeBoolean(disk_manualrefresh);
		    oos.writeObject(disk_parameters);
		    oos.writeObject(disk_alerts);
		    
		    //
	    }

	    @SuppressWarnings("unchecked")
		private void readObject(ObjectInputStream ois) throws IOException,ClassNotFoundException {
	        
	        int pid = ois.readInt();
	        // Reset 
	        ProviderManager.getInstance().ResetProvider(this, pid);
	        
	        disk_planname = (String)ois.readObject();
	        disk_manualrefresh = (boolean)ois.readBoolean();
	        disk_parameters = (paramgroup)ois.readObject();
	        disk_alerts = (Hashtable<String,Object>)ois.readObject();
	        
	    }
	    
	    // JSON
	    public JSONObject toJSON() {
	    	
	    	JSONObject obj=new JSONObject();
	    	try {
		    	obj.put("id",disk_isp);
		    	obj.put("plan_name",disk_planname);
		    	obj.put("manual_refresh",disk_manualrefresh);
		    	
		    	// Parameters
		    	JSONArray params = new JSONArray();
		    	for (int i=0;i<disk_parameters.params.size();i++) {
			    	JSONObject po=new JSONObject();
		    		parameter p = disk_parameters.params.get(i);
		    		po.put("id",p.pid);
		    		po.put("value",p.CurrentValueAsInternalString());
		    		params.put(i,po);
		    	}
		    	obj.put("parameters", params);
		    	// Alerts
            	if (disk_alerts!=null && disk_alerts.size()>=3) {
            		obj.put("alert_ideal", ((Boolean)disk_alerts.get("ideal")).booleanValue());
            		obj.put("alert_p1",((Integer)disk_alerts.get("p1")).intValue() );
            		obj.put("alert_p2",((Integer)disk_alerts.get("p2")).intValue() );
            	}
		    	return obj;
	    	} catch (Exception e) {
	    		Log.e(TAG,"toJSON() Provider exception"+e.toString());
	    		return null;
	    	}
	    }
	
}

