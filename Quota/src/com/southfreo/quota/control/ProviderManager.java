package com.southfreo.quota.control;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.southfreo.R;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.account;
import com.southfreo.quota.model.condition;
import com.southfreo.quota.model.cycle;
import com.southfreo.quota.model.datakey;
import com.southfreo.quota.model.extradata;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.model.paramgroup;
import com.southfreo.quota.model.urlinfo;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.utils.xmlUtils;
import com.southfreo.quota.xml.TagNode;
import com.southfreo.quota.xml.TextNode;
import com.southfreo.quota.xml.TreeBuilder;
import com.southfreo.quota.xml.TreeNode;
import com.southfreo.quota.xml.TreeToString;
import com.southfreo.quota.xml.TreeToXML;



public class ProviderManager extends Observable {
    private static ProviderManager INSTANCE = null;
	private static final String TAG = "Quota-ProviderManager";

	   public static final int CACHE_PERIOD_PARAMETER    = 901;

	  public  final static int PM_EVENT_UPDATE     =1;
	  public  final static int PM_EVENT_COMPLETE   =2;
	  public  final static int PM_EVENT_FAIL       =3;
	  public  final static int PM_LOADING_START    =4;
	  public  final static int PM_LOADING_COMPLETE =5;
	  public  final static int PM_WIDGET_UPDATE    =6;
	  public  final static int PM_EVENT_IGNORED    =7;
	  public  final static int PM_EVENT_ASKQUESTION=8;
	  
	  public final static int LOCAL_DATA_USAGE = 800;
	  
    // Internal Vars
	public String xmlLoadMsg;
	
	public HashMap<String,ArrayList<Provider>> types;				// Dictionary of All Provider Types/Providers
	public ArrayList<paramgroup>paramgroups;			// Array of all Parameter groups
	public String[] typesArray;
	

	//public HashMap<String,Object>paramtypes;			// Providers in Parameter format

	// Queue Management
	public ArrayList<Provider>		q_pendingupdates;
	
	public int		 q_pendingcount;
	public int		 q_activecount;
	public int		 q_maxconnections;
	
	public ArrayList<Provider>		q_activeupdates;
	
	//GCMathParser *mparser;
	public StringBuffer parselog;
	
	//<JSEvalulator> jsobject;
	
	public int xmlversion;

	public boolean updateavailable;
	public boolean providersloaded;
	
	//UpdateChecker *updatecheck;
	//UserUpdate *userupdates;

	//<PopupPickerNotify> popPickerDelegate;

	// iPhone Specific
	public Date lastAlertMessageTime;
	
	public int edit_provider_id;				// Used when Choosing a provider
	public Provider edit_provider_object;
	public int edit_provider_slot;
	public QuotaService mQuotaService;
	
	
	  public class NotifyUpdate 
	    { 
		  
		   public Provider p;
		   public int msg;
		   
		   public NotifyUpdate(int msg,Provider p) {
			   this.p=p;
			   this.msg=msg;
		   }
		   
	    } 
	  
	  
    private ProviderManager() {
       // Exists only to defeat instantiation.
    	
    	// Load Default providers
    	types = new HashMap();
    	paramgroups = new ArrayList();
    	
		// Queue to ManageRequests
		q_pendingupdates = new ArrayList();
		q_pendingcount = 0;
		q_activecount  = 0;
		q_activeupdates =  new ArrayList();
		q_maxconnections = 3;					// 3 Normal
		
		lastAlertMessageTime=null;
		
		// Set ProvidersLoaded to false
		providersloaded = false;
    }
    
    
    public static ProviderManager getInstance() {
       if(INSTANCE == null) {
          INSTANCE = new ProviderManager();
       }
       return INSTANCE;
    }

