package ru.springcoding.prefomega;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import ru.springcoding.common.CommonEnums.GameType;
import ru.springcoding.common.CommonEnums.RecieverID;
import ru.springcoding.common.RoomInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RoomsActivity extends Activity implements OnClickListener {

	Button btnBack;
	Button btnCreateNewRoom;
	Button btnConnectToRoom;
	RoomInfo[] rooms;
	TableClickListener tableClickListener;
	private int prevSelectedId = -1;
	long lastRefreshTime = System.currentTimeMillis();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrefApplication.setVisibleWindow(RecieverID.ROOMS_ACTIVITY, this);
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
			TextView col1 = (TextView)getLayoutInflater().inflate(R.layout.celltemplate, row);// TODO check if here null should be
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
		nameValuePairs.add(new BasicNameValuePair("id", GameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("password", GameInfo.password));
		nameValuePairs.add(new BasicNameValuePair("request", "existing_rooms")); // 1 = money
		nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
		PrefApplication.sendData(nameValuePairs);
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
			nameValuePairs.add(new BasicNameValuePair("room_id", Integer.toString(rooms[number].id)));
			nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
			PrefApplication.sendData(nameValuePairs);
			break;
		}
	}
	private void redrawTable(int roomsNumber) {
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
		
		for (int i = 0; i < roomsNumber; ++i) {
			cols.clear();
			cols.add(Integer.toString(rooms[i].id));
			cols.add(Integer.toString(rooms[i].bullet));
			cols.add(Float.toString(rooms[i].whistCost));
			cols.add(Integer.toString(rooms[i].playersNumber));
			contentTable = addRowToContentTable(contentTable, cols, 0);
			lengthRow = 0;
			for (String c : cols)
				lengthRow += c.length();
			
			if (longestRow.isEmpty() || lengthRow > (longestRow.length() - 1)) //Include -1 for subtracting the space occupied by "-"
				longestRow = rooms[i].id + "-" + rooms[i].bullet + "-" 
			+ rooms[i].whistCost + "-" + rooms[i].playersNumber;
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String msg = intent.getStringExtra("message");
		int msgType = Integer.parseInt(intent.getStringExtra("messageType"));
		switch (msgType)
		{
		case 0: // roomsData is got
			String[] roomsData = msg.split(" ");
			int roomsNumber = Integer.parseInt(roomsData[0]);
			rooms = new RoomInfo[roomsNumber];
			for (int i = 0; i < roomsNumber; ++i)
				rooms[i] = new RoomInfo();
			for (int i = 0; i < roomsNumber; ++i) {
				rooms[i].id = Integer.parseInt(roomsData[RoomInfo.roomInfoFields * i + 1]);
				rooms[i].name = roomsData[RoomInfo.roomInfoFields  * i + 2];
				rooms[i].bullet = Integer.parseInt(roomsData[RoomInfo.roomInfoFields  * i + 3]);
				rooms[i].whistCost = Float.parseFloat(roomsData[RoomInfo.roomInfoFields  * i + 4]);
				rooms[i].gameType = GameType.valueOf(roomsData[RoomInfo.roomInfoFields  * i + 5]);
				rooms[i].gameType = GameType.valueOf(roomsData[RoomInfo.roomInfoFields  * i + 6]);
				rooms[i].raspExit = new int[3];
				String ar[] = roomsData[RoomInfo.roomInfoFields  * i + 7].split(",");
				for (int j = 0; j < 3; j++) 
					rooms[i].raspExit[j] = Integer.parseInt(ar[j]);
				
				rooms[i].raspProgression = new int[3];
				ar = roomsData[RoomInfo.roomInfoFields  * i + 8].split(",");
				for (int j = 0; j < 3; j++) 
					rooms[i].raspProgression[j] = Integer.parseInt(ar[j]);
				
				rooms[i].withoutThree = Boolean.parseBoolean(roomsData[RoomInfo.roomInfoFields  * i + 9]);
				rooms[i].noWhistRaspasyExit = Boolean.parseBoolean(roomsData[RoomInfo.roomInfoFields  * i + 10]);
				rooms[i].stalingrad = Boolean.parseBoolean(roomsData[RoomInfo.roomInfoFields  * i + 11]);
				rooms[i].tenWhist = Boolean.parseBoolean(roomsData[RoomInfo.roomInfoFields  * i + 12]);
				rooms[i].hasPassword = Boolean.parseBoolean(roomsData[RoomInfo.roomInfoFields  * i + 13]);
				rooms[i].playersNumber = Integer.parseInt(roomsData[RoomInfo.roomInfoFields  * i + 14]);
			}
			redrawTable(roomsNumber);
			break;
			
		case 1: // answer for connection request
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
			}			
		}
		public int getSelectedRowId() {
			return prevSelectedId;
		}
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
