package ru.springcoding.prefomega;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class TalkClowd {
	Bitmap clowdBitmap;
	Bitmap textBitmap;
	
	private int xClowd;
	private int yClowd;
	private int xText;
	private int yText;
	private int width;
	private int height;
	private boolean isVisible;
	private PlayingTableView playingTable;
	
	public TalkClowd(PlayingTableView table, boolean arrowOnLeft, int _x, int _y) {
		playingTable = table;
		if (arrowOnLeft)
			clowdBitmap = BitmapFactory.decodeResource(playingTable.getResources(), R.drawable.clowd_left);
		else
			clowdBitmap = BitmapFactory.decodeResource(playingTable.getResources(), R.drawable.clowd_right);
		
		height = clowdBitmap.getHeight();
		width = clowdBitmap.getWidth();
		isVisible = false;
		xClowd = _x;
		yClowd = _y;
		xText = 0;
		yText = 0;
		textBitmap = null;
	}
	
	public void setVisible(boolean vis) {
		isVisible = vis;
	}
	
	private void recountTextPosition() {
		int dy = (11 * height / 13 - textBitmap.getHeight()) / 2;
		int dx = (width - textBitmap.getWidth()) / 2;
		yText = yClowd + dy;
		xText = xClowd + dx;
	}
	
	public void setText(String name) {
		int resourceId = playingTable.getResources().getIdentifier(name, "drawable", PrefApplication.getInstance().getPackageName());
		textBitmap = BitmapFactory.decodeResource(playingTable.getResources(), resourceId);
		recountTextPosition();
	}
	
	public void setBet(int bet) {
		switch (bet) {
		case 0:
			setText("pass");
			break;
		case 16:
			setText("misere");
			break;
		case 22:
			setText("misere_without_talon");
			break;
		default:
			setSuitAsText(bet);
			break;
		}
	}
	
	private void setSuitAsText(int bet) {
		if (bet == -1)
			return;
		
		if (bet >= 16 && bet <= 21)
			bet--;
		else if (bet >= 22)
			bet -= 2;
		
		int rowNumber = (bet - 1) / 5;
		int colNumber = (bet - 1) % 5;
		InputStream is = playingTable.getResources().openRawResource(R.drawable.suits);
		BitmapRegionDecoder decoder;
		try {
			decoder = BitmapRegionDecoder.newInstance(is, true);
			int cellWidth = decoder.getWidth() / 5;
			int cellHeight = decoder.getHeight() / 5;
			Rect rect = new Rect(colNumber * cellWidth, rowNumber * cellHeight, (colNumber + 1) * cellWidth, (rowNumber + 1) * cellHeight);
			textBitmap = decoder.decodeRegion(rect, null);
			recountTextPosition();
		} catch (IOException e) {
			Log.i("Exception: ", e.toString());
		}
		
	}
	
	public void setPosition(int newX, int newY) {
		xText += (newX - xClowd);
		yText += (newY - yClowd);
		xClowd = newX;
    	yClowd = newY;
    	
    }
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getLeft() {
		return xClowd;
	}
	
	public int getTop() {
		return yClowd;
	}
	
	public int getBottom() {
		return yClowd + height;
	}
	
	public int getRight() {
		return xClowd + width;
	}
	
	public boolean getVisible() {
		return isVisible;
	}
	
	
	public void draw(Canvas canvas) {
    	if (isVisible) {
	        canvas.drawBitmap(clowdBitmap, xClowd, yClowd, null);
	        canvas.drawBitmap(textBitmap, xText, yText, null);
    	}
    }
	
	public void destroy() {
		if (clowdBitmap != null) {
			clowdBitmap.recycle();
			clowdBitmap = null;
		}
		if (textBitmap != null) {
			textBitmap.recycle();
			textBitmap = null;
		}
	}
}