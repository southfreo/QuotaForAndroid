package com.southfreo.quota.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.southfreo.quota.utils.Base64.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class UnZip implements Runnable {

	static Handler myHandler;
	ProgressDialog myProgress;

	public Context c;
	private UnZipEvent ue;
	
    File archive;
    String outputDir;

	public interface UnZipEvent
	{
	    // This is just a regular method so it can return something or
	    // take arguments if you like.
	    public void unzipComplete(boolean ok);
	}
	
	
    final static String TAG = "UnZip";

    public UnZip(Context c,UnZipEvent ue) {
    	this.c = c;
    	this.ue = ue;
    }
    
    
    private Context getContext() {
    	return c;
    }
    
    public void unzipFile(String archive,String outDir) {
        myProgress = ProgressDialog.show(getContext(), "Extract Zip",
                        "Extracting Files...", true, false);
        File zipFile = new File(archive);
        
        myHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        // process incoming messages here
                        switch (msg.what) {
                        case 0:
                                // update progress bar
                                myProgress.setMessage("" + (String) msg.obj);
                                break;
                        case 1:
                                myProgress.cancel();
                                Toast toast = Toast.makeText(getContext(),
                                                "Zip extracted successfully", Toast.LENGTH_SHORT);
                                toast.show();
                                ue.unzipComplete(true);
                                break;
                        case 2:
                        		ue.unzipComplete(false);
                                myProgress.cancel();
                                break;
                        }
                        super.handleMessage(msg);
                }

        };
        Thread workthread = new Thread(new UnZip(zipFile, outDir));
        workthread.start();
}
    
    
    public UnZip(File ziparchive, String directory) {
            archive = ziparchive;
            outputDir = directory;
    }

    public void log(String log) {
            Log.v("unzip", log);
    }

    @SuppressWarnings("unchecked")
    public void run() {
            Message msg;
            try {
                    ZipFile zipfile = new ZipFile(archive);
                    for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                            ZipEntry entry = (ZipEntry) e.nextElement();
                            msg = new Message();
                            msg.what = 0;
                            msg.obj = "Extracting " + entry.getName();
                            myHandler.sendMessage(msg);
                            unzipEntry(zipfile, entry, outputDir);
                    }
            } catch (Exception e) {
                    log("Error while extracting file " + archive);
            }
            msg = new Message();
            msg.what = 1;
            myHandler.sendMessage(msg);
    }

    @SuppressWarnings("unchecked")
    public void unzipArchive(File archive, String outputDir) {
            try {
                    ZipFile zipfile = new ZipFile(archive);
                    for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                            ZipEntry entry = (ZipEntry) e.nextElement();
                            unzipEntry(zipfile, entry, outputDir);
                    }
            } catch (Exception e) {
                    log("Error while extracting file " + archive);
            }
    }

    private static final int IO_BUFFER_SIZE = 4 * 1024;  
    
    private static void copy(BufferedInputStream in, BufferedOutputStream out) throws IOException {  
        byte[] b = new byte[IO_BUFFER_SIZE];  
        int read;  
        while ((read = in.read(b)) != -1) {  
        out.write(b, 0, read);  
        }  
    } 
    
    private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                    String outputDir) throws IOException {

            if (entry.isDirectory()) {
                    createDir(new File(outputDir, entry.getName()));
                    return;
            }

            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                    createDir(outputFile.getParentFile());
            }

            log("Extracting: " + entry);
            BufferedInputStream inputStream = new BufferedInputStream(zipfile
                            .getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(
                            new FileOutputStream(outputFile));

            try {
                    copy(inputStream, outputStream);
            } finally {
                    outputStream.close();
                    inputStream.close();
            }
    }

    private void createDir(File dir) {
            log("Creating dir " + dir.getName());
            if (!dir.mkdirs())
                    throw new RuntimeException("Can not create dir " + dir);
    }
}
