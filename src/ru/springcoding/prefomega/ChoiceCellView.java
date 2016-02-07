package ru.springcoding.prefomega;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.view.View;
import android.widget.TextView;

public class ChoiceCellView extends TextView {
	
	private enum STATES {
		UNSELECTED,
		UNAVAILABLE,
		SELECTED
	}
	
	public enum CHOICE {
		LEFT_VARIANT,
		RIGHT_VARIANT,
		OK
	}
	
	private STATES state;
	
	public CHOICE myNumber;
	private BitmapRegionDecoder decoder;
	private int cellWidth;
	private int cellHeight;
	private int selectedDrawableId; // numeration from 0
	private int unselectedDrawableId;
	private static ChoiceCellView prevSelected;
	private Context context;
	
	public ChoiceCellView(Context _context) {
		super(_context);
		context = _context;
		myNumber = CHOICE.OK;
	}
	
	public void init(CHOICE _myNumber, int _selectedDrawableId, int _unselectedDrawableId, String text) {
		myNumber = _myNumber;
		selectedDrawableId = _selectedDrawableId;
		unselectedDrawableId = _unselectedDrawableId;
		state = STATES.SELECTED;
		setText(text);
		setUnselected();
		/*if (myNumber == 3)
			state = STATES.UNAVAILABLE;*/
		setOnClickListener(new View.OnClickListener() {

	        @Override
	        public void onClick(View v) {
	        	setSelected();
	        }
	    });
	}
	
	public void setUnselected() {
		if (state == STATES.UNSELECTED)
			return;
		setBackgroundDrawable(context.getResources().getDrawable(unselectedDrawableId));
		state = STATES.UNSELECTED;
	}
	
	public void setSelected() {
		if (state == STATES.SELECTED)
			return;
		setBackgroundDrawable(context.getResources().getDrawable(selectedDrawableId));
		state = STATES.SELECTED;
		if (prevSelected != null && myNumber != CHOICE.OK) { // if it's not ok also!
			prevSelected.setUnselected();
		} else if (myNumber == CHOICE.OK && prevSelected != null) {
			GameActivity gameActivity = (GameActivity)this.getContext();
			switch (GameInfo.gameState) {
			case 5:
				if (prevSelected.myNumber == CHOICE.LEFT_VARIANT)
					GameInfo.ownPlayer.myRole = 1;
				else
					GameInfo.ownPlayer.myRole = 0;
				gameActivity.sendMyWhistingChoiceToServer();
				break;
			case 7:
				if (prevSelected.myNumber == CHOICE.LEFT_VARIANT)
					GameInfo.isOpenGame = true;
				else
					GameInfo.isOpenGame = false;
				gameActivity.sendMyOpenCloseChoiceToServer();
				break;
			}
			
		}
		if (myNumber != CHOICE.OK)
			prevSelected = this;
	}
	
}