    public void createTypesAndSort() {
        typesArray = (String[])( types.keySet().toArray( new String[types.size()] ) );
        Arrays.sort(typesArray);
    }
    
    

    
    public void ResetProvider (Provider isp, int iid) {
    	
    	// Reset Provider, Load XML definition etc.
    	isp.Reset();
    	
    	// Clear Cache
    	isp.disk_isp=iid;
    	
    	if (!isp.notSetup()) {
        	// Get the XML Definition and Attach
        	isp.provider_definition = getProviderDefinition(iid);
        	
        	Provider xmlDefinition = isp.provider_definition;
        	
        	// Copy Values from Definition
        	isp.providerName = xmlDefinition.providerName;
        	isp.icon = xmlDefinition.icon;
        	
        	// Default Plan Name
        	isp.disk_planname = xmlDefinition.providerName;
        	
        	// Copy parameters from template
        	paramgroup xmlGroup = this.GetParameterGroupByID(xmlDefinition.paramgroupid);
        	
        	// Add Optional Program Controlled Parameters - START
        	if (xmlGroup.paramExists(CACHE_PERIOD_PARAMETER)) {
        		Log.e(TAG,"Reserved Internal Paramgroup specified!!!");
        	} else {
        		parameter p = new parameter();
        		p.reset();
        		p.pid = CACHE_PERIOD_PARAMETER;
        		p.type = parameter.param_seconds;
        		p.defaultvalue = xmlDefinition.cacheseconds+"";
        		p.description = "The time (in seconds) before the provider will refresh.";
        		p.Name = "Cache Period";
        		p.optional=true;
        		p.escape=false;
        		p.valid=".*";
        		// Add to Editing Parameters
        		xmlGroup.params.add(p);
        	}
        	// -- END
        	
           	isp.disk_parameters = new paramgroup(xmlGroup);
            
        	if (xmlGroup==null) {
        		Log.e(TAG,"No Parameters for"+iid);
        	}
    	}
    	
    	
    }
    
    
    public boolean Queue_ActiveCookieCheck (String cookie) {
    	
    	if (Utils.isBlank(cookie)) {
    		return true;	
    	}
    			
    	Log.i(TAG,"Checking cookie "+cookie);
    	
    	for (int i=0;i<q_pendingupdates.size();i++) {
    		Provider p = q_pendingupdates.get(i);
    		Log.i(TAG,String.format("   Queue %d cookie %s",i,cookie));
    		if (  (p.loadstatus==Provider.provider_status_loading) && (p.provider_definition.xmlCookie.equalsIgnoreCase(cookie)) ) {
    			Log.i(TAG,"   Notprocessing due to conflict...");
    			return false;
    		}
    	}
    	return true;
    }
    
    
    private void Queue_RemoveActive (Provider provider) {
    	Log.i(TAG,String.format("QUEUE: Remove Provider %s",provider.disk_planname));
    	
    	q_activecount--;
    	provider.loadstatus = Provider.provider_status_idle;
    	if (q_pendingupdates.contains(provider)) {
    	   q_pendingupdates.remove(provider);
    	   q_pendingcount--;
    	} else {
    		Log.i(TAG,String.format("QUEUE: Trying to remove provider who does not exist in Queue %s",provider.disk_planname));	
    	}
    	
    	if (q_activecount<0) {
    	   	Log.i(TAG,"Active Count < 0");
    		q_activecount=0;
    	}
    	if (q_pendingcount<0) {
    	   	Log.i(TAG,"Pending Count < 0");
    		q_pendingcount=0;
    	}
    	
    	if (q_pendingcount==0) {
    		// Signal we have completed all loads
    		notifyUpdate(PM_LOADING_COMPLETE,null);
    	}
    	
    	Queue_ProcessingPending();
    	
    	// Process Any Other Requests
    	//[NSTimer scheduledTimerWithTimeInterval: 0.3 target:self
    	//							   selector:@selector(Queue_ProcessingPending) userInfo:nil repeats:NO];
    }
    

    public void pingWidgets() {
    	notifyUpdate(PM_WIDGET_UPDATE,null);
    }
    
    public void Queue_AbortPending() {
    	q_pendingupdates.clear();
    	q_pendingcount=0;
    }

    
    private void notifyUpdate(final int msg,final Provider p) {
			setChanged();
			notifyObservers(new NotifyUpdate(msg,p));
	}
    
    
    public void Queue_ProcessingPending() {
    	// Loop through all Pending Requests and Kick off any
    	for (int i=0;i<q_pendingupdates.size();i++) {
    		Provider p = q_pendingupdates.get(i);
    		
    		
    		if ((p.loadstatus==Provider.provider_status_idle) && q_activecount<q_maxconnections) {
    		   // Check No Active Connections have matching Cookie
    			Log.i(TAG,String.format("Checking %s Cookie %S",p.provider_definition.providerName,p.provider_definition.xmlCookie));
    				  
    			if (Queue_ActiveCookieCheck(p.provider_definition.xmlCookie)) {
    				// Kick this off
    				Log.i(TAG,"QUEUE: Processing: "+p.disk_planname);	
    				
    				q_activecount++;	
      				// Signal Loading has commenced
    				notifyUpdate(PM_LOADING_START,p);
  
    				p.loadstatus = Provider.provider_status_loading;
    				ProcessProvider(p);
    			} else {
    				Log.i(TAG,String.format("QUEUE: Can't make active due to cookie conflict : %s %s",p.disk_planname,p.xmlCookie));
    			}
    			
    		}
    	}
    	
    }
    
    
    
