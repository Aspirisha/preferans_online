package ru.springcoding.prefomega;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RoomsActivity extends Activity implements OnClickListener {
	
	class RoomData {
		public String id;
		public String playersNumber;
		public String gameBet;
		public String roomName;
		public boolean isStalingrad;
		public String gameType;
		public String gameBullet;
	}
	
	Button btnBack;
	Button btnCreateNewRoom;
	Button btnConnectToRoom;
	RoomData[] rooms;
	Map<Integer, Integer> rowIds;
	TableClickListener tableClickListener;
	private int prevSelectedId = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrefApplication.setVisibleWindow(2, this);
		setContentView(R.layout.rooms);
		
		btnBack = (Button)findViewById(R.id.buttonRoomsBack);
		btnCreateNewRoom = (Button)findViewById(R.id.buttonCreateRoom);
		btnConnectToRoom = (Button)findViewById(R.id.buttonConnectToRoom);
		btnBack.setOnClickListener(this);
		btnCreateNewRoom.setOnClickListener(this);
		btnConnectToRoom.setOnClickListener(this);
		btnConnectToRoom.setEnabled(false);
		
		ScrollView lw = (ScrollView)findViewById(R.id.scrollview_rooms);
		
		ArrayList<String> cols = new ArrayList<String>();
		tableClickListener = new TableClickListener();
		rowIds = new HashMap<Integer, Integer>();
		refreshExistingRooms();
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
		rowIds.put(row.getId(), currentRowNumber);
		row.setOnClickListener(tableClickListener);
		
		android.widget.TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.weight = 1;
		
		for (String s : cols) {
			//TextView col1 = new TextView(context);
			TextView col1 = (TextView)getLayoutInflater().inflate(R.layout.celltemplate, null);
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
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("request", "existing_rooms")); // 1 = money
		PrefApplication.sendData(nameValuePairs, "RequestManager.php");
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
			int rowId = tableClickListener.getSelectedRowId();
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
			nameValuePairs.add(new BasicNameValuePair("request", "connect_to_existing")); 
			nameValuePairs.add(new BasicNameValuePair("room_id", Integer.toString(rowIds.get(prevSelectedId))));
			PrefApplication.sendData(nameValuePairs, "RequestManager.php");
			break;
		}
	}
	private void redrawTable(int roomsNumber) {
		String longestRow = "";
		int lengthRow = 0;
		TableLayout contentTable = (TableLayout)findViewById(R.id.content_table);
		contentTable.removeAllViews();
		
		ArrayList<String> cols = new ArrayList<String>();
		cols.add("Number");
		cols.add("Players");
		cols.add("Bet");
		contentTable = addRowToContentTable(contentTable, cols, -1);
		
		for (int i = 0; i < roomsNumber; ++i) {
			cols.clear();
			cols.add(Integer.toString(i + 1));
			cols.add(rooms[i].playersNumber);
			cols.add(rooms[i].gameBet);
			contentTable = addRowToContentTable(contentTable, cols, 0);
			lengthRow = rooms[i].id.length() + rooms[i].playersNumber.length() + rooms[i].gameBet.length();
			if (longestRow.isEmpty() || lengthRow > (longestRow.length() - 1)) //Include -1 for subtracting the space occupied by "-"
				longestRow = rooms[i].id + "-" + rooms[i].playersNumber + "-" + rooms[i].gameBet;
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
			rooms = new RoomData[roomsNumber];
			for (int i = 0; i < roomsNumber; ++i)
				rooms[i] = new RoomData();
			for (int i = 0; i < roomsNumber; ++i) {
				rooms[i].id = roomsData[3 * i + 1];
				rooms[i].playersNumber = roomsData[3 * i + 2];
				rooms[i].gameBet = roomsData[3 * i + 3];
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
		//refreshExistingRooms(); // or it must be before super?
	}
}
