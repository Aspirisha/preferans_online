package ru.springcoding.prefomega;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Arrays;

import ru.springcoding.prefomega.CommonEnums.RecieverID;
import ru.springcoding.prefomega.PlayingTableView.DrawState;
import ru.springcoding.prefomega.CommonEnums.MessageTypes;

public class GameActivity extends Activity implements OnClickListener {
	Button exitFromGame; // returns us to rooms
	GameView gameView;
	GameLayout gameHolder;
	PlayingTableView playingTable;
	
	// different message types

	// game states codes
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gameHolder = new GameLayout(this);
		gameView = gameHolder.gameView;
		playingTable = gameView.getPlayingTable();
		setContentView(gameHolder);
		
		Bundle b = getIntent().getExtras();
		GameInfo.roomId = b.getString("room_id");
		try { // TODO why is it called when we get back to rooms activity from game?
			GameInfo.ownPlayer.setMyNumber(Integer.parseInt(b.getString("own_number")));
		} catch (Exception e) {
			Log.e("Exception", e.toString());
		}
		GameInfo.ownPlayer.setMyNumber(Integer.parseInt(b.getString("own_number")));
		
		GameInfo.prevPlayer.setMyNumber(GameInfo.ownPlayer.getPrevNumber());		
		GameInfo.nextPlayer.setMyNumber(GameInfo.ownPlayer.getNextNumber());
		
