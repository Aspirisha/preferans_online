package ru.springcoding.prefomega;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.util.Log;

public class RegistrationTestThread extends Thread {
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
        	 if (!PrefApplication.pingStatus)
        	 {
        		 tries++;
	        	 sendPingRequest();
	        	 sleepTime = millisPerTry - (System.currentTimeMillis() - startTime);
	        	 try {
	        		 if (sleepTime > 0)
	        			 sleep(sleepTime);
	        		 else
	        			 sleep(10);
	        	 } catch (Exception e) {
	        		 Log.i("Exeption: ", e.toString());
	        	 }
        	 } else {
        		 break;
        	 }
         }
         if (tries == 10) {
        	 String oldId = PrefApplication.getInstance().getRegistrationId();
        	 PrefApplication.getInstance().registerInBackground();
        	 if (!oldId.isEmpty()) // we had some regId before so smth crashed. Tell it to server
        		 sendServerOldId(oldId);
         }
	 }
	 
	 private void sendPingRequest() {
		 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		 nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		 nameValuePairs.add(new BasicNameValuePair("request", "ping"));
		 PrefApplication.sendData(nameValuePairs, "RequestManager.php"); 
	 }
	 
	 private void sendServerOldId(String id) {
		 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		 nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		 nameValuePairs.add(new BasicNameValuePair("notification", "old_id"));
		 nameValuePairs.add(new BasicNameValuePair("old_id", id));
		 PrefApplication.sendData(nameValuePairs, "NotificationManager.php");  
	 }
}