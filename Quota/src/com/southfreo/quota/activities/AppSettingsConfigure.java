package com.southfreo.quota.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import com.southfreo.R;
import com.southfreo.quota.Dropbox.DropboxController;
import com.southfreo.quota.control.CacheManager;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.utils.DownloadUtils;
import com.southfreo.quota.utils.UnZip;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.utils.DownloadUtils.DownloadEvent;
import com.southfreo.quota.utils.UnZip.UnZipEvent;
import com.southfreo.quota.widgets.IconPreference;
import com.southfreo.quota.widgets.ListPreferenceValueNoParam;
import com.southfreo.quota.control.ApplictionObject;


public class AppSettingsConfigure extends PreferenceActivity {

	private final static String TAG = "Quota-AppSetttingsConfigure";
	
	public final static String UPDATE_PERIOD = "updateperiod";
	public final static String DISABLE_SERVICE = "disableservice";
	public final static String WIFI_ONLY = "wifionly";
	public final static String PIN_CODE = "scode";
	public final static String PIN_CODE_EXPIRY = "scode_expiry";
	
	private SlotManager sm;
	private ListPreferenceValueNoParam updateTime;
	private ListPreferenceValueNoParam pinExpiry;
	private CheckBoxPreference disableService;
	private CheckBoxPreference wifiOnly;
	private PreferenceScreen pinScreen;
	private PreferenceScreen dropBoxScreen;

	public  static long THIRTY_SECONDS   = 30*1000;
	public  static long ONE_MINUTE 		 = 60*1000;
	public  static long FIVE_MINUTES 	 = 5*(60*1000);
	public  static long TEN_MINUTES 	 = 10*(60*1000);
    public static final long THREE_HOURS = 3*AlarmManager.INTERVAL_HOUR;
    
	private static String[] updateTitles = {
								     "30 seconds",
									 "1 Minute",
								     "5 Minutes",
									 "10 Minutes",
									 "15 Minutes",
									 "30 Minutes",
									 "Hourly",
									 "Every 3 Hours",
									 "12 Hours"};
	
	private static long[] updateValues = {
										
										THIRTY_SECONDS,
										ONE_MINUTE,
										FIVE_MINUTES,
										TEN_MINUTES,
										AlarmManager.INTERVAL_FIFTEEN_MINUTES,
										AlarmManager.INTERVAL_HALF_HOUR,
										AlarmManager.INTERVAL_HOUR,
										THREE_HOURS,
										AlarmManager.INTERVAL_HALF_DAY};
  
	private static String[] pin_updateTitles = {
	     "Instant",
	     "30 seconds",
		 "1 Minute",
	     "5 Minutes",
		 "10 Minutes",
		 "15 Minutes",
		 "30 Minutes",
		 "Hourly",
		 "Every 3 Hours",
		 "12 Hours"};

	
	private static long[] pin_updateValues = {
			0,
			THIRTY_SECONDS,
			ONE_MINUTE,
			FIVE_MINUTES,
			TEN_MINUTES,
			AlarmManager.INTERVAL_FIFTEEN_MINUTES,
			AlarmManager.INTERVAL_HALF_HOUR,
			AlarmManager.INTERVAL_HOUR,
			THREE_HOURS,
			AlarmManager.INTERVAL_HALF_DAY};

	
	  public static Intent createIntent(Context context) {
	      Intent i = new Intent(context, AppSettingsConfigure.class);
	      return i;
	  }
	  
	  public static String periodToString(long v) {
	   		for (int i=0;i<updateValues.length;i++) {
	   			if (updateValues[i]==v) {
	   				return updateTitles[i];
	   			}
	   		}
	   		return updateTitles[0];
	  }
	  
	  public static String pin_periodToString(long v) {
	   		for (int i=0;i<pin_updateValues.length;i++) {
	   			if (pin_updateValues[i]==v) {
	   				return pin_updateTitles[i];
	   			}
	   		}
	   		return pin_updateTitles[0];
	  }
	  
	  private void LoadUserUpdate(String file) {
  		  // Do something with value!
		  Log.i(TAG,"Loading user pack"+file);
		  String url = String.format("http://quotaxml.southfreo.com/user_updates/v12_3/%s", file);
		  final String des = CacheManager.getInstance().CachePath(file);
		  
		  final UnZipEvent ue = new UnZip.UnZipEvent() {

			@Override
			public void unzipComplete(boolean ok) {
				// TODO Auto-generated method stub
				if (ok) {
					ProviderManager.getInstance().LoadUserPacks(CacheManager.getInstance().UserPath());
				}
			}
		  };
		  
		  
		  DownloadEvent de = new DownloadUtils.DownloadEvent() {

			@Override
			public void downloadComplete(boolean ok, String msg) {
				// TODO Auto-generated method stub
				if (ok) {
					  UnZip zip = new UnZip(AppSettingsConfigure.this,ue);
					  zip.unzipFile(des, CacheManager.getInstance().UserPath());

				} else {
					  UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "Load User XML", "Error : "+msg);
				}
			}
			  
		  };
		  
