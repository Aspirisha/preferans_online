package ru.springcoding.prefomega;

import java.io.IOException;
import java.io.InputStream;

import ru.springcoding.prefomega.BetCellView.SIZE;
import ru.springcoding.prefomega.GameInfo.Talon;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class GameView extends FrameLayout {
	PlayingTableView playingTable;
    TableLayout betTableSmall;
    TableLayout betTableMedium;
    TableLayout betTableBig;
    
    TableLayout choiceTableSmall;
    TableLayout choiceTableBig;
    
    GameInfo gameInfo;
    private Context context;
    private WhistCellView leftChoice;
    private WhistCellView rightChoice;
    
    enum CHOICE_TABLE_STATE {
    	EMPTY,
    	WHISTING,
    	OPEN_CLOSE, // type of whisting if one is passing
    	WHO_TESTS_MISERE
    }
    private CHOICE_TABLE_STATE choiceTableState;
    
	public GameView(Context _context) {
		super(_context);
		context = _context;
        betTableSmall = new TableLayout(context);
        betTableMedium = new TableLayout(context);
        betTableBig = new TableLayout(context);
        choiceTableSmall = new TableLayout(context);
        choiceTableBig = new TableLayout(context);
        gameInfo = GameInfo.getInstance();
        choiceTableState = CHOICE_TABLE_STATE.EMPTY;
        
        playingTable = new PlayingTableView(_context);
        this.addView(playingTable);
        
		try {
			InputStream is = getResources().openRawResource(R.drawable.small_cells);
	        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, true);
	        int cellHeight = decoder.getHeight() / 5;
	        int alphaValue = 200;
	        int number = 1;
	        for (int i = 0; i < 5; i++) {
	        	TableRow row = new TableRow(context);
        		if (i > 2)
        			number++;
	        	for (int j = 0; j < 5; j++) {
	        		BetCellView v = new BetCellView(context);
	        		v.init(decoder, number, SIZE.SMALL, i, j);
	        		v.setAlpha(alphaValue);
	        		row.addView(v);
	        		number++;
	        	}
	        	betTableSmall.addView(row);
	        }
	        
	        is = getResources().openRawResource(R.drawable.medium_cells);
	        decoder = BitmapRegionDecoder.newInstance(is, true);
	        TableRow row = new TableRow(context);
	        for (int i = 0; i < 2; i++) {
	        	BetCellView v = new BetCellView(context);
	        	v.init(decoder, 16 + i * 6, SIZE.MEDIUM, 0, i);
        		v.setAlpha(alphaValue);
        		row.addView(v);
	        }
	        betTableMedium.addView(row);
	        
	        is = getResources().openRawResource(R.drawable.big_cells);
	        decoder = BitmapRegionDecoder.newInstance(is, true);
	        for (int i = 0; i < 2; i++) {
		        row = new TableRow(context);
		        BetCellView v = new BetCellView(context);
		        v.init(decoder, 28 + i, SIZE.BIG, i, 0);
	    		v.setAlpha(alphaValue);
	    		row.addView(v);
	    		betTableBig.addView(row);
	        }
	        betTableSmall.layout(30, 100, 30 + decoder.getWidth() / 3, 100 + 5 * cellHeight);
	        betTableMedium.layout(30, 100 + 5 * cellHeight, 30 + decoder.getWidth() / 3, 100 + 6 * cellHeight);
	        betTableBig.layout(30, 100 + 6 * cellHeight, 30 + decoder.getWidth() / 3, 100 + 8 * cellHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		int initTop = playingTable.leftClowd.getBottom() + 5 * (PrefApplication.screenHeight) / 480;
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop;
		params.leftMargin = (PrefApplication.screenWidth - betTableSmall.getWidth()) / 2;
		this.addView(betTableSmall, params);	
		
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop + betTableSmall.getHeight();
		params.leftMargin = (PrefApplication.screenWidth - betTableSmall.getWidth()) / 2;
		this.addView(betTableMedium, params);
		
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop + betTableSmall.getHeight() + betTableMedium.getHeight();
		params.leftMargin = (PrefApplication.screenWidth - betTableSmall.getWidth()) / 2;
		this.addView(betTableBig, params);
		
		TableRow row = new TableRow(context);
		int alphaValue = 200;
		//this.setPadding(10, 10, 10+PrefApplication.screenWidth, 10+PrefApplication.screenHeight);
		WhistCellView v = new WhistCellView(context);
		v.init(1, R.drawable.choiceback_selected, R.drawable.choiceback_unselected, "whist");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		leftChoice = v;
		leftChoice.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		
		v = new WhistCellView(context);
		v.init(2, R.drawable.choiceback_selected, R.drawable.choiceback_unselected, "pass");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		rightChoice = v;
		rightChoice.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		choiceTableSmall.addView(row);
		choiceTableSmall.layout(30, PrefApplication.screenHeight / 2 - v.getBackground().getIntrinsicHeight(), 30 + 2 * v.getBackground().getIntrinsicWidth(), PrefApplication.screenHeight / 2);
		
		row = new TableRow(context);
		v = new WhistCellView(context);
		v.init(3, R.drawable.acceptback_selected, R.drawable.acceptback_unselected, "accept");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		v.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		choiceTableBig.addView(row);
        choiceTableBig.layout(30, PrefApplication.screenHeight / 2, 30 + v.getBackground().getIntrinsicWidth(), PrefApplication.screenHeight / 2 + v.getBackground().getIntrinsicHeight());
        
		initTop = (2 * PrefApplication.screenHeight) / 3;
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop;
		params.leftMargin = (PrefApplication.screenWidth - choiceTableSmall.getWidth()) / 2;
		this.addView(choiceTableSmall, params);
		
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop + choiceTableSmall.getHeight();
		params.leftMargin = (PrefApplication.screenWidth - choiceTableBig.getWidth()) / 2;
		this.addView(choiceTableBig, params);
		
		hideBetTable();
		hideChoiceTable();
	}
	
	private void setBetTableCells(TableLayout tbl) {
		int bet = gameInfo.currentCardBet;
		int tableChildNumber = tbl.getChildCount();
		for (int i = 0; i < tableChildNumber; i++) {
			TableRow tr = (TableRow)tbl.getChildAt(i);
			int rowChildNumber = tr.getChildCount();
			for (int j = 0; j < rowChildNumber; j++) {
				BetCellView bv = (BetCellView)tr.getChildAt(j);
				if (bet < bv.myNumber) {
					bv.setAvailable();
				} else {
					bv.setUnavailable();
				}
				if (bv.myNumber == 16 || bv.myNumber == 22 || bv.myNumber == 28) { // misere or misere with no talon or pass are unavailable
					if (gameInfo.gameState == 3) 
						bv.setUnavailable();
				}
				if (bv.myNumber == 29)
					bv.setUnavailable();
			}
		}
		
	}
	
	
	public void showBetTable() {
		setBetTablesCells();
		betTableBig.bringToFront();
		betTableMedium.bringToFront();
		betTableSmall.bringToFront();
		betTableSmall.setVisibility(VISIBLE);
		betTableMedium.setVisibility(VISIBLE);
		betTableBig.setVisibility(VISIBLE);
		this.invalidate();
	}
	
	public void hideBetTable() {
		betTableSmall.setVisibility(INVISIBLE);
		betTableMedium.setVisibility(INVISIBLE);
		betTableBig.setVisibility(INVISIBLE);	
	}
	
	public void hideChoiceTable() {
		choiceTableSmall.setVisibility(INVISIBLE);
		choiceTableBig.setVisibility(INVISIBLE);
	}
	
	private void setBetTablesCells() {
		setBetTableCells(betTableSmall);
		setBetTableCells(betTableMedium);
		setBetTableCells(betTableBig);
	}
	
	public void showWhistingTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.WHISTING) {
			leftChoice.setText("whist");
			rightChoice.setText("pass");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}
	
	public void showVisOrInvisTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.OPEN_CLOSE) {
			leftChoice.setText("open");
			rightChoice.setText("close");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}
	
	public void showWhoTestsTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.WHO_TESTS_MISERE) {
			leftChoice.setText("me");
			rightChoice.setText("companion");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}

	public void setGameHolder(GameHolder gameHolder) {
		playingTable.gameHolder = gameHolder;
	}	
	
}