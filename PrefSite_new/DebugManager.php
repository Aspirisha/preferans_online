<?php  
  $__DEBUG = true;   
  $__DEBUG_ACTIVE_ID = 31;
  $__DEBUG_ACTIVE_REG = "APA91bF90Olwo7cQi3kTPCBbI18rRUkz1bqZJrIrpfeXkJ_5hzcFdPzgOyG-nQ00LzQ2urVQsniKfPy-J_rfh4VVlzrghqfP41glA7imR07uvBOILz0gHFx0szu_hAwYTYKdCjr1fhAC4mPf8BNPsnIsQYXGQGkYOB7kzzHWOS2O0urnuuU6LYU";  
  $__DEBUG_ACTIVE_NAME = "tester";
  
  // returns id of active player during debug session or -1 if it's release mode
  function getDebugActiveId() {
    global $__DEBUG_ACTIVE_ID;
    global $__DEBUG;
    
    if ($__DEBUG)
      return $__DEBUG_ACTIVE_ID;
      
    return -1; // not debug mode so return impossible id
  }
  
  function getDebugActiveReg() {
    global $__DEBUG_ACTIVE_REG;
    global $__DEBUG;
    
    if ($__DEBUG)
      return $__DEBUG_ACTIVE_REG;
      
    return ""; // not debug mode so return impossible regid
  }
  
  function isDebug() {
    global $__DEBUG;
    return $__DEBUG;
  }
  
  function getDebugActiveName() {
    global $__DEBUG_ACTIVE_NAME;
    global $__DEBUG;
    
    if ($__DEBUG)
      return $__DEBUG_ACTIVE_NAME;
      
    return ""; // not debug mode so return impossible name
  }    
  
  function appendLog($string) {
    file_put_contents("Log.txt", date("Y-m-d H:i:s ").$string."\r\n\r\n", FILE_APPEND);  
  }
  
  function clearLog() {
    $fh = fopen("Log.txt", 'w');
    fclose($fh);
  }
  
  // this function will be called each time keepAlive appears
  function BotMove($roomId) {
  
  }

?>