		  // Kick Off Download
		  DownloadUtils du = new DownloadUtils(this,de);
		  du.downloadFile(url, des);

	  }
	  
	  
	  
	  
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Saved Options
        
 		long cp = SlotManager.getInstance().preferences.getLong(UPDATE_PERIOD, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
 		long pe = SlotManager.getInstance().pinexpiryseconds;
 		
 		boolean disableFlag = SlotManager.getInstance().preferences.getBoolean(DISABLE_SERVICE, false);
		boolean wifiFlag = SlotManager.getInstance().preferences.getBoolean(WIFI_ONLY, false);
		 		
 		
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        
        
        // Update Period
        updateTime = new ListPreferenceValueNoParam(this);
        updateTime.setEntries(updateTitles);
        updateTime.setValue(AppSettingsConfigure.periodToString(cp));
        updateTime.setEntryValues(updateTitles);
        updateTime.setDialogTitle("Update Interval");
        updateTime.setTitle("Update Interval");
        updateTime.setSummary("Time interval to check if providers cache has expired.");
        
        
        disableService = new CheckBoxPreference(this);
        disableService.setTitle("Disable Service");
        disableService.setSummary("Quota will not update in the background and issue notifications.");
        disableService.setChecked(disableFlag);

        wifiOnly = new CheckBoxPreference(this);
        wifiOnly.setTitle("Wifi Only");
        wifiOnly.setSummary("Only Check for updates when connected to Wifi (except Local Data Usage)");
        wifiOnly.setChecked(wifiFlag);
        
        pinScreen = getPreferenceManager().createPreferenceScreen(this);
        pinScreen.setTitle("PIN");
        pinScreen.setSummary("Setup/Modify PIN settings");
 
        pinExpiry = new ListPreferenceValueNoParam(this);
        pinExpiry.setEntries(pin_updateTitles);
        pinExpiry.setValue(AppSettingsConfigure.pin_periodToString(pe));
        pinExpiry.setEntryValues(pin_updateTitles);
        pinExpiry.setDialogTitle("PIN Expiry");
        pinExpiry.setTitle("PIN Expiry");
        pinExpiry.setSummary("When a PIN has been set, choose the expiry time");
 
        dropBoxScreen = getPreferenceManager().createPreferenceScreen(this);
        dropBoxScreen.setTitle("Backup/Restore");
        dropBoxScreen.setSummary("Backup your settings via DropBox");
        
        dropBoxScreen.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	// Check Pin has been setup
            	if (!SlotManager.getInstance().hasPin()) {
            		UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "PIN Required", "You must setup a PIN in order to use this screen");
            		return true;
            	} else {
                    Intent i = new Intent(AppSettingsConfigure.this,DropboxController.class); 
                    preference.setIntent(i); 
                    startActivityForResult(i,0);
                    return true;
            	}
            }
        });

        
        pinScreen.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
             	if (!UIManager.getInstance().CheckPerformanceFull()) {
            		UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "Unavailable", "The feature is only available in the full version of Quota");
            		return true;
             	}
             	
                Intent i = new Intent(AppSettingsConfigure.this,PinEntry.class); 
                preference.setIntent(i); 
                if (SlotManager.getInstance().hasPin()) {
                    i.putExtra(PinEntry.MODE_OP, PinEntry.MODE_CHANGE);
                } else {
                    i.putExtra(PinEntry.MODE_OP, PinEntry.MODE_CREATE);
                }
                startActivityForResult(i,0);
                return true;
            }
        });
        
        // Disclaimer
        PreferenceScreen disclaim = getPreferenceManager().createPreferenceScreen(this);
        disclaim.setTitle("Disclaimer");
        disclaim.setSummary("Tap to read"); 
        
        disclaim.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

            	UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "Disclaimer", getString(R.string.disclaimer));
            	return true;
            }
        });
 
        
        
        PreferenceScreen follow = getPreferenceManager().createPreferenceScreen(this);
        follow.setTitle("Follow us on Twitter");
        follow.setSummary("Product announcements and general news from SouthFreo software"); 
        
        follow.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

            	UIManager.getInstance().openWebsite(AppSettingsConfigure.this, "http://twitter.com/southfreo");
            	return true;
            }
        });
        
        
        
        PreferenceScreen discuss = getPreferenceManager().createPreferenceScreen(this);
        discuss.setTitle("Discuss on Whirlpool");
        discuss.setSummary("Discussion forum for this application"); 
        
        discuss.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

            	UIManager.getInstance().openWebsite(AppSettingsConfigure.this, "http://forums.whirlpool.net.au/forum-replies.cfm?t=1655181&p=-1#bottom");
            	return true;
            }
        });
        
        
        PreferenceScreen create = getPreferenceManager().createPreferenceScreen(this);
        create.setTitle("Create your own provider");
        create.setSummary("New providers can be created with the QuotaXML developers kit, Download today!"); 
        
        create.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

            	UIManager.getInstance().openWebsite(AppSettingsConfigure.this, "http://www.southfreo.com/iiquota/Development_Kit.html");
            	return true;
            }
        });
        
        // Load Provider Pack....
        PreferenceScreen lpp = getPreferenceManager().createPreferenceScreen(this);
        lpp.setTitle("Load QuotaXML pack");
        lpp.setSummary("Load a QuotaXML pack (.zip file) from the FTP site ftp.southfreo.com"); 
        
        lpp.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

            	AlertDialog.Builder builder = new AlertDialog.Builder(AppSettingsConfigure.this);
            	builder.setTitle("Enter pack name \n(e.g. canada.zip)");
            	final EditText input = new EditText(AppSettingsConfigure.this);
            	builder.setView(input);
            	
            	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {
            		  String value = input.getText().toString().toLowerCase();
            		  dialog.dismiss();
            		  LoadUserUpdate(value);
            		}
            		
            	});
            	AlertDialog alert = builder.create();
            	alert.show();
            	
            	return true;
            }
        });
        
        PreferenceScreen rup = getPreferenceManager().createPreferenceScreen(this);
        rup.setTitle("Remove User Packs");
        rup.setSummary("Remove all downloaded userpacks"); 
        rup.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	CacheManager.getInstance().RemoveUserPacks();
            	UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "UserPacks", "All UserPacks have been removed, please restart Quota");
            	return true;
            }
        });
        
        PreferenceScreen rph = getPreferenceManager().createPreferenceScreen(this);
        rph.setTitle("Remove Purchase History");
        rph.setSummary("Remove purchase history"); 
        rph.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	UIManager.getInstance().deletePurchases(AppSettingsConfigure.this);
            	UIManager.getInstance().MsgBoxInfo(AppSettingsConfigure.this, "Purchase History", "Purchase History has been removed");
            	return true;
            }
        });

        
        // Categories
        PreferenceCategory generalCat = new PreferenceCategory(this);
        generalCat.setTitle("General");
        root.addPreference(generalCat);
               
        generalCat.addPreference(updateTime);
        generalCat.addPreference(disableService);
        generalCat.addPreference(wifiOnly);
        generalCat.addPreference(pinScreen);
        generalCat.addPreference(pinExpiry);
        generalCat.addPreference(dropBoxScreen);
        
        PreferenceCategory supportCat = new PreferenceCategory(this);
        supportCat.setTitle("Support");
        root.addPreference(supportCat);
        
        
        supportCat.addPreference(disclaim);
        supportCat.addPreference(follow);
        supportCat.addPreference(discuss);
        
        // Advanced
        PreferenceCategory adCat = new PreferenceCategory(this);
        adCat.setTitle("Advanced");
        root.addPreference(adCat);
        
        adCat.addPreference(create);
        adCat.addPreference(lpp);
        adCat.addPreference(rup);
        adCat.addPreference(rph);          
        setPreferenceScreen(root);
    }
 	
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (   keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
  
        	if (!Utils.isBlank(updateTime.getValue())) {
        		// Write Update Period
        		ApplictionObject ap=(ApplictionObject)getApplication();

        		boolean disable = disableService.isChecked();
         		SlotManager.getInstance().preferences.edit().putBoolean(DISABLE_SERVICE,disable).commit();
         		        		
         		// Wifi only
        		SlotManager.getInstance().preferences.edit().putBoolean(WIFI_ONLY,wifiOnly.isChecked()).commit();
        		         		
        		// Update Time
          		int sn = updateTime.findIndexOfValue(updateTime.getValue());
        		long timePeriod = updateValues[sn];
          		SlotManager.getInstance().preferences.edit().putLong(UPDATE_PERIOD,timePeriod).commit();

          		// Pin Expiry
          		int pe = pinExpiry.findIndexOfValue(pinExpiry.getValue());
        		long pinPeriod = pin_updateValues[pe];
          		SlotManager.getInstance().SavePinExpiry(pinPeriod);

          		
         		if (disable) {
         			ap.stopService();
        		} else {
              		ap.restartUpdate();
        		}
          		
          		
          		
        	}
            //return true;
        }
       return super.onKeyDown(keyCode, event);
    }
  
    
}
