package com.southfreo.quota.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.southfreo.R;
import com.southfreo.quota.activities.AppSettingsConfigure;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.account;
import com.southfreo.quota.model.cycle;
import com.southfreo.quota.model.extradata;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Security;
import com.southfreo.quota.utils.Utils;


public class SlotManager {
    private static SlotManager INSTANCE = null;
	private static final String TAG = "Quota-SlotManager";

    public Provider currentProvider;
	public int currentSlot;
	public ArrayList<Provider> slotArray;
	public Application myapp;
	public SharedPreferences preferences;
	public String pinCode;
	public Date lastpinCheck;
	public long pinexpiryseconds;
	//public boolean backgroundapp;
	
	// DropBox Backup
	public String DB_lastBackup;
	public String DB_lastUser;
	public String DB_noaccounts;
	
	private final static String DB_LASTBACKUP = "db_lastbackup";
	private final static String DB_LASTUSER   = "db_lastuser";
	private final static String DB_LASTNOACC  = "db_lastnoacc";
	
	private final static String PREF_NOSLOTS = "NoSlots";
	
    private final byte[] desKeyData = {(byte) 0x91, (byte) 0x67, (byte) 0x3E, (byte) 0xE6, 
            (byte) 0x44, (byte) 0x22, (byte) 0x21, (byte) 0x14 };

    private String errmsg="";
    
    private SlotManager() {
       // Exists only to defeat instantiation.
      	slotArray = new ArrayList<Provider>();
    }
    
    // Restore Stuff
    public String[] restoreNames;
    public boolean[] restoreSelected;
    public JSONObject restoreObject;
    
    
    public void InitPreferences() {
       	// Get a handle to preferences first
    	preferences = myapp.getSharedPreferences("prefs",Context.MODE_PRIVATE);
    	lastpinCheck = DateUtils.getDateBeforeDays(10);
    	pinexpiryseconds = preferences.getLong(AppSettingsConfigure.PIN_CODE_EXPIRY, AppSettingsConfigure.ONE_MINUTE);
    	pinCode = preferences.getString(AppSettingsConfigure.PIN_CODE, "");
    	
    	// DropBox
    	DB_lastBackup = preferences.getString(DB_LASTBACKUP, "N/A");
    	DB_lastUser   = preferences.getString(DB_LASTUSER, "N/A");
    	DB_noaccounts = preferences.getString(DB_LASTNOACC, "N/A");
    	
    	//backgroundapp=true;
    	
    	// Decrypt PIN
    	if (!Utils.isBlank(pinCode)) {
    		try {
				pinCode = Security.decrypt(new String(desKeyData), pinCode);
			} catch (Exception e) {
				Log.e(TAG,"Problem reading PIN");
			}
    	}
    	}
 
    
    public boolean hasPin() {
    	return !Utils.isBlank(pinCode);
    }

    public void DropBoxUpdateBackup(String usr,String dusr,String noAccts) {
    	this.DB_lastBackup = dusr;
    	this.DB_lastUser = usr;
    	this.DB_noaccounts = noAccts;
    	
    	preferences.edit().putString(DB_LASTBACKUP, DB_lastBackup).commit();
    	preferences.edit().putString(DB_LASTUSER, DB_lastUser).commit();
    	preferences.edit().putString(DB_LASTNOACC, DB_noaccounts).commit();
    }
    
    
    public boolean showPinScreen() {
    	if (hasPin()) {
    		// Last Pin Check
    		long ssc = DateUtils.secondsBetween(lastpinCheck,new Date())*1000;	//MS
    		if (ssc>pinexpiryseconds && ssc>1 ) { //&& backgroundapp
    			return true;
    		}
    	}
    	return false;
    }

    public void SavePinExpiry(long pinPeriod) {
  		preferences.edit().putLong(AppSettingsConfigure.PIN_CODE_EXPIRY,pinPeriod).commit();
  		pinexpiryseconds=pinPeriod;
    }
    
