<?php
	include_once 'mysql.php';
	include_once 'GameLogic.php';
  include_once 'DebugManager.php';
  include_once 'Enumerations.php';
  
	$regId = $_POST["reg_id"]; // this will be empty if not online notificatiion
  $id = $_POST["id"];
  
  $link = openMysqlConnection();
  
  $result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
  $row = mysql_fetch_assoc($result);
  $id = $row["id"];
  
  $notification = $_POST["notification"];
  
  if ($notification != "online") {
     $result = mysql_query("SELECT reg_id FROM players WHERE id = ".$id, $link);
     $row = mysql_fetch_assoc($result);
     $regId = $row["reg_id"];
  }
	
  $roomId = $_POST["room_id"];
	$defaultName = "user";
  
  $filename="Test/YourFile.txt";
  
  function notifyPlayerExited($playerToNotifyId, $exitedPlayerId, $playersNumber) {
    global $link;
   
    $result = mysql_query("SELECT reg_id FROM players WHERE id = '".$playerToNotifyId."'", $link);
    $receiverRegId = "";
    if ($row = mysql_fetch_assoc($result))  
       $receiverRegId = $row["reg_id"];
    
    $activityAddress = ReceiverIds::GAME_ACTIVITY; // game activity
		$m_type = "4";            // player exited
    $message = $exitedPlayerId." ".$playersNumber;   // send id of new player and current amount of people in room
    if ($playerToNotifyId != $exitedPlayerId) // HACK just debug version not to notify myself. IRL it will be impossible
		  $response = sendMessage($receiverRegId, $message, $m_type, $activityAddress);   
      
  }                  
  
  
	switch ($notification) {
	case "online":
		$result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
		if ($row = mysql_fetch_assoc($result)) {
			$result = mysql_query("UPDATE players SET is_online = 1 WHERE reg_id = '".$regId."'", $link); 
      file_put_contents($filename, 1);
		    // send error msg to device if error occured
		} else { // new user
		    $result = mysql_query("INSERT INTO players (name, is_online, reg_id) VALUES('".$defaultName."', 1, '".$regId."')", $link); 
		}
    
		$message = $row["id"];
    $activityAddress = ReceiverIds::ENTRY_ACTIVITY;
    $m_type = EntryActivityMessageTypes::ONLINE_NOTIFICATION_ANSWER;
    $response = sendMessage($regId, $message, $m_type, $activityAddress);
   
    clearLog();
    
    appendLog("New player is online. His id = ".$row["id"]);
    
	  break;
  
  case "keep_alive":
    //$message = date(DATE_RFC822); // send current timestamp to client
    $message = time();
    $response = sendMessage($regId, $message, 0, ReceiverIds::KEEP_ALIVE);   // HARDCODED NOW: 777 means it's keepalive anser
    $result = mysql_query("SELECT room_id FROM players WHERE reg_id = ".$regId, $link);
    $row = mysql_fetch_assoc($result);
    
    //checkTimeExpiration($row["room_id"]);
    
    appendLog("Keepalive message received. Sent reply.");
    break;
  
  case "old_id":
    $oldId = $_POST["old_id"];
    $result = mysql_query("DELETE FROM players WHERE reg_id = ".$regId, $link); // no need to use new row for existing player
    $result = mysql_query("UPDATE players SET reg_id = ".$regId." WHERE reg_id = ".$oldId, $link);
    
    appendLog("Old Id is deleted. It was id = ".$oldId);
    break;
    
  case "bet_is_done":
    $newBet = $_POST["bet"];
    appendLog("Player ".$id." made bet = ".$newBet);
    playerMadeBet($regId, $roomId, $newBet);  
    break;
    
  case  "cards_are_thrown":
    $cards = $_POST["cards"];
    appendLog("Player ".$id." has thrown cards = ".$cards);
    playerThrewCards($regId, $roomId, $cards);
    break;
    
  case "real_bet_chosen":    
    $newBet = $_POST["bet"];
    appendLog("Player ".$id." has chosen real bet = ".$newBet);
    realBetIsChosen($id, $regId, $roomId, $newBet);
    break;
    
  case "whist_choice":
    $role = $_POST["chosen_role"];
    appendLog("Player ".$id." has chosen whist role = ".$newBet);
    playerMadeWhistChoice($id, $regId, $roomId, $role);
    break;
    
  case "open_close":
    $isOpenGame = $_POST["is_open_game"];
    appendLog("Player ".$id." has chosen if the game is opend = ".$isOpenGame);
    playerDecidedOpenOrClose($id, $regId, $roomId, $isOpenGame);
    break;
    
  case "card_move":
    $cardMove = $_POST["move"];
    appendLog("Player ".$id." has done card move = ".$cardMove);
    playerMadeCardMove($id, $regId, $roomId, $cardMove);
    break;
    
  case "user_exited":
    appendLog("Player ".$id." has exited");
    
    $result = mysql_query("SELECT id FROM players WHERE reg_id = '".$regId."'", $link);
    $playerId = 0;
    if ($row = mysql_fetch_assoc($result)) {
      $playerId = $row["id"];   
    }
    $result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    
    $playerToDelete = "";
    $playersNumber = -1;
    $delNumber = -1;
    if ($row = mysql_fetch_assoc($result)) {
      $playersNumber = $row["players_number"];
      if ($row["player1"] == $playerId)
      {
        $playerToDelete = "player1";
        $delNumber = 1;
      }
      else if ($row["player2"] == $playerId)  {
        $playerToDelete = "player2";
        $delNumber = 2;
      }
      else if ($row["player3"] == $playerId) {
        $playerToDelete = "player3";
        $delNumber = 3;
      }
    }
    
    $playersNumber--;
    file_put_contents("Test/players_left.txt", $playersNumber);
    if ($playersNumber == 0)
    {
      mysql_query("DELETE FROM rooms WHERE id = ".$roomId, $link);
      break;
    }
      
    if ($playerToDelete != "" && $playersNumber > 0) {
      file_put_contents("Test/zzzz.txt", "to del: ".$playerToDelete);
      $result = mysql_query("UPDATE rooms SET ".$playerToDelete." = NULL, players_number = ".$playersNumber." WHERE id = '".$roomId."'", $link);
      if ($delNumber != 1)
        notifyPlayerExited($row["player1"], $row[$playerToDelete], $playersNumber);
      if ($delNumber != 2)
        notifyPlayerExited($row["player2"], $row[$playerToDelete], $playersNumber);
      if ($delNumber != 3)
        notifyPlayerExited($row["player3"], $row[$playerToDelete], $playersNumber);
    }
    break;
  case "active_player_moved":
    //activePlayerMoved($roomId, $regId) 
    break;        
	}
  closeMysqlConnection($link);
?>