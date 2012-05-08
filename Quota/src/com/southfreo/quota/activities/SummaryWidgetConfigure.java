package com.southfreo.quota.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;

import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.widgets.ListPreferenceValueNoParam;

public class SummaryWidgetConfigure extends PreferenceActivity {

	private final static String TAG = "Quota-SummaryWidgetConfigure";
	
	private SlotManager sm;
	private ListPreferenceValueNoParam listPref;
	private ListPreferenceValueNoParam dispPref;
	private ListPreferenceValueNoParam backPref;
	
	private int appWidgetId;
	
	private static String[] displayTitles = {
	     "Progress Bars",	     // 0
		 "% Used Text",		     // 1
		 "Used",			     // 2
		 "Remaining",		     // 3
		 "Days %",			     // 4
		 "Days Remaining",       // 5
		 "Peak Used / Days %" 	 // 6
		 }; 

	
	private static String[] backgroundChoice = {
	     "Solid Black",					//0
		 "Transparent white",			//1
		 "Transparent black",			//2
		 };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sm = SlotManager.getInstance();
        
        appWidgetId=AppWidgetManager.INVALID_APPWIDGET_ID;
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
           appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        }   

        
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        root.setTitle("Quota Widget - Options");
        
        listPref = new ListPreferenceValueNoParam(this);
        listPref.setEntries(sm.ProviderNames());
        listPref.setEntryValues(sm.ProviderNames());
        listPref.setDialogTitle("Select Provider");
        listPref.setTitle("Provider");
        listPref.setSummary("Choose a provider to display");
        
        root.addPreference(listPref);
 
        backPref = new ListPreferenceValueNoParam(this);
        backPref.setEntries(backgroundChoice);
        backPref.setEntryValues(backgroundChoice);
        backPref.setDialogTitle("Background");
        backPref.setTitle("Background");
        backPref.setSummary("Widget Background");
        
        root.addPreference(backPref);
 
        
        dispPref = new ListPreferenceValueNoParam(this);
        dispPref.setEntries(displayTitles);
        dispPref.setEntryValues(displayTitles);
        dispPref.setDialogTitle("Display Type");
        dispPref.setTitle("Display Type");
        dispPref.setSummary("Choose how ISP/Mobile values are displayed");
        
        root.addPreference(dispPref);
        
  
        
        setPreferenceScreen(root);
        
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (   keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
  
        	if (!Utils.isBlank(listPref.getValue())) {
        		// Find the correct Slot for this provider
        		int sn = listPref.findIndexOfValue(listPref.getValue());
        		SlotManager.getInstance().preferences.edit().putInt("widget_id"+appWidgetId, sn).commit();
        		
        		int displayPref = dispPref.findIndexOfValue(dispPref.getValue());
        		if (displayPref<0) displayPref=0;
        		
        		SlotManager.getInstance().preferences.edit().putInt("widget_id_display"+appWidgetId, displayPref).commit();
        		
        		// Background 
           		int backgroundPref = backPref.findIndexOfValue(backPref.getValue());
        		if (backgroundPref<0) backgroundPref=0;
        		SlotManager.getInstance().preferences.edit().putInt("widget_id_background"+appWidgetId, backgroundPref).commit();
        		
        		Intent cancelResultValue = new Intent();
        		cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        	    setResult(RESULT_OK, cancelResultValue);
        	    ProviderManager.getInstance().pingWidgets();
        	    
        	    finish();
        	    return true;
        	} else {
        	     Intent cancelResultValue = new Intent();
        	     cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        	     setResult(RESULT_CANCELED, cancelResultValue);
            	    finish();
            	    return true;
            	}
        }
       return super.onKeyDown(keyCode, event);
    }
  
    
}
