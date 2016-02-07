package ru.springcoding.prefomega;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class RegistrationTestThread extends Thread  {
	private boolean isRunning = false;
	
	public void setRunning(boolean run) {
        isRunning = run;
	}
	
	
	 @SuppressLint("WrongCall")
	@Override
     public void run() {
		 long millisPerTry = 3000;
         long startTime;
         long sleepTime;
         int tries = 0;
         
         while (isRunning && tries < 10) {
        	 startTime = System.currentTimeMillis();
        	 if (!PrefApplication.pingStatus) {
        		 tries++;
        		 Log.i("Ping", "Trying to ping server. Try #" + Integer.toString(tries));
	        	 sendPingRequest();
	        	 sleepTime = millisPerTry - (System.currentTimeMillis() - startTime);
	        	 try {
	        		 if (sleepTime > 0)
	        			 sleep(sleepTime);
	        		 else
	        			 sleep(10);
	        	 } catch (Exception e) {
	        		 Log.i("Exception: ", e.toString());
	        	 }
        	 } else {
        		 tries = 0;
        		 break;
        	 }
         }
         if (tries == 10) {
        	 String oldId = PrefApplication.getInstance().getRegistrationId();
        	 // TODO: change to AsynkTask in EntryActivity
        	 // PrefApplication.getInstance().registerInBackground();

             PendingIntent contentIntent = null;
             Intent intent = new Intent();
             intent.putExtra("message", "");
             intent.putExtra("messageType",
            		 CommonEnums.MessageTypes.NEED_REGID_UPDATE.toString());
             //contentIntent = PendingIntent.getActivity(this, 1,
               //      intent, PendingIntent.FLAG_UPDATE_CURRENT);
            
        	 //if (!oldId.isEmpty()) // we had some regId before so smth crashed. Tell it to server
        		 //sendServerOldId(oldId);
         }
	 }
	 
	 private void sendPingRequest() {
		 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		 nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		 nameValuePairs.add(new BasicNameValuePair("request", "ping"));
		 nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
		 PrefApplication.sendData(nameValuePairs, false); 
	 }
	 
	 private void sendServerOldId(String id) {
		 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		 nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		 nameValuePairs.add(new BasicNameValuePair("notification", "old_id"));
		 nameValuePairs.add(new BasicNameValuePair("old_id", id));
		 nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		 PrefApplication.sendData(nameValuePairs, false);  
	 }
}