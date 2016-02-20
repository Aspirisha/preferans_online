package ru.springcoding.prefomega;

import android.app.Activity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.springcoding.prefomega.CommonEnums.RecieverID;


public class PrefApplication extends Application {
	private static PrefApplication singleton = null;
	
	private static String serverIp = null;
	public static volatile String regid;
	public static String message;
	public static int screenWidth;
	public static int screenHeight;
	private static RecieverID currentVisibleWindow; // this variable will be checked each time 
										     // when server sends us smth. For, if the data is old,
											 // we don't need it anymore and just skip.
	private static boolean metricsSet = false;
	public static volatile boolean pingStatus = false;
	
	private static String SENDER_ID = "264728257590";
	static final String TAG = "GCMDemo";
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_LOGIN = "login";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_ID = "id";
	
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
		serverIp = getResources().getString(R.string.server_ip);
	}
	
	public static void setVisibleAreaSize(int height, int width) {
		if (!metricsSet) {
			screenHeight = height;
			screenWidth = width;
			metricsSet = true;
		}
	}
	
	public void tryRegister(String login, String password) {
		ArrayList<NameValuePair> loginAndPassword = new ArrayList<NameValuePair>();
		loginAndPassword.add(new BasicNameValuePair("request_type", "request"));
		loginAndPassword.add(new BasicNameValuePair("request", "register"));
		loginAndPassword.add(new BasicNameValuePair("login", login));
		loginAndPassword.add(new BasicNameValuePair("password", password));
		loginAndPassword.add(new BasicNameValuePair("reg_id", regid));
		sendData(loginAndPassword, true);
	}

	public void trySignIn(String login, String password) {
		ArrayList<NameValuePair> loginAndPassword = new ArrayList<NameValuePair>();
		loginAndPassword.add(new BasicNameValuePair("request_type", "request"));
		loginAndPassword.add(new BasicNameValuePair("request", "signin"));
		loginAndPassword.add(new BasicNameValuePair("login", login));
		loginAndPassword.add(new BasicNameValuePair("password", password));
		loginAndPassword.add(new BasicNameValuePair("reg_id", regid));
		sendData(loginAndPassword, true);
	}
	
	public static void sendData(final List<NameValuePair> data,
			final boolean hasLoginAndPassword) {
         // 1) Connect via HTTP. 2) Encode data. 3) Send data.
		new AsyncTask<Void, Void, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(Void... params) {
		        try {
		        	if (!hasLoginAndPassword) {
		        		data.add(new BasicNameValuePair("id", 
		        				GameInfo.ownPlayer.id));
		        		data.add(new BasicNameValuePair("login", 
		        				GameInfo.ownPlayer.name));
		        		data.add(new BasicNameValuePair("password", 
		        				GameInfo.password));		        		
		        	}
		            HttpClient httpclient = new DefaultHttpClient();
		            HttpPost httppost = new HttpPost("http://"+ serverIp +":8080");
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
					if (response != null) {
						inputstream = response.getEntity().getContent();
						String line = PrefApplication.convertStreamToString(inputstream);
						Log.i("log_tag", "Response:  " + line);
					}
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
	
	public static void setVisibleWindow(RecieverID i, Context c) {
		synchronized (singleton.lock) {
			currentVisibleWindow = i;
			context = c;
		}
	}
	
	public static RecieverID getVisibleWindow() {
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
				GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)context,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
			}
			Toast.makeText(context, "CheckPlayServices: false", Toast.LENGTH_SHORT).show();
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
		final SharedPreferences prefs = getPreferences();
		
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
	
	public boolean getLoginAndPassword() {
		final SharedPreferences prefs = getPreferences();
		GameInfo.ownPlayer.name = prefs.getString(PROPERTY_LOGIN, "");
		GameInfo.ownPlayer.id = prefs.getString(PROPERTY_ID, "");
		GameInfo.password = prefs.getString(PROPERTY_PASSWORD, "");
		GameInfo.isSignedIn = !GameInfo.ownPlayer.name.isEmpty()
				&& !GameInfo.password.isEmpty() && !GameInfo.ownPlayer.id.isEmpty();
		return GameInfo.isSignedIn;
	}

	public static void signOut() {
		if (!GameInfo.isSignedIn)
			return;
		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("request_type", "notification"));
		data.add(new BasicNameValuePair("notification", "quit"));
		data.add(new BasicNameValuePair("id", GameInfo.ownPlayer.id));
		data.add(new BasicNameValuePair("login", GameInfo.ownPlayer.name));
		data.add(new BasicNameValuePair("password", GameInfo.password));
		PrefApplication.sendData(data, true);
		GameInfo.ownPlayer.name = "";
		GameInfo.password = "";
		GameInfo.ownPlayer.id = null;
		GameInfo.isSignedIn = false;
		getInstance().storeLoginAndPassword();
	}

	public void storeLoginAndPassword() {
		final SharedPreferences prefs = getPreferences();
		int appVersion = getAppVersion();
		Log.i(TAG, "Saving login and password " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_LOGIN, GameInfo.ownPlayer.name);
		editor.putString(PROPERTY_PASSWORD, GameInfo.password);
		editor.putString(PROPERTY_ID, GameInfo.ownPlayer.id);
		editor.commit();
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getPreferences() {
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
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
		Log.i("regID", "reg_id = " + PrefApplication.regid);
		PrefApplication.sendData(nameValuePairs, false);
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
		final SharedPreferences prefs = getPreferences();
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
		regTestThread.start();
	}
	
	public static void runKeepAlive(boolean run) {
		if (!keepAliveThread.isAlive()) {
			keepAliveThread.setRunning(run);
			keepAliveThread.start();
		}
	}
}