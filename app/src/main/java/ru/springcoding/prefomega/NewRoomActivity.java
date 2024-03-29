package ru.springcoding.prefomega;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class NewRoomActivity extends Activity implements OnClickListener, OnTouchListener {
    Button buttonCancel;
    Button buttonApply;
    Spinner spinGameType;
    Spinner spinBullet;
    CheckBox checkStalingrad;
    EditText editBet;
    TextView tvYourMoney;
    int myMoney = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefApplication.setVisibleWindow(CommonEnums.RecieverID.NEW_ROOM_ACTIVITY, this);
        setContentView(R.layout.new_room);

        buttonCancel = (Button) findViewById(R.id.buttonNewRoomBack);
        buttonApply = (Button) findViewById(R.id.buttonOkNewRoom);
        spinGameType = (Spinner) findViewById(R.id.spinnerGameType);
        spinBullet = (Spinner) findViewById(R.id.spinnerBullet);
        checkStalingrad = (CheckBox) findViewById(R.id.checkStalingrad);
        editBet = (EditText) findViewById(R.id.editBet);
        tvYourMoney = (TextView) findViewById(R.id.yourMoneyOnNewRoom);

        buttonCancel.setOnClickListener(this);
        buttonApply.setOnClickListener(this);

        spinGameType.getBackground().setAlpha(100);
        spinBullet.getBackground().setAlpha(100);
        ArrayAdapter<CharSequence> bulletAdapter;
        bulletAdapter = ArrayAdapter.createFromResource(this, R.array.bullet_types, android.R.layout.simple_spinner_item);
        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        bulletAdapter.setDropDownViewResource(spinner_dd_item);
        spinBullet.setAdapter(bulletAdapter);

        ArrayAdapter<CharSequence> gameTypeAdapter;
        gameTypeAdapter = ArrayAdapter.createFromResource(this, R.array.game_types, android.R.layout.simple_spinner_item);
        gameTypeAdapter.setDropDownViewResource(spinner_dd_item);
        spinGameType.setAdapter(gameTypeAdapter);

        // get money first of all
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
        nameValuePairs.add(new BasicNameValuePair("request", "my_money")); // 1 = money
        nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
        PrefApplication.sendData(nameValuePairs, false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOkNewRoom:
                String bet = editBet.getText().toString();
                if (bet.isEmpty()) {
                    Toast.makeText(this, "Insert game bet to create a room.", Toast.LENGTH_SHORT).show();
                    break;
                }
                int gameBet = Integer.parseInt(editBet.getText().toString());
                if (gameBet > myMoney) {
                    Toast.makeText(this, "You don't have enough money for this bet.", Toast.LENGTH_SHORT).show();
                    break;
                }
                String stalingrad = "0";
                if (checkStalingrad.isChecked())
                    stalingrad = "1";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
                nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
                nameValuePairs.add(new BasicNameValuePair("game_type", spinGameType.getSelectedItem().toString()));
                nameValuePairs.add(new BasicNameValuePair("whist_cost", bet));
                nameValuePairs.add(new BasicNameValuePair("bullet", spinBullet.getSelectedItem().toString()));
                nameValuePairs.add(new BasicNameValuePair("stalingrad", stalingrad));
                nameValuePairs.add(new BasicNameValuePair("is_private", "0"));
                nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
                nameValuePairs.add(new BasicNameValuePair("request", "create_new_room"));
                PrefApplication.sendData(nameValuePairs, false);
                break;
            case R.id.buttonNewRoomBack:
                finish();
                break;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        String msg = extras.getString("message");
        CommonEnums.MessageTypes msgType = CommonEnums.MessageTypes.valueOf(extras.getString("messageType"));
        switch (msgType) {
            case NEW_ROOM_MONEY: // money
                myMoney = Integer.parseInt(msg);
                tvYourMoney.setText(getResources().getString(R.string.your_money) + Integer.toString(myMoney));
                break;
            case ROOMS_NEW_ROOM_CREATION_RESULT: // room creation ret val
                String[] data = msg.split(" ");
                int error = Integer.parseInt(data[0]);
                if (error != 0) {
                    Toast.makeText(this, "Error creating new room.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent gameActivity = new Intent(this, GameActivity.class);
                Bundle b = new Bundle();
                b.putString("room_id", data[1]);
                b.putString("own_number", "1");
                gameActivity.putExtras(b);
                startActivity(gameActivity);
                break;
            default: // unknown msg_type
                return;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

}