package ru.springcoding.prefomega;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class GameInfo {
	private static GameInfo singleton = null;
	
	public String roomId;
	public String roomName;
	
	public Player nextPlayer;
	public Player prevPlayer;
	public Player ownPlayer;
	
	boolean isStalingrad;
	int gameMoneyBet;
	int gameBullet;
	int currentCardBet;
	String gameType;
	int playersNumber;
	int activePlayer;
	int gameState;
	int onside; // who is onside (1..3) or -1 if it's raspasy or trading
	boolean isOpenGame;
	int currentSuit;
	int currentTrump; // kozyr
	int cardsOnTable; // 1..3
	int firstHand;
	
	long previous_server_keepalive_time;
	long current_server_keepalive_time;
	
	Talon talon;
	Talon thrownCards; // after trading
	
	private GameInfo () {
		playersNumber = 0;
		nextPlayer = new Player();
		prevPlayer = new Player();
		ownPlayer = new Player();
		
		talon = new Talon();
		thrownCards = new Talon();
		gameState = -1;		
		onside = -1;
		isOpenGame = false;
		
		previous_server_keepalive_time = -1;
		current_server_keepalive_time = -1;
	}
	
	public static GameInfo getInstance() {
		if (singleton == null)
			synchronized (GameInfo.class) {
				if (singleton == null) {
					singleton = new GameInfo();
				}
				
			}
		return singleton;
	}
	
	public class Player {
		int number;
		String name;
		String id;   // id in database
		int cardsNumber;
		ArrayList<Integer> cards;
		boolean cardsAreVisible; 
		boolean moveIsDrawn;
		int myNewBet;
		int myRole; // true if I'm whisting or if other player asked me to help him whisting (closed whisting)
		int lastCardMove;
		boolean hasNoTrumps;
		boolean hasNoSuit;
		ArrayList<Integer> bullet;
		ArrayList<Integer> mountain;
		ArrayList<Integer> whists_left;
		ArrayList<Integer> whists_right;
		
		public Player() {
			cards = new ArrayList<Integer>(12);
			this.cardsNumber = 0;
			myNewBet = -1;
			myRole = -1;
			bullet = new ArrayList<Integer>();
			mountain = new ArrayList<Integer>();
			whists_left = new ArrayList<Integer>();
			whists_right = new ArrayList<Integer>();
			name = "";
			hasNoTrumps = false;
			hasNoSuit = false;
		}
		
		public void initBeforeNewParty() {
			cardsNumber = 10;
			cardsAreVisible = false;
			myRole = -1;
			myNewBet = -1;
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
	}
	
	public class Talon {
		int cardsNumber;
		int[] cards;
		Talon() {
			cards = new int[2];
			cards[0] = cards[1] = 0;
			this.cardsNumber = 0;
		}
	}
	
	public void initNewDistribution() {
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
	
	public void initNewGame() {
		ownPlayer.initBeforeNewGame();
		prevPlayer.initBeforeNewGame();
		nextPlayer.initBeforeNewGame();
	}
	
	public int getCardSuit(int clientCard) {
		int serverCard = PrefApplication.ServerToClientCards.get(clientCard);
		
		if (serverCard == -1)
			return -1;
		return ((serverCard - 1) / 8 + 1);	//1..4
	}
}