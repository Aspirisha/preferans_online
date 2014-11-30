<?php
	include_once  'Connection.php';
	include_once  'mysql.php';
	include_once  'DebugManager.php';
  include_once  'Enumerations.php';
  
  $link = openMysqlConnection();
   
  function getSuit($cardNumber) {
    return (floor(($cardNumber - 1) / 8) + 1);
  }
  
  function getNextPlayer($playerId) {
    $playerId++;
    if ($playerId == 4)
      return 1;
    return $playerId;
  }
  
  function setNextActive($roomId) {
    global $link;
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    
    $active = $roomRow["active_player"] + 1;
    if ($active == 4)
      $active = 1;
    
    $result = mysql_query("UPDATE rooms SET active_player = ".$active." WHERE id = ".$roomId, $link);
    return $active;
  }
  
  function checkTimeExpiration($roomId) {
    global $link;
    
    appendLog("Check time expiration function started");
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $active = $roomRow["active_player"];
    $result = mysql_query("SELECT * FROM players WHERE room_id = '".$roomId."' AND my_number == ".$active."", $link);
    $activeRow = mysql_fetch_assoc($result);
    $curTime = time(); 
    $prevTime = $activeRow["start_move_time"];
    $leftTime = $activeRow["time_left"];
    if ($curTime - $prevTime > $leftTime) { // active player actually lost!
      appendLog("User ".$activeRow["id"]." has lost by time in room ".$roomRow["id"]);
      $newActiveMoney = $activeRow["money"] - $roomRow["game_bet"];
      mysql_query("UPDATE players SET money = ".$newActiveMoney." WHERE id = ".$activeRow["id"], $link);
      $result = mysql_query("SELECT * FROM players WHERE room_id = '".$roomId."' AND my_number <> ".$active."", $link);
      while ($row = mysql_fetch_assoc($result)) {
        $newMoney = $row["money"] + $roomRow["game_bet"] / 2;
        mysql_query("UPDATE players SET money = ".$newMoney." WHERE id = ".$row["id"], $link);
      }
      // send info about time has expired
    }
  }
    
  // sets active player to be the next after shuffler
  function newShuffle($roomId) {    
    global $link;
    $cards = range(1, 32);
    shuffle($cards);
    
    $cards1 = $cards[0];
    for ($i = 1; $i < 10; $i++)
      $cards1 = $cards1." ".$cards[$i];
      
    $cards2 = $cards[10];
    for ($i = 11; $i < 20; $i++)
      $cards2 = $cards2." ".$cards[$i];
      
    $cards3 = $cards[20];
    for ($i = 21; $i < 30; $i++)
      $cards3 = $cards3." ".$cards[$i];
      
    $talon = $cards[30]." ".$cards[31];  
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link); 
    
    if (!($row = mysql_fetch_assoc($result)))  { // something went wrong
    
    }
    
    $shuffler = $row["shuffler"];
    $active = getNextPlayer($shuffler);
     
    mysql_query("UPDATE players SET cards = '".$cards1."', last_card_move = -1 WHERE id = ".$row["player1"]."", $link);
    mysql_query("UPDATE players SET cards = '".$cards2."', last_card_move = -1 WHERE id = ".$row["player2"]."", $link);
    mysql_query("UPDATE players SET cards = '".$cards3."', last_card_move = -1 WHERE id = ".$row["player3"]."", $link);
    mysql_query("UPDATE rooms SET talon = '".$talon."', game_state = 1, active_player = ".$active.", current_first_hand = ".$active.", triplets_thrown = 0, current_trump = 0 WHERE id = '".$roomId."'", $link);  
    // now we need to send all data about current cards to players
    sendCards($cards1, $row["player1"]);
    sendCards($cards2, $row["player2"]);
    sendCards($cards3, $row["player3"]);
    

    sendGameState($roomId, 1);
    
     // write (append) the data to the file
    //file_put_contents($filename, $talon);    
  }
  
  function sendCards($cards, $playerId)
  {
    global $link;
    global $__DEBUG;
    
    $result = mysql_query("SELECT reg_id FROM players WHERE id = ".$playerId, $link);
    $row = mysql_fetch_assoc($result);
    
    $regId = $row["reg_id"];
    $activityAddress = ReceiverIds::GAME_ACTIVITY; // game activity
		$m_type = GameActivityMessageTypes::CARDS_INFO;            // cards info
    
		$response = sendMessage($regId, (string)$cards, $m_type, $activityAddress);      
  }
  
    /*
     * This function will be called each time active player is changed. It sends actual data to all 
     * three players. After data is got, server and passive players wait for active player to make a move.     
     * NB: first two fields of message are reserved. IT MUST BE STATE CODE and ACTIVE PLAYER number!!!     
     * Existing game states:
     * 1. Trading for the talon
     * Format: 
     *      state code: 1
     *      current Active player number : (1..3)      
     *      current max Bet : (-1 if no bet yet)     
     *      current user bets : 3 numbers separated by space, containing bets of player1, ..., player3   
     *               
     * 2. raspasy
     * Format: 
     *      state code: 2
     *      current Active player number : (1..3)        
     *      new Talon Card number : if it's first or second time, else this fiels is -1    
     *               
     * 3. Active player thinks what to throw away (he just got talon) and what game will he play      
     * Format:
     *      state code: 3
     *      current Active player number : (1..3)        
     *      min bet: bet code - for active player: his game should be not less than this 
     *              
     * 4. Active player has chosen his game
     * Format:
     *      state code: 4  
     *      current Active player number : (1..3)
     *      player's bet: the game that in fact player is goinf to play                               
     * 5. Whisting player decide what to do : pass or whist (if it's not stalingrad situation)    
     *      
     *     state code: 5
     *     current Active player number : (1..3)   
     *     roles info: 3 numbers characterizing current whist choices: -1 - hasn't decided yet, 0 - pass, 1 - whist, 2 - onside 
     *             
     * 6. Whisting choices are done. Both players said pass. So finish this game and start new.   
     *    state code: 6
     *    current Active player number : (1..3) // no matter who is it now
     *    current scores: {(mountain1, bullet1, whists_left1, whists_right1), ..., (mountain3, bullet3, whists_left3, whists_right3)}:
     *                    it's twelve numbers separated by spaces                       
     *                         
     *  7. Whisting choices are done. One player said whist, one player said pass. Make whister to be an active player and wait for his choice
     *     about how they should play: open or close game
     *     state code: 7
     *     current Active player number : (1..3) - it's that only whister!
     *          
     *  8. The only whister decided, open or close game. 
     *     state code: 8
     *     current Active player number : (1..3) - it's the player that is the next after shuffler
     *     whister choice: 1 if opened, else 0  
     *          
     * 10. Whisting choices are done. Both players are whisting.
     *     state code: 10 
     *     current Active player number : (1..3) - it's the player that is the next after shuffler
     *     roles info: 3 numbers characterizing current whist choices: -1 - hasn't decided yet, 0 - pass, 1 - whist, 2 - onside 
     *                                                                 
     *  9. "Real game" state.
     *     state code: 9
     *     current Active player number : (1..3)  
     *     5 numbers separated by space: current suit, first player move, second player move, third player move, # of cards on table (1..3). If no move was done yet, then -1
     * 11. Playing is finished: 10 tricks are got by all players in sum 
     *     state code: 11
     *     current Active player number : (1..3) - THIS NUMBER IS SENSELESS HERE                   
     *     current scores: {(mountain1, bullet1, whists_left1, whists_right1), ..., (mountain3, bullet3, whists_left3, whists_right3)}:
     *                    it's twelve numbers separated by spaces          
     *
     *                         
     * ?. Normal Game is going on : active player  
     *      state code: 
     *      current Active player number : (1..3)      
     * ?. Misere   
     *      state code: 7
     *      current Active player number : (1..3)                                                  
     */
  function sendGameState($roomId, $gameState) {
    global $link;
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    $roomRow = mysql_fetch_assoc($result);   
    $activePlayer = $roomRow["active_player"];
    
    $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId, $link);
      
    $player1 = 0;
    $player2 = 0;
    $player3 = 0;
    while ($row = mysql_fetch_assoc($result))   {
      if ($row["my_number"] == 1)
        $player1 = $row;
      else if ($row["my_number"] == 2) 
        $player2 = $row;
      else if ($row["my_number"] == 3) 
        $player3 = $row;
    }
    
    $message = "";
    switch ($gameState) {
    case GameStates::TALON_TRADING:
     // $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId, $link);
      $bet1 = $player1["current_trade_bet"];
      $bet2 = $player2["current_trade_bet"];
      $bet3 = $player3["current_trade_bet"];
      
      $message = "1 ".$activePlayer." ".$roomRow["current_trade_bet"]." ".$bet1." ".$bet2." ".$bet3; 
      break;
    case 2:
      $message = "2 ".$activePlayer." ";
      $talonCards = explode(" ", $roomRow["talon"]);
      if (count($talonCards) > 0) {
        $message = $message.$talonCards[0];
        $suit = getSuit($talonCards[0]);
        $result = mysql_query("UPDATE rooms SET current_suit = '".$suit."' WHERE id = '".$roomId."' VALUES", $link);
        if (count($talonCards) > 1) {
          $result = mysql_query("UPDATE rooms SET talon = '".$talonCards[1]."' WHERE id = '".$roomId."' VALUES", $link);
        }
      } else {
        $message = $message."-1";
      } 
      break;
    case 3: // show talon to everyone for some period of time and let active player choose what to throw       
      $bet1 = $player1["current_trade_bet"];
      $bet2 = $player2["current_trade_bet"];
      $bet3 = $player3["current_trade_bet"];  
      $message = "3 ".$activePlayer." ".$roomRow["talon"]." ".$bet1." ".$bet2." ".$bet3;          
      break; 
    case 4:
      $message = "4 ".$activePlayer." ".$roomRow["current_trade_bet"];
      break; 
    case 5:
      $role1 = $player1["my_current_role"];
      $role2 = $player2["my_current_role"];
      $role3 = $player3["my_current_role"];

      $message = "5 ".$activePlayer." ".$role1." ".$role2." ".$role3;
      break;
    case 6:
      $message = "6 ".$activePlayer;
      $message = $message." ".$player1["my_mountain"]." ".$player1["my_bullet"]." ".$player1["my_whists_left"]." ".$player1["my_whists_right"];
      $message = $message." ".$player2["my_mountain"]." ".$player2["my_bullet"]." ".$player2["my_whists_left"]." ".$player2["my_whists_right"];
      $message = $message." ".$player3["my_mountain"]." ".$player3["my_bullet"]." ".$player3["my_whists_left"]." ".$player3["my_whists_right"];
      break;
      
    case 7:
      $message = "7 ".$activePlayer;
      break;
    case 8:
      $message = "8 ".$activePlayer." ".$roomRow["open_game"];
      if ($activePlayer != $roomRow["trade_winner"] && $roomRow["open_game"] == 1) {
        sendPassersAndWhistersCards($roomId);
      }
      break;
    case 10:
      $role1 = $player1["my_current_role"];
      $role2 = $player2["my_current_role"];
      $role3 = $player3["my_current_role"];

      $message = "10 ".$activePlayer." ".$role1." ".$role2." ".$role3;
      break;
    case 9:
      $message = "9 ".$activePlayer." ".$roomRow["current_suit"]." ".$player1["last_card_move"]." ".$player2["last_card_move"]." ".$player3["last_card_move"]." ".$roomRow["cards_on_table"];
      if ($activePlayer != $roomRow["trade_winner"] && $roomRow["passers_cards_are_sent"] == 0 && $roomRow["open_game"] == 1) {
        sendPassersAndWhistersCards($roomId);
      }
      break;
    case 11:
      $message = "11 ".$activePlayer;
      $message = $message." ".$player1["my_mountain"]." ".$player1["my_bullet"]." ".$player1["my_whists_left"]." ".$player1["my_whists_right"];
      $message = $message." ".$player2["my_mountain"]." ".$player2["my_bullet"]." ".$player2["my_whists_left"]." ".$player2["my_whists_right"];
      $message = $message." ".$player3["my_mountain"]." ".$player3["my_bullet"]." ".$player3["my_whists_left"]." ".$player3["my_whists_right"];
      break;
    }
    
    for ($i = 1; $i < 4; $i++) {
      $ind = "player".$i;
      $result = mysql_query("SELECT reg_id FROM players WHERE id = ".$roomRow[$ind], $link);
      $row = mysql_fetch_assoc($result);  
      
      $regId = $row["reg_id"];
      $activityAddress = "3"; // game activity
  		$m_type = "5";            // sending current game state
  		$response = sendMessage($regId, $message, $m_type, $activityAddress);  
    }    
  }
  
  function playerMadeBet($regId, $roomId, $newBet) {
    global $link;  
      
     appendLog("In player made bet.");
    $result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
    $row = mysql_fetch_assoc($result);
    $playerNumber = $row["my_number"];
    $playerId = $row["id"];
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $row = mysql_fetch_assoc($result);   
    
    appendLog("Player number is ".$playerNumber." and id is ".$playerId);
    if (($row["active_player"] == $playerNumber) && ($row["game_state"] == 1)) { // it's really his move
      mysql_query("UPDATE players SET current_trade_bet = ".$newBet." WHERE id = '".$playerId."'", $link);
      
      if ($row["current_trade_bet"] < $newBet) {
         mysql_query("UPDATE rooms SET current_trade_bet = ".$newBet.", trade_winner = ".$playerNumber." WHERE id = '".$roomId."'", $link);
      }
      if ($newBet == 0) {
        mysql_query("UPDATE players SET stopped_trading = 1 WHERE id = '".$playerId."'", $link);
      }
      
      appendLog("Now we need to set next active player.");
      if (!isDebug()) {
        setNextPlayerActiveDuringTrading($roomId);
      } else { // DEBUG: both other players passed and current player passed or not - depends on his solution
        mysql_query("UPDATE players SET stopped_trading = 0 WHERE id = ".$playerId, $link);
        for ($i = 0; $i < 2; $i++) {
          appendLog("In debug loop setting bots to pass");
          $next = getNextPlayer($playerNumber);
          mysql_query("UPDATE rooms SET active_player = ".$next." WHERE id = ".$roomId, $link);
          sendGameState($roomId, $row["game_state"]);
          mysql_query("UPDATE players SET stopped_trading = 1, current_trade_bet = 0 WHERE room_id =".$roomId." AND my_number = ".$next, $link);
          $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
          $row = mysql_fetch_assoc($result);
          $playerNumber = $next;
          
          sendGameState($roomId, $row["game_state"]);
          sleep(5);
        }
        setNextPlayerActiveDuringTrading($roomId);
      }
      
      $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
      $row = mysql_fetch_assoc($result);
      appendLog("Now gamestate is ".$row["game_state"]);
      sendGameState($roomId, $row["game_state"]);
    }
  }
  
  function setNextPlayerActiveDuringTrading($roomId) {
    global $link;
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    $gameRow = mysql_fetch_assoc($result);
    switch ($gameRow["game_state"]) {
    case 1:
      $activePlayer = $gameRow["active_player"];
      $allPlayersStoppedtrading = 1;
      $passersNumber = 0;
      $j = $activePlayer;
      
      for ($i = 0; $i < 3; $i++) {
        if ($allPlayersStoppedtrading) {
          $activePlayer = getNextPlayer($activePlayer);
        }
        
        $j = getNextPlayer($j);
        
        $playerString = "player".$j;
        $playerId = $gameRow[$playerString];
        $result = mysql_query("SELECT * FROM players WHERE id = ".$playerId, $link);
        $row = mysql_fetch_assoc($result);
        if ($row["stopped_trading"] == 0) { // this is next player
          $allPlayersStoppedtrading = 0;
        } else {
          $passersNumber++;
        }
      }

      // DEBUG VERSION: force assign. Delete this two rows later
      if ($roomId == 14 && isDebug()) {
        $allPlayersStoppedTrading = 1;
        $activePlayer = 1;
        $passersNumber = 2;
      }
      
      if ($passersNumber <= 1) {
        $result = mysql_query("UPDATE rooms SET active_player = ".$activePlayer." WHERE id = '".$roomId."'", $link);
      } else {   // all depends on last player choice: if it was game, then he plays, else raspas
        $playerString = "player".$activePlayer;
        $playerId = $gameRow[$playerString];
        $result = mysql_query("SELECT * FROM players WHERE id = ".$playerId, $link);
        $row = mysql_fetch_assoc($result);
        if ($row["stopped_trading"] == 0) { // player that has just send us his descicion is going to play
          mysql_query("UPDATE rooms SET game_state = 3, active_player = ".$activePlayer." WHERE id = ".$roomId, $link);
          $oldCards = $row["cards"];
          $newCards = $oldCards." ".$gameRow["talon"];
          mysql_query("UPDATE players SET cards = '".$newCards."', my_current_role = 2 WHERE id = ".$row["id"], $link);
        } else {  // raspasy start
          $activePlayer = $gameRow["shuffler"] + 1;
          if ($activePlayer == 4)
            $activePlayer = 1;
          mysql_query("UPDATE rooms SET game_state = 2, active_player = ".$activePlayer." WHERE id = ".$roomId, $link);
          mysql_query("UPDATE players SET my_current_role = 3 WHERE room_id = ".$roomId, $link);
        }
        
      }
      
      break;    
    }     
  }
  // this function sends all users info about active user move
  function activePlayerMoved($roomId, $regId) {
  
  }
  
  function playerThrewCards($regId, $roomId, $cards) {
    global $link;
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    $roomRow = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
    $playerRow = mysql_fetch_assoc($result);
    
    appendLog("Player threw cards. His id = ".$playerRow["id"]);
    
    if ($playerRow["my_number"] == $roomRow["active_player"] && $playerRow["room_id"] == $roomId) {
      $cards = explode(" ", $cards);
      if (count($cards) != 2)  {
        appendLog("Player has thrown wrong number of cards. His id = ".$playerRow["id"]);
        return;                 
      }
      $cardsOnHand = explode(" ", $playerRow["cards"]);
      $foundCards = 0;
      $newCards="";
      
      for ($j = 0; $j < 12; $j++) {
        if ($cards[0] == $cardsOnHand[$j] || ($cards[1] == $cardsOnHand[$j])) {
          
          $foundCards++;   
        } else {
          $newCards = $newCards." ".$cardsOnHand[$j];
        }                            
      }
      if ($foundCards < 2)   {
        // cheater!
        return;
      }
        
      mysql_query("UPDATE players SET cards = '".$newCards."' WHERE id = ".$playerRow["id"], $link);
      notifyThatPlayerThrew($roomId);
    }
  }
  
  function notifyThatPlayerThrew($roomId) {
    global $link;
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    $roomRow = mysql_fetch_assoc($result);
    for ($i = 1; $i <= 3; $i++) {
      appendLog("Notifying other players about what player threw in room number ".$roomId);
      $m_type = GameActivityMessageTypes::ACTIVE_PLAYER_THROWN_CARDS; // active player has thrown cards
      $id = $roomRow["player".$i];
      $result = mysql_query("SELECT reg_id FROM players WHERE id = ".$id, $link);
      $row = mysql_fetch_assoc($result);
      $message = "";
      $activityAddress = ReceiverIds::GAME_ACTIVITY;
      $response = sendMessage($row["reg_id"], $message, $m_type, $activityAddress);
    }
  }
  
  function realBetIsChosen($id, $regId, $roomId, $newBet) {
    global $link;
    
    appendLog("Bet is chosen by player with id ".$id);
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE id = ".$id, $link);
    $playerRow = mysql_fetch_assoc($result);
    if ($playerRow["my_number"] == $roomRow["active_player"] && $playerRow["room_id"] == $roomId) {
      $temp = $newBet;
      if ($temp >= 16 && $temp <= 21)
  	    $temp--;
  		else if ($temp >= 22)
  			$temp -= 2;
        
      $trump = $temp % 5;
      
      mysql_query("UPDATE rooms SET game_state = 4, current_trade_bet = ".$newBet.", current_trump = ".$trump." WHERE id = ".$roomId, $link); 
      sendGameState($roomId, 4);
      
      $active = $roomRow["active_player"];
      $active = getNextPlayer($active);
      
      mysql_query("UPDATE rooms SET game_state = 5, active_player = ".$active." WHERE id = ".$roomId, $link);
      if (!isDebug()) {
        sendGameState($roomId, 5); 
      } else { // DEBUG! Here both other players choose whist automatically
        appendLog("Making both bots whisting.");
        mysql_query("UPDATE players SET my_current_role = 1 WHERE room_id = ".$roomId." AND my_number = ".$active, $link);
        sendGameState($roomId, 5);
        
        $active = getNextPlayer($active);
        mysql_query("UPDATE rooms SET active_player = ".$active." WHERE id = ".$roomId, $link);  
        mysql_query("UPDATE players SET my_current_role = 1 WHERE room_id = ".$roomId." AND my_number = ".$active, $link);
        sendGameState($roomId, 5);
        sleep(5);
        
        $active = getNextPlayer($active);
        mysql_query("UPDATE rooms SET game_state = 9, whisters_number = 2, whister = 0, passer = 0, current_first_hand = ".$active.", active_player = ".$active." WHERE id = ".$roomId, $link);
        sendGameState($roomId, 10);
        sleep(5);
        sendGameState($roomId, 9);
      }
    }
  }
  
  
  
  /**till here it's debug processed currently**/
  
  
  
  function playerMadeWhistChoice($id, $regId, $roomId, $role) {
    global $link;
    
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE id = ".$id, $link);
    $playerRow = mysql_fetch_assoc($result);
    if ($playerRow["my_number"] == $roomRow["active_player"] && $playerRow["room_id"] == $roomId) { 
      mysql_query("UPDATE players SET my_current_role = ".$role." WHERE id = ".$id, $link);
      $active = $roomRow["active_player"];
      
      // check if someone is still has undefined role
      $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId." AND my_current_role = -1", $link);
      if ($row = mysql_fetch_assoc($result)) {  // we still wait for another player to make a choice
        $active = $row["my_number"];
        mysql_query("UPDATE rooms SET active_player = ".$active." WHERE id = ".$roomId, $link);
        sendGameState($roomId, 5); 
      } else {  // first of all send final roles the players have chosen
        $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId, $link);
        $whisters = 0;
        $passers = 0;
        sendRoles($roomId);
        while ($row = mysql_fetch_assoc($result)) {
          if ($row["my_current_role"] == 2) {
            $player = $row;
          } else if ($row["my_current_role"] == 1) {            
            $whister[$whisters] = $row;
            $whisters++;
          } else {    // passer
            $passer[$passers] = $row;
            $passers++;
          }
        }

          if ($whisters == 0) { // just finish the game
            // update score table
            
            $deltaBullet = getGameCost($player["current_trade_bet"]);             
            $newBullet = $player["my_bullet"] + $deltaBullet; 
            
            $gameIsFinished = updatePlayerBullet($playerId, $roomId, $newBullet); 
            sendGameState($roomId, 6);
            if (!$gameIsFinished)
              initNewDistribution($roomId);
            else 
              gameIsFinished($roomId);
          }
          else if ($whisters == 1) {  // NB: if only one player is whisting, we have a field passer in table rooms containing passer player number
            // ask the only whister if he wants to play open or close game
            mysql_query("UPDATE rooms SET game_state = 7, whisters_number = 1, whister = ".$whisters[0]["my_number"].", passer = ".$passer[0]["my_number"].", active_player = ".$whisters[0]["my_number"]." WHERE id = ".$roomId, $link);
            sendGameState($roomId, 7);
            file_put_contents("Test/Log.txt", "WWWWisters = ".$whisters." passers = ".$passers." Game state is 7");
          } else {   // both are whisting
            $shuffler = $roomRow["shuffler"];
            $newActive = $shuffler + 1;
            if ($newActive == 4)
              $newActive = 1;
            
            // DEBUG: set my phone to be active after trading. Delete next 4 rows then
            $result = mysql_query("SELECT * FROM players WHERE name = 'tester'", $link);
            $row = mysql_fetch_assoc($result);
            $newActive = $row["my_number"];
            appendLog("newActive = ".$newActive);
              
              
            mysql_query("UPDATE rooms SET game_state = 9, whisters_number = 2, whister = 0, passer = 0, current_first_hand = ".$newActive.", active_player = ".$newActive." WHERE id = ".$roomId, $link);
            sendGameState($roomId, 10);
            sleep(5);
            sendGameState($roomId, 9);
          }
        }
      
    } else {
      file_put_contents("Test/Log.txt", "Cheater!", FILE_APPEND);
    }
  }
  
  function sendRoles($roomId) {
    global $link;
    $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId, $link);
    $role1 = 0;
    $role2 = 0;
    $role3 = 0;
    $i = 0;
    while ($row = mysql_fetch_assoc($result))   {
      if ($row["my_number"] == 1)
        $role1 = $row["my_current_role"];
      else if ($row["my_number"] == 2) 
        $role2 = $row["my_current_role"];
      else if ($row["my_number"] == 3) 
        $role3 = $row["my_current_role"];
      $rows[$i] = $row;
      $i++;
    }
    $message = $role1." ".$role2." ".$role3;  
    $m_type = 7;
    for ($i = 0; $i < 3; $i++) {      
      $regId = $rows[$i]["reg_id"];
      $activityAddress = "3"; // game activity
  		$response = sendMessage($regId, $message, $m_type, $activityAddress);  
    } 
  }
  
  // gives bullet points using given game trade bet
  function getGameCost($tradeBet) {
    $deltaBullet = 0;
    if ($tradeBet <= 15) {  
      $deltaBullet = 2 * ($tradeBet / 5) + 2; 
    } else if ($tradeBet == 16) {
      $deltaBullet = 10;
    } else if ($tradeBet <= 21) {
      $deltaBullet = 8;
    } else 
      $deltaBullet = 10;
    return $deltaBullet;
  }
  
  function updatePlayerBullet($playerId, $roomId, $newBullet) {
    global $link;
    $result = mysql_query("SELECT * FROM players WHERE id = ".$playerId, $link);
    $player = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId." AND id <> ".$playerId, $link);
    
    $player1 = mysql_fetch_assoc($result);
    $player2 = mysql_fetch_assoc($result);
    
    $oldBullet = $player["my_bullet"];
    $gameIsFinished = false;
    $newBullet1 = $player1["my_bullet"];
    $newBullet2 = $player2["my_bullet"];
        
    if ($newBullet > $roomRow["game_bullet"]) {
      $delta_bullet = $newBullet - $roomRow["game_bullet"]; // this bullet part should be given to another player
      $whists1 = 0;  // how many whists will have player on player1 after we write to player 1 bullet instead of player
      $whists2 = 0;
      $delta_bullet1 = 0;
      $delta_bullet2 = 0;
      $newBullet = $roomRow["game_bullet"];
      
      $result = mysql_query("SELECT * FROM game_types WHERE id = ".$roonRow["game_type_id"], $link);
      $gameTypeRow = mysql_fetch_assoc($result);
      
      $bulletCost = $gameTypeRow["bullet_cost"];
      
      
      if ($player1["my_bullet"] + $delta_bullet <= $roomRow["game_bullet"]) {
        $delta_bullet1 = $delta_bullet;
        $whists1 = $bulletCost * $delta_bullet1;
      } else {
        $delta_bullet1 = $roomRow["game_bullet"] - $player1["my_bullet"];
        $whists1 = $bulletCost * $delta_bullet1;
        $delta_bullet2 = $delta_bullet - $delta_bullet1;
        $whists2 = $bulletCost * $delta_bullet2;
      }   
      
      $newBullet1 = $player1["my_bullet"] + $delta_bullet1;
      $newBullet2 = $player2["my_bullet"] + $delta_bullet2;  
      
      $leftNumber = getNextPlayer($player["my_number"]);
      $rightNumber = $player["my_number"] - 1;
      if ($leftNumber == 0)
        $leftNumber = 3;
        
      if ($delta_bullet1 != 0) {
        mysql_query("UPDATE players SET my_bullet = ".$newBullet1." WHERE id = ".$player1["id"], $link);
        if ($player1["my_number"] == $leftNumber)
        {
          $newWhists = $player["my_whists_left"] + $whists1;
          mysql_query("UPDATE players SET my_whists_left = ".$newWhists." WHERE id = ".$playerId, $link);
        } else {
          $newWhists = $player["my_whists_right"] + $whists1;
          mysql_query("UPDATE players SET my_whists_right = ".$newWhists." WHERE id = ".$playerId, $link);
        }
      }
      if ($delta_bullet2 != 0) {
        mysql_query("UPDATE players SET my_bullet = ".$newBullet2." WHERE id = ".$player2["id"], $link);
        if ($player2["my_number"] == $leftNumber)
        {
          $newWhists = $player["my_whists_left"] + $whists2;
          mysql_query("UPDATE players SET my_whists_left = ".$newWhists." WHERE id = ".$playerId, $link);
        } else {
          $newWhists = $player["my_whists_right"] + $whists2;
          mysql_query("UPDATE players SET my_whists_right = ".$newWhists." WHERE id = ".$playerId, $link);
        }
      }  
    }
    mysql_query("UPDATE players SET my_bullet = ".$newBullet." WHERE id = ".$player["id"], $link);  
    if ($newBullet >= $roomRow["game_bullet"] && $newBullet1 >= $roomRow["game_bullet"] && $newBullet2 >= $roomRow["game_bullet"]) {
      $gameIsFinished = true;
    }
    return $gameIsFinished;
    
  }
  
  function playerDecidedOpenOrClose($id, $regId, $roomId, $isOpenGame) {
    global $link;
    if (userIsReallyActive($id, $roomId, 7)) {
      $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId);
      $row = mysql_fetch_assoc($result);
      $active = $row["shuffler"] + 1;
      if ($active == 4)
        $active = 1;
      if ($isOpenGame == "true") {      
        mysql_query("UPDATE rooms SET active_player = ".$active.", game_state = 9, open_game = 1, passers_cards_are_sent = 0 WHERE id = ".$roomId, $link);
      } else {
        mysql_query("UPDATE rooms SET active_player = ".$active.", game_state = 9, open_game = 0, passers_cards_are_sent = 0 WHERE id = ".$roomId, $link);  
      }
      sendGameState($roomId, 8); 
      sendGameState($roomId, 9);
    }
  }
  
  // declared game state is the gamestate we get by the context that user sent us. For example, if he sent us
  //   that he won't whist, we expect the gamestate is whist or not whist choosing. But bfore calling this
  // function it's necessary to understand is user at all the person he says: test if his id corresponds to his key
  function userIsReallyActive($id, $roomId, $declaredGameState) {
    global $link;
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId);
    $roomRow = mysql_fetch_assoc($result); 
    $result = mysql_query("SELECT * FROM players WHERE id = ".$id);
    $playerRow = mysql_fetch_assoc($result); 
    
    if ($playerRow["my_number"] == $roomRow["active_player"] && $declaredGameState == $roomRow["game_state"]) {
      return 1;
    }
    return 0;
  }
  
  // new shuffle and set game state to 1, clear all ditribution-dependent data
  function initNewDistribution($roomId) {
    global $link;
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $shuffler = $roomRow["shuffler"];
    $shuffler = getNextPlayer($shuffler);
    
    mysql_query("UPDATE players SET my_current_role = -1, last_card_move = -1, current_trade_bet = -1, cards = ' ', stopped_trading = 0, my_tricks = 0 WHERE room_id = ".$roomId, $link);
    mysql_query("UPDATE rooms SET game_state = 1, shuffler = ".$shuffler.", whisters_number = 0, current_trade_bet = 0, passers_cards_are_sent = 0, current_suit = -1, current_trump = -1 cards_on_table = 0 WHERE id = ".$roomId, $link);
    newShuffle($roomId);
  }
  
  
  // sends whister cards and passer cards to everyone
  
  function sendPassersAndWhistersCards($roomId) {
    global $link; 
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $passerId = $roomRow["player".$roomRow["passer"]];
    $whisterId = $roomRow["player".$roomRow["whister"]];
    $result = mysql_query("SELECT cards FROM players WHERE id = ".$passerId, $link);
    $passer = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT cards FROM players WHERE id = ".$whisterId, $link);
    $whister = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE room_id = ".$roomId, $link);
    
    $message = $whister["cards"]." ".$passer["cards"];
    while ($row = mysql_fetch_assoc($result)) {
      $m_type = 8;
      $regId = $row["reg_id"];
      $activityAddress = "3"; // game activity
  		$response = sendMessage($regId, $message, $m_type, $activityAddress); 
    }
    mysql_query("UPDATE rooms SET passers_cards_are_sent = 1 WHERE id = ".$roomId, $link);
  }
  
  function playerMadeCardMove($id, $regId, $roomId, $cardMove, $debugCall = false) {
    global $link;
    
    if (!userIsReallyActive($id, $roomId, 9)) { // then it can be not only 9'th gameState, but now it is
      appendLog("User with id ".$id." is not active but tries to make move.");
      return false;
    }
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE id = ".$id, $link);
    $playerRow = mysql_fetch_assoc($result);
    
    $active = $roomRow["active_player"];
    $firstHand = $roomRow["current_first_hand"];
    $next = getNextPlayer($active);
    $currentSuit = $roomRow["current_suit"];
    $currentTrump = $roomRow["current_trump"];
      
    // first of all, understand whether the move is correct
    $cardsOnHand = explode(" ", $playerRow["cards"]);
    $foundCards = 0;
    $newCards="";
    $cardsNumber = count($cardsOnHand);  
    
    $hasNoTrumpsAndSuit = true;
    $hasCurrentSuit = false;
    $hasTrumps = false;
    for ($j = 0; $j < $cardsNumber; $j++) {
      $curSuit = getSuit($cardsOnHand[$j]);
      if ($curSuit == $currentSuit || $curSuit == $currentTrump)
        $hasNoTrumpsAndSuit = false;
      
      if ($curSuit == $currentSuit) 
        $hasCurrentSuit = true;
      if ($curSuit == $currentTrump) 
        $hasTrumps = true;
        
      if ($cardMove == $cardsOnHand[$j]) { 
        $foundCards++;   
      } else {
        $newCards = $newCards." ".$cardsOnHand[$j];
      }                            
    }
    
    if ($foundCards != 1) {    // user has cheated or some error during data transfer has occured
       appendLog("this cards not found: ".$cardMove." at user with id ".$id);
       return false;      
    }
    
    // ok, user really had such a card, but is his move correct according to rules?
    
    $suit = getSuit($cardMove);
    
    
    /*if (!$hasNoTrumpsAndSuit && $suit != $currentSuit && $suit != $currentTrump && $active != $firstHand) {
      file_put_contents("Test/zzzz.txt", "impossible move: ".$cardMove);
      return;
    }   */
    
    if ($active != $firstHand && $suit != $currentSuit)  {
      if ($hasCurrentSuit) {
        appendLog("impossible move: player has the suit ".$cardMove);
        return false;
      }
      
      if ($suit != $currentTrump && $hasTrumps) {
        appendLog("impossible move: player has the trumps ".$cardMove);
        return false;
      }
    }
        
    mysql_query("UPDATE players SET cards = '".$newCards."', last_card_move = ".$cardMove." WHERE id = ".$playerRow["id"], $link);
    
    $newCardsOnTable = $roomRow["cards_on_table"] + 1;
    
    if ($next == $firstHand) {   // 3 cards are thrown so we need to understand who has won
      $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player1"], $link);
      $playerRow1 = mysql_fetch_assoc($result);
      $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player2"], $link);
      $playerRow2 = mysql_fetch_assoc($result);
      $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player3"], $link);
      $playerRow3 = mysql_fetch_assoc($result); 
      
      $move1 = $playerRow1["last_card_move"]; 
      $move2 = $playerRow2["last_card_move"]; 
      $move3 = $playerRow3["last_card_move"];   
      
      $suit1 = getSuit($move1);      
      $suit2 = getSuit($move2);   
      $suit3 = getSuit($move3); 
      
      if ($suit1 == $currentTrump)    
        $move1 += 100;
      else if ($suit1 != $currentSuit)
        $move1 = 0;
        
      if ($suit2 == $currentTrump)    
        $move2 += 100;
      else if ($suit2 != $currentSuit)
        $move2 = 0;
        
      if ($suit3 == $currentTrump)    
        $move3 += 100;
      else if ($suit3 != $currentSuit)
        $move3 = 0;  
       
      $triplets = $roomRow["triplets_thrown"] + 1; 
      $newActive = -1;
      if ($move1 > $move2 && $move1 > $move3) {    //1
        $tricks = $playerRow1["my_tricks"] + 1;
        mysql_query("UPDATE players SET my_tricks = ".$tricks." WHERE id = ".$playerRow1["id"], $link);
        mysql_query("UPDATE rooms SET active_player = 1, triplets_thrown = ".$triplets.", cards_on_table = $newCardsOnTable WHERE id = ".$roomId, $link);
        $newActive = 1;
      } else if ($move2 > $move1 && $move2 > $move3) {  // 2
        $tricks = $playerRow2["my_tricks"] + 1;
        mysql_query("UPDATE players SET my_tricks = ".$tricks." WHERE id = ".$playerRow2["id"], $link);
        mysql_query("UPDATE rooms SET active_player = 2, triplets_thrown = ".$triplets.", cards_on_table = $newCardsOnTable WHERE id = ".$roomId, $link);
        $newActive = 2;
      } else { //3
        $tricks = $playerRow3["my_tricks"] + 1;
        mysql_query("UPDATE players SET my_tricks = ".$tricks." WHERE id = ".$playerRow3["id"], $link);
        mysql_query("UPDATE rooms SET active_player = 3, triplets_thrown = ".$triplets.", cards_on_table = $newCardsOnTable WHERE id = ".$roomId, $link);
        $newActive = 3;
      }
      
      sendGameState($roomId, 9);
      sleep(7); // sleep for 7 secs
      mysql_query("UPDATE players SET last_card_move = -1 WHERE room_id = ".$roomId, $link);
      mysql_query("UPDATE rooms SET cards_on_table = 0, current_suit = -1, current_first_hand = ".$newActive." WHERE id = ".$roomId, $link);
      
      sendGameState($roomId, 9);
      
      if ($triplets == 10) {
        realDistribFinished($roomId);
      }
        
    } else {
      if ($active == $firstHand) // set suit
        mysql_query("UPDATE rooms SET active_player = ".$next.", current_suit = ".$suit.", cards_on_table = ".$newCardsOnTable." WHERE id = ".$roomId, $link);
      else
        mysql_query("UPDATE rooms SET active_player = ".$next.", cards_on_table = ".$newCardsOnTable." WHERE id = ".$roomId, $link);
        
      sendGameState($roomId, 9);
    }
    
    if (isDebug() && !$debugCall) {
      appendLog("Random moves begin!");
      $tester = $active;
      $active = $next;
      while ($active != $tester) {
        sleep(5);
        $cardMove = 1;
        $moveTrial = false;
        $activeId = $roomRow["player".$active];
        appendLog("Active player number is ".$active." and id is ".$activeId);
        while (!$moveTrial && $cardMove < 33) {
          $moveTrial = playerMadeCardMove($activeId, 0, $roomId, $cardMove, true);
          $cardMove++;
          if (!$moveTrial)
            appendLog("Failed to make random move with card ".($cardMove - 1)." by bot with number ".$active);
        }
        
        if (!$moveTrial) {
          appendLog("Bot couldn't make move. Something is wrong, man.");
        }
        $result = mysql_query("SELECT active_player from rooms WHERE id = ".$roomId, $link);
        $row = mysql_fetch_assoc($result);
        $active = $row["active_player"];
      }
    }
    
    return true;
  }
  
  // all players reached needed bullet. End game. may be ask if they want to play again
  function gameIsFinished($roomId) {
    
  }
  
  
  // this function is called if real game was played during the distribution
  function realDistribFinished($roomId) { // count here score each user gained, set up new shuffle, test if the game hasn't finished...
    global $link;
    
    appendLog("In real distribution finished.");
    $result = mysql_query("SELECT * FROM rooms WHERE id = ".$roomId, $link);
    $roomRow = mysql_fetch_assoc($result);
    
    $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player1"], $link);
    $playerRow1 = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player2"], $link);
    $playerRow2 = mysql_fetch_assoc($result);
    $result = mysql_query("SELECT * FROM players WHERE id = ".$roomRow["player3"], $link);
    $playerRow3 = mysql_fetch_assoc($result); 
    
    $tricks1 = $playerRow1["my_tricks"];
    $tricks2 = $playerRow2["my_tricks"];
    $tricks3 = $playerRow3["my_tricks"]; 
    
    $result = mysql_query("SELECT * FROM game_types WHERE id = ".$roomRow["game_type_id"], $link);
    $gameRules = mysql_fetch_assoc($result);
    
    $role1 = $playerRow1["my_current_role"];
    $role2 = $playerRow2["my_current_role"];
    $role3 = $playerRow3["my_current_role"];
    appendLog("First player got ".$tricks1." tricks.");
    appendLog("Second player got ".$tricks2." tricks.");
    appendLog("Third player got ".$tricks3." tricks.");
    
    $delta_mount1 = 0;
    $delta_bullet1 = 0;
    $delta_whists_left1 = 0;
    $delta_whists_right1 = 0;
    
    $delta_mount2 = 0;
    $delta_bullet2 = 0;
    $delta_whists_left2 = 0;
    $delta_whists_right2 = 0;
    
    $delta_mount3 = 0;
    $delta_bullet3 = 0;
    $delta_whists_left3 = 0;
    $delta_whists_right3 = 0; 
    
    $player_must_take = 5;
    $temp = $roomRow["current_trade_bet"];
    if ($temp >= 16 && $temp <= 21)
  	  $temp--;
  	else if ($temp >= 22)
    	$temp -= 2;
    
    $player_must_take += floor(1 + ($temp - 1) / 5); 
    appendLog("Player should have taken ".$player_must_take." tricks.");
    
    $costs = explode(" ", $gameRules["games_costs"]);
    $ind = $player_must_take - 6;
    $game_cost = $costs[$ind];
    appendLog("Game cost is ".$game_cost." tricks.");
    
    $untakenString = "untaken_game_cost_".$player_must_take;
    $untakenGameTrickCost = $gameRules[$untakenString]; 
    appendLog("Each untaken trick for player costs ".$untakenGameTrickCost." whists.");
    
    $whistCost = $gameRules["whist_cost_on_".$player_must_take];
    appendLog("Whist trick cost is ".$whistCost." whists.");
    
    $untakenWhistCost = $gameRules["untaken_whist_cost_on_".$player_must_take];
    appendLog("Untaken whist trick cost is ".$untakenWhistCost." whists.");
    
    $whisters_must_take = 0;   // if whister takes such amount, we is guaranteed not to get mount
    $one_whister_must_take = 0;
    if ($roomRow["whisters_number"] == 1) {
      if ($player_must_take == 6) {
        $whisters_must_take = 4;
        $one_whister_must_take = 4;
      }
      if ($player_must_take == 7) {
        $whisters_must_take = 2;
        $one_whister_must_take = 2;
      }
      if ($player_must_take == 8 || $player_must_take == 9) {
        $whisters_must_take = 1;
        $one_whister_must_take = 1;
      }
    } else { // 2 whisters
      if ($player_must_take == 6) {
        $whisters_must_take = 4;
        $one_whister_must_take = 2;
      }
      if ($player_must_take == 7) {
        $whisters_must_take = 2;
        $one_whister_must_take = 1;
      }
      if ($player_must_take == 8 || $player_must_take == 9) {
        $whisters_must_take = 1;
        $one_whister_must_take = 1;
      }
    }
    
    $playerTricks = $tricks1;
    if ($role2 == 2)
      $playerTricks = $tricks2; 
    if ($role3 == 2)
      $playerTricks = $tricks3; 
    
    
    
    if ($role1 == 2) { // onside
      appendLog("First player is onside.");
      if ($tricks1 >= $player_must_take) 
        $delta_bullet1 = $game_cost;
      else {
        $notTakenTricks = $player_must_take - $tricks1;
        $delta_mount1 = $untakenGameTrickCost * $notTakenTricks;
        $deltaWhists = $notTakenTricks * $whistCost;   // remiz
        $delta_whists_right2 += $deltaWhists;
        $delta_whists_left3 += $deltaWhists;
      }
    } else if ($role1 == 1) {
      if ($role3 == 2)
        $delta_whists_right1 += $whistCost * $tricks1;
      else
        $delta_whists_left1 += $whistCost * $tricks1;
        
      if ($tricks1 < $one_whister_must_take && (10 - $playerTricks) < $whisters_must_take) {
        $delta_mount1 = ($one_whister_must_take - $tricks1) * $untakenWhistCost;
      }
    } else if ($role1 == 0) {
      if ($role3 == 2) 
        $delta_whists_left2 += $whistCost * $tricks1;
      else
        $delta_whists_right3 += $whistCost * $tricks1;
    }
    
    
    
    if ($role2 == 2) { // onside
      if ($tricks2 >= $player_must_take) 
        $delta_bullet2 = $game_cost;
      else {
        $notTakenTricks = $player_must_take - $tricks2;
        $delta_mount2 = $untakenGameTrickCost * $notTakenTricks;
        $deltaWhists = $notTakenTricks * $whistCost;   // remiz
        $delta_whists_right3 += $deltaWhists;
        $delta_whists_left1 += $deltaWhists;
      }
    } else if ($role2 == 1) {
      if ($role1 == 2)
        $delta_whists_right2 += $whistCost * $tricks2;
      else
        $delta_whists_left2 += $whistCost * $tricks2;
        
      if ($tricks2 < $one_whister_must_take && (10 - $playerTricks) < $whisters_must_take) {
        $delta_mount2 = ($one_whister_must_take - $tricks2) * $untakenWhistCost;
      }
    } else if ($role2 == 0) {
      if ($role3 == 2) 
        $delta_whists_right1 += $whistCost * $tricks2;
      else
        $delta_whists_left3 += $whistCost * $tricks2;
    }
    
    
    if ($role3 == 2) { // onside
      if ($tricks3 >= $player_must_take) 
        $delta_bullet3 = $game_cost;
      else {
        $notTakenTricks = $player_must_take - $tricks3;
        $delta_mount3 = $untakenGameTrickCost * $notTakenTricks;
        $deltaWhists = $notTakenTricks * $whistCost;   // remiz
        $delta_whists_right1 += $deltaWhists;
        $delta_whists_left2 += $deltaWhists;
      }
    } else if ($role3 == 1) {
      if ($role2 == 2)
        $delta_whists_right3 += $whistCost * $tricks3;
      else
        $delta_whists_left3 += $whistCost * $tricks3;
        
      if ($tricks3 < $one_whister_must_take && (10 - $playerTricks) < $whisters_must_take) {
        $delta_mount3 = ($one_whister_must_take - $tricks3) * $untakenWhistCost;
      }
    } else if ($role3 == 0) {
      if ($role2 == 2) 
        $delta_whists_left1 += $whistCost * $tricks3;
      else
        $delta_whists_right2 += $whistCost * $tricks3;
    }
    
    $mount1 = $playerRow1["my_mountain"] + $delta_mount1;
    $bullet1 = $playerRow1["my_bullet"] + $delta_bullet1;
    $whists_left1 = $playerRow1["my_whists_left"] + $delta_whists_left1;
    $whists_right1 = $playerRow1["my_whists_right"] + $delta_whists_right1;
    
    $mount2 = $playerRow2["my_mountain"] + $delta_mount2;
    $bullet2 = $playerRow2["my_bullet"] + $delta_bullet2;
    $whists_left2 = $playerRow2["my_whists_left"] + $delta_whists_left2;
    $whists_right2 = $playerRow2["my_whists_right"] + $delta_whists_right2;
    
    $mount3 = $playerRow3["my_mountain"] + $delta_mount3;
    $bullet3 = $playerRow3["my_bullet"] + $delta_bullet3;
    $whists_left3 = $playerRow3["my_whists_left"] + $delta_whists_left3;
    $whists_right3 = $playerRow3["my_whists_right"] + $delta_whists_right3;
    
    
    mysql_query("UPDATE players SET my_mountain = ".$mount1.", my_whists_left = ".$whists_left1.", my_whists_right = ".$whists_right1." WHERE id = ".$roomRow["player1"], $link);
    mysql_query("UPDATE players SET my_mountain = ".$mount2.", my_whists_left = ".$whists_left2.", my_whists_right = ".$whists_right2." WHERE id = ".$roomRow["player2"], $link);
    mysql_query("UPDATE players SET my_mountain = ".$mount3.", my_whists_left = ".$whists_left3.", my_whists_right = ".$whists_right3." WHERE id = ".$roomRow["player3"], $link);
    
    
    $gameIsFinished = 0;
    if ($delta_bullet1 > 0) {
      $gameIsFinished = updatePlayerBullet($roomRow["player1"], $roomId, $bullet1);
    } else if ($delta_bullet2 > 0) {
      $gameIsFinished = updatePlayerBullet($roomRow["player2"], $roomId, $bullet2);
    } else if ($delta_bullet3 > 0) {
      $gameIsFinished = updatePlayerBullet($roomRow["player3"], $roomId, $bullet3);
    }
    
    sendGameState($roomId, 11);
    if (!$gameIsFinished)
      initNewDistribution($roomId);
    else 
      gameIsFinished($roomId);
  }
  
  closeMysqlConnection($link);
?>