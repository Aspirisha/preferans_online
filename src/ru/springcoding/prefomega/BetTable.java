package ru.springcoding.prefomega;

import java.io.IOException;
import java.io.InputStream;

import ru.springcoding.prefomega.BetCellView.SIZE;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class BetTable extends LinearLayout {
	private TableLayout betTableSmall = null;
    private TableLayout betTableMedium = null;
    private TableLayout betTableBig = null;
    
    public BetTable(Context context) {
		super(context);
		
		this.setOrientation(VERTICAL);
		betTableSmall = new TableLayout(context);
        betTableMedium = new TableLayout(context);
        betTableBig = new TableLayout(context);
        
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
	        
	        // hardcoded now
	        betTableSmall.layout(0, 0, decoder.getWidth() / 3, 5 * cellHeight);
	        betTableMedium.layout(0, 0, decoder.getWidth() / 3, cellHeight);
	        betTableBig.layout(0, 0, decoder.getWidth() / 3, 2 * cellHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        addView(betTableSmall);
        addView(betTableMedium);
        addView(betTableBig);
	}
    
	public void showBetTable() {
		setBetTablesCells();
		betTableSmall.bringToFront();
		betTableMedium.bringToFront();
		betTableBig.bringToFront();
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
	
	private void setBetTablesCells() {
		setBetTableCells(betTableSmall);
		setBetTableCells(betTableMedium);
		setBetTableCells(betTableBig);
	}
	
	
	private void setBetTableCells(TableLayout tbl) {
		int bet = GameInfo.currentCardBet;
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
					if (GameInfo.gameState == 3) 
						bv.setUnavailable();
				}
				if (bv.myNumber == 29)
					bv.setUnavailable();
			}
		}
	}
	
	public int getRawWidth() {
		return betTableSmall.getWidth();
	}
}
