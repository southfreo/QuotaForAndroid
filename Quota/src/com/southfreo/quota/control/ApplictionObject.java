package com.southfreo.quota.control;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.southfreo.R;
import com.southfreo.quota.activities.AppSettingsConfigure;
import com.southfreo.quota.billing.PurchaseDatabase;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Security;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.utils.jsHelper;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ApplictionObject extends Application {
	   private static final String TAG = "Quota-Application";
	   private QuotaService mQuotaService;
	   public PurchaseDatabase mPurchaseDatabase; 

	   public static final String LICENCE_INTENT = "com.southfreo.quotalicence.quotalicence";
	   
	   private ServiceConnection mConnection = new ServiceConnection() {
		    public void onServiceConnected(ComponentName className, IBinder service) {
		        // This is called when the connection with the service has been
		        // established, giving us the service object we can use to
		        // interact with the service.  Because we have bound to a explicit
		        // service that we know is running in our own process, we can
		        // cast its IBinder to a concrete class and directly access it.
		    	mQuotaService = ((QuotaService.LocalBinder)service).getService();
			    mQuotaService.startMyService(ApplictionObject.this);
			    
			    pm.mQuotaService = mQuotaService;

		    	//Toast.makeText(ApplictionObject.this, "ServiceConnection", Toast.LENGTH_LONG);
		    }

		    public void onServiceDisconnected(ComponentName className) {
		        // This is called when the connection with the service has been
		        // unexpectedly disconnected -- that is, its process crashed.
		        // Because it is running in our same process, we should never
		        // see this happen.
		    	mQuotaService = null;
		    }
		};
		
	   
	   // My Instance Vars
	   ProviderManager pm;
	   SlotManager sm;
	   UIManager ui;
	   CacheManager cm;
	   
	   private boolean mIsBound;
	   
	   void doBindService() {
		    // Establish a connection with the service.  We use an explicit
		    // class name because we want a specific service implementation that
		    // we know will be running in our own process (and thus won't be
		    // supporting component replacement by other applications).
		    bindService(new Intent(ApplictionObject.this, QuotaService.class), mConnection, Context.BIND_AUTO_CREATE);
		    mIsBound = true;
		}
	   

		void doUnbindService() {
		    if (mIsBound) {
		        // Detach our existing connection.
		        unbindService(mConnection);
		        mIsBound = false;
		    }
		}
		
		public void startQuotaService() {
			   boolean disableFlag = SlotManager.getInstance().preferences.getBoolean(AppSettingsConfigure.DISABLE_SERVICE, false);
			   if (!disableFlag) {
			       startService(new Intent(this, QuotaService.class));
			       doBindService();
			   }
		}
		
		public void restartUpdate() {
			if (mQuotaService!=null) {
				mQuotaService.restartService();
			} else {
				startQuotaService();
			}
		}
		
		public void pauseService() {
			if (mIsBound) {
				if (mQuotaService!=null) {
					mQuotaService.pauseService();
				}
			}
		}
			
		public void unpauseService() {
			if (mIsBound) {
				if (mQuotaService!=null) {
					mQuotaService.unPauseService();
				}
			}
		}
		
		
		public void stopService() {
			if (mIsBound) {
				if (mQuotaService!=null) {
					mQuotaService.stopTimer();
				}
				doUnbindService();
				pm.mQuotaService=null;
				stopService(new Intent(ApplictionObject.this, QuotaService.class));
			}
			
		}
		
			
		public boolean happyChappy() {
			 return false;
		}
		
		
	    /**
	     * Called when the application is starting, before any other application
	     * objects have been created.  Implementations should be as quick as
	     * possible (for example using lazy initialization of state) since the time
	     * spent in this function directly impacts the performance of starting the
	     * first activity, service, or receiver in a process.
	     * If you override this method, be sure to call super.onCreate().
	     */
	   @Override
	    public void onCreate() {
	    	// Create my singletons here
		   super.onCreate();
		   try {
			   // Check Database for Upgrade Key
			   
			   // Test Code
			   
			   
		       sm = SlotManager.getInstance();
			   sm.myapp = this;
			   sm.InitPreferences();
			   
		       ui = UIManager.getInstance();
		       ui.myapp = this;

		       // UnLock
		       mPurchaseDatabase = new PurchaseDatabase(this);
			   ui.isUnlocked = mPurchaseDatabase.up001();
			   //ui.isUnlocked=true;
			   
		       
		       pm = ProviderManager.getInstance();
		       
		       boolean ok = pm.LoadProvidersFromResource(this,R.xml.qproviders,false,false,false);
		       
		       if (!ok) {
		    	   Toast.makeText(this, "Problem loading Providers"+pm.xmlLoadMsg, Toast.LENGTH_LONG);
		       }
		       
			   // Cache
			   cm  = CacheManager.getInstance();
			   cm.InitCache(this);

			   if (cm.hasUserPacks()) {
				   pm.LoadUserPacks(cm.UserPath());
			   }
			   
			   // Load Slots
			   sm.InitLoadSlots();
	
		       // Start Update Service
			   startQuotaService();
			   
		   } catch (Exception e) {
		       Log.e(TAG, "Application.onCreate()",e);
		   }
	       
	    }

	    /**
	     * Called when the application is stopping.  There are no more application
	     * objects running and the process will exit.  <em>Note: never depend on
	     * this method being called; in many cases an unneeded application process
	     * will simply be killed by the kernel without executing any application
	     * code.</em>
	     * If you override this method, be sure to call super.onTerminate().
	     */
	   @Override
	    public void onTerminate() {
	    	// Terminate Singletons
		   super.onTerminate();
		   
	    }
	    
	   @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	    }
	    
	   @Override
	    public void onLowMemory() {
	    	
	    }
}
