package com.southfreo.quota.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.southfreo.R;
import com.southfreo.quota.billing.PurchaseDatabase;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.utils.Utils;

public class UIManager {
	
	   private static final String TAG = "Quota-UIManager";
	   public boolean isUnlocked;
	   
	   public ApplictionObject myapp;

	   // Singleton
	   
	   public String AppName() {
		   if (isUnlocked) {
			   return "Quota";
		   } else {
			   return "QuotaLite";
		   }
	   }
	   
	    public static boolean XXX_isApplicationBroughtToBackground(final Context context) {
	        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
	        if (!tasks.isEmpty()) {
	            ComponentName topActivity = tasks.get(0).topActivity;
	            if (!topActivity.getPackageName().equals(context.getPackageName())) {
	                return true;
	            }
	        }

	        return false;
	    }
	    
	   private static UIManager INSTANCE = null;

	   private UIManager() {
		   isUnlocked=false;
	   }

	    public void deletePurchases(Context c) {
			myapp.mPurchaseDatabase.close();
			c.deleteDatabase(PurchaseDatabase.DATABASE_NAME);
	    }

	    
	    public boolean checkUnlock() {
			return (this.myapp.mPurchaseDatabase.up001());
	    }
	    
	    public boolean CheckPerformanceFull() {
	    	// TESTING CODE ONLY....
	    	//return true;
	    	return (isUnlocked || checkUnlock());
	    }
	    
	    
	    
	    public static UIManager getInstance() {
	        if(INSTANCE == null) {
	           INSTANCE = new UIManager();
	        }
	        return INSTANCE;
	     }
	    
		public String getVersion(Context c) {
			try {
				return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				// Ignore.
			}
			return "";
		}
		
		public void checkValid() {
		    // Show Welcome or Die
		    Calendar bye = new GregorianCalendar(2013, Calendar.APRIL, 1);
		    Calendar today = Calendar.getInstance();
		    
		    if (today.after(bye)) {
		 	    android.os.Process.killProcess(android.os.Process.myPid());
		    }
		    
		}
		
		public void showProviderChoice(final Provider p,Context c,String[] choices) {

			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Please Choose");
			builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	p.questionAnswered=true;
			    	p.questioninProgress=false;
			    	p.questionanswer = item;
			    	dialog.dismiss();
			    }
			});
			
			AlertDialog alert = builder.create();
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
			    	p.questioninProgress=false;
			    	p.questionAnswered=true;
			    	p.questionanswer=0;		// Default it
			    	dialog.dismiss();
				}
			});
			
			alert.show();
		}
		
	    public String getResourceString(int id) {
	    	return myapp.getString(id);
	    }
	    

	    
	    
	    public void openWebsite(Context c,String s) {
	    	try {
	    	   Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(s));
	    	   browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	   myapp.startActivity(browserIntent);
	    	} catch (Exception e) {
	    		Log.e(TAG,"Could not open website:"+s);
	    	}
	    }
	    
		public void showWelcomeMessage(boolean force,final Context c) {
			final SharedPreferences preferences = SlotManager.getInstance().preferences;
			
			final String current = getVersion(c);
			String version = preferences.getString("version", "");
			// Only shows the dialog if the version strings are different.
		    // Hello Canada
			Locale here=Locale.getDefault();
			boolean canada=here==Locale.CANADA || here==Locale.CANADA_FRENCH;
			//boolean canada=true;
		    if (canada && (!version.equals(current) || force)) {
		    	MsgBoxInfo(c, "Canada Notice", "Canadian Providers can be enabled by tapping \"i\" then Load QuotaXML pack canada.zip");
		    }
		    
			if (!version.equals(current) || force) {
				AlertDialog dialog = new AlertDialog.Builder(c).create();
				dialog.setIcon(R.drawable.icon);
				dialog.setTitle(AppName()+" for Android\n"+getVersion(c));
				//String aTxt = String.format(c.getString(R.string.welcomemsg), UIManager.getInstance().getVersion());
				
				String welcome_msg = null;
				if (this.isUnlocked) {
					welcome_msg = c.getString(R.string.welcomemsg_full);
				} else {
					welcome_msg = c.getString(R.string.welcomemsg_lite);
				}
				
				dialog.setMessage(welcome_msg+"\n\n"+c.getString(R.string.release_notes));
				
				
				dialog.setButton2(c.getString(R.string.close), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						preferences.edit().putString("version", current).commit();
					}
				});
				
				dialog.setButton(c.getString(R.string.website), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						openWebsite(c,c.getString(R.string.support_site));
					}
				});
				dialog.show();
			}
		}

		
	    public void ToastLong(String s) {
      		Toast t = Toast.makeText(myapp, s, Toast.LENGTH_LONG);
      		t.show();
 	    }

	    public void ToastShort(String s) {
  			Toast t = Toast.makeText(myapp, s, Toast.LENGTH_SHORT);
  			t.show();
	    }
	    
	    public int getDipSize(int px) {
	    	return (int) (px * 
	    			myapp.getResources().getDisplayMetrics().density + 0.5f);
	    }
	    
		 public void MsgBoxInfo(Context c,String Title,String msg) {
		   		AlertDialog alertDialog = new AlertDialog.Builder(c).create();
	    		alertDialog.setTitle(Title);
	    		alertDialog.setMessage(msg);
	    		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	    		   public void onClick(DialogInterface dialog, int which) {
	    		      // here you can add functions
	    			  dialog.dismiss();
	    		   }
	    		});
	    		//alertDialog.setIcon(R.drawable.icon);
	    		alertDialog.show();
		 }
		 
		 
	    
	   public int getIconIdentifier(String iconName) {
		   
		   if (Utils.isBlank(iconName)) {
			   iconName="question";
		   }
		   
		   String iconfile=iconName.toLowerCase();
		   if (Utils.isNumber(iconName)) {
			   iconfile="n"+iconName.toLowerCase();
		   }
		   
		   int id = myapp.getResources().getIdentifier(iconfile, "drawable", myapp.getString(R.string.app_namespace));
		   if (id==0) {
			   Log.e(TAG,"Could not locate icon :"+iconfile);
			   iconName="question";
			   id = myapp.getResources().getIdentifier(iconfile, "drawable", myapp.getString(R.string.app_namespace));
		   }
		   
		   return id;
	   }

	   public Bitmap getBitMapforIcon(String icon) {
		   String ifile = CacheManager.getInstance().UserUpdateFile(icon+".png");
		   return BitmapFactory.decodeFile(ifile);
	   }
	   
	   public void setIconForProviderFile(ImageView view,String file) {
		   Bitmap myBitmap = BitmapFactory.decodeFile(file);
		   if (myBitmap==null) {
			   view.setImageResource(getIconIdentifier("question"));
		   } else {
			   view.setImageBitmap(myBitmap);
		   }
	   }

	   private void setIconForProvider(ImageView view,String iconName) {
		   view.setImageResource(getIconIdentifier(iconName));
	   }

	   public static boolean isHoneyComb = android.os.Build.VERSION.SDK_INT > 11;
	   
	   
	   public void setIconForProvider(ImageView view, Provider p,int loadedfrom) {
		   if (loadedfrom==0) {
			   setIconForProvider(view,p.icon);
		   } else {
			   String ifile = CacheManager.getInstance().UserUpdateFile(p.icon+".png");
			   setIconForProviderFile(view,ifile);
		   }
		   
	   }
	   
	   
}
