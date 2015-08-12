package ru.springcoding.prefomega;

import ru.springcoding.common.CommonEnums.RecieverID;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String LOG_TAG = "GetAClue::GCMIntentService";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    // Message receivers codes
    static final int ENTRY_ACTIVITY = 0;
    static final int NEW_ROOM_ACTIVITY = 1;
    static final int EXISTING_ROOMS_ACTIVITY = 2;
    static final int GAME_ACTIVITY = 3;
    static final int SETTINGS_ACTIVITY = 4;
    static final int KEEPALIVE_MANAGER = 777;
    
    public GCMIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification(RecieverID.ENTRY_ACTIVITY, intent);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(RecieverID.ENTRY_ACTIVITY, intent);
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String s = extras.getString("message");
                RecieverID receiver;
                
                try {
                	receiver = RecieverID.valueOf(extras.getString("receiver"));
                	Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                } 
                
                catch (Exception e) {
                	Log.i(TAG, "Exception on receiver defining:" + e.toString());
                	return;
                }
                
                // Post notification of received message.
                sendNotification(receiver, intent);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(RecieverID receiver, Intent intent) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = null;
        
        if (receiver != PrefApplication.getVisibleWindow()) // message is not fresh
        	return; 
        
        switch (receiver) {
        	case ENTRY_ACTIVITY: 
        		intent.setClass(this, EntryActivity.class);
        		break;
        	case NEW_ROOM_ACTIVITY:
        		intent.setClass(this, NewRoomActivity.class);
                break;
        	case ROOMS_ACTIVITY:
        		intent.setClass(this, RoomsActivity.class);
        		break;
        	case GAME_ACTIVITY:
        		intent.setClass(this, GameActivity.class);
        		break;
        	case SETTINGS_ACTIVITY:
        		intent.setClass(this, SettingsActivity.class);
        		break;
        	case KEEP_ALIVE: // it's keepalive message 
        		String msg[] = intent.getStringExtra("message").split(" ");
        		GameInfo.previous_server_keepalive_time = GameInfo.current_server_keepalive_time;
        		GameInfo.current_server_keepalive_time = Long.parseLong(msg[0]);
        		GameInfo.ownPlayer.timeLeft = Integer.parseInt(msg[GameInfo.ownPlayer.getMyNumber()]);
        		GameInfo.prevPlayer.timeLeft = Integer.parseInt(msg[GameInfo.prevPlayer.getMyNumber()]);
        		GameInfo.nextPlayer.timeLeft = Integer.parseInt(msg[GameInfo.nextPlayer.getMyNumber()]);
        		break;
            default: // no activity has such address => it's error
            	return;
        }
		contentIntent = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try { 
        	contentIntent.send();
        } 
        catch (Exception e)
        {
        	Log.i(TAG, "Exception: " + e.toString());        	
        }
        
        if (contentIntent != null) {
	     /*   NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(this)
	        .setContentTitle("GCM Notification")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(msg))
	        .setContentText(msg);
	
	        mBuilder.setContentIntent(contentIntent);
	        
	        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/
        }
    }
    
   

}
