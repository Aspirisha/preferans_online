package ru.springcoding.prefomega.rooms;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import ru.springcoding.prefomega.R;
import ru.springcoding.prefomega.RoomInfo;

public class FullRoomInfoDialog extends DialogFragment implements OnClickListener {
	private TextView tv_id;
	private TextView tv_hasPassword;
	private TextView tv_gameType;
	private TextView tv_numberOfPlayers;
	private TextView tv_bullet;
	private TextView tv_whistCost;
	private TextView tv_hasStalingrad;
	private TextView tv_raspExit;
	private TextView tv_raspProg;
	private TextView tv_raspIsNoWhistExit;
	private TextView tv_hasWithoutThree;
	private TextView tv_hasTenWhist;
	private Button btn_back;
	private Button btn_connect;
	private RoomInfo ri = null;
	private boolean isInitialized = false;
	
	public FullRoomInfoDialog(RoomInfo ri) {
		this.ri = ri;
	}
	
	static FullRoomInfoDialog newInstance(RoomInfo ri) {
		FullRoomInfoDialog f = new FullRoomInfoDialog(ri);
        return f;
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.full_room_info, container);
		
		tv_id = (TextView) v.findViewById(R.id.fi_roomID);
		tv_hasPassword = (TextView) v.findViewById(R.id.fi_hasPassword);
		tv_gameType = (TextView) v.findViewById(R.id.fi_gameType);
		tv_numberOfPlayers = (TextView) v.findViewById(R.id.fi_playersNumber);
		tv_bullet = (TextView) v.findViewById(R.id.fi_bullet);
		tv_whistCost = (TextView) v.findViewById(R.id.fi_whistCost);
		tv_hasStalingrad = (TextView) v.findViewById(R.id.fi_hasStalingrad);
		tv_raspExit = (TextView) v.findViewById(R.id.fi_raspExit);
		tv_raspProg = (TextView) v.findViewById(R.id.fi_raspProg);
		tv_raspIsNoWhistExit = (TextView) v.findViewById(R.id.fi_noWhistRaspExit);
		tv_hasWithoutThree = (TextView) v.findViewById(R.id.fi_withoutThree);
		tv_hasTenWhist = (TextView) v.findViewById(R.id.fi_tenWhist);
		btn_back = (Button) v.findViewById(R.id.fi_button_hide);
		btn_connect = (Button) v.findViewById(R.id.fi_button_connect);
			
		btn_back.setOnClickListener(this);
		btn_connect.setOnClickListener(this);
		
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (null != ri)
			updateInfo(ri);
		else
			Log.e("FullRoomInfo", "Null room info passed");
		return v;
	}
	
	public void updateInfo(RoomInfo ri) {
		tv_bullet.setText(Integer.toString(ri.bullet));
		tv_gameType.setText("Gag");
		tv_hasPassword.setText(Boolean.toString(ri.hasPassword));
		tv_hasStalingrad.setText(Boolean.toString(ri.stalingrad));
		tv_hasTenWhist.setText(Boolean.toString(ri.tenWhist));
		tv_hasWithoutThree.setText(Boolean.toString(ri.tenWhist));
		tv_id.setText(Integer.toString(ri.id));
		tv_numberOfPlayers.setText(Integer.toString(ri.playersNumber));
		
		String raspExitArray = Integer.toString(ri.raspExit[0]);
		for (int i = 1; i < 3; i++)
			raspExitArray += ", " + Integer.toString(ri.raspExit[i]);
		tv_raspExit.setText(raspExitArray.substring(1, raspExitArray.length() - 1));
		tv_raspIsNoWhistExit.setText(Boolean.toString(ri.noWhistRaspasyExit));
		
		String raspProgArray = Integer.toString(ri.raspProgression[0]);
		for (int i = 1; i < 3; i++)
			raspProgArray += ", " + Integer.toString(ri.raspProgression[i]);
		tv_raspProg.setText(raspProgArray.substring(1, raspProgArray.length() - 1));
		
		tv_whistCost.setText(Float.toString(ri.whistCost));
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fi_button_connect:
			
			break;
		case R.id.fi_button_hide:
			this.dismiss();
			break;
		default:
			break;
		}
	}

}
