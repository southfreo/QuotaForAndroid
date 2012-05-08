package com.southfreo.quota.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.cycle;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Utils;


public class CacheManager {
    private static CacheManager INSTANCE = null;
	private static final String TAG = "Quota-CacheManager";
 	public File filesDir;
 	public File cacheDir;
	 	
 	private static final String cacheNameProvider = "Provider";
 	private static final String cacheUsageName = "Usage.html";
 	private static final String cacheDebugName = "Debug.txt";
	
    private CacheManager() {
       // Exists only to defeat instantiation.
   }
    
    public static CacheManager getInstance() {
       if(INSTANCE == null) {
          INSTANCE = new CacheManager();
       }
       return INSTANCE;
    }

    
    public void WriteCachedProviderData(Provider p) {
    	// Get Cache Path
    	String cp=CachePath(p.slotnumber,cacheNameProvider);
    	
    	try {
        	FileOutputStream fos = new FileOutputStream(cp);
        	ObjectOutputStream oos = new ObjectOutputStream(fos);
    		p.writeCachedData(oos);
    		oos.close();
    	} catch (Exception e) {
    		Log.e(TAG,"Could not write Cached output data"+e.toString());
    	}
    }
    
    public String UsageFile(Provider p) {
    	return CachePath(p.slotnumber,cacheUsageName);
    }

    public String DebugFile(Provider p) {
    	return CachePath(p.slotnumber,cacheDebugName);
    }
    
    public void DeleteDebugFile(Provider p) {
    	File f = new File(DebugFile(p));
    	try {
    		f.delete();
    	} catch (Exception e){
    	}
    }

    public void WriteDebugLog(Provider p,String s,boolean append) {
    	
    	try {
    		FileWriter fwriter=null;
    		if (!append) {
    			DeleteDebugFile(p);
    		}
        	fwriter = new FileWriter(DebugFile(p),append);
    		BufferedWriter writer = new BufferedWriter(fwriter);
    		writer.write(s);
    		writer.close();
    	} catch (Exception e) {
    		Log.e(TAG,"Could not write Cached output data"+e.toString());
    	}
    }
   
    
    public void WriteCachedusageData(Provider p,String s,String cp,int number) {
    	
    	try {
    		FileWriter fwriter=null;
    		
    		if (number==0) {
    			fwriter = new FileWriter(cp,false);
    		} else {
       			fwriter = new FileWriter(cp,true);
    		}
    		BufferedWriter writer = new BufferedWriter(fwriter);
    		
    		if (number>0)  {
    			writer.write("\n\n<!-- QUOTAURL_START: " + number + "-->\n");
    		}
    		writer.write(s);
    		writer.close();
    	} catch (Exception e) {
    		Log.e(TAG,"Could not write Cached output data"+e.toString());
    	}
    }
   
    public String copyToSDCard(String file,String filedest) {
    	
    	try {
    		File sd = Environment.getExternalStorageDirectory();
    		String c2 = sd.toString()+"/"+filedest;
    		
    		if (sd.canWrite()) {
    			Utils.copyFile(new File(file), new File(c2));
    			return c2;
    		}
    		return null;
    		} catch (Exception e) {
    			Log.i(TAG,"Could not copy file to SD"+e);
    			return null;
    		}
    }
    
    
    public void ReadCachedProviderData(Provider p) {
       	String cp=CachePath(p.slotnumber,cacheNameProvider);
       	try {
        	FileInputStream fos = new FileInputStream(cp);
        	ObjectInputStream ois = new ObjectInputStream(fos);
        	p.readCachedData(ois);
    		ois.close();
    	} catch (Exception e) {
    		Log.e(TAG,"Could not read Cached output data"+e.toString());
    	}
    }
    
    
    public void InitCache(Application myApp) {
    	filesDir = myApp.getFilesDir();
    	cacheDir = myApp.getCacheDir();
    }
    
    private String CacheDir(String name,int slot) {
    	return cacheDir.getPath()+"/"+ name +"/"+String.valueOf(slot+1);
    }

    private String CacheDir(String name) {
    	return cacheDir.getPath()+"/"+ name;
    }
    
    private String FilesDir(String name,int slot) {
    	return filesDir.getPath()+"/"+ name +"/"+String.valueOf(slot+1);
    }

    private String FilesDir(String name) {
    	return filesDir.getPath()+"/"+ name;
    }
    
    
    public String SettingsPath(int sn,String name) {
       	File f = new File(FilesDir("Provider",sn));
      	if (!f.exists()) {
    		f.mkdirs();
    	}
      	return f.getPath()+"/"+name;
    }
    
   // Zero Based
   public String CachePath(int sn,String name) {
    	File f = new File(CacheDir("Cache",sn));
      	if (!f.exists()) {
    		f.mkdirs();
    	}
      	return f.getPath()+"/"+name;
    }

   public String CachePath(String name) {
   	File f = new File(CacheDir("Cache"));
     	if (!f.exists()) {
   		f.mkdirs();
   	}
    return f.getPath()+"/"+name;
   }
   
   
   public String UserPath() {
   	File f = new File(FilesDir("UserUpdate"));
     	if (!f.exists()) {
   		f.mkdirs();
   	}
    return f.getPath();
   }
   
   public String UserUpdateFile(String file) {
	   return UserPath()+"/"+file;
   }
   

   public boolean hasUserPacks() {
	   return new File(FilesDir("UserUpdate")).exists();
   }
   
   public void RemoveUserPacks() {
	   	File f = new File(FilesDir("UserUpdate"));
     	if (f.exists()) {
   		   Utils.deleteDirectory(f);
   	}
  }

   
   private File getNetStatsFile() {
	   File f = new File(FilesDir("stats",0));
    	if (!f.exists()) {
    		f.mkdirs();
    	}
      	return new File(f.getPath()+"/netstats");
   }
   
   @SuppressWarnings("unchecked")
   public Hashtable<String,?> getStatsDict() {
	   File f = getNetStatsFile();
	   if (f.exists()) {
		    try {
		      	FileInputStream fos = new FileInputStream(f);
	        	ObjectInputStream ois = new ObjectInputStream(fos);
	        	Hashtable h = (Hashtable)ois.readObject();
	    		ois.close();
		    	return h;
		    }  catch (Exception e) {
		    	Log.e(TAG,"Problem reading network stats"+e);
		    	return NetworkUtils.resetNetworkCounters();
		    }
  
	   } else {
		   // Create Dictionary
	    	Log.i(TAG,"NetStats not found init");
		   return NetworkUtils.resetNetworkCounters();
	   }
   }
   
   @SuppressWarnings("unchecked")
   public void writeStatsDict(Hashtable h) {
	   File f = getNetStatsFile();

	  	try {
        	FileOutputStream fos = new FileOutputStream(f);
        	ObjectOutputStream oos = new ObjectOutputStream(fos);
    		oos.writeObject(h);
    		oos.close();
    	} catch (Exception e) {
    		Log.e(TAG,"Could not write Cached netstats"+e);
    	}
   }
   
   
   
   public void RemoveCache(int sn) {
	   	File f = new File(CacheDir("Cache",sn));
      	if (!f.exists()) {
    		f.delete();
    	}
   }

   
 }
