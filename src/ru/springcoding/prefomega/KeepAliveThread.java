package ru.springcoding.prefomega;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import ru.springcoding.prefomega.PlayingTableView.DrawState;
import android.graphics.Canvas;
import android.util.Log;

public class KeepAliveThread extends Thread {
	
	private boolean isRunning = false;
	private int delay = 30000; // delay in microseconds between keepAlive messages
	
	public void setRunning(boolean run) {
        isRunning = run;
	}
	
	public void setDelay(int newDelay) {
		delay = newDelay;
	}
	
	@Override
    public void run() {
        long startTime;
        long sleepTime;
        
        while (isRunning) {
        	startTime = System.currentTimeMillis(); 

        	sendKeepAliveMessage();

        	sleepTime = delay - (System.currentTimeMillis() - startTime);
        	try {
        		if (sleepTime > 0)
        			sleep(sleepTime);
        		else
        			sleep(10);
        	} catch (Exception e) {
        		Log.i("Exeption: ", e.toString());
        	}
        }
	 }
	
	private void sendKeepAliveMessage() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("notification", "keep_alive"));
		nameValuePairs.add(new BasicNameValuePair("id", GameInfo.getInstance().ownPlayer.id));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
	}
	
}