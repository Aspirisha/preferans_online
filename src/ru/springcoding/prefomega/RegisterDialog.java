package ru.springcoding.prefomega;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class RegisterDialog extends DialogFragment implements OnClickListener {
	EditText login;
	EditText password;
	Button regButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.registerlayout, container);
		login = (EditText) v.findViewById(R.id.loginTE);
		password = (EditText) v.findViewById(R.id.passwordTE);
		regButton = (Button) v.findViewById(R.id.registerButton);
		regButton.setOnClickListener(this);
		
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.registerButton:
			PrefApplication.getInstance().tryRegister(login.getText().toString(), 
					password.getText().toString());
			Log.i("login", login.getText().toString());
			
			break;
		}
		
	}

}
