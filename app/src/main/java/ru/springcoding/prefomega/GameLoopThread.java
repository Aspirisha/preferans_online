package ru.springcoding.prefomega;

import ru.springcoding.prefomega.PlayingTableView.DrawState;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;

public class GameLoopThread extends Thread {
	private PlayingTableView playingTable;
	private boolean isRunning = false;
	static final long FPS = 20;

	
	public GameLoopThread(PlayingTableView view) {
        this.playingTable = view;
        playingTable.drawState = DrawState.WAITING_FOR_MORE_PLAYERS;
        setName("Game loop thread");
	}
	
	public void setRunning(boolean run) {
        isRunning = run;
	}
	
	
	 @SuppressLint("WrongCall")
	@Override
     public void run() {
		 long ticksPS = 1000 / FPS;
         long startTime = System.currentTimeMillis();
         long prevTime;
         long sleepTime;
         
         while (isRunning) {
        	 Canvas c = null;
        	 prevTime = startTime;
        	 startTime = System.currentTimeMillis();
        	 long delta_t = startTime - prevTime;
        	 
        	 switch (GameInfo.gameState) {
        	 case 3: {
        		 long time = GameInfo.getTimeToShowClouds();
        		 if (time > 0)
        			 GameInfo.setTimeToShowClouds(time - delta_t);
        		 else {
        			 playingTable.hideLeftClowd();
        			 playingTable.hideRightClowd();
        			 playingTable.hideOwnClowd();
        		 }
        		 break;
        	 	}
        	 }
        	 
        	 switch (playingTable.drawState) {
        	 case TALON_SHOW:
        		 if (playingTable.talonShowTimer > 0) {
        			 playingTable.talonShowTimer--;
        			 if (playingTable.talonShowTimer == 0) {
        				 playingTable.talonShowTimer = -1;
        				 playingTable.addTalonCardsToPlayer();
        				 playingTable.drawState = DrawState.THROW_EXTRA_CARDS;
        			 }
        		 }
        		 break;
        	 case THROW_EXTRA_CARDS:
        		 break;
        	 case PLAYER_CHOOSES_REAL_GAME_BET:
       
        		 break;
        	 }
        	 
        	 try {
        		 c = playingTable.getHolder().lockCanvas();
        		 if (c != null) {
	        		 synchronized (playingTable.getHolder()) {
	        			 playingTable.postInvalidate();
	        		 }
        		 }
        	 } catch (Exception e) {
        		Log.i("Exeption in gameThread: ", e.toString());
        	 } finally {
        		 if (c != null) {
        			 playingTable.getHolder().unlockCanvasAndPost(c);
        		 }
        	 }
        	 
        	 sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
        	 try {
        		 if (sleepTime > 0)
        			 sleep(sleepTime);
        		 else
        			 sleep(10);
        	 } catch (Exception e) {
        		 Log.i("Exeption: ", e.toString());
        	 }
         }
	 }
}