    private void NotifyProblem(Provider p,String title,String msg) {
    	if (this.mQuotaService!=null) {
    		mQuotaService.NotifyProblemProvider(p.slotnumber,title,msg,title);
    	}
    }
    
    
    @SuppressWarnings("unchecked")
	private void CheckAlerts(Provider p) {
    	
    	if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
        	// Get Alerts
          	Hashtable h1 = p.disk_alerts;
        	if (h1!=null && h1.size()>=3) {
        		boolean ideal = (Boolean)h1.get("ideal");
        		int p1 = ((Integer)h1.get("p1")).intValue();
           		int p2 = ((Integer)h1.get("p2")).intValue();
           		
           		// Check Ideal
           		boolean overideal=false;
           		
           		if (p.p1OverIdeal() && ideal) {
           			overideal=true;
           		}
           		
           		if (p.p2OverIdeal() && ideal) {
          			overideal=true;
           		}
           		
           		if (overideal) {
           			this.NotifyProblem(p,p.disk_planname,"Over ideal usage!");
           		}
           		
           		if (p1>0) {
           			String msg = p.p1OverPercent(p1);
           			if (msg!=null) {
               			this.NotifyProblem(p,p.disk_planname,msg);
           			}
           		}
           		if (p2>0) {
           			String msg = p.p2OverPercent(p2);
           			if (msg!=null) {
               			this.NotifyProblem(p,p.disk_planname,msg);
           			}
           		}
           		
        	}

    	}
    		
    }
    
    
    @SuppressWarnings("unchecked")
	private void createLocalDataUsageProvider(Provider p) {
    
    	try {
    		// Start Day
    		
    		ProviderParser.DebugLogStart(p);
    		
        	
        	
        	parameter sd = p.getParameterByID(6);
        	parameter celltotal = p.getParameterByID(7);
        	parameter wifitotal = p.getParameterByID(8);
        	
        	int sday=(int)sd.numberval;
        	
           	Hashtable h = NetworkUtils.updateNetworkCounters(sday);

        	ProviderParser.DebugLogLog(p, "Netstats Hash" + h.toString());

        	p.current_cycle = new cycle();
        	p.current_cycle.initWithAnniversaryDay(sday);
        	
    		p.progress = new ArrayList<kbProgressBar>();
    	  	
        	kbProgressBar pb = new kbProgressBar();
           	p.progress.add(pb);
            
           	pb.pid = 0;
        	pb.type=0;
        	pb.name = "Cellular (3G)";
        	pb.value_max = celltotal.numberval;
        	pb.value_value = NetworkUtils.getLong(h,NetworkUtils.mobTotcurrent) / 1024.0 / 1024.0;
        	pb.used = true;
        	pb.outtype = 7;
        	pb.cycle_usage = p.current_cycle;
        	pb.UpdateValues();
        	
        	// 2 
           	kbProgressBar pb2 = new kbProgressBar();
           	p.progress.add(pb2);
     
           	pb2.pid = 1;
           	pb2.type=0;
           	pb2.name = "Wifi";
           	pb2.value_max = wifitotal.numberval;
           	pb2.value_value = NetworkUtils.getLong(h,NetworkUtils.wifiTotcurrent)/ 1024.0 / 1024.0;
           	pb2.used = true;
           	pb2.outtype = 7;
           	pb2.cycle_usage = p.current_cycle;
           	pb2.UpdateValues();
        	
          	String ip=NetworkUtils.getLocalIpAddress();
          	 
           	p.clearExtras();
           	p.addExtra(0,"UpTime",DateUtils.millisToShortDHMS(NetworkUtils.rebootMilli()));
           	p.addExtra(0, "Last reboot", DateUtils.DateUpTime(NetworkUtils.rebootTime()));
           	p.addExtra(0,"IP Address",ip==null?"N/A":ip);
           	
           	String lw=(String)h.get(NetworkUtils.lastWifi);
           	String lc=(String)h.get(NetworkUtils.lastMobile);
           	
        	p.addExtra(0,"Last Cell usage",lc==null?"N/A":lc);
        	p.addExtra(0,"Last Wifi usage",lw==null?"N/A":lw);
        	           	
           	
    		p.loadsuccess=true;
    		p.loaderrormsg = "";
    		
    	} catch (Exception e) {
       		p.loadsuccess=false;
    		p.loaderrormsg="Internal error :"+e.toString();
    		p.failurereason = Provider.fail_internalerror;
    	}


       	
    }
    
    
    // Params, Progress, Result
    
    public class asyncUpdateProvider extends AsyncTask<Provider,Provider,String> {
    
    	Provider myP;
    	
    	protected void onPreExecute(){
    	
    	}
    	
    	public void updateMsg() {
    		publishProgress(myP);
    	}
    	
    	public void askQuestion() {
    		myP.questioninProgress=true;
    		myP.questionAnswered=false;
    		
    		publishProgress(myP);
    	}
    	
    	
    	 @Override
    	     protected String doInBackground(Provider... params) {
    	         // perform Long time consuming operation
    	     	try {
    	     		myP=params[0];
    	     		myP.questioninProgress=false;
    	     		
    	     	 	 if (myP.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE && myP.disk_isp==LOCAL_DATA_USAGE) {
   	    	     		myP.loadmsg = "Checking network stats";
	    	     		publishProgress(myP);
  	     	 		    createLocalDataUsageProvider(myP);
    	     	 	 }  else {
    	    	     	
    	     	 		    if (myP.provider_definition.disk_isp==0 || myP.provider_definition.disk_isp==19 ) {
    	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.iiNetUpdate(myP);
    	     	 		    } else if (myP.provider_definition.disk_isp==250) {
      	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.westNet(myP);
       	     	 		    } else if (myP.provider_definition.disk_isp==17) {
      	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.Netspace(myP);
    	     	 		    } else if (myP.provider_definition.disk_isp==3) {
      	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.internodeUpdater(myP,this);
       	     	 		    } else if (myP.provider_definition.disk_isp==26) {
      	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.amNet(myP);
    	     	 		    } else if (myP.provider_definition.disk_isp==11) {
    	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.virginPostpay(myP);
       	     	 		    } else if (myP.provider_definition.disk_isp==14) {
    	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.threemobile(myP);
    	     	 		    } else if (myP.provider_definition.disk_isp==21) {
       	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.aaNet(myP);
       	     	 		    } else if (myP.provider_definition.disk_isp==24) {
       	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    ProviderParser.dodoISP(myP);
    	     	 		    }
    	     	 		    else {
      	   	     	 		    myP.loadmsg = "Getting usage";
        	    	     		publishProgress(myP);
    	   	     	 		    XMLProviderParser.ProcessProvider(myP,this);
    	     	 		    }
   	 		 
    	     	 	 }
 
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		 	return null;
    	 
    	     }

      	  @Override
      	  protected void onProgressUpdate(Provider... values) {
      		    if (myP.questioninProgress) {
      		    	notifyUpdate(PM_EVENT_ASKQUESTION,myP);
      		    } else {
    				notifyUpdate(PM_EVENT_UPDATE,values[0]);
      		    }
      	  }
    	   
    	  @Override
    	    protected void onPostExecute(String result) {
    	      // execution of result of Long time consuming operation
    		  if (myP.loadsuccess) {
    	   		  FinishLoadingSuccess(myP);
    		  } else {
      	   		  FinishLoadingFailure(myP);
    		  }
    	    }
    	
    }
    
    public void FinishLoadingSuccess(Provider p) {
    	p.cacheFileDate = new Date();
    	p.loadstatus = Provider.provider_status_parsing;
    	
    	// Call Parse Data
    	
    	// Complete
    	
    	// Cache Data
    	CacheManager.getInstance().WriteCachedProviderData(p);
    	
    	CheckAlerts(p);
    	
    	p.loadstatus = Provider.provider_status_idle;
    	Queue_RemoveActive(p);
    	notifyUpdate(PM_EVENT_COMPLETE,p);
    }

    public void FinishLoadingFailure(Provider p) {
    	// Write Failed Data to disk
    	Queue_RemoveActive(p);
    	
    	p.loadstatus = Provider.provider_status_idle;
    	notifyUpdate(PM_EVENT_FAIL,p);
    	
    }
 
    
    
    private void ProcessProvider(final Provider p) {
    	new asyncUpdateProvider().execute(p);
    	
    }
    
    
    
    // Queue Management
    public void Queue_ScheduleUpdate (Provider p) {
    	
    	// Already in Queue
    	if (this.q_pendingupdates.contains(p)) {
    		Log.i(TAG,String.format("QUEUE: Ignoring ScheduleUpdate for %s",p.disk_planname));
    	} else {
    		// Add to Pending Update
    		Log.i(TAG,String.format("QUEUE: Add to Pending Provider %s",p.disk_planname));	

    		this.q_pendingupdates.add(p);
    		q_pendingcount++;
    		Queue_ProcessingPending();
    	}
    	
    }
    
    
    
    public ArrayList<Provider> getProvidersForKeyCreate (String key) {
    	
    	// Try to Find Array for this Key
    	if (!types.containsKey(key)) {
       		types.put(key, new ArrayList<Provider>());
    	}
  		return (ArrayList<Provider>) types.get(key);
    }
    
    public paramgroup GetParameterGroupByID (int id) {
    	for (int i=0;i<paramgroups.size();i++) {
    		paramgroup p = paramgroups.get(i);
    		if (p.pid==id) {
    			return p;	
    		}
    	}
    	return null;
    }
    
    
    public Provider getProviderDefinition (int iid) {
    	
    	Set<?> keys = types.entrySet();
    	Iterator<?> it = keys.iterator();
    	    
    	while (it.hasNext()) 
    	{
    	    Map.Entry entry = (Map.Entry) it.next();
    		
    	    // Get Array of Providers for this Type
    		ArrayList<Provider> plist = (ArrayList<Provider>) entry.getValue();
    		
    		for (int i=0;i<plist.size();i++) {
    			Provider isp = plist.get(i);
    			if (isp.disk_isp==iid) {
    				return isp;	
    			}
    		}
    	}
    	return null;
    }
    	
    
    private paramgroup ProcessParamGroup(TagNode keys) {
    	paramgroup pg=null;
    	
    	pg = new paramgroup();
    	
    	pg.description = keys.getAttribute("description");
		pg.pid = Utils.getIntegerFromString(keys.getAttribute("id"));
		
		if (Utils.isBlank(pg.description)) {
			xmlLoadMsg=String.format("Parameter group description cannot be blank",pg.pid);
			return null;
		}
		
		pg.params = new ArrayList<parameter>();
		
        for (TreeNode node = keys.getChild(); node != null; node = node.getSibling()) {
            	 
		{
				// Ignore Whitespace
				if (node instanceof TextNode) {
					continue;
				}
				
			    TagNode opt = (TagNode)node;
			
				String id              = opt.getAttribute("id");
				String name            = opt.getAttribute("name");
				String type            = opt.getAttribute("type");
				String valid           = opt.getAttribute("validate");
				String description     = opt.getAttribute("description");
				String value		   = opt.getAttribute("value");
				String blankmsg		   = opt.getAttribute("novalue");
				String format		   = opt.getAttribute("format");
				String defaultval	   = opt.getAttribute("defaultvalue");
				
				
				boolean optional		      = Utils.getBoolFromString(opt.getAttribute("optional"));
				boolean escape		          = Utils.getBoolFromString(opt.getAttribute("escape"));
				
				if (Utils.isBlank(id)) {
					xmlLoadMsg=String.format("No ID Specified for parameter");
					return null;
				}
				
				if (Utils.isBlank(name)) {
					String.format("No Name Specified for parameter");	
					return null;
				}
				
				if (Utils.isBlank(valid)) {
					// Default Validation String
					valid=".*";				
				}
				
				// Setup Parameters
				// Try to get and Replace
				parameter p= new parameter();
				p.pid = Utils.getIntegerFromString(id);
				p.optional = optional;
				p.blankmsg = blankmsg;
				p.formatString = format;
				p.escape = escape;
				p.defaultvalue = defaultval;
				
				// Check not already setup
				if (pg.paramExists(p.pid)) {
					xmlLoadMsg=String.format("Parameter %d already defined",p.pid);
					return null;
				}
				
				
				p.Name=name;
				p.description=description;
				
				if (Utils.isBlank(type)) {
					xmlLoadMsg=String.format("No Type Specified for parameter");
					return null;
				} else {
					if (type.equalsIgnoreCase("TEXT")) {
						p.type = parameter.param_text;
					} else if (type.equalsIgnoreCase("CHECK_LIST_MULTIPLE") ||
							type.equalsIgnoreCase("CHECK_LIST_SINGLE")) {
						// Create the Array
						if (Utils.isBlank(value)) {
							xmlLoadMsg=String.format("No values specified for checklist");
							return null;
						}
//						String[] tokens = 
//						p.ListValues = new ArrayList<String>(Arrays.asList(tokens));
						p.ListValues = value.split("\\|");
						
						if (type.equalsIgnoreCase("CHECK_LIST_SINGLE")) {
							p.type = parameter.param_textchoice; 
						} else {
							p.type = parameter.param_textchoicemulti;
						}
					} else if (type.equalsIgnoreCase("PASSWORD")) {
						p.type = parameter.param_password;
					} else if (type.equalsIgnoreCase("DECIMAL")) {
						p.type = parameter.param_decimal;
					} else if (type.equalsIgnoreCase("NUMBER")) {
						p.type = parameter.param_number;
					} else if (type.equalsIgnoreCase("NUMBERSTR")) {
						p.type = parameter.param_numberstr;
					} else if (type.equalsIgnoreCase("PIN")) {
						p.type = parameter.param_pinnumber;
					} else if (type.equalsIgnoreCase("DATE")) {
						p.type = parameter.param_dateval;
					} else if (type.equalsIgnoreCase("CURRENCY")) {
						p.type = parameter.param_currency;
					} else if (type.equalsIgnoreCase("SECONDS")) {
						p.type = parameter.param_seconds;
					} else if (type.equalsIgnoreCase("RANGE")) {
						if (Utils.isBlank(valid)) {
							xmlLoadMsg=String.format("Specify range in the form low..high");
							return null;
						}
						p.type = parameter.param_range;
						
					} else {
						xmlLoadMsg=String.format("Unknown Type Specified for parameter");	
						return null;
					}
					
				}
				// Extra Init
				p.setValidationString(valid);
				
				pg.params.add(p);
		}
    }
        
		return pg;
    }
    
   public void xmlError(String msg) {
   	if (parselog!=null) {
   		parselog.append(msg);
   	}
   }
   
   private void ProcessProviderDatasource(Provider p,TagNode ditem) {

	   // Reset holders
	   p.clearURLs(false);

	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
		     
		   
			  if (node.toString().equalsIgnoreCase("URL")) {
				    urlinfo url = new urlinfo();
				  
				    TagNode item = (TagNode)node;
				    
					url.myid            = Utils.getIntegerFromString(item.getAttribute("id"));
					url.urlstring       = item.getAttribute("http");
					url.urlalternate    = item.getAttribute("alternate");	
					url.postalternate   = item.getAttribute("alternatepost");	
					url.postdata		= item.getAttribute("post");
					url.msg	      		= item.getAttribute("message");
					url.type            = item.getAttribute("type");
					url.username		= item.getAttribute("username");
					url.password        = item.getAttribute("password");
					url.headers		  	= item.getAttribute("headers");
					int timeout = 		Utils.getIntegerFromString(item.getAttribute("timeout"));
					url.timeout 		= timeout==0?60:timeout;
				    
					p.urls.add(url);
			  }
			  if (node.toString().equalsIgnoreCase("TEXT")) {

			  }
	   }
   }
   
   private void ProcessProviderDatakeys(Provider p,TagNode ditem) {

	   // Reset holders
	   p.clearDataKeys();
	   
	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
			  if (node.toString().equalsIgnoreCase("KEY")) {
				  	datakey key = new datakey();
				  	

				    TagNode item = (TagNode)node;

					key.myid			   = Utils.getIntegerFromString(item.getAttribute("id"));
					key.name			   = item.getAttribute("name");
					key.type		   	   = item.getAttribute("type");
					key.src		   	       = item.getAttribute("src");
					key.subkey	   	       = Utils.getIntegerFromString(item.getAttribute("subkey"));
					
					int srcid		   	   = Utils.getIntegerFromString(item.getAttribute("srcid"));
					key.srcid = srcid;
					
					key.extract	   		   = Utils.BlankString(item.getAttribute("extract")).toUpperCase();
					key.format	   		   = item.getAttribute("parseformat");
					key.outputformat	   = item.getAttribute("outputformat");
					int otype			   = Utils.getIntegerFromString(item.getAttribute("outputtype"));
					key.outputtype = otype;
					key.condition	   	   = item.getAttribute("condition");
					
					key.removechars		   = item.getAttribute("removechars");
					key.replacechars	   = item.getAttribute("replacechars");
					key.prefix	    	   = item.getAttribute("prefix");
					key.postfix	     	   = item.getAttribute("postfix");
					key.defaultval	       = item.getAttribute("default");
					
					key.trimspace	   	   = Utils.getBoolFromString(item.getAttribute("trimspace"));
					key.removehtml	   	   = Utils.getBoolFromString(item.getAttribute("trimhtml"));
					key.escape  	       = Utils.getBoolFromString(item.getAttribute("escape"));
					key.javascript	       = Utils.getBoolFromString(item.getAttribute("javascript"));
				  	
					// Process SubNodes
					String find = xmlUtils.subNodeValue(node, "find");
					int pos = Utils.getIntegerFromString(xmlUtils.subNodeValue(node, "pos"));
					String start = xmlUtils.subNodeValue(node, "start");
					String end = xmlUtils.subNodeValue(node, "end");
					
					key.find = find;
					key.pos = pos;
					key.start = start;
					key.end = end;
					
					
				    p.datakeys.add(key);
			  }
			  if (node.toString().equalsIgnoreCase("TEXT")) {

			  }
	   }
   }
   
   private void ProcessProviderConditions(Provider p,TagNode ditem) {

	   // Reset holders
	   p.conditions = new ArrayList<condition>();

	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
			  if (node.toString().equalsIgnoreCase("CONDITION")) {
				  	condition cond = new condition();
				    
				  	TagNode item = (TagNode)node;

				  	cond.xmlid 	  = item.getAttribute("id");
				  	cond.when 	  = item.getAttribute("when");
				  	if (Utils.isBlank(cond.when)) {
				  		cond.when="AFTER";
				  	}
				  	
				  	cond.operator = item.getAttribute("operator");
				  	cond.p1 	  = item.getAttribute("p1");
				  	cond.p2 	  = item.getAttribute("p2");
				  	cond.p3 	  = item.getAttribute("p3");
				  	cond.action   = item.getAttribute("action");
				  	
				  	cond.ap1 = item.getAttribute("ap1");
				  	cond.ap2 = item.getAttribute("ap2");
				  	cond.ap3 = item.getAttribute("ap3");
				  	
				  	p.conditions.add(cond);
				  	
			  }
			  
	   }
   }
   private void ProcessProviderSummaryPanel(Provider p,TagNode ditem) {

	   // Reset holders
	   p.progress = new ArrayList<kbProgressBar>();
	   p.current_cycle = new cycle();
	   
	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
				
		   if (node.toString().equalsIgnoreCase("PROGRESS")) {
			  	
			   TagNode item = (TagNode)node;

			   kbProgressBar pbar = new kbProgressBar();
				    
				  
				  pbar.pid 			= Utils.getIntegerFromString(item.getAttribute("id"));
				  pbar.name 		= item.getAttribute("name");
				  
				  pbar.srcValue 	= item.getAttribute("value");
				  pbar.srcMaxValue 	= item.getAttribute("maxvalue");
				  pbar.used 		= Utils.getBoolFromString(item.getAttribute("used"));
				  pbar.outtype 		= Utils.getIntegerFromString(item.getAttribute("outputtype"));
				  pbar.outformat 	= item.getAttribute("outformat");
				  p.progress.add(pbar);
				  
			  }
			  
			  if (node.toString().equalsIgnoreCase("CYCLE")) {
				   TagNode item = (TagNode)node;

				   p.current_cycle.pid 			= Utils.getIntegerFromString(item.getAttribute("id"));
				   p.current_cycle.typecycle    = Utils.getIntegerFromString(item.getAttribute("type"));
				   p.current_cycle.srcStartDate = item.getAttribute("startdate");
				   p.current_cycle.srcEndDate   = item.getAttribute("enddate");
				   p.current_cycle.includeend   = Utils.getBoolFromString(item.getAttribute("includeend"));
				   p.current_cycle.srcStartDay  = item.getAttribute("startday");

			  }
			  
	   }
   }
   
   
   
   private void ProcessProviderAccountSummary(Provider p,TagNode ditem) {
	   
	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
			
		   if (node.toString().equalsIgnoreCase("SUMMARY")) {
			  	
			   TagNode item = (TagNode)node;

			   account acc = new account();
			   
				acc.bal1name = item.getAttribute("bal1name");
				acc.bal2name = item.getAttribute("bal2name");
				acc.bal1value = item.getAttribute("bal1value");
				acc.bal2value = item.getAttribute("bal2value");

				acc.hidesummary = Utils.getBoolFromString(item.getAttribute("hidesummary"));
				acc.rssTitleKey = item.getAttribute("rsstitle");
				acc.rssDescKey  = item.getAttribute("rssdesc");
				acc.rssTimeKey  = item.getAttribute("rsstime");
				acc.rssLinkKey  = item.getAttribute("rsslink");
				
				// Transactions
				acc.srcData = item.getAttribute("src");
				acc.dateColumn = item.getAttribute("date");
				acc.descriptionColumn = item.getAttribute("description");
				acc.amountColumn = item.getAttribute("amount");
				acc.headings = item.getAttribute("headings");
				acc.descriptionformat= item.getAttribute("descriptionformat");
				acc.amountformat= item.getAttribute("amountformat");
				acc.dateformat =  item.getAttribute("dateformat");
				
				
			   p.accountdata = acc;
		   }
	   }
	   

   }
   
   private void ProcessProviderExtraPanel(Provider p,TagNode ditem) {
	   
	   p.clearExtras();
	   
	   for (TreeNode node = ditem.getChild(); node != null; node = node.getSibling()) {
			
		   if (node.toString().equalsIgnoreCase("EXTRA")) {
			  	
			   TagNode item = (TagNode)node;

			   extradata ed = new extradata();
			   
			   
			   ed.name = item.getAttribute("name");
			   ed.value = item.getAttribute("value");
			   
			   ed.nameFormat  = item.getAttribute("nameformat");
			   ed.valueFormat = item.getAttribute("valueformat");
			   ed.nameFormat  = item.getAttribute("nameformat");
			   ed.src 		  = item.getAttribute("src");
			   ed.setExtraType(Utils.BlankString(item.getAttribute("type")));
			   
			   ed.order 	    = Utils.getIntegerFromString(item.getAttribute("order"));
			   ed.showwhenempty = Utils.getBoolFromString(item.getAttribute("showonempty"));
			   p.extras.add(ed);
		   }
	   }
	   

   }
   
   
   private void ProcessProviderBody(Provider p,TagNode item) {
       for (TreeNode node = item.getChild(); node != null; node = node.getSibling()) {
			  if (node.toString().equalsIgnoreCase("MODEL")) {
			      for (TreeNode mnode = node.getChild(); mnode != null; mnode = mnode.getSibling()) {
					  if (mnode.toString().equalsIgnoreCase("DATASSOURCE")) {
						  ProcessProviderDatasource(p,(TagNode)mnode);
					  }
					  
					  if (mnode.toString().equalsIgnoreCase("DATAKEYS")) {
						  ProcessProviderDatakeys(p,(TagNode)mnode);
					  }
					  if (mnode.toString().equalsIgnoreCase("CONDITIONS")) {
						  ProcessProviderConditions(p,(TagNode)mnode);
					  }
				}
			  }
			  
			  
			  if (node.toString().equalsIgnoreCase("VIEW")) {
				  // Hack for Paramgroup
				  if (node instanceof TagNode) {
					  int pgid = Utils.getIntegerFromString(item.getAttribute("paramgroup"));
					  if (pgid!=0 && p.paramgroupid==0) {
						  p.paramgroupid=pgid;
					  }
				  }
			      for (TreeNode mnode = node.getChild(); mnode != null; mnode = mnode.getSibling()) {
					  if (mnode.toString().equalsIgnoreCase("SUMMARYPANEL")) {
						  ProcessProviderSummaryPanel(p,(TagNode)mnode);
					  }
					  if (mnode.toString().equalsIgnoreCase("EXTRAPANEL")) {
						  ProcessProviderExtraPanel(p,(TagNode)mnode);
					  }
					  if (mnode.toString().equalsIgnoreCase("ACCOUNTBALANCE")) {
						  ProcessProviderAccountSummary(p,(TagNode)mnode);
					  }
			      }				  
			  }
			  
       }
       
   }
   
   
   private Provider ProcessProvider(TagNode item,boolean loadBody) {
    	 
	   Provider conn = new Provider();
    	 
    	 try {
 			String idStr        = item.getAttribute("id");
			String name         = item.getAttribute("name");
			
			String type         = item.getAttribute("type");
			String version	    = item.getAttribute("version");
			String author	    = item.getAttribute("author");
			String description  = item.getAttribute("description");
			String m		    = item.getAttribute("parsemethod");
			String icon	        = item.getAttribute("icon");
			String supporturl   = item.getAttribute("supporturl");
			String providerurl  = item.getAttribute("providerurl");

			conn.disk_display_type = Utils.getIntegerFromString(item.getAttribute("dtype"));
			
			if 	(!(conn.disk_display_type==Provider.kDisplayType_ISPMOBILE 
				 || conn.disk_display_type== Provider.kDisplayType_ACCOUNT)
				 ){
				// Provider Type Unsupported
				xmlLoadMsg = String.format("Can't load %s unsupported type",Utils.BlankString(name));
				return null;
			}
			
			int pgid 			= Utils.getIntegerFromString(item.getAttribute("pgid"));
 	 
			conn.debugprovider  = Utils.getBoolFromString(item.getAttribute("debug"));
			
			String cookie	    = Utils.BlankString(item.getAttribute("cookie"));

			boolean	secure			= Utils.getBoolFromString(item.getAttribute("secure"));
			
			conn.paramgroupid=pgid;

			if (pgid==0) {
				Log.e(TAG,"Paramgroup ID not setup for provider"+idStr);
			}
			
			int dtype = Utils.getIntegerFromString(item.getAttribute("dtype"));
			if (dtype>0) {
				conn.disk_display_type = dtype;
			}
			
			// Get Datasource attribues
			conn.xmlCookie = cookie;
			
			if (Utils.isBlank(idStr)) {
				xmlLoadMsg = String.format("No ID Specified for Provider",idStr);	
				return null;
			}
			
			if (Utils.isBlank(type)) {
				xmlLoadMsg= String.format("No Type Specified for %s",idStr);	
				return null;
			}

			//
			// Default to Code Parsing unless XML Specified...
			//
			
			conn.parse_method=Provider.method_code;
			if (!Utils.isBlank(m)) {
				if (m.equalsIgnoreCase("xml")) {
					conn.parse_method=Provider.method_xml; 	
				} else if (m.equalsIgnoreCase("code")) {
				    conn.parse_method=Provider.method_codeparser;
				}
			} 
			
			
			if (Utils.isBlank(icon)) {
				icon = idStr;
			}
			int cache;
			cache=0;
			
			cache=Utils.getIntegerFromString(item.getAttribute("cache"));
			
			int pid = Utils.getIntegerFromString(idStr);
			
			if (cache==0) {
				cache=Provider.URL_CACHEINTERVAL;				// Default if not specified
			} 

			
			conn.disk_isp = pid;
			conn.providerName = name;
			conn.providertype = type;						// Don't uppercase
			conn.xmlVersion = version==null?"":version;
			conn.xmlAuthor  = author==null?"":author;
			conn.xmlDescription = description;
			conn.cacheseconds=cache;
			conn.icon=icon;
			conn.supportUrl=(Utils.isBlank(supporturl)?"http://www.southfreo.com/QuotaforAndroid/Home.html":supporturl);
			conn.providerUrl=(Utils.isBlank(providerurl)?"http://www.southfreo.com/QuotaforAndroid/Home.html":providerurl);
			conn.secure=secure;
			
			// Process the Body 
			if (loadBody) {
				ProcessProviderBody(conn,item);
			} else {
				//Log.i(TAG,"Not loading body");
				
			}
			
			// Does this exist
			Provider pd = this.getProviderDefinition(pid);
			
			if (pd!=null) {
		 		// Remove Current Provider
				Log.i(TAG,"Removing Provider"+pid);
				ArrayList<Provider> pa = getProvidersForKeyCreate(type);
				pa.remove(pd);
			}
			
			// Add this to Types
			ArrayList<Provider> pa = getProvidersForKeyCreate(type);
			pa.add(conn);
			
			return conn;
    	 } catch (Exception e) {
    		 this.xmlLoadMsg = "Problem parsing provider : " + e.toString();
    		 return null;
    	 }

    }
    
   //
   
   public boolean reloadProvider(Provider p) {
	   
	   int id = p.provider_definition.disk_isp;

	   if (p.provider_definition.loadedFrom==0) {
		   ApplictionObject ao=UIManager.getInstance().myapp;
		   int resid = ao.getResources().getIdentifier("n"+id, "xml", ao.getString(R.string.app_namespace));
	       boolean ok = LoadProvidersFromResource(ao,resid,false,false,true);
	       return ok;
	   } else {
		   // User Pack
		   String fileLoad = CacheManager.getInstance().UserUpdateFile(id+".xml");
	       boolean ok = LoadProvidersFromFile(fileLoad,false,false,true);
	       return ok;
	   }
   }
   
   

    public boolean LoadProvidersFromResource(Context c,int resource,boolean reset,boolean updateVersion,boolean sort) {
    
        XmlResourceParser parser =  c.getResources().getXml(resource);
    	return LoadProviders(parser,reset,updateVersion,sort,0);
    
    }
    
    public boolean LoadProvidersFromFile(String sFileName,boolean reset,boolean updateVersion,boolean loadbody) {
    	try {
        	XmlPullParserFactory factory =  XmlPullParserFactory.newInstance();
        	XmlPullParser xpp = factory.newPullParser();
        	
        	// StripNewline 
        	//String sfile = Utils.readFileAsString(new File(sFileName));
        	//if (loadbody) {
            //	sfile = sfile.replaceAll("\r\n","");
            //	sfile = sfile.replaceAll("\n","");
            //	sfile = sfile.replaceAll("\t","");
        	//}
           	//xpp.setInput( new StringReader(sfile) );

        	xpp.setInput( new FileReader(sFileName) );
            return LoadProviders(xpp,reset,updateVersion,loadbody,1);
        	
    	} catch (Exception e) {
    		this.xmlLoadMsg = "LoadProvidersFromFile exception :"+e.getMessage();
    		Log.e(TAG,"Problem Loading pack"+e.getMessage());
    		return false;
    	}
    }

    public boolean LoadUserPacks(String dir) {

    	try {
    	   	File directory = new File(dir);
        	String filename[] = directory.list();

        	for (int i = 0; i < filename.length; i++) {
        		String file =  filename[i];
        		if (Utils.RegEx(file, ".*?\\.xml", 0)!=null) {
        			String uxml = dir + "/" + file;
        			
        			Log.i(TAG,"Processing UserPack :"+uxml);
        			boolean ok = LoadProvidersFromFile(uxml,false,false,false);
        			if (!ok) {
               			Log.i(TAG,"Problem "+xmlLoadMsg);
        			}
        		}
        	}
    	} catch (Exception e) {
    		Log.e(TAG,"Exception loading UserPacks"+e.toString());
    	}
    	
    	return true;
    }
    
    
    
    // Load Providers from XML
      public boolean LoadProviders(XmlPullParser parser, boolean reset,boolean updateVersion, boolean loadbody,int location) {

    	xmlLoadMsg = "OK";
    	
    	try {
    		
    		if (reset) {
    			types.clear();	
    			//paramtypes.clear();
    			paramgroups.clear();
    		}
    		
            TreeBuilder builder = new TreeBuilder();
            
            long st = System.currentTimeMillis();
            TagNode root = (TagNode) builder.parseXML( parser );
  
            int version = Utils.getIntegerFromString(root.getAttribute("version"));
          
      		Log.i(TAG, "Version is "+version);
      		
     		//if (root!=null && version>0) {
         	if (root!=null) {
    			// Parse Commence
    			if (updateVersion) {
    				if (version>xmlversion) {
    					xmlversion = version;
    				}
    			}
    			
    			int noProviders=0;
                for (TreeNode node = root.getChild(); node != null; node = node.getSibling()) {
    				  if (node.toString().equalsIgnoreCase("PARAMGROUPS")) {
    					  // Process Paramgroups
    		              for (TreeNode pnode = node.getChild(); pnode != null; pnode = pnode.getSibling()) {
    		   				  if (pnode.toString().equalsIgnoreCase("PARAMGROUP")) {
    	   						  paramgroup pg = ProcessParamGroup((TagNode)pnode);
    	   						  if (pg!=null) {
    	   							  // Check it doesn't already exist
    	   							  paramgroup tpg = GetParameterGroupByID(pg.pid);
    	   							  if (tpg!=null) {
    	   								  if (reset) {
        	   								  Log.e(TAG,"PARAMGROUP DEFINED TWICE!!!! : "+pg.pid);
    	   								  }
    	   								  Log.i(TAG,"Removing paramgroup : "+pg.pid);
    	   								  paramgroups.remove(tpg);
    	   							  }
    	  	   						  paramgroups.add(pg);
    	  	   					  } else {
    	  	   						  Log.e(TAG,"Problem parsing parameter group : "+this.xmlLoadMsg);
    	  	   						  return false;
    	  	   					  }
    		   				  } // End Paramgroup
    		              }  					  
    				  } // End Paramgroups
	   				  if (node.toString().equalsIgnoreCase("PROVIDER")) {
	   						 Provider p = ProcessProvider((TagNode)node,loadbody);
	   						 if (p==null) {
 	  	   						  Log.e(TAG,"Problem parsing provider : "+this.xmlLoadMsg);
 	  	   						  // Try loading all Providers
 	  	   						  //return false;
	   						 } else {
		   						 p.loadedFrom=location;
		   						 noProviders++;
	   						 }
	   				  }
	   				  
    			}
                
                Log.i(TAG,"XML Parse/Load of " + noProviders + " took: "+ (System.currentTimeMillis()-st) + "ms");
    		}
    		
    		
    		
    	} catch (Exception e) {
    		Log.e(TAG,"Problem parsing Provider XML"+e);
    		xmlLoadMsg = "Problem parsing Provider XML: "+e.toString();
    		return false;
    	}
    	
    	return true;
    }
    
    

    public Provider CreateProvider (int iid) {
    	 
    	//
    	// Create Provider and Reset
    	//
    	Provider p = new Provider();

    	if (iid!=-1) {
    		this.ResetProvider(p,iid);
    	} else {
    	    p.disk_isp = -1;	
    	}
    	
    	return p;
    }
    
    
    
    
    
    
 }
