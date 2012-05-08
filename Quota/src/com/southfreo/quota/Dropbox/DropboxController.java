/*
 * Copyright (c) 2010 Evenflow, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.southfreo.quota.Dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.southfreo.R;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;


public class DropboxController extends Activity {
    private static final String TAG = "DropboxController";

    // Replace this with your consumer key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    
 
    private DropboxAPI api = new DropboxAPI();

    final static public String ACCOUNT_PREFS_NAME = "drop_pref";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    final static public String QUOTA_PROVIDER_FILE = "Providers.quota";
    
    private boolean mLoggedIn;
    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mSubmit;
    private Button mUpload;
    private Button mDownload;
    
    private ProgressDialog mProgressBar;
    private LinearLayout mLoginPanel;
    private LinearLayout mLoggedInPanel;
    private ImageView mLogo;
    
    private TextView txtLastBackup;
    private TextView txtUser;
    private TextView txtNumAccounts;
    
    private Config mConfig;
    
    
    public void UpdateBackup() {
        SlotManager sm = SlotManager.getInstance();
        txtLastBackup.setText(sm.DB_lastBackup);
        txtUser.setText(sm.DB_lastUser);
        txtNumAccounts.setText(sm.DB_noaccounts);
    }
    
    
    public void DownloadComplete() {
    	SlotManager sm = SlotManager.getInstance();
    	File filePath = getFilesDir();
		File file = new File(filePath, QUOTA_PROVIDER_FILE);

    	boolean ok = sm.restoreSlotsProviderArray(file);
    	
    	if (!ok) {
    		UIManager.getInstance().MsgBoxInfo(this, "Error", "Could not find Providers within Backup, Check PIN!");
    		showToast("Problem restoring providers");
    	} else {
    		// Display Alert Dialog
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Add Providers");
    		builder.setMultiChoiceItems(sm.restoreNames, sm.restoreSelected, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
				}
			});
    		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	               		// Commence Restore
	                   	SlotManager sm = SlotManager.getInstance();
	                   	String res = sm.restoreSlotsAsJson64();
	                	showToast(res);
	                }
	            })
	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	// Do Nothing
	                	showToast("Cancelled");
	                }
	            });

    		
    		AlertDialog alert = builder.create();
    		alert.show();
    		
    	}
    }
    
    
    private void backupAccounts() {
   		try {
          	// Creates a file in the internal, app private storage
      		SlotManager sm = SlotManager.getInstance();
      		
   			String pf = sm.slotsAsJson64();
      		if (pf==null) {
  			   UIManager.getInstance().MsgBoxInfo(DropboxController.this, "Error", "Could not create provider file");
  			   return;
      		}
    		FileOutputStream fos;
    		fos = openFileOutput(QUOTA_PROVIDER_FILE, Context.MODE_PRIVATE);
    		fos.write(pf.getBytes());
    		fos.close();
    		} catch (Exception e) {
   			   UIManager.getInstance().MsgBoxInfo(DropboxController.this, "Error", "Problem creating upload file");
   		 }
		
    		
		File filePath = getFilesDir();
		File file = new File(filePath, QUOTA_PROVIDER_FILE);

		mProgressBar = ProgressDialog.show(DropboxController.this, "Dropbox","Uploading...", true, false);
		mProgressBar.show();
		
		UploadFile upload = new UploadFile(DropboxController.this,file);
		upload.execute();    	
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox);
        
        mLoginEmail = (EditText)findViewById(R.id.login_email);
        mLoginPassword = (EditText)findViewById(R.id.login_password);
        mSubmit = (Button)findViewById(R.id.login_submit);
        mUpload = (Button)findViewById(R.id.upload_submit);
        mLoginPanel   = (LinearLayout)findViewById(R.id.loginpanel);
        mLoggedInPanel =  (LinearLayout)findViewById(R.id.loggedinpanel);
        mLogo = (ImageView)findViewById(R.id.logo);
        mDownload = (Button)findViewById(R.id.download_submit);
        
        //mText.setMovementMethod(new ScrollingMovementMethod());
        //mText = (TextView)findViewById(R.id.text);
        
        txtLastBackup  = (TextView)findViewById(R.id.lastbackup);
        txtUser        = (TextView)findViewById(R.id.userbackup);
        txtNumAccounts = (TextView)findViewById(R.id.noproviders);
        
        UpdateBackup();
        
        mLogo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.dropbox.com/m/login"));
                startActivity(intent);
            }
        });
        
        mSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (mLoggedIn) {
            		// We're going to log out
            		api.deauthenticate();
            		clearKeys();
            		setLoggedIn(false);
            	} else {
            		// Try to log in
            		getAccountInfo();
            	}
            }
        });
        
        
 
        mUpload.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (mLoggedIn) {
            		SlotManager sm = SlotManager.getInstance();
            		if (sm.NoSlots()==0) {
            			UIManager.getInstance().MsgBoxInfo(DropboxController.this, "Error", "No accounts setup");
            			return;
            		}
            		
            	      AlertDialog.Builder builder = new AlertDialog.Builder(DropboxController.this);
            	      
            	      builder.setTitle("Backup")
           	            .setMessage("Any previously created backups will be overwritten! (Move Providers.quota to another location)\n\nDo you want to continue?")
            	            .setCancelable(true)
            	            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            	                public void onClick(DialogInterface dialog, int which) {
            	               		backupAccounts();
            	                }
            	            })
            	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            	                public void onClick(DialogInterface dialog, int which) {
            	                }
            	            });
            	        AlertDialog d = builder.create();
            	        d.show();
            		
            	} else {
        			   UIManager.getInstance().MsgBoxInfo(DropboxController.this, "Error", "You are not logged in");
            	}
            }
        });
 
        // Download Button
        mDownload.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (mLoggedIn) {
            		 // Try to Download SomeProviders
            		 
            		File filePath = getFilesDir();
            		File file = new File(filePath, QUOTA_PROVIDER_FILE);

            		mProgressBar = ProgressDialog.show(DropboxController.this, "Dropbox","Checking for backup...", true, false);
            		mProgressBar.show();
            		
            		DownloadFile download = new DownloadFile(DropboxController.this, "/Quota/"+QUOTA_PROVIDER_FILE, file);
            		download.execute();    	
            		
            	} else {
        			   UIManager.getInstance().MsgBoxInfo(DropboxController.this, "Error", "You are not logged in");
            	}
            }
        });
      
        
        String[] keys = getKeys();
        if (keys != null) {
        	setLoggedIn(true);
        	Log.i(TAG, "Logged in already");
        } else {
        	setLoggedIn(false);
            Log.i(TAG, "Not logged in");
        }
        if (authenticate()) {
        	// We can query the account info already, since we have stored 
        	// credentials
        	getAccountInfo();
        }
    }

    /**
     * This lets us use the Dropbox API from the LoginAsyncTask
     */
    public DropboxAPI getAPI() {
    	return api;
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    public void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    	mLoginEmail.setEnabled(!loggedIn);
    	mLoginPassword.setEnabled(!loggedIn);
    	if (loggedIn) {
    		mSubmit.setText("Log Out");
          	mLoginPanel.setVisibility(View.GONE);
          	mLoggedInPanel.setVisibility(View.VISIBLE);
    	} else {
    		mSubmit.setText("Login");
          	mLoginPanel.setVisibility(View.VISIBLE);
          	mLoggedInPanel.setVisibility(View.GONE);
    	}
    }

    public void showToast(String msg) {
        if (mProgressBar!=null) {
        	mProgressBar.cancel();
        }
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    private void getAccountInfo() {
    	if (api.isAuthenticated()) {
    		// If we're already authenticated, we don't need to get the login info
       		mProgressBar = ProgressDialog.show(DropboxController.this, "Dropbox","Login...", true, false);
    		mProgressBar.show();

	        LoginAsyncTask login = new LoginAsyncTask(this, null, null, getConfig());
	        login.execute();    		
    	} else {
    	
	        String email = mLoginEmail.getText().toString();
	        if (email.length() < 5 || email.indexOf("@") < 0 || email.indexOf(".") < 0) {
	            showToast("Error, invalid e-mail");
	            return;
	        }
	
	        String password = mLoginPassword.getText().toString();
	        if (password.length() < 6) {
	            showToast("Error, password too short");
	            return;
	        }

	        // It's good to do Dropbox API (and any web API) calls in a separate thread,
	        // so we don't get a force-close due to the UI thread stalling.
       		mProgressBar = ProgressDialog.show(DropboxController.this, "Dropbox","Login...", true, false);
    		mProgressBar.show();

	        LoginAsyncTask login = new LoginAsyncTask(this, email, password, getConfig());
	        login.execute();
    	}
    }

    /**
     * Displays some useful info about the account, to demonstrate
     * that we've successfully logged in
     * @param account
     */
    public void displayAccountInfo(DropboxAPI.Account account) {
    	if (account != null) {
    		String info = account.displayName + " (" + account.email + ") has logged in";
    		
    		//	"E-mail: " + account.email + "\n" + 
    		//	"User ID: " + account.uid + "\n" +
    		//	"Quota: " + account.quotaQuota;
    		showToast(info);
    		//mText.setText(info);
    	}
    }
    
    /**
     * This handles authentication if the user's token & secret
     * are stored locally, so we don't have to store user-name & password
     * and re-send every time.
     */
    protected boolean authenticate() {
    	if (mConfig == null) {
    		mConfig = getConfig();
    	}
    	String keys[] = getKeys();
    	if (keys != null) {
	        mConfig = api.authenticateToken(keys[0], keys[1], mConfig);
	        if (mConfig != null) {
	            return true;
	        }
    	}
    	//showToast("Failed user authentication for stored login tokens.");
    	clearKeys();
    	setLoggedIn(false);
    	return false;
    }
    
    protected Config getConfig() {
    	if (mConfig == null) {
 	    	mConfig = api.getConfig(null, false);
	    	mConfig.consumerKey="";
	    	mConfig.consumerSecret="";
	    	mConfig.server="api.dropbox.com";
	    	mConfig.contentServer="api-content.dropbox.com";
	    	mConfig.port=80;
    	}
    	return mConfig;
    }
    
    public void setConfig(Config conf) {
    	mConfig = conf;
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     * 
     * @return Array of [access_key, access_secret], or null if none stored
     */
    public String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
    public void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }    	
}