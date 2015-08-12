package ru.springcoding.prefomega;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import ru.springcoding.common.CommonEnums.MessageTypes;
import ru.springcoding.common.CommonEnums.RecieverID;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EntryActivity extends FragmentActivity implements OnClickListener {
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
	RegisterDialog dlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry);
		
		PrefApplication.setVisibleWindow(RecieverID.ENTRY_ACTIVITY, this);
		
		if (!PrefApplication.getInstance().getLoginAndPassword())
			showRegistrationPopup();
		
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
		
		if (!isOnline()) {
			Toast.makeText(this, "Internet connection not available.", 0).show();
		}
		
		// tell the server we are online now
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("notification", "online"));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		PrefApplication.sendData(nameValuePairs);
		
		PrefApplication.runKeepAlive(true);
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonExit:
			// send notification to server that we have exited
			finish();
			break;
		case R.id.buttonStart:		
			if (!GameInfo.isRegistered) {
				showRegistrationPopup();
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
		MessageTypes msgType = MessageTypes.valueOf(intent.getStringExtra("messageType"));
		switch (msgType)
		{
		case ENTRY_ONLINE_NOTIFICATION_ANSWER:
			break;
		case ENTRY_REGISTRATION_RESULT:
			try {
				String data[] = msg.split(" ");
				
				int id = Integer.parseInt(data[0]);
				GameInfo.ownPlayer.id = data[0];
				
				GameInfo.ownPlayer.name = data[1];
				GameInfo.password = data[2];
				GameInfo.isRegistered = true;
				PrefApplication.getInstance().storeLoginAndPassword();
				
				dlg.dismiss();
				dlg = null;
			} catch (NumberFormatException e) {
				
			}
				
			break;
		default:
			break;
			
		}
	}
	
	private void showRegistrationPopup() {		
		dlg = new RegisterDialog();
		dlg.show(getSupportFragmentManager(), "dlg");
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
