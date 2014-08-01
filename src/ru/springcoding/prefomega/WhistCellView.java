package ru.springcoding.prefomega;

import ru.springcoding.prefomega.BetCellView.STATES;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.view.View;
import android.widget.TextView;

public class WhistCellView extends TextView {
	
	private enum STATES {
		UNSELECTED,
		UNAVAILABLE,
		SELECTED
	}
	
	private STATES state;
	
	public int myNumber;
	private BitmapRegionDecoder decoder;
	private int cellWidth;
	private int cellHeight;
	private int selectedDrawableId; // numeration from 0
	private int unselectedDrawableId;
	private static WhistCellView prevSelected;
	private Context context;
	
	public WhistCellView(Context _context) {
		super(_context);
		context = _context;
		myNumber = -1;
	}
	
	public void init(int _myNumber, int _selectedDrawableId, int _unselectedDrawableId, String text) {
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
		if (prevSelected != null && myNumber != 3) { // if it's not ok also!
			prevSelected.setUnselected();
		} else if (myNumber == 3 && prevSelected != null) {
			GameInfo gameInfo = GameInfo.getInstance();
			GameActivity gameActivity = (GameActivity)this.getContext();
			switch (gameInfo.gameState) {
			case 5:
				if (prevSelected.myNumber == 1)
					gameInfo.ownPlayer.myRole = 1;
				else
					gameInfo.ownPlayer.myRole = 0;
				gameActivity.sendMyWhistingChoiceToServer();
				break;
			case 7:
				if (prevSelected.myNumber == 1)
					gameInfo.isOpenGame = true;
				else
					gameInfo.isOpenGame = false;
				gameActivity.sendMyOpenCloseChoiceToServer();
				break;
			}
			
		}
		if (myNumber != 3)
			prevSelected = this;
	}
	
}