    public void SavePin(String newPin) {
    	pinCode=newPin;
		lastpinCheck  = new Date();
		//backgroundapp = false;

    	String npenc;
		try {
			npenc = Security.encrypt(new String(desKeyData), pinCode);
	   		preferences.edit().putString(AppSettingsConfigure.PIN_CODE,npenc).commit();
		} catch (Exception e) {
			Log.e(TAG,"Problem saving PIN");
		}
    }
    
    
    public String[] ProviderNames() {
    	String name[] = new String[slotArray.size()];
    	
    	for (int i=0;i<slotArray.size();i++) {
    		Provider p = slotArray.get(i);
    		name[i] = p.disk_planname;
    	}
    	return name;
    }
    
    
    // 
    public boolean restoreSlotsProviderArray(File f) {
    	try {
        	String ef = Security.decryptBase64(this.pinCode,Utils.readFileAsString(f));
        	restoreObject = (JSONObject) new JSONTokener(ef).nextValue();
        	ArrayList<String> pa = new ArrayList<String>();
           	ArrayList<Boolean> pi = new ArrayList<Boolean>();
        	
        	// Provider Array
        	JSONArray jpa = restoreObject.getJSONArray("Providers");
        	for (int i=0;i<jpa.length();i++) {
        		JSONObject jp = jpa.getJSONObject(i);
        		String pname   = jp.getString("plan_name");
        		pa.add(pname);
        		pi.add(new Boolean(true));
        	}
        	this.restoreNames=Utils.toStringArray(pa);
        	this.restoreSelected = new boolean[this.restoreNames.length];
        	for (int i=0;i<this.restoreSelected.length;i++) {
        		this.restoreSelected[i] = true;
        	}
        	return true;
    	} catch (Exception e) {
    		return false;
    	}
    	
    }
    
    
    public String restoreSlotsAsJson64() {
    	try {
    	    
    		int ok=0;
    		int er=0;
    		
        	// Provider Array
        	JSONArray jpa = restoreObject.getJSONArray("Providers");
        	for (int i=0;i<jpa.length();i++) {
        		if (restoreSelected[i]) {
        			// Create Slot
            		JSONObject jp = jpa.getJSONObject(i);
            		
            		int pid        = jp.getInt("id");
            		String pname   = jp.getString("plan_name");
            		boolean mrfresh = jp.getBoolean("manual_refresh");
                	
               		Provider p = addSlot();
                	try {
                		ProviderManager.getInstance().ResetProvider(p, pid);
                		
                		p.disk_planname = pname;
                		p.disk_manualrefresh = mrfresh;
                		
                		JSONArray params = jp.getJSONArray("parameters");
                       	for (int j=0;j<params.length();j++) {
                       	    // Get Params
                       		JSONObject po= params.getJSONObject(j);
                       		int pindex = po.getInt("id");
                       		String val = po.getString("value");
                       		parameter param = p.disk_parameters.getParameterByID(pindex);
                       		if (param!=null) {
                           		// Handle Dates?
                           		param.setTextValue(val);
                       		}
                       	} 

                       	try {
                       		Hashtable<String,Object> h = new Hashtable<String,Object>();
                    		
                    		h.put("ideal", jp.getBoolean("alert_ideal"));
                    		h.put("p1", jp.getInt("alert_p1"));
                    		h.put("p2", jp.getInt("alert_p2"));
                    		p.disk_alerts = h;
                    		
                       	} catch (Exception e) {
                       		
                       	}
      
               		ok++;
               		saveSlot(currentSlot,true);

                	} catch (Exception e) {
            			this.deleteSlotmemory(currentSlot);
                       	er++;
                    }
        		}
        	}
        	return ok+" created "+er+" errors";
        	
    	} catch (Exception e) {
    		return "Problem";
    	}
    	
    }
    
    public String slotsAsJson64() {
    	int savedSlots = getNoSavedSlots();

    	try {
        	JSONObject jps=new JSONObject();
        	JSONArray params = new JSONArray();
       		for (int i=0;i<savedSlots;i++) {
    			Provider p = loadSlot(i);
    	    	params.put(i,p.toJSON());
       		}   	
       		jps.put("Providers", params);
       		// Encrypt
       		
       		String jp=jps.toString();
       		String ep=Security.encryptBase64(this.pinCode, jp);
       		
       		return ep;
       		
    	} catch (Exception e) {
    		return null;
    	}
   		
    }
    
