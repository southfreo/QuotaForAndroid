package com.southfreo.quota.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;

import com.southfreo.R;
import com.southfreo.quota.control.CacheManager;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.model.paramgroup;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.widgets.DatePreference;
import com.southfreo.quota.widgets.IconPreference;
import com.southfreo.quota.widgets.ListPreferenceMultiSelect;
import com.southfreo.quota.widgets.ListPreferenceValue;
import com.southfreo.quota.widgets.TextParamValidate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ProviderSettings extends PreferenceActivity {

	   private static final String TAG = "Quota-ProviderSettings";
	   public static final int ACTIVITY_CHOOSE_PROVIDER    = 99;
	   

	   private ImageView providerIcon;
	   PreferenceScreen root;
	   private boolean addscreen;
	   
	   IconPreference providerPref;
	   PreferenceCategory dynamicCat;
	   PreferenceCategory dynamicCatOptional;
	   PreferenceScreen intentProviderLink;
	   PreferenceScreen intentSupportLink;
	   TextParamValidate displayName;
	   CheckBoxPreference autoUpdate;
	   
	   private Provider currentEditProvider;
	   private paramgroup editingparameters;
	   PreferenceCategory alertCat;
	   
	   private SlotManager sm;
	   
	   private ProviderManager pm;
	   private UIManager ui;
	   
	   // Alerts
	   CheckBoxPreference   idealAlert;
	   TextParamValidate    p1Alert;
	   TextParamValidate    p2Alert;
 	   
	  public static Intent createIntent(Context context) {
	      Intent i = new Intent(context, ProviderSettings.class);
	      return i;
	  }
	  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Who called this intent
        addscreen = getIntent().getBooleanExtra("addprovider", false);
        
        pm = ProviderManager.getInstance();
        ui = UIManager.getInstance();
        sm = SlotManager.getInstance();
        
        setPreferenceScreen(createPreferenceHierarchy());
        
    }
    
    
    @Override 
    protected void onResume(){
        super.onResume();
        UIManager.getInstance().myapp.pauseService();
    }
    
    @Override 
    protected void onPause() { 
        super.onPause();
        UIManager.getInstance().myapp.unpauseService();
    } 
    

  
    private void setGeneralPref(int i,DialogPreference p,parameter param) {
  		p.setTitle(param.Name);
  		p.setDialogTitle(param.Name);
  		p.setSummary(param.description);
    }
    
    
    @SuppressWarnings("unchecked")
	private boolean checkNetwork(parameter p) {
    	if (currentEditProvider.disk_isp==ProviderManager.LOCAL_DATA_USAGE) {
    	 	if (p.pid==10) {
    	 		// Current Cell Value
    	 		Hashtable h = CacheManager.getInstance().getStatsDict();
    	 		long cv = NetworkUtils.getLong(h, NetworkUtils.mobTotcurrent);
    	 		if (cv>0) {
    	 			p.numberval = cv / 1024.0 / 1024.0;
    	 		}
    	 		return true;
    	 	}
       	 	if (p.pid==11) {
       	 		Hashtable h = CacheManager.getInstance().getStatsDict();
    	 		long cv = NetworkUtils.getLong(h, NetworkUtils.wifiTotcurrent);
     	 		if (cv>0) {
    	 			p.numberval = cv / 1024.0 / 1024.0;
    	 		}
    	 		return true;
       	 	}   		
    	}
    	return false;
    }
    
    private void saveNetwork() {
      	if (currentEditProvider.disk_isp==ProviderManager.LOCAL_DATA_USAGE) {
      		parameter pCell = editingparameters.getParameterByID(10);
     		parameter pWifi = editingparameters.getParameterByID(11);
     		Hashtable h = CacheManager.getInstance().getStatsDict();
     		
     		double mbCell=pCell.numberval;
     		double mbWifi=pWifi.numberval;
     		
     		if (mbCell>0) {
     			mbCell = Utils.DecMBtoBytes(mbCell);	
     		}
     		if (mbWifi>0) {
     			mbWifi = Utils.DecMBtoBytes(mbWifi);	
     		}
     		
     		NetworkUtils.ignoreCurrentTotals(h);
   			NetworkUtils.putLong(h,NetworkUtils.mobTotcurrent,(long)mbCell);
  			NetworkUtils.putLong(h,NetworkUtils.wifiTotcurrent,(long)mbWifi);
  			CacheManager.getInstance().writeStatsDict(h);
      	}
    }
    
    
    private void updateProviderSettings(int newID) {
    	

    	pm.edit_provider_id=newID;
        currentEditProvider = pm.getProviderDefinition(newID);
        
        if (currentEditProvider.loadedFrom==1) {
        	providerPref.setBitMap(ui.getBitMapforIcon(currentEditProvider.icon));
        } else {
            providerPref.setImage(ui.getIconIdentifier(currentEditProvider.icon));
        }
        
        providerPref.setTitle(currentEditProvider.providerName);
        addDynamicParameters();	
        
        // Update Support Page links
        intentProviderLink.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Utils.ParseUrl(currentEditProvider.providerUrl)));
          
        intentSupportLink.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Utils.ParseUrl(currentEditProvider.supportUrl)));
     
        
        if (pm.edit_provider_object.notSetup()) {
        	if (currentEditProvider!=null && !currentEditProvider.notSetup()) {
        		displayName.setText(Utils.BlankString(currentEditProvider.providerName));
        	}
        } else {
            displayName.setText(pm.edit_provider_object.disk_planname);
        }
        addAlerts();
           
    }

    
    private void addDynamicParameters() {
    	
    	// Change to Dynamic
    	
    	// Loop Through and create Preference Editors
    	editingparameters = pm.GetParameterGroupByID(currentEditProvider.paramgroupid);
    	if (editingparameters==null) {
    		ui.MsgBoxInfo(this,"Internal Error","Provider" + currentEditProvider+" points to an invalid parameter group (Check pgid defined in XML): "+currentEditProvider.paramgroupid);
    		// No Paramters for Provider....
    		return;
    	}
    	
    	dynamicCat.removeAll();
    	dynamicCatOptional.removeAll();
    	
    	editingparameters.ResetValues();
    	
    	// Copy the values from the provider being edited into 
    	
    	if (!pm.edit_provider_object.notSetup()) {
    		// Only copy the state from the edited provider when the paramgroup matches, otherwise it's reset
        	editingparameters.copyStateCheck(pm.edit_provider_object.disk_parameters);
     	}
    	
    	for (int i=0;i<editingparameters.params.size();i++) {
    		parameter p = editingparameters.params.get(i);
    		
    		if (p.type==parameter.param_textchoice) {
    			ListPreferenceValue listPref = new ListPreferenceValue(this);
    	        listPref.setEntries(p.ListValues);
    	        listPref.setEntryValues(p.ListValues);
    			setGeneralPref(i,listPref,p);
    			listPref.param =p;
    			if (!p.isBlank()){
    				try {
    	 	      	   listPref.setValueIndex((int)p.numberval);
    				} catch (Exception e) {}
    			}
    			
    	        dynamicCat.addPreference(listPref);
    		} else if (p.type==parameter.param_textchoicemulti){
      			ListPreferenceMultiSelect listPref = new ListPreferenceMultiSelect(this);
    	        listPref.setEntries(p.ListValues);
    	        listPref.setEntryValues(p.ListValues);
       			listPref.param =p;
       			setGeneralPref(i,listPref,p);
//	      		if (!p.isBlank()) {
//	   	   		   listPref.setValuesFromArray(p.CurrentSelectedValues);
//	   	   		 }
	      		
    	        dynamicCat.addPreference(listPref);
      		} else if (p.type==parameter.param_dateval) {
      			//
      			// Fix default date setup
      			//
     			DatePreference datePref = new DatePreference(this);
    			setGeneralPref(i,datePref,p);
    			datePref.param =p;
	      		if (!p.isBlank()) {
	      			datePref.setDate(p.CurrentValueAsInternalString());
	      		}
	      		
    	        dynamicCat.addPreference(datePref);
    		} else {
    			
    			TextParamValidate editTextPref = new TextParamValidate(this);
    			setGeneralPref(i,editTextPref,p);
	      		
	      		EditText txtParam = editTextPref.getEditText();
	      		txtParam.setSingleLine();
	      		if (p.isSecure()) {
	              PasswordTransformationMethod transMethod = new PasswordTransformationMethod();
	              txtParam.setTransformationMethod(transMethod);
	      		}
	      		
	      		if (p.useNumberEntry()) {
	      			txtParam.setInputType(InputType.TYPE_CLASS_PHONE| InputType.TYPE_NUMBER_FLAG_DECIMAL );
	      		} else {
	      			txtParam.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
	      		}
	      		
	      		editTextPref.param =p;
	      		if (p.optional) {
		      		dynamicCatOptional.addPreference(editTextPref);
	      		} else {
		      		dynamicCat.addPreference(editTextPref);
	      		}
	      		
	      		p.checkDefault();
	      		if (!p.isBlank()) {
		      		editTextPref.setText(p.CurrentValueAsInternalString());
	      		}
	      		
	      		if (checkNetwork(p)) {
		      		editTextPref.setText(p.CurrentValueAsInternalString());
	      		}
	      		
    		}
     
    	}
    	
    	if (dynamicCatOptional.getPreferenceCount()==0) {
    		//root.removePreference(dynamicCatOptional);
    	}

    	
        
        
    }
    
    
    private void addAlerts() {
    	if (currentEditProvider!=null) {
        	if (currentEditProvider.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
        		// Get saved Alerts
        		
        	   	alertCat.removeAll();
        	    
        		Boolean ideal=false;
        		Integer p1 = new Integer(0);
        		Integer p2 = new Integer(0);
        		//
        	   	if (!pm.edit_provider_object.notSetup()) {
                	Hashtable h1 = pm.edit_provider_object.disk_alerts;
                	if (h1!=null && h1.size()>=3) {
                		ideal = (Boolean)h1.get("ideal");
                		p1 = (Integer)h1.get("p1");
                   		p2 = (Integer)h1.get("p2");
                	}
            	}
        	   	
        		// Add Alert Parameters
        	    
        	    idealAlert = new CheckBoxPreference(this);
        	    idealAlert.setTitle("Over Ideal");
        	    idealAlert.setSummary("Notify when over ideal usage");
        	    idealAlert.setChecked(ideal);
        	    alertCat.addPreference(idealAlert);
        	   
        	    // P1 - %
        	    p1Alert = new TextParamValidate(this);
    			
        	    p1Alert.setTitle("Progress 1 - %");
        	    p1Alert.setSummary("Entering 75 will notify you when usage > 75%");
        	    p1Alert.setDialogTitle("Progress 1 - %");
        	    p1Alert.setText(p1.toString());
          		EditText txtParam = p1Alert.getEditText();
     			txtParam.setInputType(InputType.TYPE_CLASS_PHONE| InputType.TYPE_NUMBER_FLAG_DECIMAL );
     			txtParam.setSingleLine();
     	  	    alertCat.addPreference(p1Alert);
     	  	    		
     	  	    //... P2 - %
        	    p2Alert = new TextParamValidate(this);
    			
        	    p2Alert.setTitle("Progress 2 - %");
        	    p2Alert.setSummary("Entering 75 will notify you when usage > 75%");
          	    p2Alert.setText(p2.toString());
          	  
        	    p2Alert.setDialogTitle("Progress 2 - %");
          		EditText txtParam2 = p2Alert.getEditText();
          		txtParam2.setInputType(InputType.TYPE_CLASS_PHONE| InputType.TYPE_NUMBER_FLAG_DECIMAL );
          		txtParam2.setSingleLine();
     	  	    alertCat.addPreference(p2Alert);
        	}
    		
    	}
    
    }
    
    private void saveAlerts(Provider p) {
    	
      	if (currentEditProvider!=null) {
        	if (currentEditProvider.disk_display_type==Provider.kDisplayType_ISPMOBILE &&idealAlert!=null) {
        
        		Boolean ideal = new Boolean(idealAlert.isChecked());
        		Integer p1 = new Integer(p1Alert.getText());
        		Integer p2 = new Integer(p2Alert.getText());
        		
        		Hashtable<String,Object> h = new Hashtable<String,Object>();
        		
        		h.put("ideal", ideal);
        		h.put("p1", p1);
        		h.put("p2", p2);
        		p.disk_alerts = h;
        		
        		        	
        	}
      	}
      	
 		 
 		
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        root = getPreferenceManager().createPreferenceScreen(this);

        providerPref = new IconPreference(this);
        
        root.addPreference(providerPref);
        
        //Intent i = new Intent(this,ChooseProviderActivity.class); 
        // providerPref.setIntent(i); 
        
        providerPref.setSummary("Tap to change"); 
        
        providerPref.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(ProviderSettings.this,ChooseProviderActivity.class); 
                preference.setIntent(i); 
                startActivityForResult(i,ACTIVITY_CHOOSE_PROVIDER);
                return true;
            }
        });
 
        
        // Edit text preference
        displayName = new TextParamValidate(this);
        displayName.getEditText().setSingleLine();
        displayName.setDialogTitle("Display Name");
        displayName.setTitle("Display name");
        displayName.setSummary("A short descriptive name");
        
        root.addPreference(displayName);
        
        // Dynamic preferences 
        dynamicCat = new PreferenceCategory(this);
        dynamicCat.setTitle("Mandatory fields");
        root.addPreference(dynamicCat);

        dynamicCatOptional= new PreferenceCategory(this);
        dynamicCatOptional.setTitle("Optional fields");
        root.addPreference(dynamicCatOptional);
      
 		alertCat= new PreferenceCategory(this);
		alertCat.setTitle("Notification Alerts");
	    root.addPreference(alertCat);
        
        PreferenceCategory otherCat = new PreferenceCategory(this);
        otherCat.setTitle("Extra Information");
        root.addPreference(otherCat);
        
        autoUpdate = new CheckBoxPreference(this);
        autoUpdate.setTitle("Auto Refresh");
        autoUpdate.setSummary("Provider will update automatically");
        autoUpdate.setChecked(!pm.edit_provider_object.disk_manualrefresh);
        otherCat.addPreference(autoUpdate);
      
        intentProviderLink = getPreferenceManager().createPreferenceScreen(this);
        intentProviderLink.setTitle("Providers website");
        intentProviderLink.setSummary("Tap here to open website");
        otherCat.addPreference(intentProviderLink);
      
        intentSupportLink = getPreferenceManager().createPreferenceScreen(this);
        intentSupportLink.setTitle("Support website");
        intentSupportLink.setSummary("Tap here to open support link");
        otherCat.addPreference(intentSupportLink);
    
       	// Setup Dynamic Parameters
    	updateProviderSettings(pm.edit_provider_id);
    	
    	
        return root;
    }

    private void CancelBacktoSummary() {
	      // here you can add functions
		  Intent i = SummaryActivity.createIntent(this);
		  if (addscreen) {
			  // Abort Setup
			  sm.deleteSlotmemory(ProviderSettings.this.pm.edit_provider_slot);
		  }
		  startActivity(i);
		  finish();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (   keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
          	
        	if (!editingparameters.Validate()) {
        		
		   		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    		alertDialog.setTitle("Check Settings");
	    		alertDialog.setMessage(editingparameters.invalidmsg);
	    		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	    		   public void onClick(DialogInterface dialog, int which) {
	    		      // here you can add functions
	    			  dialog.dismiss();
	    		   }
	    		});
	    		
	    		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
		    		   public void onClick(DialogInterface dialog, int which) {
		    				  dialog.dismiss();
		    				  CancelBacktoSummary();
		    		   }
		    		});
		    		
	    		alertDialog.show();
        	  	
        	  	return true;
          	} else {
          		
          		
          		String dispName = displayName.getText();
          		
          		// Check Display Name filled in
          		if (Utils.isBlank(dispName)) {
               	  	ui.MsgBoxInfo(this,"Check settings", "Enter a display name for this provider");
               	  	return true;
          		} else {
          			
              		// Get the Slot Provider in the ProviderManager Editor
              		Provider ep = pm.edit_provider_object;
              		
              		// Reset the Provider
              		pm.ResetProvider(ep, currentEditProvider.disk_isp);
              		
              		// Copy all Values into this provider from Edited Values
              		ep.disk_planname=dispName;
              		ep.disk_manualrefresh = !autoUpdate.isChecked();;
              		ep.disk_parameters.copyState(editingparameters);
          			
              		// Add Alerts
              		saveAlerts(ep);
              		
              		// Network Stats
              		saveNetwork();
              		
              		
              		boolean ok = SlotManager.getInstance().saveSlot(pm.edit_provider_slot,true);
              		if (ok) {
              			ui.ToastShort("Provider : "+dispName+" saved successfuly");
              		} else {
              			ui.ToastLong("ERROR!! SAVING PROVIDER!!");
              		}
              		
              		// Who called
              		if (addscreen) {
              			// Send to Detail Screen
              			finish();
              			Intent i = DetailActivity.CreateDetailScreen(this, pm.edit_provider_slot);
              			startActivityForResult(i,0);
              		}
              		
          		}
          			
           		
          		
          		
          	}
         }

        return super.onKeyDown(keyCode, event);
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        switch (requestCode) {

            case ACTIVITY_CHOOSE_PROVIDER:
            {
            	if (resultCode==RESULT_OK) {
            		// Get the Provider Code
            		int newProviderID = data.getIntExtra(ChooseProviderActivity.CHOSEN_PROVIDER_CODE,-1);
            		
                   	Log.i(TAG,"Choose Provider: OK Code:"+newProviderID);
                    updateProviderSettings(newProviderID);
                    
                    
            	} else {
                  	// Cancel From ChooseProvider
           			if (ProviderManager.getInstance().edit_provider_id==-1){
                   		CancelBacktoSummary();
                   	}
                   	
            		Log.i(TAG,"Choose Provider: CANCELLED");
                  	
                  	
            	}
            }
     }
 }
}
