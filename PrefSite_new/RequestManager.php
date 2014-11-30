<?php
	include_once  'Connection.php';
	include_once  'mysql.php';
	include_once  'GameLogic.php';
  include_once  'DebugManager.php';
  include_once  'Enumerations.php';
  
	$regId = $_POST["reg_id"];
	$request = $_POST["request"];
	$link = openMysqlConnection();
  
  $result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
  $row = mysql_fetch_assoc($result);
  $id = $row["id"];
  appendLog("ID IS ".$id);
   
  function notifyNewPlayerAppeared($playerToNotifyId, $newPlayerId, $playersNumber, $newPlayerNumber)
  {
    global $link;
    
    $result = mysql_query("SELECT reg_id FROM players WHERE id = '".$playerToNotifyId."'", $link);
    $receiverRegId = "";
    if ($row = mysql_fetch_assoc($result))  
       $receiverRegId = $row["reg_id"];    
    
    $activityAddress   = "3"; // game activity
		$m_type = "1";            // new player appeared
    $message = $newPlayerId." ".$playersNumber." ".$newPlayerNumber;   // send id of new player, new player number and current amount of people in room
    if ($playerToNotifyId != $newPlayerId) // HACK just debug version not to notify myself. IRL it will be impossible
		  $response = sendMessage($receiverRegId, $message, $m_type, $activityAddress);     
      
    appendLog("New player appeared");
  }                
    
  
  
	switch ($request)
	{
  case "ping":
    $response = sendMessage($regId, "1", "1", "100");
    appendLog("User ".$id." has pinged the server"); 
    break;
  
  case "check_time":
    $result = mysql_query("SELECT room_id FROM players WHERE reg_id = ".$regId, $link);
    $row = mysql_fetch_assoc($result);
    appendLog("User ".$id." requested time checking"); 
    checkTimeExpiration($row["room_id"]);
    break;
  
  case "current_name":
    $result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
    $row = mysql_fetch_assoc($result);
    $name = $row["name"];
    appendLog("User ".$id." has asked for his name"); 
    $response = sendMessage($regId, $name, 0, 4); 
    break;
    
	case "my_money":
		$result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regId."'", $link);
		$yourMoney = 0;
    appendLog("User ".$id." asked for his amount of money"); 
		if ($row = mysql_fetch_assoc($result)) {
		    $yourMoney = $row["money"];
		} else { // process error
		
		}
		
		$message = (string)$yourMoney;
		$activityAddress   = "1"; // new room activity
		$m_type = "0";            // money
	 
		$response = sendMessage($regId, $message, $m_type, $activityAddress);
	 
	  break;   
    
	case "existing_rooms":
    appendLog("User ".$id." asked for existing rooms"); 
    
    if (isDebug()) {
      $result = mysql_query("UPDATE rooms SET player1 = NULL, player2 = 9, player3 = 10, players_number=2, game_state=0, current_trade_bet=0, current_suit = -1 WHERE id = 14", $link);
      $result = mysql_query("UPDATE players SET current_trade_bet=-1 WHERE room_id = ".$roomId, $link);
      $result = mysql_query("UPDATE players SET my_number = 2 WHERE id = 9", $link);
      $result = mysql_query("UPDATE players SET my_number = 3 WHERE id = 10", $link);
      mysql_query("");
    }
    
		$result = mysql_query("SELECT * FROM rooms", $link);
		$roomsNumber = 0;
		$message = "";
		while ($row = mysql_fetch_assoc($result)) {
			$roomsNumber++; // only basic info here. If user wants to know all info 
							// about room, he will request info.
			$roomId = (string)$row["id"];
			$playersNumber = (string)$row["players_number"];
			$gameBet = (string)$row["game_bet"];
			$message = $message." ".$roomId." ".$playersNumber." ".$gameBet;   
		}
		$message = trim($message);
		$roomsNumber = (string)$roomsNumber;
		$message = $roomsNumber." ".$message;
    
		$activityAddress   = "2"; // rooms activity
		$m_type = "0";
		$response = sendMessage($regId, $message, $m_type, $activityAddress);
    appendLog("User ".$id." was sent message with existing rooms info. The response is: ".$response."\r\nRegid is ".$regId);   
		break; 

	case "connect_to_existing": 
		$roomId = $_POST["room_id"];
    appendLog("User ".$id." decided to connect to existing room ".$roomId);
    
    $result = mysql_query("SELECT id FROM players WHERE reg_id = '".$regId."'", $link);
    $playerId = 0;
    if ($row = mysql_fetch_assoc($result)) 
    	$playerId = $row["id"];
    
		$result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    $row = mysql_fetch_assoc($result);
		$message = "2 ".$roomId." 0";
    $playersNumber = $row["players_number"];
    $playerNextId = 0;
    $playerPrevId = 0;
    
		if (1) {
			if ($playersNumber == 3)          // no free space, go away
				$message = "1 ".$roomId." 0";   
			else
			{ 
        // add info to database that new user has entered this room
        // this means edit player1 or player2 or... (depends on what is NULL)
        // and also edit players_number: increase it
        // and notify all users in this room that a new user has come
        $ownNumber = "1";
        $newPlayersNumber = $row["players_number"] + 1;
        if ($row["player1"] === NULL) 
        {
          $result = mysql_query("UPDATE rooms SET player1 = '".$playerId."' WHERE id = '".$roomId."'", $link);
          $result = mysql_query("UPDATE players SET my_number = 1, room_id = ".$roomId." WHERE id = ".$playerId, $link); 
          file_put_contents("Test/result.txt", $result); 
          if (!($row["player2"] === NULL))
             notifyNewPlayerAppeared($row["player2"], (string)$playerId, $newPlayersNumber, 1);
          if (!($row["player3"] === NULL))
             notifyNewPlayerAppeared($row["player3"], (string)$playerId, $newPlayersNumber, 1);
          $playerNextId = $row["player2"];
          $playerPrevId = $row["player3"];
        }
        else if ($row["player2"] === NULL) 
        {
          $ownNumber = "2";
          $result = mysql_query("UPDATE rooms SET player2 = '".$playerId."' WHERE id = '".$roomId."'", $link); 
          mysql_query("UPDATE players SET my_number = 2, room_id = ".$roomId." WHERE id = ".$playerId, $link);
          notifyNewPlayerAppeared($row["player1"], (string)$playerId, $newPlayersNumber, 2);
          if (!($row["player3"] === NULL))
             notifyNewPlayerAppeared($row["player3"], (string)$playerId, $newPlayersNumber, 2);
          $playerNextId = $row["player3"];
          $playerPrevId = $row["player1"];
          appendLog("User ".$id." will be second!");
        }
        else
        {
          $ownNumber = "3";
          $result = mysql_query("UPDATE rooms SET player3 = '".$playerId."' WHERE id = '".$roomId."'", $link);  
          mysql_query("UPDATE players SET my_number = 3, room_id = ".$roomId." WHERE id = ".$playerId, $link);
          notifyNewPlayerAppeared($row["player1"], (string)$playerId, $newPlayersNumber, 3); 
          notifyNewPlayerAppeared($row["player2"], (string)$playerId, $newPlayersNumber, 3);
          $playerNextId = $row["player1"];
          $playerPrevId = $row["player2"];
        }   

        //uncomment then!
        $result = mysql_query("UPDATE rooms SET players_number = '".$newPlayersNumber."' WHERE id = '".$roomId."'", $link);
				$message = "0 ".$roomId." ".$ownNumber; 
			}
      
		}
		
		$activityAddress = ReceiverIds::ROOMS_ACTIVITY; // rooms activity
		$m_type = "1";    // new player appeared
    
    appendLog("Sending message = ".$message);
		$response = sendMessage($regId, $message, $m_type, $activityAddress);
    appendLog("response is = ".$response);
    if ($newPlayersNumber == 3) { // ask if players are ready to play?
      appendLog("User ".$id." connected to room and now there are 3 players. The game is about to start!");
      srand();
      $shuffler = rand(1, 3);
      $result = mysql_query("UPDATE players SET current_trade_bet = 0 WHERE room_id = ".$roomId, $link);    
      
      
      if (isDebug()) {  
        if ($roomId == 14) { // !!! DEBUG VERSION: shuffler is always 1 so 2 will be active player
          appendLog("User ".$id." connected to 14th room so he is next player after the shuffler (DEBUG!)");
          $shuffler = 3;
        }
      } else
        appendLog("room Id is ".$roomId." and isDebug()=".isDebug()." so it's Not debug!");
      
      $result = mysql_query("UPDATE rooms SET shuffler = ".$shuffler.", game_state = 1, cards_on_table = 0 WHERE id = '".$roomId."'", $link);
      $result = mysql_query("UPDATE players SET my_bullet = 0, my_mountain = 0, my_whists_left = 0, my_whists_right = 0, my_current_role = -1, current_trade_bet = -1, stopped_trading = 0, my_tricks = 0 WHERE room_id = ".$roomId, $link);
      // change this then to just init new distribution function call
      newShuffle($roomId);
    }   
		break;
    
	case "all_data_about_room":
		$roomId = $_POST["room_id"];
    appendLog("User ".$id." asked about all data about room ".$roomId);
		$result = mysql_query("SELECT * FROM rooms WHERE id = '".$roomId."'", $link);
    
		$message = "";
		if ($row = mysql_fetch_assoc($result)) {
			  $roomName = (string)$row["room_name"];
        $playersNumber = (string)$row["players_number"];
        $player1 =  (string)$row["player1"];
        $player2 =  (string)$row["player2"];
        $player3 =  (string)$row["player3"];
        $gameType = (string)$row["game_type"];
        $gameBet = (string)$row["game_bet"];
        $gameBullet = (string)$row["game_bullet"];
        $stalingrad =  (string)$row["Stalingrad"];
        
        $result = mysql_query("SELECT * FROM game_types WHERE id = '".$row["game_type_id"]."'", $link);
        $gameTypeRow = mysql_fetch_assoc($result);
        $gameType = $gameTypeRow["name"];
        
        $result =  mysql_query("SELECT * FROM players WHERE id = '".$player1."'", $link);
        $playerRow1 = mysql_fetch_assoc($result);
        $result =  mysql_query("SELECT * FROM players WHERE id = '".$player2."'", $link);
        $playerRow2 = mysql_fetch_assoc($result);
        $result =  mysql_query("SELECT * FROM players WHERE id = '".$player3."'", $link);
        $playerRow3 = mysql_fetch_assoc($result);
        
        if ($player1 == NULL)
        {
          $player1 = -1;
          $name1 = "";
        } else 
          $name1 =  $playerRow1["name"];
        if ($player2 == NULL) {
          $player2 = -1;
          $name2 = "";
        } else 
          $name2 =  $playerRow2["name"];
        if ($player3 == NULL) {
          $player3 = -1;
          $name3 = "";
        } else 
          $name3 =  $playerRow3["name"];
          
        $message = $roomName." ".$playersNumber;
        $message = $message." ".$player1." ".$player2." ".$player3;
        $message = $message." ".$name1." ".$name2." ".$name3;
        $message = $message." ".$gameType." ".$gameBet." ".$gameBullet." ".$stalingrad;
		}

		$activityAddress   = "3"; // game activity
		$m_type = "0";
		$response = sendMessage($regId, $message, $m_type, $activityAddress);
		break;
	}                
 	closeMysqlConnection($link);
	
?>