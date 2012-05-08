package com.southfreo.quota.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import com.southfreo.quota.control.UIManager;

import android.util.Log;


public class HttpHelper {

    private static final String TAG = "Quota-HttpHelper";

	DefaultHttpClient httpClient;
    HttpContext localContext;
    private String ret;

    HttpResponse response = null;
    HttpPost httpPost = null;
    HttpGet httpGet = null;

    
    public static DefaultHttpClient QuotaClient()  {
        HttpParams myParams = new BasicHttpParams();

        int defaultTimeOut = 60 * 1000;
        
        HttpConnectionParams.setConnectionTimeout(myParams, defaultTimeOut);
        HttpConnectionParams.setSoTimeout(myParams, defaultTimeOut);

        HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(myParams, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUserAgent(myParams, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");
        
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(myParams, schReg);
        
        // Not sure if this is good
        HttpProtocolParams.setUseExpectContinue(myParams, false);
        
        return new DefaultHttpClient(conMgr,myParams); 
        
    }

    public static DefaultHttpClient IE8HttpOpenClient() {
    	return OpenClient("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1");
    }
    
    public static DefaultHttpClient OpenClient() {
    	return OpenClient("");
    }
    
    
    public static DefaultHttpClient OpenClient(String user_agent)  {
            
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new OpenSSLFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            if (Utils.isBlank(user_agent)) {
                HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");
            } else {
			    HttpProtocolParams.setUserAgent(params,user_agent);
            }

            
            HttpClientParams.setRedirecting(params, true);
            params.setBooleanParameter("http.protocol.allow-circular-redirects", true); 

            int defaultTimeOut = 60 * 1000;
            
            HttpConnectionParams.setConnectionTimeout(params, defaultTimeOut);
            HttpConnectionParams.setSoTimeout(params, defaultTimeOut);
            
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            DefaultHttpClient client =  new DefaultHttpClient(ccm, params);
            
            // Change Cookie Handling
            client.getParams().setParameter(
                    ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
 
            return client;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
        
     }
    
    public static DefaultHttpClient TelstraClient()  {
        HttpParams myParams = new BasicHttpParams();

        int defaultTimeOut = 60 * 1000;
        
        HttpConnectionParams.setConnectionTimeout(myParams, defaultTimeOut);
        HttpConnectionParams.setSoTimeout(myParams, defaultTimeOut);

        HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(myParams, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUserAgent(myParams, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");
        
        
        TelstraStore client = new TelstraStore(UIManager.getInstance().myapp);
    	client.setParams(myParams);
        return client; 
    }
    
	public static void addHttpAuthentication(HttpRequestBase request,String username,String password) {
        String auth = Base64.encodeBytes((username + ":" + password).getBytes()).toString();
        request.addHeader("Authorization", "Basic " + auth);
	}
	
    public static String HttpGetAuthenticated(DefaultHttpClient client, String sURL,String username, String password) {
    	
    	try {
    	   	HttpGet method = new HttpGet(new URI(sURL));  
        	addHttpAuthentication(method, username, password);
        
    		HttpResponse response = client.execute(method);
        	if (response!=null) {
        		return EntityUtils.toString(response.getEntity());
        	} else {
        		return null;
        	}
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    public static String sendHttpGet(DefaultHttpClient client,String url) {
    	HttpGet httpGet = new HttpGet(url);  
    	String data=null;
    	HttpResponse response=null;
    	
        try {
        	response = client.execute(httpGet);  
        	if (response!=null) {
            	data = EntityUtils.toString(response.getEntity());  
        	}

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return data;
    }
    
    public static String sendHttpPost(DefaultHttpClient client,String url, String data) {
        String ret = null;

        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = null;

        StringEntity tmp = null;        

        Log.d(TAG, "sendPost");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
    
        try {
            tmp = new StringEntity(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "HttpUtils : UnsupportedEncodingException : "+e);
        }

        httpPost.setEntity(tmp);

         try {
            response = client.execute(httpPost);

            if (response != null) {
                ret = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            Log.e(TAG, "HttpUtils: " + e);
        }
        return ret;
    }
    
  
    
    

    public void clearCookies() {
        httpClient.getCookieStore().clear();
    }

    public void abort() {
        try {
            if (httpClient != null) {
                System.out.println("Abort.");
                httpPost.abort();
            }
        } catch (Exception e) {
            System.out.println("Your App Name Here" + e);
        }
    }

    public String sendPost(String url, String data) {
        return sendPost(url, data, null);
    }

    public String sendJSONPost(String url, JSONObject data) {
        return sendPost(url, data.toString(), "application/json");
    }

    public String sendPost(String url, String data, String contentType) {
        ret = null;

        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

        httpPost = new HttpPost(url);
        response = null;

        StringEntity tmp = null;        

        Log.d(TAG, "sendPost");

        //httpPost.setHeader("User-Agent", "SET YOUR USER AGENT STRING HERE");
        //httpPost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

        if (contentType != null) {
            httpPost.setHeader("Content-Type", contentType);
        } else {
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }

        try {
            tmp = new StringEntity(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("Your App Name Here", "HttpUtils : UnsupportedEncodingException : "+e);
        }

        httpPost.setEntity(tmp);

        Log.d(TAG, url + "?" + data);

        try {
            response = httpClient.execute(httpPost,localContext);

            if (response != null) {
                ret = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            Log.e(TAG, "HttpUtils: " + e);
        }

        Log.d(TAG, "Returning value:" + ret);

        return ret;
    }

    public String sendGet(String url) {
        httpGet = new HttpGet(url);  

        try {
            response = httpClient.execute(httpGet);  
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        //int status = response.getStatusLine().getStatusCode();  

        // we assume that the response body contains the error message  
        try {
            ret = EntityUtils.toString(response.getEntity());  
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return ret;
    }

    public InputStream getHttpStream(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException("Not an HTTP connection");

        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect(); 

            response = httpConn.getResponseCode();                 

            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();                                 
            }                     
        } catch (Exception e) {
            throw new IOException("Error connecting");            
        } // end try-catch

        return in;     
    }
    
}