		getInfoAboutRoom();
		PrefApplication.setVisibleWindow(RecieverID.GAME_ACTIVITY, this);
	}

	private void getInfoAboutRoom() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("request_type", "request"));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("request", "all_data_about_room")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		PrefApplication.sendData(nameValuePairs, false);
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBackPressed() {
		notifyUserExitedFromRoom();
		super.onBackPressed();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String msg = intent.getStringExtra("message");
		MessageTypes msgType = MessageTypes.valueOf(intent.getStringExtra("messageType"));
		String[] data;
		switch (msgType)
		{
		case GAME_ROOM_INFO: // all data about room has come
			try {
				data = msg.split(" ");
				GameInfo.roomName = data[0];
				GameInfo.playersNumber = Integer.parseInt(data[1]);
				
				GameInfo.prevPlayer.id = data[1 + GameInfo.prevPlayer.getMyNumber()];
				GameInfo.nextPlayer.id = data[1 + GameInfo.nextPlayer.getMyNumber()];
				
				GameInfo.nextPlayer.name = data[4 + GameInfo.nextPlayer.getMyNumber()];
				GameInfo.prevPlayer.name = data[4 + GameInfo.prevPlayer.getMyNumber()];
				GameInfo.ownPlayer.name = data[4 + GameInfo.ownPlayer.getMyNumber()];
				
				gameView.updateRoomInfo();
				
				GameInfo.gameType = data[8];
				GameInfo.gameMoneyBet = Integer.parseInt(data[9]);
				GameInfo.gameBullet = Integer.parseInt(data[10]);
				GameInfo.isStalingrad = (Integer.parseInt(data[11]) != 0);
			}
			catch (Exception e) {
				Log.i("GameAct", "Exception: " + e.toString());     
			}
			break;
		case GAME_NEW_PLAYER_APPEARED:  // new player appeared
			data = msg.split(" ");
			GameInfo.playersNumber = Integer.parseInt(data[1]);
			int newPlayerNumber = Integer.parseInt(data[2]);
			String newPlayerId = data[0];
			
			if (GameInfo.ownPlayer.getNextNumber() == newPlayerNumber)
				GameInfo.nextPlayer.id = newPlayerId;
			else
				GameInfo.prevPlayer.id = newPlayerId;
			
			break;
		case GAME_CARDS_ARE_SENT: // cards are sent
			manageSentCards(msg);
			break;
		case GAME_ACTIVE_PLAYER_INFO:  // active player info
			GameInfo.activePlayer = Integer.parseInt(msg);
			break;
		case GAME_PLAYER_EXITED:  // player exited
			break;
		case GAME_GAME_STATE_INFO: // server has sent us current game state
			manageNewState(msg);
			break;
		case GAME_PLAYER_THREW_CARDS: // we are notified that player has thrown cards. So draw them on table
			if (GameInfo.activePlayer != GameInfo.ownPlayer.getMyNumber()) { // for else we already know that we have thrown cards
				playingTable.drawThrownCards();
			} else { // if we are active, we need to choose real game
				GameInfo.currentCardBet--;
				if (GameInfo.currentCardBet != 16 && GameInfo.currentCardBet != 21)
					gameView.showBetTable();
			}
			break;
		case GAME_ROLES_INFO: // all the roles are sent
			data = msg.split(" ");
			// our own role is definitely known for us
			GameInfo.prevPlayer.myRole = Integer.parseInt(data[GameInfo.prevPlayer.getMyNumber() - 1]);
			GameInfo.nextPlayer.myRole = Integer.parseInt(data[GameInfo.nextPlayer.getMyNumber() - 1]);
			break;
		case GAME_WHISTERS_VISIBLE_CARDS: // server has sent us visible cards of whisters
			manageWhistersCards(msg);
			break;
		case GAME_BETS_INFO: // current bets
			data = msg.split(" ");
			GameInfo.prevPlayer.setNewBet(Integer.parseInt(data[GameInfo.prevPlayer.getMyNumber() - 1]));
			GameInfo.nextPlayer.setNewBet(Integer.parseInt(data[GameInfo.nextPlayer.getMyNumber() - 1]));
			playingTable.rightClowd.setBet(GameInfo.prevPlayer.getNewBet());
			playingTable.ownClowd.setBet(GameInfo.ownPlayer.getNewBet());
			playingTable.leftClowd.setBet(GameInfo.nextPlayer.getNewBet());
			playingTable.showMyClowd();
			playingTable.showRightClowd();
			playingTable.showLeftClowd();
			break;
		default:
			return;
		}
	}
	
	private void manageWhistersCards(String msg) {
		String[] data = msg.split(" ");
		
		if (GameInfo.prevPlayer.myRole == 1 || GameInfo.prevPlayer.myRole == 0) {
			int[] prevCards = new int[10];
			int startIndex = 10 * (GameInfo.prevPlayer.myRole - 1);
			int endIndex = startIndex + 10;
			for (int i = startIndex; i < endIndex; i++)
				prevCards[i - startIndex] = GameInfo.ServerToClientCards.get(Integer.parseInt(data[i])); 
			Arrays.sort(prevCards);
			GameInfo.prevPlayer.cardsAreVisible = true;
			playingTable.rightCardsChanged(prevCards);
		} 
		
		if (GameInfo.nextPlayer.myRole == 1 || GameInfo.nextPlayer.myRole == 0) {
			int[] nextCards = new int[10];
			int startIndex = 10 * (GameInfo.nextPlayer.myRole - 1);
			int endIndex = startIndex + 10;
			for (int i = startIndex; i < endIndex; i++)
				nextCards[i - startIndex] = GameInfo.ServerToClientCards.get(Integer.parseInt(data[i])); 
			Arrays.sort(nextCards);
			GameInfo.nextPlayer.cardsAreVisible = true;
			playingTable.leftCardsChanged(nextCards);
		} 

	}
	
	private void notifyUserExitedFromRoom() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("notification", "user_exited")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		PrefApplication.sendData(nameValuePairs, false);
	}
	
	private void manageNewState(String msg) {
		String[] data = msg.split(" ");
		GameInfo.gameState = Integer.parseInt(data[0]);
		GameInfo.activePlayer = Integer.parseInt(data[1]);
		int prevActivePlayer = GameInfo.activePlayer - 1;
		if (prevActivePlayer == 0)
			prevActivePlayer = 3;
		
		switch (GameInfo.gameState) {
		case 1:  // trading for the talon!
			// get actual info about other players bets
			GameInfo.nextPlayer.setNewBet(Integer.parseInt(data[2 + GameInfo.nextPlayer.getMyNumber()]));
			GameInfo.prevPlayer.setNewBet(Integer.parseInt(data[2 + GameInfo.prevPlayer.getMyNumber()]));
			
			if (prevActivePlayer == GameInfo.prevPlayer.getMyNumber())
				playingTable.rightClowd.setBet(GameInfo.prevPlayer.getNewBet());
			else if (prevActivePlayer == GameInfo.ownPlayer.getMyNumber())
				playingTable.ownClowd.setBet(GameInfo.ownPlayer.getNewBet());
			else
				playingTable.leftClowd.setBet(GameInfo.nextPlayer.getNewBet());
			
			if (GameInfo.activePlayer == GameInfo.ownPlayer.getMyNumber()) {
				// draw some table that allows user to choose bet
				int currentBet = Integer.parseInt(data[2]);
				GameInfo.currentCardBet = currentBet;
				GameInfo.ownPlayer.setNewBet(-1);
				gameView.showBetTable();
			} else { // else do nothing
				gameView.hideBetTable();
			}
			playingTable.showMyClowd();
			playingTable.showRightClowd();
			playingTable.showLeftClowd();
			break;
		case 2:{ // raspasy
			GameInfo.currentTalonCardRaspasy = Integer.parseInt(data[2]);
			GameInfo.currentSuit = Integer.parseInt(data[3]);
			Log.i("Card suit on raspasy", String.valueOf(GameInfo.currentSuit));
			if (GameInfo.currentTalonCardRaspasy != -1) {
				playingTable.hideOwnClowd();
				playingTable.hideRightClowd();
				playingTable.hideLeftClowd();
				playingTable.showNextTalonCardForRaspasy();
			} else {
				playingTable.hideThrownCards();
			}
		}
			break;
		case 3: // active thinks what to throw; others watch talon and admire
			GameInfo.nextPlayer.setNewBet(Integer.parseInt(data[2 + GameInfo.nextPlayer.getMyNumber()]));
			GameInfo.prevPlayer.setNewBet(Integer.parseInt(data[2 + GameInfo.prevPlayer.getMyNumber()]));
			
			if (prevActivePlayer == GameInfo.prevPlayer.getMyNumber())
				playingTable.rightClowd.setBet(GameInfo.prevPlayer.getNewBet());
			else if (prevActivePlayer == GameInfo.ownPlayer.getMyNumber())
				playingTable.ownClowd.setBet(GameInfo.ownPlayer.getNewBet());
			else
				playingTable.leftClowd.setBet(GameInfo.nextPlayer.getNewBet());
			
			playingTable.showMyClowd();
			playingTable.showRightClowd();
			playingTable.showLeftClowd();
			
			GameInfo.setTimeToShowClouds(10000);
			GameInfo.setTimeToShowTalon(10000);
			
			playingTable.talonShowTimer = 100; // TODO DELETE it
			playingTable.setDrawState(DrawState.TALON_SHOW);
			GameInfo.talon.cardsNumber = 2;
			int cards[] = new int[2];
			for (int i = 0; i < 2; i++) {
				cards[i] = GameInfo.ServerToClientCards.get(Integer.parseInt(data[i + 2]));
				playingTable.talonChanged(cards);
			}
			if (GameInfo.activePlayer == GameInfo.ownPlayer.getMyNumber()) {
				// for 10 seconds we show talon, then it goes to us. If user taps, talon goes immediately
				
			} else { // maybe draw some rotating sand clock
				
			}
			break;
		case 4: // server sent us info about what game active decided to play
			GameInfo.currentCardBet = Integer.parseInt(data[2]); // now it's real bet that is going to be played
			GameInfo.onside = GameInfo.activePlayer;
			int temp = GameInfo.currentCardBet;
			if (temp >= 16 && temp <= 21)
				temp--;
			else if (temp >= 22)
				temp -= 2;
			GameInfo.currentTrump = temp % 5; // zero means no trumps or misere
			
			break;
		case 5:
			GameInfo.prevPlayer.myRole = Integer.parseInt(data[GameInfo.prevPlayer.getMyNumber() + 1]);
			GameInfo.nextPlayer.myRole = Integer.parseInt(data[GameInfo.nextPlayer.getMyNumber() + 1]);
			GameInfo.ownPlayer.myRole = Integer.parseInt(data[GameInfo.ownPlayer.getMyNumber() + 1]);
			playingTable.hideThrownCards();
			playingTable.showCurrentRolesClowds();
				
			if (GameInfo.activePlayer == GameInfo.ownPlayer.getMyNumber()) {
				gameView.showWhistingTable();
			} else {}
			break;
		case 6: // both whisters said pass, just recount scores
			playingTable.hideOwnClowd();
			playingTable.hideLeftClowd();
			playingTable.hideRightClowd();
		case 11: // playing the distribution is finished. Server sent us new scores
			GameInfo.ownPlayer.mountain.add(Integer.parseInt(data[(GameInfo.ownPlayer.getMyNumber() - 1) * 4 + 2]));
			GameInfo.ownPlayer.bullet.add(Integer.parseInt(data[(GameInfo.ownPlayer.getMyNumber() - 1) * 4 + 3]));
			GameInfo.ownPlayer.whists_left.add(Integer.parseInt(data[(GameInfo.ownPlayer.getMyNumber() - 1) * 4 + 4]));
			GameInfo.ownPlayer.whists_right.add(Integer.parseInt(data[(GameInfo.ownPlayer.getMyNumber() - 1) * 4 + 5]));
			
			GameInfo.prevPlayer.mountain.add(Integer.parseInt(data[(GameInfo.prevPlayer.getMyNumber() - 1) * 4 + 2]));
			GameInfo.prevPlayer.bullet.add(Integer.parseInt(data[(GameInfo.prevPlayer.getMyNumber() - 1) * 4 + 3]));
			GameInfo.prevPlayer.whists_left.add(Integer.parseInt(data[(GameInfo.prevPlayer.getMyNumber() - 1) * 4 + 4]));
			GameInfo.prevPlayer.whists_right.add(Integer.parseInt(data[(GameInfo.prevPlayer.getMyNumber() - 1) * 4 + 5]));
			
			GameInfo.nextPlayer.mountain.add(Integer.parseInt(data[(GameInfo.nextPlayer.getMyNumber() - 1) * 4 + 2]));
			GameInfo.nextPlayer.bullet.add(Integer.parseInt(data[(GameInfo.nextPlayer.getMyNumber() - 1) * 4 + 3]));
			GameInfo.nextPlayer.whists_left.add(Integer.parseInt(data[(GameInfo.nextPlayer.getMyNumber() - 1) * 4 + 4]));
			GameInfo.nextPlayer.whists_right.add(Integer.parseInt(data[(GameInfo.nextPlayer.getMyNumber() - 1) * 4 + 5]));
			
			gameHolder.updateScores();
			break;
			
		case 7:
			if (GameInfo.activePlayer == GameInfo.ownPlayer.getMyNumber()) {
				gameView.showVisOrInvisTable();
			} else { // draw timer
				
			}
			break;
		case 8:
			if (GameInfo.activePlayer != GameInfo.ownPlayer.getMyNumber()) {
				GameInfo.isOpenGame = (data[2] == "1");
				playingTable.updateCardFlips();
			}
			break;
		case 9:
			GameInfo.currentSuit = Integer.parseInt(data[2]);
			GameInfo.prevPlayer.lastCardMove = GameInfo.ServerToClientCards.get(Integer.parseInt(data[2 + GameInfo.prevPlayer.getMyNumber()]));
			GameInfo.nextPlayer.lastCardMove = GameInfo.ServerToClientCards.get(Integer.parseInt(data[2 + GameInfo.nextPlayer.getMyNumber()]));
			GameInfo.ownPlayer.lastCardMove = GameInfo.ServerToClientCards.get(Integer.parseInt(data[2 + GameInfo.ownPlayer.getMyNumber()]));
			GameInfo.cardsOnTable = Integer.parseInt(data[6]);
			
			if (GameInfo.cardsOnTable == 1) {
				playingTable.CheckIfCanThrowEverything();
			} else if (GameInfo.cardsOnTable == 0) {
				GameInfo.firstHand = GameInfo.activePlayer;
				playingTable.initNewThreeCards();
			}
			playingTable.updateLastCardMove();
			break;
			
		case 10: // both are whisting
			GameInfo.prevPlayer.myRole = Integer.parseInt(data[GameInfo.prevPlayer.getMyNumber() + 1]);
			GameInfo.nextPlayer.myRole = Integer.parseInt(data[GameInfo.nextPlayer.getMyNumber() + 1]);
			GameInfo.ownPlayer.myRole = Integer.parseInt(data[GameInfo.ownPlayer.getMyNumber() + 1]);
			
			playingTable.hideOwnClowd();
			playingTable.hideLeftClowd();
			playingTable.hideRightClowd();
			
			playingTable.updateCardFlips();
			break;
		}
		
	}
	
	private void manageSentCards(String msg) {
		String[] data = msg.split(" ");
		if (data.length != 10) // smth is wrong!
			return;
		int cards[] = new int[10];
		for (int i = 0; i < 10; i++)
			cards[i] = GameInfo.ServerToClientCards.get(Integer.parseInt(data[i]));
		Arrays.sort(cards);
		GameInfo.initNewDistribution();

		playingTable.showCardsOnTable(cards, null, null);
		// show cards on table!
	}
	
	public void sendMyTradeBetChoiceToServer() {
		if (GameInfo.currentCardBet < GameInfo.ownPlayer.getNewBet())
			GameInfo.currentCardBet = GameInfo.ownPlayer.getNewBet();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		if (GameInfo.gameState == 1) { // trading is going on
			nameValuePairs.add(new BasicNameValuePair("notification", "bet_is_done")); 
		} else if (GameInfo.gameState == 3) { // we send put chosen game
			nameValuePairs.add(new BasicNameValuePair("notification", "real_bet_chosen")); 
		}
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("bet", Integer.toString(GameInfo.ownPlayer.getNewBet())));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		PrefApplication.sendData(nameValuePairs, false);
		gameView.hideBetTable();
	}
	
	public void sendThrownCardsToServer() {		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("notification", "cards_are_thrown")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		String s = Integer.toString(GameInfo.ServerToClientCards.get(GameInfo.thrownCards.cards[0])) + " " 
				+ Integer.toString(GameInfo.ServerToClientCards.get(GameInfo.thrownCards.cards[1]));
		nameValuePairs.add(new BasicNameValuePair("cards", s));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		PrefApplication.sendData(nameValuePairs, false);
	}
	
	public void sendMyWhistingChoiceToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("notification", "whist_choice")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("chosen_role", Integer.toString(GameInfo.ownPlayer.myRole)));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		PrefApplication.sendData(nameValuePairs, false);
		gameView.hideChoiceTable();
	}
	
	public void sendMyOpenCloseChoiceToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("notification", "open_close")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("is_open_game", Boolean.toString(GameInfo.isOpenGame)));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		PrefApplication.sendData(nameValuePairs, false);
		gameView.hideChoiceTable();
	}
	
	public void sendMyCardMoveToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("notification", "card_move")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", GameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("request_type", "notification"));
		nameValuePairs.add(new BasicNameValuePair("move", Integer.toString(GameInfo.ServerToClientCards.get(GameInfo.ownPlayer.lastCardMove))));
		PrefApplication.sendData(nameValuePairs, false);
	}
	
	@Override // recycle bitmaps here to avoid memory leaks
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
	}
	
	
}