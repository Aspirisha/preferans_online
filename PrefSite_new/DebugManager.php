<?php  
  $__DEBUG = true;   
  $__DEBUG_ACTIVE_ID = 26;
  $__DEBUG_ACTIVE_REG = "APA91bFmtbGSNd35KqKE1Wp5XNN0UysWzRkdUhciZsEH5wjTBtCX9mZouTJd87kWettTEn1DJ2vzNFmn1v4wvIL1mWyq_slwUFTBbXfzTDSaYUqa3epryn3grnR6c4xgcflHMUgOCXIAwKNnVVbZAL9U_mE-HYz_rPuPH7uC5uNc1pPFJET6wdM";
  $__DEBUG_ACTIVE_NAME = "Andy";
  
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
    file_put_contents("Test/Log.txt", $string."\r\n\r\n", FILE_APPEND);
    
  }
  
  // this function will be called each time keepAlive appears
  function BotMove($roomId) {
  
  }

?>