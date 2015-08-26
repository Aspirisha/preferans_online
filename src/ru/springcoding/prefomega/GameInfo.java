package ru.springcoding.prefomega;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Point;

public class GameInfo {
	private static Long timeToShowClouds = Long.valueOf(0);
	private static Long timeToShowTalon = Long.valueOf(0);
	
	// dynamic dimens
	public static Point cardDimens = null;
	public static int myCardsPaddingBottom = 0;
	public static int myNameWidth = 0;
	public static int myNameHeight = 0;
	public static int myCardsPaddingLeft = 10;
	public static int myCardsPaddingRight = 10;
	public static int myNamePaddingLeft = 5;
	public static int otherNamesPaddingTop = 5;
	public static int otherNamesPaddingLeft = 5;
	public static int otherNamesPaddingRight = 5;
	public static int leftNameWidth = 0;
	public static int leftNameHeight = 0;
	public static int rightNameWidth = 0;
	public static int rightNameHeight = 0;
	public static boolean paddingsAreCounted = false;
	public static Map<Integer, Integer> ServerToClientCards;
	
	public static String password = "";
	public static boolean isRegistered = false;
	
	public static String roomId;
	public static String roomName;
	
	public static Player nextPlayer = new Player();
	public static Player prevPlayer = new Player();
	public static Player ownPlayer = new Player();
	
	public static boolean isStalingrad;
	public static int gameMoneyBet;
	public static int gameBullet;
	public static int currentCardBet;
	public static String gameType;
	public static int playersNumber = 0;
	public static int activePlayer;
	public static Integer gameState = -1;
	public static int onside = -1; // who is onside (1..3) or -1 if it's raspasy or trading
	public static boolean isOpenGame = false;
	public static int currentSuit;
	public static int currentTrump; 
	public static int cardsOnTable; // 1..3
	public static int firstHand;
	public static int currentTalonCardRaspasy = -1;
	
	public static long previous_server_keepalive_time = -1;
	public static long current_server_keepalive_time = -1;
	
	public static Talon talon = new Talon();
	public static Talon thrownCards = new Talon(); // after trading

	static {
		ServerToClientCards = new HashMap<Integer, Integer>();
		for (int i = 1; i <= 8; i++)
			ServerToClientCards.put(i, 9 - i);
		for (int i = 1; i <= 8; i++)
			ServerToClientCards.put(i + 8, 25 - i);
		for (int i = 1; i <= 8; i++)
			ServerToClientCards.put(i + 16, 17 - i);
		for (int i = 1; i <= 8; i++)
			ServerToClientCards.put(i + 24, 33 - i);		
		ServerToClientCards.put(-1, -1);
	}
	
	public enum GameState {
		TALON_TRADING,
		RASPASY,
		PLAYER_THROWS,
		PLAYER_GAME_CHICE_INFO,
		WHIST_OR_PASS_CHOICE,
		OPEN_OR_CLOSE_CHOICE,
		WHO_CHECKS_MISERE_CHOICE,
		NORMAL_GAME,
		MISERE
	}
	
	public static class Player {
		private int myNumber;
		private int nextNumber;
		private int prevNumber;
		String name = "";
		String id;   // id in database
		int cardsNumber = 0;
		ArrayList<Integer> cards;
		boolean cardsAreVisible; 
		boolean moveIsDrawn;
		private Integer myBet = -1;
		int myRole = -1; // true if I'm whisting or if other player asked me to help him whisting (closed whisting)
		int lastCardMove;
		boolean hasNoTrumps = true;
		boolean hasNoSuit = true;
		int timeLeft = 0;
		ArrayList<Integer> bullet;
		ArrayList<Integer> mountain;
		ArrayList<Integer> whists_left;
		ArrayList<Integer> whists_right;
		
		public Player() {
			cards = new ArrayList<Integer>(12);
			bullet = new ArrayList<Integer>();
			mountain = new ArrayList<Integer>();
			whists_left = new ArrayList<Integer>();
			whists_right = new ArrayList<Integer>();
		}
		
		public void initBeforeNewParty() {
			cardsNumber = 10;
			cardsAreVisible = false;
			myRole = -1;
			myBet = -1;
			cards.clear();
			lastCardMove = -1;
			moveIsDrawn = false;
			hasNoSuit = false;
			hasNoTrumps = false;
		}
		
		public void initBeforeNewGame() {
			bullet.clear();
			mountain.clear();
			whists_left.clear();
			whists_right.clear();
			
		}
		
		public void setNewBet(int bet) {
			myBet = bet;
		}
		
		public int getNewBet() {
			return myBet;
		}
		
		public void setMyNumber(int n) {
			if (n > 3 || n < 1)
				return;
			myNumber = n;
			nextNumber = n + 1;
			prevNumber = n - 1;
			if (prevNumber == 0)
				prevNumber = 3; 
			if (nextNumber == 4)
				nextNumber = 1;
		}
		
		public int getMyNumber() {
			return myNumber;
		}
		
		public int getNextNumber() {
			return nextNumber;
		}
		
		public int getPrevNumber() {
			return prevNumber;
		}
	}
	
	public static class Talon {
		int cardsNumber;
		int[] cards;
		Talon() {
			cards = new int[2];
			cards[0] = cards[1] = 0;
			this.cardsNumber = 0;
		}
	}
	
	public static void initNewDistribution() {
		currentCardBet = 0;
		ownPlayer.initBeforeNewParty();
		prevPlayer.initBeforeNewParty();
		nextPlayer.initBeforeNewParty();
		talon.cardsNumber = 2;
		thrownCards.cardsNumber = 0;
		ownPlayer.myRole = -1;
		currentSuit = -1;
		currentTrump = -1;
		isOpenGame = false;
		cardsOnTable = 0;
	}
	
	public static void initNewGame() {
		ownPlayer.initBeforeNewGame();
		prevPlayer.initBeforeNewGame();
		nextPlayer.initBeforeNewGame();
	}
	
	public static int getClientCardSuit(final int clientCard) {		
		int serverCard = ServerToClientCards.get(clientCard);
		if (serverCard == -1)
			return -1;
		return ((serverCard - 1) / 8 + 1);	//1..4
	}
	
	public static int getServerCardSuit(final int serverCard) {
		if (serverCard == -1)
			return -1;
		return ((serverCard - 1) / 8 + 1);	//1..4
	}
	
	
	public static void setTimeToShowClouds(long time) {
		synchronized (timeToShowClouds) {
			timeToShowClouds = time;
		}
	}
	
	public static long getTimeToShowClouds() {
		synchronized (timeToShowClouds) {
			return timeToShowClouds;
		}
	}
	
	public static void setTimeToShowTalon(long time) {
		synchronized (timeToShowTalon) {
			timeToShowTalon = time;
		}
	}
	
	public static long getTimeToShowTalon() {
		synchronized (timeToShowTalon) {
			return timeToShowTalon;
		}
	}
}