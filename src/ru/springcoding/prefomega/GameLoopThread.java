package ru.springcoding.prefomega;

import ru.springcoding.prefomega.PlayingTableView.DrawState;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;

public class GameLoopThread extends Thread {
	private PlayingTableView playingTable;
	private boolean isRunning = false;
	private GameInfo gameInfo;
	static final long FPS = 20;

	
	public GameLoopThread(PlayingTableView view) {
        this.playingTable = view;
        gameInfo = GameInfo.getInstance();
        playingTable.drawState = DrawState.WAITING_FOR_MORE_PLAYERS;
	}
	
	public void setRunning(boolean run) {
        isRunning = run;
	}
	
	
	 @SuppressLint("WrongCall")
	@Override
     public void run() {
		 long ticksPS = 1000 / FPS;
         long startTime;
         long sleepTime;
         int timer;
         
         while (isRunning) {
        	 Canvas c = null;
        	 startTime = System.currentTimeMillis();
        	 switch (playingTable.drawState) {
        	 case TALON_SHOW:
        		 if (playingTable.talonShowTimer > 0) {
        			 playingTable.talonShowTimer--;
        			 if (playingTable.talonShowTimer == 0) {
        				 playingTable.talonShowTimer = -1;
        				 playingTable.addTalonCardsToPlayer();
        				 timer = 50;
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