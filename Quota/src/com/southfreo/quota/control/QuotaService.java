package com.southfreo.quota.control;

import com.southfreo.R;
import com.southfreo.quota.activities.AppSettingsConfigure;
import com.southfreo.quota.activities.DetailActivity;
import com.southfreo.quota.activities.SummaryActivity;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.utils.NetworkUtils;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class QuotaService extends Service {

	private static final String TAG = "Quota-QuotaService";

	   public ProviderManager pm;
	   public SlotManager sm;
	   public UIManager ui;
	   public CacheManager cm;

	   private Timer timer=new Timer();    
	   //private static long UPDATE_INTERVAL = 1*10*1000;  //default
	   private static long DELAY_INTERVAL = 0;  
	   private NotificationManager mNotificationManager;
   	   private static final int NOTIFY_ID = 9999;

	   private Handler handler = new Handler();
	   private boolean paused=false;
	   private boolean wifionly=false;
	   
	   private final Handler toastHandler = new Handler()
	    {
	        public void handleMessage(Message msg)
	        {
	            Toast.makeText(getApplicationContext(), "QuotaTimer", Toast.LENGTH_SHORT).show();
	        }
	    };
	    
	    public class LocalBinder extends Binder {
	    	QuotaService getService() {
	            return QuotaService.this;
	        }
	    }
	    
	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }

	    private final IBinder mBinder = new LocalBinder();
	    
	    public void pauseService() {
	    	synchronized (this) {
		    	paused=true;
	    	}
	    }
	    
	    public void unPauseService() {
	    	synchronized (this) {
	    		paused=false;
	    	}
	    }
	    
	    public void NotifyMsg(String msg) {
	    	int icon = R.drawable.notify;        // icon from resources
	    	
	    	Notification notification = new Notification(icon, msg, System.currentTimeMillis());
	    	
	    	Context context = getApplicationContext();      // application Context
	    	CharSequence contentTitle = "Quota";  // expanded message title
	    	CharSequence contentText = msg;      // expanded message text

			Intent intent = SummaryActivity.createIntent(context);
	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
	        
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        
	    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	    	mNotificationManager.notify(NOTIFY_ID, notification);
	    }
	    
	    
	    public void NotifyProblemProvider(int slot,String title,String msg,String ticker) {
	    	int icon = R.drawable.notify_red;        // icon from resources
	    	
	    	Notification notification = new Notification(icon, ticker, System.currentTimeMillis());
	    	
	    	Context context = getApplicationContext();      // application Context
	    	CharSequence contentTitle = title;  			// expanded message title
	    	CharSequence contentText = msg;      			// expanded message text

			Intent intent = DetailActivity.CreateDetailScreen(context, slot);
	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);


	        notification.ledARGB = 0xFFff0000;
	        notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
	        notification.ledOnMS = 100; 
	        notification.ledOffMS = 100; 
	        
	    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	    	mNotificationManager.notify(slot, notification);
	    }
	    
	    
	    
	    private void startUpdateTimer() {
	 		long updateperiod = SlotManager.getInstance().preferences.getLong(AppSettingsConfigure.UPDATE_PERIOD, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
		    // Find Time Period
		    timer.scheduleAtFixedRate(new MyTimerTask(),DELAY_INTERVAL,updateperiod);
		    String tp = AppSettingsConfigure.periodToString(updateperiod);
		    //NotifyMsg("Started: Updates every "+tp);
		    wifionly = SlotManager.getInstance().preferences.getBoolean(AppSettingsConfigure.WIFI_ONLY, false);

		    paused=false;
	    }
	    
	    public class MyTimerTask extends TimerTask {
	        private Runnable runnable = new Runnable() {
	            public void run() {
	    			//Log.d(TAG, "Timer()");
				    //NotifyMsg("Checking Providers ");
    	 		
	            	if (paused) {
	            		Log.i(TAG,"Quotaservice paused - ingoring timer");
	            		return;
	            	}
	            	
	            	
				    for (int i=0;i<sm.NoSlots();i++) {
              			Provider p = sm.slotArray.get(i);
              			if (p.hasCacheExpired()) {
              				// Check Network first
         					if (NetworkUtils.isOnline(getApplicationContext())) {
         						// Check this is valid
         						if (wifionly) {
         							// Wifi Only Setting
         							if (p.disk_isp==ProviderManager.LOCAL_DATA_USAGE || NetworkUtils.isOnlineWifi(getApplicationContext())) {
             							// On Wifi Schedule
                              			pm.Queue_ScheduleUpdate(p);
                              		}
         						} else {
                        			pm.Queue_ScheduleUpdate(p);
         						}
        				    }
             			}
              		}
              		
	            }
	        };

	        public void run() {
	            handler.post(runnable);
	        }
	    }
	    
	    public void stopTimer() {
	    	timer.cancel();
	    }
	    
	    
	    public void restartService() {
	    	timer.cancel();
	    	timer = new Timer();
	    	startUpdateTimer();
	    }
	    
	    
	    public void startMyService(Application app) {
			Log.d(TAG, "started");
		    pm = ProviderManager.getInstance();
		    sm = SlotManager.getInstance();
		    String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			
			startUpdateTimer();
	    }
	    
	    
 		@Override
		public void onCreate() {
			Log.d(TAG, "onCreate");
			
			//handler = new Handler(Looper.getMainLooper());
		}

		@Override
		public void onDestroy() {
			Log.d(TAG, "onDestroy");
			NotifyMsg("Quota Service stopped.");
	        //Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onStart(Intent intent, int startid) {
			Log.d(TAG, "onStart()");
		}
	}
