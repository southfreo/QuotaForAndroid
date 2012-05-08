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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.FileDownload;


public class DownloadFile extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "DownloadFile";

    File downloadfile;
    DropboxController mDropboxSample;
    DropboxAPI api;
    String dbfile;
    
    // Will just log in
    public DownloadFile(DropboxController act, String DropboxFile,File file) {
        super();
        mDropboxSample = act;
        downloadfile = file;
        dbfile=DropboxFile;
       	api = mDropboxSample.getAPI();
    }

    private boolean downloadDropboxFile(String dbPath, File localFile) throws IOException {

		BufferedInputStream br = null;
		BufferedOutputStream bw = null;

		try {
			if (!localFile.exists()) {
				localFile.createNewFile(); //otherwise dropbox client will fail silently
			}

			FileDownload fd = api.getFileStream("dropbox", dbPath, null);
			br = new BufferedInputStream(fd.is);
			bw = new BufferedOutputStream(new FileOutputStream(localFile));

			byte[] buffer = new byte[4096];
			int read;
			while (true) {
			read = br.read(buffer);
			if (read <= 0) {
			break;
			}
			bw.write(buffer, 0, read);
			}
		} finally {
			//in finally block:
			if (bw != null) {
				bw.close();
			}
			if (br != null) {
				br.close();
			}
		}

		return true;
	}
    
    @Override
    protected Integer doInBackground(Void... params) {
       	try {
        	
        	if (api.isAuthenticated()) {
        		if (downloadDropboxFile(dbfile,downloadfile)) {
     	            return DropboxAPI.STATUS_SUCCESS;
        		}
        	}
        } catch (Exception e) {
            Log.e(TAG, "Error in logging in.", e);
            return DropboxAPI.STATUS_FAILURE;
        }
        return DropboxAPI.STATUS_FAILURE;
    }
    

    @Override
    protected void onPostExecute(Integer result) {
        if (result==DropboxAPI.STATUS_SUCCESS) {
           mDropboxSample.DownloadComplete();
        } else {
    		mDropboxSample.showToast("Did not find a Backup file to restore!");
        }
    }

}
