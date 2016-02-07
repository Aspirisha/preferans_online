package ru.springcoding.prefomega.rooms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;

import ru.springcoding.prefomega.CommonEnums;
import ru.springcoding.prefomega.GameActivity;
import ru.springcoding.prefomega.NewRoomActivity;
import ru.springcoding.prefomega.PrefApplication;
import ru.springcoding.prefomega.R;
import ru.springcoding.prefomega.RoomInfo;

public class RoomsActivity extends FragmentActivity implements OnClickListener {

	Button btnBack;
	Button btnCreateNewRoom;
	Button btnConnectToRoom;
	LinkedList<RoomInfo> rooms;
	TableClickListener tableClickListener;
	private int prevSelectedId = -1;
	long lastRefreshTime = System.currentTimeMillis();
	private int selectedRoomId = -1;
	private FullRoomInfoDialog fullRoomInfoDlg = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrefApplication.setVisibleWindow(CommonEnums.RecieverID.ROOMS_ACTIVITY, this);
		setContentView(R.layout.rooms);
		
		btnBack = (Button)findViewById(R.id.buttonRoomsBack);
		btnCreateNewRoom = (Button)findViewById(R.id.buttonCreateRoom);
		btnConnectToRoom = (Button)findViewById(R.id.buttonConnectToRoom);
		btnBack.setOnClickListener(this);
		btnCreateNewRoom.setOnClickListener(this);
		btnConnectToRoom.setOnClickListener(this);
		btnConnectToRoom.setEnabled(false);
		
