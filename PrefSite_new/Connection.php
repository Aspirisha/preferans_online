<?php

function sendNotification($registrationIdsArray, $messageData)
{   
    $apiKey = "AIzaSyBNk5c2dMLc2G7_UeleuVW5_C9mezqWB3c";
    $headers = array("Content-Type:" . "application/json", "Authorization:" . "key=" . $apiKey);
    $data = array(
        'data' => $messageData,
        'registration_ids' => $registrationIdsArray
    );
 
    $ch = curl_init();
 
    curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers ); 
    curl_setopt( $ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send" );
    curl_setopt( $ch, CURLOPT_SSL_VERIFYHOST, 0 );
    curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, 0 );
	curl_setopt( $ch, CURLOPT_POST, true );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
    curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode($data) );
 
    $response = curl_exec($ch);
    curl_close($ch);
 
    return $response;
}

// send Message to 1 android
/*
 * Available recievers currently are:
 * 0: EntryActivity
 *    message types:
 *    0: message contains players id in database
 * 1: NewRoomActivity
 *    message types:  
 *    0: message contains amount of users money
 *    1: message contain error code for room creation. If it's 0, then new room is created
 *       and user sees GameActivity
 * 2: RoomsActivity
 *    message types:
 *    0: message contains all available data about room. This is sent when user has
 *       just connected to the room
 *    1: message contains answer about connection success. If it's 0, then ok, 
 *       user is connected. If it's 1, then room is full, son throw user back to 
 *       roomsActivity. If it's 2, room doesn't exist anymore and do the same as in 1.
 * 3: GameActivity
 *    message types:
 *                 
 */
function sendMessage($regId, $msg, $msgType, $receiver)
{   
	$regArray = array($regId);
    $messageData = array('message' => $msg, 'receiver' => $receiver, "messageType" => $msgType);
    $apiKey = "AIzaSyBNk5c2dMLc2G7_UeleuVW5_C9mezqWB3c";
    $headers = array("Content-Type:" . "application/json", "Authorization:" . "key=" . $apiKey);
    $data = array(
        'data' => $messageData,
        'registration_ids' => $regArray
    );
 
    $ch = curl_init();
 
    curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers ); 
    curl_setopt( $ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send" );
    curl_setopt( $ch, CURLOPT_SSL_VERIFYHOST, 0 );
    curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, 0 );
	  curl_setopt( $ch, CURLOPT_POST, true );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
    curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode($data) );
 
    $response = curl_exec($ch);
    curl_close($ch);
 
    return $response;
}
?>