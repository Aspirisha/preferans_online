package ru.springcoding.prefomega;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class TalkClowd {
	private Bitmap clowdBitmap = null;
	private Bitmap textBitmap = null;
	
	private int xClowd;
	private int yClowd;
	private int xText = 0;
	private int yText = 0;
	private int width;
	private int height;
	private boolean isVisible = false;
	private PlayingTableView playingTable;
	private Paint paint = null;
	
	public TalkClowd(PlayingTableView table, boolean arrowOnLeft, int _x, int _y) {
		playingTable = table;
		if (arrowOnLeft)
			clowdBitmap = BitmapFactory.decodeResource(playingTable.getResources(), R.drawable.clowd_left);
		else
			clowdBitmap = BitmapFactory.decodeResource(playingTable.getResources(), R.drawable.clowd_right);
		
		height = clowdBitmap.getHeight();
		width = clowdBitmap.getWidth();
		xClowd = _x;
		yClowd = _y;
		//paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	  //  paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
	  //  paint.setColor(Color.TRANSPARENT);
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
			//setSuitAsText(1); // TODO test
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
			decoder = BitmapRegionDecoder.newInstance(is, false);
			int cellWidth = decoder.getWidth() / 5;
			int cellHeight = decoder.getHeight() / 5;
			Rect rect = new Rect(colNumber * cellWidth, rowNumber * cellHeight, (colNumber + 1) * cellWidth, (rowNumber + 1) * cellHeight);
			textBitmap = decoder.decodeRegion(rect, null);
			recountTextPosition();
			is.close();
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
	        canvas.drawBitmap(clowdBitmap, xClowd, yClowd, paint);
	        if (textBitmap != null)
	        	canvas.drawBitmap(textBitmap, xText, yText, paint);
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