		tableClickListener = new TableClickListener();
		refreshExistingRooms(); // or it must be before super?
	}
	
	private int findUnusedId() {
		int id = 1;
		while (findViewById(++id) != null);
		return id;
	}
	
	private TableLayout addRowToContentTable(TableLayout table, ArrayList<String> cols, int currentRowNumber) {
		Context context = getApplicationContext();
		TableRow row = new TableRow(context);
		
		row.setClickable(true);
		row.setFocusable(true);
		row.setId(findUnusedId());
		row.setOnClickListener(tableClickListener);
		
		android.widget.TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.weight = 1;
		
		for (String s : cols) {
			//TextView col1 = new TextView(context);
			TextView col1 = (TextView)getLayoutInflater().inflate(R.layout.celltemplate, null);// TODO check if here null should be
			col1.setText(s);

			col1.setLayoutParams(params);
			//col1.setBackgroundResource(R.style.TableCell);
			//col1.setTextAppearance(this, R.style.TableCell);
			//col1.setTextSize(getResources().getDimension(R.dimen.table_text_size));
			row.addView(col1);
			
		}
		
		table.addView(row);
		return table;
	}
	
	protected void refreshExistingRooms() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("request", "existing_rooms")); // 1 = money
		nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
		PrefApplication.sendData(nameValuePairs, false);
		lastRefreshTime = System.currentTimeMillis();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rooms, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonRoomsBack:
			finish();
			break;
		case R.id.buttonCreateRoom:
			Intent newRoomActivity = new Intent(this, NewRoomActivity.class);
			startActivity(newRoomActivity);
			break;
		case R.id.buttonConnectToRoom:
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
			nameValuePairs.add(new BasicNameValuePair("request", "connect_to_existing")); 
			TableRow row = (TableRow)findViewById(prevSelectedId);
			TextView numberTextView = (TextView)row.getChildAt(0);
			int number = Integer.parseInt(numberTextView.getText().toString()) - 1;
			nameValuePairs.add(new BasicNameValuePair("room_id", 
					Integer.toString(rooms.get(number).id)));
			nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
			PrefApplication.sendData(nameValuePairs, false);
			break;
		}
	}
	private void redrawTable() {
		String longestRow = "";
		int lengthRow = 0;
		TableLayout contentTable = (TableLayout)findViewById(R.id.content_table);
		contentTable.removeAllViews();
		
		ArrayList<String> cols = new ArrayList<String>();
		cols.add("Room ID"); // TODO remove hardcode
		cols.add("Bullet"); // TODO remove hardcode
		cols.add("Whist cost"); // TODO remove hardcode
		cols.add("Players"); // TODO remove hardcode
		contentTable = addRowToContentTable(contentTable, cols, -1);
		
		for (RoomInfo r : rooms) {
			cols.clear();
			cols.add(Integer.toString(r.id));
			cols.add(Integer.toString(r.bullet));
			cols.add(Float.toString(r.whistCost));
			cols.add(Integer.toString(r.playersNumber));
			contentTable = addRowToContentTable(contentTable, cols, 0);
			lengthRow = 0;
			for (String c : cols)
				lengthRow += c.length();
			
			if (longestRow.isEmpty() || lengthRow > (longestRow.length() - 1)) //Include -1 for subtracting the space occupied by "-"
				longestRow = r.id + "-" + r.bullet + "-" 
			+ r.whistCost + "-" + r.playersNumber;
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String msg = intent.getStringExtra("message");
		CommonEnums.MessageTypes msgType = CommonEnums.MessageTypes.
				valueOf(intent.getStringExtra("messageType"));
		switch (msgType)
		{
		case ROOMS_EXISTING_ROOMS: // roomsData is got
			Log.i("ROOMS_EXISTING_ROOMS", msg);
			
			Gson gson = new Gson();
			try {
				Type collectionType = new TypeToken<LinkedList<RoomInfo>>(){}.getType();
				rooms = gson.fromJson(msg, collectionType);
			} catch (Exception e) { // meaning "ERROR" was sent
				Log.e("ROOMS_EXISTING_ROOMS", "Error sending rooms");
				rooms = null;
			}
			
			redrawTable();
			break;
			
		case ROOMS_CONNECTION_RESULT: // answer for connection request
			String[] data = msg.split(" ");
			int status = Integer.parseInt(data[0]);
			switch (status) {
			case 0: // connection succeeded
				Intent gameActivity = new Intent(this, GameActivity.class);
				Bundle b = new Bundle();
				b.putString("room_id", data[1]);
				b.putString("own_number", data[2]);
				gameActivity.putExtras(b);
				startActivity(gameActivity);
				break;
			case 1: // room is full already
				refreshExistingRooms();
				break;
			case 2:  // room doesn't exist anymore
				refreshExistingRooms();
				break;
			}
			break;
		case ROOMS_NEW_ROOM_CREATION_RESULT:
			
			break;
		default: // unknown msg_type
			return;
		}
	}
	
	class TableClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int newId = v.getId();
			if (newId != prevSelectedId) {
				//v.setBackgroundColor(getResources().getColor(R.color.selected_row));
				TableRow tRow = (TableRow)v;
				TableLayout contentTable = (TableLayout)findViewById(R.id.content_table);
				if (contentTable.getChildAt(0) == tRow)
					return;
				int childNumber = tRow.getChildCount();
				selectedRoomId = Integer.parseInt((String) ((TextView) 
						tRow.getChildAt(0)).getText());
				
				for (int i = 0; i < childNumber; i++) {
					tRow.getChildAt(i).setSelected(true);
				}
				if (prevSelectedId != -1) {
					TableRow u = (TableRow)findViewById(prevSelectedId);
					childNumber = u.getChildCount();
					for (int i = 0; i < childNumber; i++) {
						u.getChildAt(i).setSelected(false);
					}
					//u.setBackgroundColor(getResources().getColor(R.color.unselected_row));
				}
				prevSelectedId = newId;
				btnConnectToRoom.setEnabled(true);
				if (selectedRoomId != -1) {
					
					int index = contentTable.indexOfChild(tRow);

					//fullRoomInfoDlg.updateInfo(rooms.get(index - 1));
					showDialog(rooms.get(index - 1));
				}
			}			
		}
		public int getSelectedRowId() {
			return prevSelectedId;
		}
	}
	
	void showDialog(RoomInfo ri) {

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    // Create and show the dialog.
	    DialogFragment newFragment = FullRoomInfoDialog.newInstance(ri);
	    
	    newFragment.show(ft, "dialog");
	    Log.i("here", "gfyurfegre");
	    //((FullRoomInfoDialog)newFragment).updateInfo(ri);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		refreshExistingRooms();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		long curTime = System.currentTimeMillis();
		if (curTime - lastRefreshTime > 30000) // TODO hardcoded
			refreshExistingRooms();
	}
}
