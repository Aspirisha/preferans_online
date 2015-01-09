package ru.springcoding.prefomega;

import ru.springcoding.prefomega.GameInfo.Player;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.MonthDisplayHelper;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class ScoreView extends ImageView {
	float bulletRadius;
	float bulletX;
	float bulletY;
	float sin;
	float cos;
	float tg;
	
	private RectF lineCentreLeft;
	private RectF lineCentreRight;
	private RectF lineCentreTop;
	
	private RectF lineMountLeft; // this 3 lines underline mount
	private RectF lineMountRight;
	private RectF lineMountOwn;
	
	private RectF lineBulletLeft; // this 3 lines underline bullet
	private RectF lineBulletRight;
	private RectF lineBulletOwn;
	
	private RectF paperList;
	
	private RectF lineSeparatorLeft; // this 3 lines define whist separators
	private RectF lineSeparatorRight;
	private RectF lineSeparatorOwn;
	
	private Paint paint;
	
	private int width;
	private int height;
	float paperHeight;
	float paperWidth;
	boolean recounted;
	private Typeface bulletTypeFace;
	private Typeface scoreTypeFace;
	private int bulletFontSize;
	private int scoreFontSize;
	
	private ScoreStrings ownScoreStrings;
	private ScoreStrings leftScoreStrings;
	private ScoreStrings rightScoreStrings;
	
	public ScoreView(Context context) {
		super(context);
		paint = new Paint();
		recounted = false;
		
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setLayoutParams(params);
		
		setBackgroundResource(R.drawable.score_table);
		width = PrefApplication.screenWidth;
		height = PrefApplication.screenHeight;
		bulletTypeFace = Typeface.create("Helvetica",Typeface.BOLD);
		scoreTypeFace = Typeface.create("Helvetica", Typeface.ITALIC);
		
		ownScoreStrings = new ScoreStrings();
		leftScoreStrings = new ScoreStrings();
		rightScoreStrings = new ScoreStrings();
		
	}
	
	private void countAllMetrics() {
		countPaperMetrics();
		countBulletMetrics();
		countOwnMetrics();
		countLeftMetrics();
		countRightMetrics();
	}
	
	private void countPaperMetrics() {
		float x1 = (width - 258 * width / 319) / 2;
		float x2 = width - x1;
		float y1 = (height - 331 * height / 439) / 2;
		float y2 = height - y1;
		paperHeight = y2 - y1;
		paperWidth = x2 - x1;
		paperList = new RectF(x1, y1, x2, y2);
	}
	
	private void countBulletMetrics() {
		bulletRadius = paperHeight / 15;
		bulletX = paperList.left + paperWidth / 2;
		bulletY = paperList.top + paperHeight / 2;
		sin = (float) ((paperList.bottom - bulletY) / Math.sqrt((paperList.bottom - bulletY) * (paperList.bottom - bulletY) + (paperList.right - bulletX) * (paperList.right - bulletX)));
		cos = (float) Math.sqrt(1 - sin * sin);
		tg = sin / cos;
		
		float x1 = bulletX - bulletRadius * cos;
		float x2 = paperList.left;
		float y1 = bulletY + bulletRadius * sin;
		float y2 = paperList.bottom;
		lineCentreLeft = new RectF(x1, y1, x2, y2);
		
		x1 = bulletX + bulletRadius * cos;
		x2 = paperList.right;
		lineCentreRight = new RectF(x1, y1, x2, y2);
		
		lineCentreTop = new RectF(bulletX, bulletY - bulletRadius, bulletX, paperList.top);
	    
		String str = Integer.toString(GameInfo.gameBullet);
		bulletFontSize = 0;
		do {
	        paint.setTextSize(++bulletFontSize);
	    } while(paint.measureText(str) < 0.5f * bulletRadius);
	}
	
	private void countOwnMetrics() {
		float x1 = paperList.left + 14 * (bulletX - paperList.left) / 30;
		float x2 = paperList.right - 14 * (bulletX - paperList.left) / 30;
		float y1 = paperList.bottom - (x1 - paperList.left) * tg;
		float y2 = y1;
		lineMountOwn = new RectF(x1, y1, x2, y2);
		
		x1 = paperList.left + 8 * (bulletX - paperList.left) / 30;
		x2 = paperList.right - 8 * (bulletX - paperList.left) / 30;
		y1 = paperList.bottom - (x1 - paperList.left) * tg;
		y2 = y1;
		lineBulletOwn = new RectF(x1, y1, x2, y2);
				
		lineSeparatorOwn = new RectF(bulletX, y1, bulletX, paperList.bottom);
		
		int maxWidth = (int) ((lineBulletOwn.right - lineBulletOwn.left) / 20);
		scoreFontSize = 0;
		do {
	        paint.setTextSize(++scoreFontSize);
	    } while(paint.measureText("a") < maxWidth);
		
		Rect b = new Rect();
		paint.getTextBounds("9", 0, 1, b);
		float letterHeight = b.bottom - b.top;
		float dx = 2 * letterHeight / tg;
		
		x1 = lineMountOwn.left + dx;
		y1 = lineMountOwn.bottom - 1.5f * letterHeight;
		x2 = lineMountOwn.right - dx;
		y2 = lineMountOwn.bottom - 0.5f * letterHeight;
		ownScoreStrings.mountainMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineBulletOwn.left + dx;
		y1 = lineBulletOwn.bottom - 1.5f * letterHeight;
		x2 = lineBulletOwn.right - dx;
		y2 = lineBulletOwn.bottom - 0.5f * letterHeight;
		ownScoreStrings.bulletMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = paperList.left + dx;
		y1 = paperList.bottom - 1.5f * letterHeight;
		x2 = lineSeparatorOwn.left - 0.5f * dx;
		y2 = paperList.bottom - 0.5f * letterHeight;
		ownScoreStrings.leftWhistsMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineSeparatorOwn.left + 0.5f * dx;
		x2 = paperList.right - dx;
		ownScoreStrings.rightWhistsMetrics = new RectF(x1, y1, x2, y2);
	}
	
	private void countLeftMetrics() {
		lineMountLeft = new RectF(lineMountOwn.left, paperList.top, lineMountOwn.left, lineMountOwn.bottom);
		lineBulletLeft = new RectF(lineBulletOwn.left, paperList.top, lineBulletOwn.left, lineBulletOwn.bottom);
		lineSeparatorLeft = new RectF(paperList.left, paperList.top + paperHeight / 2, lineBulletLeft.left, paperList.top + paperHeight / 2);
	
		Rect b = new Rect();
		paint.getTextBounds("9", 0, 1, b);
		float letterHeight = b.bottom - b.top;
		float dx = 2 * letterHeight * tg;
		
		float x1 = lineMountLeft.top + 0.5f * dx;
		float y1 = lineMountLeft.left + 1.5f * letterHeight;
		float x2 = lineMountLeft.bottom - dx;
		float y2 = lineMountLeft.left + 0.5f * letterHeight;
		leftScoreStrings.mountainMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineBulletLeft.top + 0.5f * dx;
		y1 = lineBulletLeft.left + 1.5f * letterHeight;
		x2 = lineBulletLeft.bottom - dx;
		y2 = lineBulletLeft.left + 0.5f * letterHeight;
		leftScoreStrings.bulletMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = paperList.top + 0.5f * dx;
		y1 = paperList.left + 1.5f * letterHeight;
		x2 = lineSeparatorLeft.top - 0.5f * dx;
		y2 = paperList.left + 0.5f * letterHeight;
		leftScoreStrings.leftWhistsMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineSeparatorLeft.top + 0.5f * dx;
		x2 = paperList.bottom - dx;
		leftScoreStrings.rightWhistsMetrics = new RectF(x1, y1, x2, y2);
	}
	
	private void countRightMetrics() {
		lineMountRight = new RectF(lineMountOwn.right, paperList.top, lineMountOwn.right, lineMountOwn.bottom);
		lineBulletRight = new RectF(lineBulletOwn.right, paperList.top, lineBulletOwn.right, lineBulletOwn.bottom);
		lineSeparatorRight = new RectF(paperList.right, paperList.top + paperHeight / 2, lineBulletRight.left, paperList.top + paperHeight / 2);
		
		Rect b = new Rect();
		paint.getTextBounds("9", 0, 1, b);
		float letterHeight = b.bottom - b.top;
		float dx = 2 * letterHeight * tg;
		
		float x1 = lineMountRight.top + 0.5f * dx;
		float y1 = lineMountRight.left - 1.5f * letterHeight;
		float x2 = lineMountRight.bottom - dx;
		float y2 = lineMountRight.left - 0.5f * letterHeight;
		rightScoreStrings.mountainMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineBulletRight.top + 0.5f * dx;
		y1 = lineBulletRight.left - 1.5f * letterHeight;
		x2 = lineBulletRight.bottom - dx;
		y2 = lineBulletRight.left - 0.5f * letterHeight;
		rightScoreStrings.bulletMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = paperList.top + 0.5f * dx;
		y1 = paperList.right - 1.5f * letterHeight;
		x2 = lineSeparatorRight.top - 0.5f * dx;
		y2 = paperList.right - 0.5f * letterHeight;
		rightScoreStrings.rightWhistsMetrics = new RectF(x1, y1, x2, y2);
		
		x1 = lineSeparatorRight.top + 0.5f * dx;
		x2 = paperList.bottom - dx;
		rightScoreStrings.leftWhistsMetrics = new RectF(x1, y1, x2, y2);
	}
	
	public void updateScoreTable() {
		ownScoreStrings.update(GameInfo.ownPlayer);
		leftScoreStrings.update(GameInfo.nextPlayer);
		rightScoreStrings.update(GameInfo.prevPlayer);
	}
	
	public void clearScoreTable() {
		ownScoreStrings.clearScore();
		leftScoreStrings.clearScore();
		rightScoreStrings.clearScore();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas); 
		
		paint.setColor(getResources().getColor(R.color.score_line));
		paint.setStyle(Paint.Style.STROKE);
		
		canvas.drawCircle(bulletX, bulletY, bulletRadius, paint);
		
		canvas.drawLine(lineCentreLeft.left, lineCentreLeft.top, lineCentreLeft.right, lineCentreLeft.bottom, paint);
		canvas.drawLine(lineCentreRight.left, lineCentreRight.top, lineCentreRight.right, lineCentreRight.bottom, paint);
		canvas.drawLine(lineCentreTop.left, lineCentreTop.top, lineCentreTop.right, lineCentreTop.bottom, paint);
	
		canvas.drawLine(lineMountOwn.left, lineMountOwn.top, lineMountOwn.right, lineMountOwn.bottom, paint);
		canvas.drawLine(lineBulletOwn.left, lineBulletOwn.top, lineBulletOwn.right, lineBulletOwn.bottom, paint);
		canvas.drawLine(lineSeparatorOwn.left, lineSeparatorOwn.top, lineSeparatorOwn.right, lineSeparatorOwn.bottom, paint);
	
		canvas.drawLine(lineMountLeft.left, lineMountLeft.top, lineMountLeft.right, lineMountLeft.bottom, paint);
		canvas.drawLine(lineBulletLeft.left, lineBulletLeft.top, lineBulletLeft.right, lineBulletLeft.bottom, paint);
		canvas.drawLine(lineSeparatorLeft.left, lineSeparatorLeft.top, lineSeparatorLeft.right, lineSeparatorLeft.bottom, paint);
	
		canvas.drawLine(lineMountRight.left, lineMountRight.top, lineMountRight.right, lineMountRight.bottom, paint);
		canvas.drawLine(lineBulletRight.left, lineBulletRight.top, lineBulletRight.right, lineBulletRight.bottom, paint);
		canvas.drawLine(lineSeparatorRight.left, lineSeparatorRight.top, lineSeparatorRight.right, lineSeparatorRight.bottom, paint);
	
		paint.setColor(Color.BLACK);
		paint.setTypeface(bulletTypeFace);
		paint.setTextSize(bulletFontSize);
		canvas.drawText(Integer.toString(GameInfo.gameBullet), bulletX - bulletRadius / 2, bulletY, paint);
		
		paint.setTypeface(scoreTypeFace);
		paint.setTextSize(scoreFontSize);
		canvas.drawText(ownScoreStrings.mountain, ownScoreStrings.mountainMetrics.left, ownScoreStrings.mountainMetrics.bottom, paint);
		canvas.drawText(ownScoreStrings.bullet, ownScoreStrings.bulletMetrics.left, ownScoreStrings.bulletMetrics.bottom, paint);
		canvas.drawText(ownScoreStrings.leftWhists, ownScoreStrings.leftWhistsMetrics.left, ownScoreStrings.leftWhistsMetrics.bottom, paint);
		canvas.drawText(ownScoreStrings.rightWhists, ownScoreStrings.rightWhistsMetrics.left, ownScoreStrings.rightWhistsMetrics.bottom, paint);
		ownScoreStrings.drawScores(canvas);
		
		// draw right scores
		canvas.save();
		canvas.rotate(-90, rightScoreStrings.mountainMetrics.bottom, rightScoreStrings.mountainMetrics.right);
		canvas.drawText(rightScoreStrings.mountain, rightScoreStrings.mountainMetrics.bottom, rightScoreStrings.mountainMetrics.right, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(-90, rightScoreStrings.bulletMetrics.bottom, rightScoreStrings.bulletMetrics.right);
		canvas.drawText(rightScoreStrings.bullet, rightScoreStrings.bulletMetrics.bottom, rightScoreStrings.bulletMetrics.right, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(-90, rightScoreStrings.leftWhistsMetrics.bottom, rightScoreStrings.leftWhistsMetrics.right);
		canvas.drawText(rightScoreStrings.leftWhists, rightScoreStrings.leftWhistsMetrics.bottom, rightScoreStrings.leftWhistsMetrics.right, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(-90, rightScoreStrings.rightWhistsMetrics.bottom, rightScoreStrings.rightWhistsMetrics.right);
		canvas.drawText(rightScoreStrings.rightWhists, rightScoreStrings.rightWhistsMetrics.bottom, rightScoreStrings.rightWhistsMetrics.right, paint);
		canvas.restore();
		
		// draw left scores
		canvas.save();
		canvas.rotate(90, leftScoreStrings.mountainMetrics.bottom, leftScoreStrings.mountainMetrics.left);
		canvas.drawText(leftScoreStrings.mountain, leftScoreStrings.mountainMetrics.bottom, leftScoreStrings.mountainMetrics.left, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(90, leftScoreStrings.bulletMetrics.bottom, leftScoreStrings.bulletMetrics.left);
		canvas.drawText(leftScoreStrings.bullet, leftScoreStrings.bulletMetrics.bottom, leftScoreStrings.bulletMetrics.left, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(90, leftScoreStrings.leftWhistsMetrics.bottom, leftScoreStrings.leftWhistsMetrics.left);
		canvas.drawText(leftScoreStrings.leftWhists, leftScoreStrings.leftWhistsMetrics.bottom, leftScoreStrings.leftWhistsMetrics.left, paint);
		canvas.restore();
		
		canvas.save();
		canvas.rotate(90, leftScoreStrings.rightWhistsMetrics.bottom, leftScoreStrings.rightWhistsMetrics.left);
		canvas.drawText(leftScoreStrings.rightWhists, leftScoreStrings.rightWhistsMetrics.bottom, leftScoreStrings.rightWhistsMetrics.left, paint);
		canvas.restore();
	}
	
	
	class ScoreStrings {
		String mountain;
		String bullet;
		String leftWhists;
		String rightWhists;
		
		RectF bulletMetrics;
		RectF mountainMetrics;
		RectF leftWhistsMetrics;
		RectF rightWhistsMetrics;
		
		int mountainNumberAmount; // how many numbers are there in fact in mountain, including those who are hidden
		int bulletNumberAmount;
		int leftWhistsNumberAmount;
		int rightWhistsNumberAmount;
		
		public ScoreStrings() {
			clearScore();
		}
		
		public void clearScore() {
			mountainNumberAmount = 0;
			bulletNumberAmount = 0;
			leftWhistsNumberAmount = 0;
			rightWhistsNumberAmount = 0;
			mountain = "0"; 
			bullet = "0";
			leftWhists = "0";
			rightWhists = "0";
		}

		public void drawScores(Canvas canvas) {
			canvas.drawText(mountain, mountainMetrics.left, mountainMetrics.bottom, paint);
			canvas.drawText(bullet, bulletMetrics.left, bulletMetrics.bottom, paint);
			canvas.drawText(leftWhists, leftWhistsMetrics.left, leftWhistsMetrics.bottom, paint);
			canvas.drawText(rightWhists, rightWhistsMetrics.left, rightWhistsMetrics.bottom, paint);
		}
		
		public void update(Player player) {
			paint.setTypeface(scoreTypeFace);
			paint.setTextSize(scoreFontSize);
			
			if (mountainNumberAmount < player.mountain.size()) {
				mountain += ".";
				mountain += player.mountain.get(mountainNumberAmount);
				float mountWidth = mountainMetrics.right - mountainMetrics.left;
				if (paint.measureText(mountain) > mountWidth) {
					mountain = "0";
					int i = mountainNumberAmount;
					while (paint.measureText("..." + player.mountain.get(i) + mountain) < mountWidth) {
						mountain = player.mountain.get(i--) + mountain;
					}
					mountain = "..." + mountain;
				}
				mountainNumberAmount++;
			}
			
			if (bulletNumberAmount < player.bullet.size()) {
				bullet += ".";
				bullet += player.bullet.get(bulletNumberAmount);
				float bulletWidth = bulletMetrics.right - bulletMetrics.left;
				if (paint.measureText(bullet) > bulletWidth) {
					bullet = "0";
					int i = bulletNumberAmount;
					while (paint.measureText("..." + player.bullet.get(i) + bullet) < bulletWidth) {
						bullet = player.bullet.get(i--) + bullet;
					}
					bullet = "..." + bullet;
				}
				bulletNumberAmount++;
			}
			
			if (leftWhistsNumberAmount < player.whists_left.size()) {
				leftWhists += ".";
				leftWhists += player.whists_left.get(leftWhistsNumberAmount);
				float wistWhidth = leftWhistsMetrics.right - leftWhistsMetrics.left;
				if (paint.measureText(leftWhists) > wistWhidth) {
					leftWhists = "0";
					int i = leftWhistsNumberAmount;
					while (paint.measureText("..." + player.whists_left.get(i) + leftWhists) < wistWhidth) {
						leftWhists = player.whists_left.get(i--) + leftWhists;
					}
					leftWhists = "..." + leftWhists;
				}
				leftWhistsNumberAmount++;
			}
			
			if (rightWhistsNumberAmount < player.whists_right.size()) {
				rightWhists += ".";
				rightWhists += player.whists_right.get(rightWhistsNumberAmount);
				float wistWhidth = rightWhistsMetrics.right - rightWhistsMetrics.left;
				if (paint.measureText(rightWhists) > wistWhidth) {
					rightWhists = "0";
					int i = leftWhistsNumberAmount;
					while (paint.measureText("..." + player.whists_right.get(i) + rightWhists) < wistWhidth) {
						rightWhists = player.whists_right.get(i--) + rightWhists;
					}
					rightWhists = "..." + rightWhists;
				}
				rightWhistsNumberAmount++;
			}
		}
	}
	
	public void recountCoordinates() {
		if (!recounted) {
			width = PrefApplication.screenWidth;
			height = PrefApplication.screenHeight;
			
			countAllMetrics();
			recounted = true;
		}
	}
	
}