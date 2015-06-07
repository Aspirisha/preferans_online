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
	Bitmap cardBitmap = null;
	private int x = 0;
	private int y = 0;
	private int savedX;
	private int savedY;
	int width = 0;
	int height = 0;
	private int value = -1;
	private int roundRadius = 0;
    private PlayingTableView playingTable;
    private int xSpeed = 0;
    private int ySpeed = 0;
    
    private int destinationX;
    private int destinationY;
    private boolean movingToDest = false;
    private boolean isVisible = false;
    
    private RectF flipRectangle = null; // card changes side whe it has at least 1 point out of rect
    private SIDE inFlipRectangleSide;
    private SIDE outFlipRectangleSide;
    
    private SIDE side = SIDE.FRONT;
    
    enum SIDE {
    	BACK, 
    	FRONT
    }

	public PlayingCard(PlayingTableView table) {
		flipRectangle = new RectF();
		playingTable = table;
	}
    
	public int getY() {
		return y;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int newX) {
		x = newX;
	}
	
	public void setY(int newY) {
		y = newY;
	}
	
	public void setPosition(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int newValue) {
		if (value > 32)
			return;
		value = newValue;
	}
	
    public void setMovingToDestination(int lastX, int lastY, int speed) {
    	double dist = Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY));
    	double xs = speed * (lastX - x) / dist;
    	double ys = speed * (lastY - y) / dist;
    	
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
    
    public void setMovingToSavedPos(int speed) {
    	setMovingToDestination(savedX, savedY, speed);
    }
    
    public void setFlipRectAndBehaviour(float bottom, float left, float top, float right, SIDE in, SIDE out) {
    	flipRectangle.bottom = bottom;
    	flipRectangle.left = left;
    	flipRectangle.top = top;
    	flipRectangle.right = right;
    	
    	inFlipRectangleSide = in;
    	outFlipRectangleSide = out;
    	
    	if (needFlip())
    		flip();
    }
    
    private boolean needFlip() {
    	SIDE newSide;
    	if (y < flipRectangle.top || y + width > flipRectangle.bottom || x < flipRectangle.left || x + width > flipRectangle.right)
    		newSide = outFlipRectangleSide;
    	else
    		newSide = inFlipRectangleSide;
    	
    	return (newSide != side);
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
        	
        	if (needFlip())
        		flip();
        }
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
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
    
    private void setBackUp() {
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
    
    private void setFrontUp() {
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
    
     void flip() {
    	if (side == SIDE.BACK)
    		setFrontUp();
    	else
    		setBackUp();
    	
    }
    
    void setVisibility(boolean visibilitiy) {
    	isVisible = visibilitiy;
    }
    
    public void saveCurrentPosition() {
    	savedX = x;
    	savedY = y;
    }
    
    public void restorePosition() {
    	x = savedX;
    	y = savedY;
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

    public void destroy() {
    	if (cardBitmap != null) {
    		cardBitmap.recycle();
    		cardBitmap = null;
    	}
    }
    
    public boolean containtsPoint(int pointX, int pointY) {
    	return (y <= pointY && (pointY <= y + height) && pointX >= x && (pointX <= x + width));
    }
    
    public boolean containtsPoint(float pointX, float pointY) {
    	return (y <= pointY && (pointY <= y + height) && pointX >= x && (pointX <= x + width));
    }
    
    public void translate(float dx, float dy) {
    	x += dx;
    	y += dy;
    	if (needFlip())
    		flip();
    }
}