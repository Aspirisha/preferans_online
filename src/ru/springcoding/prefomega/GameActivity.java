package ru.springcoding.prefomega;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import ru.springcoding.prefomega.GameHolder.REDRAW_TYPE;
import ru.springcoding.prefomega.PlayingTableView.DrawState;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class GameActivity extends Activity implements OnClickListener {
	Button exitFromGame; // returns us to rooms
	GameView gameView;
	GameHolder gameHolder;
	PlayingTableView playingTable;
	static GameInfo gameInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrefApplication.setVisibleWindow(3, this);
		gameInfo = GameInfo.getInstance();
		gameHolder = new GameHolder(this);
		gameView = gameHolder.gameView;
		playingTable = gameHolder.gameView.playingTable;
		setContentView(gameHolder);
		
		Bundle b = getIntent().getExtras();
		gameInfo.roomId = b.getString("room_id");
		gameInfo.ownPlayer.number = Integer.parseInt(b.getString("own_number"));
		
		gameInfo.prevPlayer.number = gameInfo.ownPlayer.number - 1;
		if (gameInfo.prevPlayer.number == 0)
			gameInfo.prevPlayer.number = 3;
		
		gameInfo.nextPlayer.number = gameInfo.ownPlayer.number + 1;
		if (gameInfo.nextPlayer.number == 4)
			gameInfo.nextPlayer.number = 1;
		getInfoAboutRoom();
	}

	private void getInfoAboutRoom() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("reg_id", PrefApplication.regid));
		nameValuePairs.add(new BasicNameValuePair("request", "all_data_about_room")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		PrefApplication.sendData(nameValuePairs, "RequestManager.php");
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		notifyUserExitedFromRoom();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String msg = intent.getStringExtra("message");
		int msgType = Integer.parseInt(intent.getStringExtra("messageType"));
		String[] data;
		switch (msgType)
		{
		case 0: // all data about room has come
			try {
				data = msg.split(" ");
				gameInfo.roomName = data[0];
				gameInfo.playersNumber = Integer.parseInt(data[1]);
				
				gameInfo.prevPlayer.id = data[1 + gameInfo.prevPlayer.number];
				gameInfo.nextPlayer.id = data[1 + gameInfo.nextPlayer.number];
				
				gameInfo.nextPlayer.name = data[4 + gameInfo.nextPlayer.number];
				gameInfo.prevPlayer.name = data[4 + gameInfo.prevPlayer.number];
				
				playingTable.updateRoomInfo();
				
				gameInfo.gameType = data[8];
				gameInfo.gameMoneyBet = Integer.parseInt(data[9]);
				gameInfo.gameBullet = Integer.parseInt(data[10]);
				gameInfo.isStalingrad = (Integer.parseInt(data[11]) != 0);
			}
			catch (Exception e) {
				Log.i("GameAct", "Exception: " + e.toString());     
			}
			break;
		case 1:  // new player appeared
			data = msg.split(" ");
			gameInfo.playersNumber = Integer.parseInt(data[1]);
			int newPlayerNumber = Integer.parseInt(data[2]);
			String newPlayerId = data[0];
			switch (newPlayerNumber) {
			case 1:
				if (gameInfo.ownPlayer.number == 2) 
					gameInfo.prevPlayer.id = newPlayerId;
				else 
					gameInfo.nextPlayer.id = newPlayerId;
				break;
			case 2:
				if (gameInfo.ownPlayer.number == 3) 
					gameInfo.prevPlayer.id = newPlayerId;
				else 
					gameInfo.nextPlayer.id = newPlayerId;
				break;
			case 3:
				if (gameInfo.ownPlayer.number == 1) 
					gameInfo.prevPlayer.id = newPlayerId;
				else 
					gameInfo.nextPlayer.id = newPlayerId;
				break;
			}
			
			break;
		case 2: // cards are sent
			manageSentCards(msg);
			break;
		case 3:  // active player info
			gameInfo.activePlayer = Integer.parseInt(msg);
			break;
		case 4:  // player exited
			break;
		case 5: // server has sent us current game state
			manageNewState(msg);
			break;
		case 6: // we are notified that player has thrown cards. So draw them on table
			if (gameInfo.activePlayer != gameInfo.ownPlayer.number) { // for else we already know that we have thrown cards
				playingTable.drawThrownCards();
			} else { // if we are active, we need to choose real game
				gameInfo.currentCardBet--;
				if (gameInfo.currentCardBet != 16 && gameInfo.currentCardBet != 21)
					gameView.showBetTable();
			}
			break;
		case 7: // all the roles are sent
			data = msg.split(" ");
			// our own role is definitely known for us
			gameInfo.prevPlayer.myRole = Integer.parseInt(data[gameInfo.prevPlayer.number - 1]);
			gameInfo.nextPlayer.myRole = Integer.parseInt(data[gameInfo.nextPlayer.number - 1]);
			break;
		case 8: // server has sent us visible cards of whisters
			manageWhistersCards(msg);
			break;
		default:
			return;
		}
	}
	
	private void manageWhistersCards(String msg) {
		String[] data = msg.split(" ");
		
		if (gameInfo.prevPlayer.myRole == 1 || gameInfo.prevPlayer.myRole == 0) {
			int[] prevCards = new int[10];
			int startIndex = 10 * (gameInfo.prevPlayer.myRole - 1);
			int endIndex = startIndex + 10;
			for (int i = startIndex; i < endIndex; i++)
				prevCards[i - startIndex] = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[i])); 
			Arrays.sort(prevCards);
			gameInfo.prevPlayer.cardsAreVisible = true;
			playingTable.rightCardsChanged(prevCards);
		} 
		
		if (gameInfo.nextPlayer.myRole == 1 || gameInfo.nextPlayer.myRole == 0) {
			int[] nextCards = new int[10];
			int startIndex = 10 * (gameInfo.nextPlayer.myRole - 1);
			int endIndex = startIndex + 10;
			for (int i = startIndex; i < endIndex; i++)
				nextCards[i - startIndex] = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[i])); 
			Arrays.sort(nextCards);
			gameInfo.nextPlayer.cardsAreVisible = true;
			playingTable.leftCardsChanged(nextCards);
		} 

	}
	
	private void notifyUserExitedFromRoom() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("notification", "user_exited")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
	}
	
	private void manageNewState(String msg) {
		String[] data = msg.split(" ");
		gameInfo.gameState = Integer.parseInt(data[0]);
		gameInfo.activePlayer = Integer.parseInt(data[1]);
		switch (gameInfo.gameState) {
		case 1:  // trading for the talon!
			// get actual info about other players bets
			for (int i = 1; i <= 3; i++) {
				if (gameInfo.prevPlayer.number == i) {
					gameInfo.prevPlayer.myNewBet = Integer.parseInt(data[i + 2]);
				} else if (gameInfo.nextPlayer.number == i) {
					gameInfo.nextPlayer.myNewBet = Integer.parseInt(data[i + 2]);
				}
			}
			
			if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
				// draw some table that allows user to choose bet
				int currentBet = Integer.parseInt(data[2]);
				gameInfo.currentCardBet = currentBet;
				gameInfo.ownPlayer.myNewBet = -1; 
				gameView.showBetTable();
			} else { // else do nothing
				gameView.hideBetTable();
				if (gameInfo.ownPlayer.myNewBet != -1)
					playingTable.showMyClowdBet();
				else
					playingTable.hideOwnClowd();
			}
			if (gameInfo.nextPlayer.myNewBet != -1) {
				playingTable.showLeftClowdBet();
			}
			else
				playingTable.hideLeftClowd();
			
			if (gameInfo.prevPlayer.myNewBet != -1)
				playingTable.showRightClowdBet();
			else
				playingTable.hideRightClowd();
			break;
		case 2: // raspasy
			break;
		case 3: // active thinks what to throw; others watch talon and admire
			playingTable.hideOwnClowd();
			playingTable.hideLeftClowd();
			playingTable.hideRightClowd();
			playingTable.talonShowTimer = 100;
			playingTable.setDrawState(DrawState.TALON_SHOW);
			gameInfo.talon.cardsNumber = 2;
			int cards[] = new int[2];
			for (int i = 0; i < 2; i++) {
				cards[i] = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[i + 2]));
				gameHolder.updateTalonOnPlayingTable(cards);
			}
			if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
				// for 10 seconds we show talon, then it goes to us. If user taps, talon goes immediately
				
			} else { // maybe draw some rotating sand clock
				
			}
			break;
		case 4: // server sent us info about what game active decided to play
			gameInfo.currentCardBet = Integer.parseInt(data[2]); // now it's real bet that is going to be played
			gameInfo.onside = gameInfo.activePlayer;
			int temp = gameInfo.currentCardBet;
			if (temp >= 16 && temp <= 21)
				temp--;
			else if (temp >= 22)
				temp -= 2;
			gameInfo.currentTrump = temp % 5; // zero means no trumps or misere
			
			break;
		case 5:
			gameInfo.prevPlayer.myRole = Integer.parseInt(data[gameInfo.prevPlayer.number + 1]);
			gameInfo.nextPlayer.myRole = Integer.parseInt(data[gameInfo.nextPlayer.number + 1]);
			gameInfo.ownPlayer.myRole = Integer.parseInt(data[gameInfo.ownPlayer.number + 1]);
			playingTable.hideThrownCards();
			playingTable.showCurrentRolesClowds();
				
			if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
				gameView.showWhistingTable();
			} else {}
			break;
		case 6: // both whisters said pass, just recount scores
			playingTable.hideOwnClowd();
			playingTable.hideLeftClowd();
			playingTable.hideRightClowd();
		case 11: // playing the distribution is finished. Server sent us new scores
			gameInfo.ownPlayer.mountain.add(Integer.parseInt(data[(gameInfo.ownPlayer.number - 1) * 4 + 2]));
			gameInfo.ownPlayer.bullet.add(Integer.parseInt(data[(gameInfo.ownPlayer.number - 1) * 4 + 3]));
			gameInfo.ownPlayer.whists_left.add(Integer.parseInt(data[(gameInfo.ownPlayer.number - 1) * 4 + 4]));
			gameInfo.ownPlayer.whists_right.add(Integer.parseInt(data[(gameInfo.ownPlayer.number - 1) * 4 + 5]));
			
			gameInfo.prevPlayer.mountain.add(Integer.parseInt(data[(gameInfo.prevPlayer.number - 1) * 4 + 2]));
			gameInfo.prevPlayer.bullet.add(Integer.parseInt(data[(gameInfo.prevPlayer.number - 1) * 4 + 3]));
			gameInfo.prevPlayer.whists_left.add(Integer.parseInt(data[(gameInfo.prevPlayer.number - 1) * 4 + 4]));
			gameInfo.prevPlayer.whists_right.add(Integer.parseInt(data[(gameInfo.prevPlayer.number - 1) * 4 + 5]));
			
			gameInfo.nextPlayer.mountain.add(Integer.parseInt(data[(gameInfo.nextPlayer.number - 1) * 4 + 2]));
			gameInfo.nextPlayer.bullet.add(Integer.parseInt(data[(gameInfo.nextPlayer.number - 1) * 4 + 3]));
			gameInfo.nextPlayer.whists_left.add(Integer.parseInt(data[(gameInfo.nextPlayer.number - 1) * 4 + 4]));
			gameInfo.nextPlayer.whists_right.add(Integer.parseInt(data[(gameInfo.nextPlayer.number - 1) * 4 + 5]));
			
			gameHolder.updateScores();
			break;
			
		case 7:
			if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
				gameView.showVisOrInvisTable();
			} else { // draw timer
				
			}
			break;
		case 8:
			if (gameInfo.activePlayer != gameInfo.ownPlayer.number) {
				gameInfo.isOpenGame = (data[2] == "1");
			}
			break;
		case 9:
			gameInfo.currentSuit = Integer.parseInt(data[2]);
			gameInfo.prevPlayer.lastCardMove = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[2 + gameInfo.prevPlayer.number]));
			gameInfo.nextPlayer.lastCardMove = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[2 + gameInfo.nextPlayer.number]));
			gameInfo.ownPlayer.lastCardMove = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[2 + gameInfo.ownPlayer.number]));
			gameInfo.cardsOnTable = Integer.parseInt(data[6]);
			
			if (gameInfo.cardsOnTable == 1) {
				playingTable.CheckIfCanThrowEverything();
			} else if (gameInfo.cardsOnTable == 0) {
				gameInfo.firstHand = gameInfo.activePlayer;
				playingTable.initNewThreeCards();
			}
			playingTable.updateLastCardMove();
			break;
			
		case 10: // both are whisting
			gameInfo.prevPlayer.myRole = Integer.parseInt(data[gameInfo.prevPlayer.number + 1]);
			gameInfo.nextPlayer.myRole = Integer.parseInt(data[gameInfo.nextPlayer.number + 1]);
			gameInfo.ownPlayer.myRole = Integer.parseInt(data[gameInfo.ownPlayer.number + 1]);
			
			playingTable.hideOwnClowd();
			playingTable.hideLeftClowd();
			playingTable.hideRightClowd();
			break;
		}
		
	}
	
	
	private void manageSentCards(String msg) {
		String[] data = msg.split(" ");
		if (data.length != 10) // smth is wrong!
			return;
		int cards[] = new int[10];
		for (int i = 0; i < 10; i++)
			cards[i] = PrefApplication.ServerToClientCards.get(Integer.parseInt(data[i]));
		Arrays.sort(cards);
		gameInfo.initNewDistribution();

		gameHolder.updateCardsOnPlayingTable(cards, null, null);
		// show cards on table!
	}
	
	public void sendMyTradeBetChoiceToServer() {
		if (gameInfo.currentCardBet < gameInfo.ownPlayer.myNewBet)
			gameInfo.currentCardBet = gameInfo.ownPlayer.myNewBet;
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		if (gameInfo.gameState == 1) { // trading is going on
			nameValuePairs.add(new BasicNameValuePair("notification", "bet_is_done")); 
		} else if (gameInfo.gameState == 3) { // we send put chosen game
			nameValuePairs.add(new BasicNameValuePair("notification", "real_bet_chosen")); 
		}
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("bet", Integer.toString(gameInfo.ownPlayer.myNewBet)));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
		gameView.hideBetTable();
	}
	
	public void sendThrownCardsToServer() {		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("notification", "cards_are_thrown")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		String s = Integer.toString(PrefApplication.ServerToClientCards.get(gameInfo.thrownCards.cards[0])) + " " + Integer.toString(PrefApplication.ServerToClientCards.get(gameInfo.thrownCards.cards[1]));
		nameValuePairs.add(new BasicNameValuePair("cards", s));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
	}
	
	public void sendMyWhistingChoiceToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("notification", "whist_choice")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("chosen_role", Integer.toString(gameInfo.ownPlayer.myRole)));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
		gameView.hideChoiceTable();
	}
	
	public void sendMyOpenCloseChoiceToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("notification", "open_close")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("is_open_game", Boolean.toString(gameInfo.isOpenGame)));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
		gameView.hideChoiceTable();
	}
	
	public void sendMyCardMoveToServer() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("id", gameInfo.ownPlayer.id));
		nameValuePairs.add(new BasicNameValuePair("notification", "card_move")); 
		nameValuePairs.add(new BasicNameValuePair("room_id", gameInfo.roomId));
		nameValuePairs.add(new BasicNameValuePair("move", Integer.toString(PrefApplication.ServerToClientCards.get(gameInfo.ownPlayer.lastCardMove))));
		PrefApplication.sendData(nameValuePairs, "NotificationManager.php");
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