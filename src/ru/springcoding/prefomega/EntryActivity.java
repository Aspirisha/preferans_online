package ru.springcoding.prefomega;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EntryActivity extends Activity implements OnClickListener {
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	String SENDER_ID = "841120567778";
	static final String TAG = "GCMDemo";

	// TextView mDisplay;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	Button btnStartNewGame;
	Button btnSettings;
	Button btnExit;

	WifiManager wifi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry);
		
		PrefApplication.setVisibleWindow(0, this);
		
		findViews();
		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (PrefApplication.checkPlayServices()) {
			PrefApplication.getInstance().getRegistrationId();

			if (PrefApplication.regid.isEmpty()) {
				PrefApplication.getInstance().registerInBackground();
			} 
		/*else {
				PrefApplication.getInstance().pingServer();
			}*/
			
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
			Toast.makeText(this, "CheckPlayServices: No valid Google Play Services APK found.", 0).show();
		}

		String service = Context.WIFI_SERVICE;
		wifi = (WifiManager) getSystemService(service);

		btnStartNewGame.setOnClickListener(this);
		btnSettings.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		
		// tell the server we are online now
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("notification", "online"));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
		
		PrefApplication.runKeepAlive(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonExit:
			// send notification to server that we have exited
			finish();
			break;
		case R.id.buttonStart:
			if (!wifi.isWifiEnabled()) {
				Toast.makeText(this, "Internet connection not available. Turn on wi-fi or mobile network to start playong online.", 0).show();
				//wifi.setWifiEnabled(true);
				break;
			}
			
			Intent roomsAct = new Intent(this, RoomsActivity.class);
			startActivity(roomsAct);
			break;
		case R.id.buttonSettings:
			Intent settAct = new Intent(this, SettingsActivity.class);
			startActivity(settAct);
			break;
			
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		PrefApplication.checkPlayServices();
	}

	private void findViews() {
		btnStartNewGame = (Button) findViewById(R.id.buttonStart);
		btnSettings = (Button) findViewById(R.id.buttonSettings);
		btnExit = (Button) findViewById(R.id.buttonExit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry, menu);
		return true;
	}


	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String msg = intent.getStringExtra("message");
		int msgType = Integer.parseInt(intent.getStringExtra("messageType"));
		switch (msgType)
		{
		case 0: 
			GameInfo.getInstance().ownPlayer.id = msg;
		}
	}
	/*
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("notification", "offline"));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
	}*/
	
}
