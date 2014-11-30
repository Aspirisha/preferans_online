<?php
  include 'Connection.php';
	include 'mysql.php';
	
    $gameTypeName = $_POST["game_type"]; // Sochi/rostov/etc
    $gameType = 1; // hardcode now!!!
    $gameBet = (int)$_POST["game_bet"];
    $gameBullet = (int)$_POST["game_bullet"];
    $regId = $_POST["reg_id"];
    $isPrivate = $_POST["is_private"];
	$enableStalingrad = (int)$_POST["enable_stalingrad"];
    $playersNumber = 1; // now it's only game creator
	$roomName = "New_room";
	
	$filename="Test/qq.txt";
	// write (append) the data to the file
	file_put_contents($filename, $enableStalingrad);
	
  $link = openMysqlConnection();    
	$result = mysql_query("SELECT id FROM players WHERE reg_id = '".$regId."'", $link);
	$playerId = 0;
	if ($row = mysql_fetch_assoc($result)) 
		$playerId = $row["id"];
	
	
	$result = mysql_query("INSERT INTO rooms (room_name, players_number, player1, game_type_id, game_bet, game_bullet, Stalingrad) VALUES ('".$roomName."', '".$playersNumber."', '".$playerId."', '".$gameType."', '".$gameBet."', '".$gameBullet."', '".$enableStalingrad."')", $link); 
	$newId = mysql_insert_id();
  if (!$result) {
	   file_put_contents($filename, "errr");
	   //send to android app errorcode 1
	} else { // send ok
    mysql_query("UPDATE players SET room_id = ".$newId.", my_number = 1 WHERE id = $playerId");
    file_put_contents($filename, 11);
	  $message = "0 ".$newId;
		$activityAddress = "1"; // new room activity
		$m_type = "1";
		sendMessage($regId, $message, $m_type, $activityAddress);
	}
	closeMysqlConnection($link);
?>