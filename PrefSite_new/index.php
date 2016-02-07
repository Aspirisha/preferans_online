<html>test page</html>
<?php
// get the "message" variable from the post request
// this is the data coming from the Android app
$message=$_POST["test"]; 
// specify the file where we will save the contents of the variable message
//print($message);
$filename="Test/androidmessages.html";
// write (append) the data to the file
file_put_contents($filename,$message);
// load the contents of the file to a variable
$androidmessages=file_get_contents($filename);
?>	