    public void InitLoadSlots() {
    	
    		
    	// Load up all Slots
    	int savedSlots = getNoSavedSlots();
    	
    	if (savedSlots==0) {
    		// Change this to show the Local Data Usage provider
    		// CreateDummyProviders();
     		Provider p = addSlot();
     		
      		ProviderManager.getInstance().ResetProvider(p, ProviderManager.LOCAL_DATA_USAGE);
      	 	for (int i=0;i<p.disk_parameters.params.size();i++) {
        		parameter param = p.disk_parameters.params.get(i);
        		param.checkDefault();
      	 	}
      	 	saveSlot(currentSlot,true);
 
    	} else {
    		
    		for (int i=0;i<savedSlots;i++) {
    			Provider p = loadSlot(i);
    			if (p==null) {
    				UIManager.getInstance().ToastLong("Problem loading slot "+i + "Reason:"+errmsg);
    			} else {
    	   			slotArray.add(p);
    			}
    		}
    		// Get first Slot
    		if (NoSlots()>0){
    	   		getSlot(0);
    		}
    		
    	}
    	
    }
    
    
    public int NoSlots() {
    	return slotArray.size();
    }
    
    
   public Provider getSlot (int slot) {
    	if (slot>=slotArray.size() || slot<0) {
    		Log.e(TAG,"getSlot - invalid slot requested");
    		return null;	
    	} else {
    		currentProvider =  slotArray.get(slot);
    		currentSlot = slot;
    		return currentProvider;
    	}
    }
    
   public int nextSlot() {
		if (currentSlot<this.NoSlots()-1) {
			return currentSlot+1;
		} else {
			return 0;
		}
   }
   
   public void saveAllSlots() {
      Log.i(TAG,"SaveAllSlots not implemented");
      for (int i=0;i<slotArray.size();i++) {
    	  saveSlot(i,false);
      }
      updateNoSavedSlots();
   }
    
   
    public Provider addSlot()  {
    	
    	// Load up a new Slot
    	Provider p = ProviderManager.getInstance().CreateProvider(-1);
    	slotArray.add(p);
    	
    	p.slotnumber = slotArray.size();
       	p.isvalid=false;
           	
    	// Setup where to cache files
    	return getSlot(p.slotnumber-1);
    	
    }
    
    // 1 Based
    public void deleteSlotSaveAll(int row) {
    	Log.i(TAG,"Deleting slot"+row);
    	slotArray.remove(row);
    	this.saveAllSlots();
    }
    
    public void deleteSlotmemory(int row) {
       	slotArray.remove(row);         
    }  
    
    private String providerFile(int slot) {
       	return CacheManager.getInstance().SettingsPath(slot, "provider.es");
    }
    
    
    private int getNoSavedSlots() {
    	int noSlots;
    	noSlots = preferences.getInt(PREF_NOSLOTS, 0);
    	return noSlots;
    }
    
    private void updateNoSavedSlots() {

    	Editor ed = preferences.edit();
    	ed.putInt(PREF_NOSLOTS,slotArray.size());
    	ed.commit();
       	
    }
    
    
    private Cipher getCode(boolean open) {
    	
    	Cipher des=null;
    	
    	try {
            DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey desKey = keyFactory.generateSecret(desKeySpec);
              
            // Use Data Encryption Standard.
            des = Cipher.getInstance("DES/ECB/PKCS5Padding");
            
            if (!open) {
                des.init(Cipher.ENCRYPT_MODE, desKey);
            } else {
                des.init(Cipher.DECRYPT_MODE, desKey);
            }
    	} catch (Exception e) {
    		Log.e(TAG,"Encryption problem");
    	}
        
        return des;
        
    }
    
    
    public boolean saveSlot(int row,boolean clearcache) {
    	Provider p = slotArray.get(row);
    	try {
    		
    		p.slotnumber=row;
        	//FileOutputStream fos = myapp.openFileOutput(providerFile(row), Context.MODE_PRIVATE);
        	
          	FileOutputStream fos = new FileOutputStream(providerFile(row));
        	ObjectOutputStream oos = new ObjectOutputStream(fos);
        	
            SealedObject so = new SealedObject(p, getCode(false));
        	
        	oos.writeObject(so);
        	oos.close();
        	
        	updateNoSavedSlots();
        	
        	// Clear Cache
        	if (clearcache) {
            	CacheManager.getInstance().RemoveCache(row);
            	p.cacheFileDate=null;
        	}
        	
        	return true;
    	} catch (Exception e) {
    		Log.e(TAG,"Could not save Provider "+row + "reason: "+e.toString());
    		return false;
    	}
    }
    
    
    
