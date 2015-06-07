package ru.springcoding.prefomega;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;


public class PrefApplication extends Application {
	private static PrefApplication singleton = null;
	
	public static String regid;
	public static String message;
	public static int screenWidth;
	public static int screenHeight;
	private static int currentVisibleWindow; // this variable will be checked each time 
										     // when server sends us smth. For, if the data is old,
											 // we don't need it anymore and just skip.
	private static boolean metricsSet = false;
	public static boolean pingStatus = false;
	
	private static String SENDER_ID = "841120567778";
	static final String TAG = "GCMDemo";
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static Context context;
	private static GoogleCloudMessaging gcm;
	private RegistrationTestThread regTestThread;
	private static KeepAliveThread keepAliveThread;
	private Object lock;
	
	public static PrefApplication getInstance() {
		return singleton;
	}
	
	@Override
	public final void onCreate() {
		super.onCreate();
		singleton = this;
		lock = new Object();
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		regTestThread = new RegistrationTestThread();
		keepAliveThread = new KeepAliveThread();
	}
	
	public static void setVisibleAreaSize(int height, int width) {
		if (!metricsSet) {
			screenHeight = height;
			screenWidth = width;
			metricsSet = true;
		}
	}
	
	public static void sendData(final ArrayList<NameValuePair> data) {
         // 1) Connect via HTTP. 2) Encode data. 3) Send data.
		new AsyncTask<Void, Void, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(Void... params) {
		        try {
		            HttpClient httpclient = new DefaultHttpClient();
		            HttpPost httppost = new HttpPost("http://192.168.1.35:8080/PrefServer/Dispatcher");
		            httppost.setEntity(new UrlEncodedFormEntity(data));
		            HttpResponse response = httpclient.execute(httppost);
		            Log.i("postData", response.getStatusLine().toString());
		            return response;
		        }
		        catch(Exception e) {
		            Log.e("log_tag", "Error:  " + e.toString());
		        }  
				return null;
			}
			
			@Override
			protected void onPostExecute(HttpResponse response) {
				InputStream inputstream;
				try {
					inputstream = response.getEntity().getContent();
					String line = PrefApplication.convertStreamToString(inputstream);
					Log.i("log_tag", "Response:  " + line);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				super.onPostExecute(response);
			}
			
		}.execute(null, null, null);
        
    }
	
	public static String convertStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    try {
	        while ((line = rd.readLine()) != null) {
	            total.append(line);
	        }
	    } catch (Exception e) {
	        Log.e("Stream2String", "Stream exception");
	    }
	    return total.toString();
	}
	
	public static void setVisibleWindow(int i, Context c) {
		synchronized (singleton.lock) {
			currentVisibleWindow = i;
			context = c;
		}
	}
	
	public static int getVisibleWindow() {
		synchronized (singleton.lock) {
			return currentVisibleWindow;
		}
	}
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public static boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, null,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
			}
			Toast.makeText(context, "CheckPlayServices: false", 0).show();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences();
		
		regid = prefs.getString(PROPERTY_REG_ID, "");
		if (regid.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return regid;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(EntryActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion() {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	public void registerInBackground() {
		
		new AsyncTask<Object, Object, Object>() {
			@Override
			protected String doInBackground(Object... params) {
				if (Looper.myLooper() == null)
					Looper.prepare();
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					PrefApplication.regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID="+ PrefApplication.regid;

					sendRegistrationIdToBackend();

					// Persist the regID - no need to register again.
					storeRegistrationId(context, PrefApplication.regid);
				} catch (Exception ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			protected void onPostExecute(String msg) {
				
			}
		}.execute(null, null, null);
		// ...
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private static void sendRegistrationIdToBackend() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		
		PrefApplication.sendData(nameValuePairs);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion();
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
	
		editor.commit();
	}

	public static void onReceive(Context context, Intent intent) {
		String regId = intent.getExtras().getString("registration_id");
		if (regId != null && !regId.equals("")) {
			/* Do what ever you want with the regId eg. send it to your server */
		}
	}
	
	public void pingServer() {
		regTestThread.setRunning(true);
	}
	
	public static void runKeepAlive(boolean run) {
		keepAliveThread.setRunning(run);
		keepAliveThread.start();
	}
}