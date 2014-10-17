package ru.springcoding.prefomega;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class PlayingCard {
	Bitmap cardBitmap;
	int x;
	int y;
	int savedX;
	int savedY;
	int width;
	int height;
	int value;
	int roundRadius;
    private PlayingTableView playingTable;
    int xSpeed;
    int ySpeed;
    
    int destinationX;
    int destinationY;
    int distToFlip;
    boolean isFlipped;
    boolean movingToDest;
    boolean isVisible;
    
    SIDE side;
    
    enum SIDE {
    	BACK, 
    	FRONT
    }

	public PlayingCard(PlayingTableView table) {
		playingTable = table;
		cardBitmap = null;
		
		width = 0;
		height = 0;
		roundRadius = 0;
		side = SIDE.FRONT;
		
		xSpeed = 0;
		ySpeed = 0;
		value = -1;
		x = y = 0;
		isVisible = false;
		movingToDest = false;
	}
    
    public void setMovingToDestination(int lastX, int lastY, int speed, int distanceToFlip) {
    	double dist = Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY));
    	double xs = speed * (lastX - x) / dist;
    	double ys = speed * (lastY - y) / dist;
    	
    	distToFlip = distanceToFlip;
    	if (distToFlip > 0)
    		isFlipped = false;
    	xSpeed = (int)Math.round(xs);
    	ySpeed = (int)Math.round(ys);
    	if (xSpeed == 0) {
    		xSpeed = (int) Math.signum(xs);
    	}
    	if (ySpeed == 0) {
    		ySpeed = (int)Math.signum(ys);
    	}
    	destinationX = lastX;
    	destinationY = lastY;
    	movingToDest = true;
    }
    
	private void update() {
		y = y + ySpeed;
        x = x + xSpeed;
        if (movingToDest) {
        	if (Math.abs(x - destinationX) < Math.abs(xSpeed)) {
        		x = destinationX;
        		xSpeed = 0;
        	}
        	if (Math.abs(y - destinationY) < Math.abs(ySpeed)) {
        		y = destinationY;
        		ySpeed = 0;
        	}
        	if (xSpeed == 0 && ySpeed == 0) {
        		movingToDest = false;
        	}
        	int dist = (int)Math.sqrt((x - destinationX) * (x - destinationX) + (y - destinationY) * (y - destinationY));
        	if (!isFlipped && dist <= distToFlip) {
        		flip();
        		isFlipped = true;
        	}
        }
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
		.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = radius;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
	
    public void draw(Canvas canvas) {
    	if (isVisible) {
	        update();
	
	        canvas.drawBitmap(cardBitmap, x, y, null);
    	}
    }
    
    public void setBackUp() {
    	if (side == SIDE.FRONT) {
	    	cardBitmap = BitmapFactory.decodeResource(playingTable.getResources(), R.drawable.back);
	    	if (cardBitmap != null) {
		    	width = cardBitmap.getWidth();
		    	height = cardBitmap.getHeight();
		    	roundRadius = width / 10;
		    	cardBitmap = getRoundedCornerBitmap(cardBitmap, roundRadius);
	    	}
	    	side = SIDE.BACK;
    	}
    }
    
    public void setFrontUp() {
    	if (value > 0 && value < 33 && side == SIDE.BACK) {
    		String name = "card".concat(Integer.toString(value));
    		int resourceId = playingTable.getResources().getIdentifier(name, "drawable", PrefApplication.getInstance().getPackageName());
			cardBitmap = BitmapFactory.decodeResource(playingTable.getResources(), resourceId);
			if (cardBitmap != null) {
		    	width = cardBitmap.getWidth();
		    	height = cardBitmap.getHeight();
		    	roundRadius = width / 10;
		    	cardBitmap = getRoundedCornerBitmap(cardBitmap, roundRadius);
		    	side = SIDE.FRONT;
	    	}
    	}
    }
    
    public void flip() {
    	if (side == SIDE.BACK)
    		setFrontUp();
    	else
    		setBackUp();
    	
    }
    
    public void saveCurrentPosition() {
    	savedX = x;
    	savedY = y;
    }
    
    public void changeBitmap(Bitmap bmp, SIDE _side) {
    	cardBitmap = bmp;
    	side = _side;
    	if (bmp != null) {
	    	width = bmp.getWidth();
	    	height = bmp.getHeight();
	    	roundRadius = width / 10;
	    	cardBitmap = getRoundedCornerBitmap(cardBitmap, roundRadius);
    	}
    }
    
    public void setPosition(int newX, int newY) {
    	x = newX;
    	y = newY;
    }
    
    public void destroy() {
    	if (cardBitmap != null) {
    		cardBitmap.recycle();
    		cardBitmap = null;
    	}
    }
}