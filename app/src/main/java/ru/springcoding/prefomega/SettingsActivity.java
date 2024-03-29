package ru.springcoding.prefomega;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import ru.springcoding.prefomega.CommonEnums.RecieverID;

public class SettingsActivity extends Activity implements OnClickListener {
	Button btnSettingsBack;
	Button btnApply;
	EditText etUserName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrefApplication.setVisibleWindow(RecieverID.SETTINGS_ACTIVITY, this);
		setContentView(R.layout.settings);
		
        btnSettingsBack = (Button)findViewById(R.id.buttonSettingsBack);
        btnApply = (Button)findViewById(R.id.buttonApply);
        etUserName = (EditText)findViewById(R.id.editTextUserName);
        
        btnSettingsBack.setOnClickListener(this);
        btnApply.setOnClickListener(this);
        
        if (GameInfo.ownPlayer.name.isEmpty()) {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
			nameValuePairs.add(new BasicNameValuePair("request", "current_name"));
			nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
			PrefApplication.sendData(nameValuePairs, false);
        } else {
        	etUserName.setText(GameInfo.ownPlayer.name);
        }
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonSettingsBack:
			
			finish();
			break;
		case R.id.buttonApply:
			String newName = etUserName.getText().toString();
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
	        nameValuePairs.add(new BasicNameValuePair("new_name", newName));
	        nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
			PrefApplication.sendData(nameValuePairs, false);
			GameInfo.ownPlayer.name = newName;
			break;
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.entry, menu);
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	// TODO Auto-generated method stub
    	super.onNewIntent(intent);
    	String msg = intent.getStringExtra("message");
		int msgType = Integer.parseInt(intent.getStringExtra("messageType"));
		switch (msgType)
		{
		case 0: // our current name (in future may be another current data)
			etUserName.setText(msg);
			GameInfo.ownPlayer.name = msg;
			break;
		default:
			return;
		}
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    
    // save here all ui settings user has to restore than
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
};