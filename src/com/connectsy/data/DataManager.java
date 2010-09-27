package com.connectsy.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.connectsy.Launcher;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.settings.Settings;

public abstract class DataManager implements ApiRequestListener {
	@SuppressWarnings("unused")
	private static final String TAG = "DataManager";
	public int returnCode;
	public int pendingUpdates = 0;
	public Context context;
	public DataUpdateListener listener;
	
	public interface DataUpdateListener{
		public void onDataUpdate(int code, String response);
		public void onRemoteError(int httpStatus, int code);
	}
	
	public DataManager(Context c, DataUpdateListener passedListener){
		context = c;
		listener = passedListener;
	}
	
	public static SharedPreferences getCache(Context c){
		SharedPreferences d = c.getSharedPreferences(Settings.PREFS_NAME, 
				Context.MODE_PRIVATE);
		if (d.getInt("CACHE_VERSION", 0) != Settings.CACHE_VERSION)
			cleanCache(c);
		return d;
	}
	
	public static void cleanCache(Context context){
		getCache(context).edit().clear()
				.putInt("CACHE_VERSION", Settings.CACHE_VERSION).commit();
	}
	
	public void onApiRequestFinish(int status, String response, int code){
		listener.onDataUpdate(code, response);
	};
	
	public void onApiRequestError(int httpStatus, int code) {
		if (httpStatus == 401){
            SharedPreferences.Editor dataEditor = context
            		.getSharedPreferences(Settings.PREFS_NAME, 
        					Context.MODE_PRIVATE).edit(); 
            dataEditor.putBoolean("authed", false);
            dataEditor.commit();
        	context.startActivity(new Intent(context, Launcher.class));
        	((Activity) context).finish();
		}
		listener.onRemoteError(httpStatus, returnCode);
	}
}