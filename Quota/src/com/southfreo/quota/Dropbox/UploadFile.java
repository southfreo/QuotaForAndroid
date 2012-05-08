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
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client.DropboxAPI;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.utils.DateUtils;


public class UploadFile extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "UploadFile";

    File uploadfile;
    DropboxController mDropboxSample;
    DropboxAPI api;
    
    // Will just log in
    public UploadFile(DropboxController act, File file) {
        super();
        mDropboxSample = act;
        uploadfile = file;
    }

    @Override
    protected Integer doInBackground(Void... params) {
       	int success = DropboxAPI.STATUS_NONE;
        api = mDropboxSample.getAPI();
        
       	try {
        	
        	if (api.isAuthenticated()) {
        		
        		if (!api.delete("dropbox", "/Quota/"+uploadfile.getName())) {
        			return DropboxAPI.STATUS_FAILURE;
        		}
  
        		success = api.putFile("dropbox", "/Quota", uploadfile);
        		if (success != DropboxAPI.STATUS_SUCCESS) {
	            	return success;
	            }
        	}
        } catch (Exception e) {
            Log.e(TAG, "Error in logging in.", e);
            return DropboxAPI.STATUS_NETWORK_ERROR;
        }
        return success;
    }
    

    @Override
    protected void onPostExecute(Integer result) {
        if (result == DropboxAPI.STATUS_SUCCESS) {
           mDropboxSample.showToast("Backup successful");
           SlotManager sm = SlotManager.getInstance();
           sm.DropBoxUpdateBackup(api.accountInfo().displayName, DateUtils.DateLong(new Date()), sm.NoSlots()+"");
           mDropboxSample.UpdateBackup();
           
        } else {
        	if (result == DropboxAPI.STATUS_NETWORK_ERROR) {
        		mDropboxSample.showToast("Network error");
        	} else {
        		mDropboxSample.showToast("Unsuccessful login.");
        	}
        }
    }

}
