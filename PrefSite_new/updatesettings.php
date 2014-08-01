<?php

include 'Connection.php';
include 'mysql.php';

// this is the data coming from the Android app
$name = $_POST["new_name"]; 
$regid = $_POST["reg_id"];
// specify the file where we will save the contents of the variable message
$filename="Test/name.html";
// write (append) the data to the file
file_put_contents($filename,$regid);
//file_put_contents($filename,$name);
// load the contents of the file to a variable
$androidmessages = file_get_contents($filename);

// maybe thats shit?!
$link = openMysqlConnection();

// update username if he already exists. maybe use a password later
// it must be the only way cause if user sends data he must be already registered so he must exist in db
$result = mysql_query("SELECT * FROM players WHERE reg_id = '".$regid."'", $link);
if ($row = mysql_fetch_assoc($result)) {
   $result = mysql_query("UPDATE players SET name = '".$name."' WHERE reg_id = '".$regid."'", $link); 
   // send error msg to device if error occured
}
closeMysqlConnection($link);

// test feedback
$message      = "the test message";
$tickerText   = "2";
$contentTitle = "content title";
$contentText  = "content body";

 
$response = sendNotification( 
                array($regid), 
                array('message' => $message, 'tickerText' => $tickerText, 'contentTitle' => $contentTitle, "contentText" => $contentText) );
 
echo $response;
?>	