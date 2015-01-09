package ru.springcoding.prefomega;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;


class BetCellView extends ImageView {
	public int myNumber;
	private BitmapRegionDecoder decoder;
	private int cellWidth;
	private int cellHeight;
	private int rowNumber; // numeration from 0
	private int colNumber;
	private static BetCellView prevSelected = null;
	private static BetCellView accept = null;
	//private int state;
	enum STATES {
		AVALIABLE,
		UNAVAILABLE,
		SELECTED
		}
	enum SIZE {
		SMALL,
		MEDIUM,
		BIG
	}
	STATES state;
	SIZE size;
	
	public BetCellView(Context context) {
		super(context);
		myNumber = -1;
	}

	public void init(BitmapRegionDecoder _decoder, int _myNumber, SIZE _size, int _rowNumber, int _colNumber) {
		myNumber = _myNumber;
		decoder = _decoder;
		size = _size;
		rowNumber = _rowNumber;
		colNumber = _colNumber;
		
		if (myNumber == 29) 
			accept = this;
		
		switch (size) {
		case SMALL:
			cellWidth = decoder.getWidth() / 15;
			cellHeight = decoder.getHeight() / 5;
			break;
		case MEDIUM:
			cellWidth = decoder.getWidth() / 6;
			cellHeight = decoder.getHeight();
			break;
		case BIG:
			cellWidth = decoder.getWidth() / 3;
			cellHeight = decoder.getHeight() / 2;
			break;
		}	
		state = STATES.UNAVAILABLE; // just not available
		setAvailable();
		setOnClickListener(new View.OnClickListener() {

	        @Override
	        public void onClick(View v) {
	        	if (state != STATES.UNAVAILABLE)
	        		setSelected();
	        }
	    });
	}
	
	public void setAvailable() {
		if (state == STATES.AVALIABLE)	
			return;
		Rect rect = new Rect(colNumber * cellWidth, rowNumber * cellHeight, (colNumber + 1) * cellWidth, (rowNumber + 1) * cellHeight);
		setImageBitmap(decoder.decodeRegion(rect, null));
		state = STATES.AVALIABLE;
	}
	
	public void setUnavailable() {
		if (state == STATES.UNAVAILABLE)
			return;
		int widthDelta = decoder.getWidth() * 2 / 3;
		Rect rect = new Rect(widthDelta + colNumber * cellWidth, rowNumber * cellHeight, widthDelta + (colNumber + 1) * cellWidth, (rowNumber + 1) * cellHeight);
		setImageBitmap(decoder.decodeRegion(rect, null));
		state = STATES.UNAVAILABLE;
	}
	
	public void setSelected() {
		if (state == STATES.SELECTED)
			return;
		int widthDelta = decoder.getWidth() / 3;
		Rect rect = new Rect(widthDelta + colNumber * cellWidth, rowNumber * cellHeight, widthDelta + (colNumber + 1) * cellWidth, (rowNumber + 1) * cellHeight);
		setImageBitmap(decoder.decodeRegion(rect, null));
		state = STATES.SELECTED;
		if (prevSelected != null && myNumber != 29) { // if it's not ok also!
			prevSelected.setAvailable();
		}
		else if (myNumber == 29 && prevSelected != null) {
			if (prevSelected.myNumber != 28)
				GameInfo.ownPlayer.setNewBet(prevSelected.myNumber);
			else
				GameInfo.ownPlayer.setNewBet(0);
			GameActivity gameActivity = (GameActivity)this.getContext();
			gameActivity.sendMyTradeBetChoiceToServer();
		}
		if (myNumber != 29) {
			prevSelected = this;
			accept.setAvailable();
		}
		else
			prevSelected = null;
	}
	
	
	
}