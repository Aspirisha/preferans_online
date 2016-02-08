package ru.springcoding.prefomega;

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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import ru.springcoding.prefomega.CommonEnums.MessageTypes;
import ru.springcoding.prefomega.CommonEnums.RecieverID;
import ru.springcoding.prefomega.rooms.RoomsActivity;

public class EntryActivity extends FragmentActivity implements OnClickListener {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";

    String SENDER_ID = "841120567778";
    static final String TAG = "GCMDemo";
    static final int MAX_PING_TRIES = 10;

    // TextView mDisplay;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    Button btnStartNewGame;
    Button btnSettings;
    Button btnExit;
    Button btnQuit;

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
            Log.i(TAG, "Found play services");
            PrefApplication.getInstance().getRegistrationId();

            if (PrefApplication.regid.isEmpty()) {
                PrefApplication.getInstance().registerInBackground();
            }

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            Toast.makeText(this, "CheckPlayServices: No valid Google Play Services APK found.", Toast.LENGTH_LONG);
        }
        String service = Context.WIFI_SERVICE;
        wifi = (WifiManager) getSystemService(service);

        btnQuit.setOnClickListener(this);
        btnStartNewGame.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        reset_logged_as();
        if (!isOnline()) {
            Toast.makeText(this, "Internet connection not available.", Toast.LENGTH_LONG).show();
        } else {
            pingServer();
        }

        // tell the server we are online now

        if (GameInfo.isSignedIn) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
            nameValuePairs.add(new BasicNameValuePair("notification", "online"));
            nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
            PrefApplication.sendData(nameValuePairs, false);
        }

        PrefApplication.runKeepAlive(true);
    }


    public void pingServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long millisPerTry = 5000;
                long sleepTime;
                int tries = 0;

                while (tries < MAX_PING_TRIES) {
                    long startTime = System.currentTimeMillis();
                    if (!PrefApplication.pingStatus) {
                        tries++;
                        Log.i("Ping", "Trying to ping server. Try #" + Integer.toString(tries));
                        sendPingRequest();
                        sleepTime = millisPerTry - (System.currentTimeMillis() - startTime);
                        try {
                            if (sleepTime > 0)
                                Thread.sleep(sleepTime);
                            else
                                Thread.sleep(10);
                        } catch (Exception e) {
                            Log.i("Exception: ", e.toString());
                        }
                    } else {
                        tries = 0;
                        break;
                    }
                }
                if (tries == MAX_PING_TRIES) {
                    String oldId = PrefApplication.getInstance().getRegistrationId();
                    PrefApplication.getInstance().registerInBackground();
                }

            }

        });
        thread.start();
    }

    private void sendPingRequest() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
        nameValuePairs.add(new BasicNameValuePair("request", "ping"));
        nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
        PrefApplication.sendData(nameValuePairs, true);
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
                if (!GameInfo.isSignedIn) {
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
            case R.id.buttonQuit:
                PrefApplication.signOut();
                reset_logged_as();
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
        btnQuit = (Button) findViewById(R.id.buttonQuit);
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
        switch (msgType) {
            case ENTRY_ONLINE_NOTIFICATION_ANSWER:
                break;
            case ENTRY_REGISTRATION_RESULT:
            case ENTRY_LOGIN_RESULT:
                try {
                    String data[] = msg.split(" ");

                    GameInfo.ownPlayer.id = data[0];

                    GameInfo.ownPlayer.name = data[1];
                    GameInfo.password = data[2];
                    GameInfo.isSignedIn = true;
                    PrefApplication.getInstance().storeLoginAndPassword();

                    dlg.dismiss();
                    dlg = null;
                    if (msgType == MessageTypes.ENTRY_REGISTRATION_RESULT)
                        Toast.makeText(this, "Successfully registered!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, "Successfully signed in!", Toast.LENGTH_LONG).show();

                    reset_logged_as();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }

                break;
            default:
                break;

        }
    }

    void reset_logged_as() {
        TextView logged_as = (TextView) findViewById(R.id.loagged_as_tv);
        String prefix = getResources().getString(R.string.currently_logged_as);
        logged_as.setText(String.format(prefix, GameInfo.ownPlayer.name));
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
