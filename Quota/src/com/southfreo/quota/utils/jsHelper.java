package com.southfreo.quota.utils;

//import android.content.Context;
//import android.webkit.WebView;

//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.Scriptable;
//import org.mozilla.javascript.ScriptableObject;

import android.util.Log;

public class jsHelper {

	public final static String TAG = "jsHelper";
	
//	public static String callFunction(Context c,String html) {
//	
//		 WebView webview = new WebView(c);
//		 webview.getSettings().setJavaScriptEnabled(true);
//		 String summary = "<html><script type='text/javascript'> function check(p,k,a)  {  for(var i=a.length-1;i>0;i--)  {  if(i!=a.indexOf(a.charAt(i)) )  {  a=a.substring(0,i)+a.substring(i+1)  }  }  var r=new Array(p.length); for(var i=0;i<p.length;i++ ) { r[i]=p.charAt(i); var b=a.indexOf( p.charAt(i) ); if( b >= 0 && i<k.length ) { var c=a.indexOf(k.charAt(i)); if(c>=0) { b -= c;if(b<0)b+=a.length;r[i]=a.charAt(b) }  }  } return r.join('') } </script>";
//		 webview.loadData(summary, "text/html", "utf-8");
//		 webview.loadUrl("javascript:");
//
//	}
	
	public static void testJS() {
		 String jsCode = "function check(p,k,a)  {  for(var i=a.length-1;i>0;i--)  {  if(i!=a.indexOf(a.charAt(i)) )  {  a=a.substring(0,i)+a.substring(i+1)  }  }  var r=new Array(p.length); for(var i=0;i<p.length;i++ ) { r[i]=p.charAt(i); var b=a.indexOf( p.charAt(i) ); if( b >= 0 && i<k.length ) { var c=a.indexOf(k.charAt(i)); if(c>=0) { b -= c;if(b<0)b+=a.length;r[i]=a.charAt(b) }  }  } return r.join('') }";
		 
		 //String answer = callJS(jsCode,"check('password','Something','0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ')");
	
		 //Log.i(TAG,"Answer = "+answer);
	}
	
//    public static String callJS(String code,String method)
//    {
//        // Create an execution environment.
//        Context cx = Context.enter();
//
//        // Turn compilation off.
//        cx.setOptimizationLevel(-1);
//
//        String answer="";
//        
//        try 
//        {
//        	
//            // Initialize a variable scope with bindnings for  
//            // standard objects (Object, Function, etc.)
//            Scriptable scope = cx.initStandardObjects();
//
//            // Set a global variable that holds the activity instance.
//            ScriptableObject.putProperty(
//                scope, "TheActivity", Context.javaToJS(cx, scope));
//
//            // Evaluate the script.
//            Object o = cx.evaluateString(scope, code, method, 1, null); 
//            answer = o.toString();
//        } 
//        catch (Exception e) {
//        	Log.e(TAG,"Exception jsHelper:"+e.toString());
//        }
//        finally 
//        {
//            Context.exit();
//            return answer;
//        }
//    }
    
    
}

