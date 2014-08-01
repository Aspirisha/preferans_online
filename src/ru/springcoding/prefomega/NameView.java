package ru.springcoding.prefomega;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.view.View;
import android.widget.TextView;

public class NameView extends TextView {
	
	private enum STATES {
		ACTIVE,
		PASSIVE
	}
	
	private STATES state;
	
	public NameView(Context _context) {
		super(_context);
		setBackgroundResource(R.drawable.rounded_corners);
	}
	
	public void init(String text) {
		state = STATES.PASSIVE;
		setText(text);
	}
	
	public void setPassive() {
		if (state == STATES.PASSIVE)
			return;
		setBackgroundColor(0xAEADA7);
		state = STATES.PASSIVE;
	}
	
	public void setSelected() {
		if (state == STATES.ACTIVE)
			return;
		setBackgroundColor(0xFFD633);
		state = STATES.ACTIVE;
	}
	
}