    public Provider loadSlot(int row) {
    	Provider p=null;
    	
    	try {
    		//FileInputStream fos = myapp.openFileInput(providerFile(row));
       		FileInputStream fos = new FileInputStream(providerFile(row));
       	    		
    		ObjectInputStream oos = new ObjectInputStream(fos);
    		
    		SealedObject o = (SealedObject)oos.readObject();
          	oos.close();
                      
            p = (Provider)o.getObject(getCode(true));
     		
            // Try to load the Cached Data
            p.slotnumber=row;
            CacheManager.getInstance().ReadCachedProviderData(p);
            
     	   
        	return p;
        	
    	} catch (Exception e) {
    		errmsg=e.toString();
    		
    		Log.e(TAG,"Could not load Provider "+row + "reason"+e.toString());
    		return null;
    	}
    }
    
    
    public static SlotManager getInstance() {
       if(INSTANCE == null) {
          INSTANCE = new SlotManager();
       }
       return INSTANCE;
    }


    @SuppressWarnings("unchecked")
	public void DummyData(Provider p) {
    	
    	Random randomGenerator = new Random();
    	   
    	 if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
    		 
    	    if (p.disk_isp==800) {
    			 
    		} else {
     		  	p.current_cycle = new cycle();
    	    	p.current_cycle.initWithAnniversaryDay(Utils.RandomInteger(1, 31, randomGenerator));
    	    	// Add ProgressBars
    	    	
    			p.progress = new ArrayList<kbProgressBar>();
    	  	
    	    	kbProgressBar pb = new kbProgressBar();
    	       	p.progress.add(pb);
    	        
    	       	pb.pid = 0;
    	    	pb.type=0;
    	    	pb.name = "Peak";
    	    	pb.value_max = 50000;
    	    	pb.value_value = Utils.RandomInteger(0, 50000, randomGenerator);
    	    	pb.used = true;
    	    	pb.outtype = 7;
    	    	pb.cycle_usage = p.current_cycle;
    	    	pb.UpdateValues();
    	    	
    	    	// 2 
    	       	kbProgressBar pb2 = new kbProgressBar();
    	       	p.progress.add(pb2);
    	 
    	       	pb2.pid = 1;
    	       	pb2.type=0;
    	       	pb2.name = "Off-Peak";
    	       	pb2.value_max = 150000;
    	       	pb2.value_value = Utils.RandomInteger(0, 150000, randomGenerator);
    	       	pb2.used = true;
    	       	pb2.outtype = 7;
    	       	pb2.cycle_usage = p.current_cycle;
    	       	pb2.UpdateValues();
    	       	
    	       	//
    	       	// Create Extras
    	       	//
    	       	p.clearExtras();
    	       	p.addExtra(0,"Connected since","25/2/2011 04:48PM");
    	     	p.addExtra(0,"IP Address","127.4.5.8");
    	    	p.addExtra(0,"Peak Shaped","No");
    	    	p.addExtra(0,"Off-Peak Shaped","No");
    	       	p.addExtra(0,"Freezone","22GB");
    	       	p.addExtra(0,"Off Peak Period","02:00-08:00");
    	      	p.addExtra(0,"Plan","Home3");				
    		}
  
	      	
	      	
    	 } else if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
    		// Create Account Data
    		account ad = new account();
    		
    		ad.bal1value = String.valueOf(Utils.RandomInteger(0, 2500, randomGenerator));
    		ad.bal2value = "";
    		p.accountdata = ad;
    		
    	 }
  
      	
    }
    
    
    public void CreateDummyProviders() {
    	
    	// Add a new Slot
    	addSlot();
    	
    	Provider p = currentProvider;
    	
    	// Setup Cycle
    	ProviderManager.getInstance().ResetProvider(p, 0);
     	p.disk_planname = "iiNet Home";
        
     	DummyData(p);
    		
   	}
    
    
 }
