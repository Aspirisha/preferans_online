<?php

  abstract class ReceiverIds {
    const ENTRY_ACTIVITY = 0;
    const NEW_ROOM_ACTIVITY = 1;
    const ROOMS_ACTIVITY = 2;
    const GAME_ACTIVITY = 3;
    const SETTINGS_ACTIVITY = 4;
    const PING = 100;
    const KEEP_ALIVE = 777;
  }
  
  abstract class GameStates {
    const GAME_NOT_STARTED = 0;
    const TALON_TRADING = 1;
    
  }
  
  abstract class EntryActivityMessageTypes {
    const ONLINE_NOTIFICATION_ANSWER = 0;
  }
  
  abstract class NewRoomActivityMessageTypes {
    const BLA = 0;
  }
  
  abstract class RoomsActivityMessageTypes {
    const BLA = 0;
  }
  
  abstract class GameActivityMessageTypes {
    const CARDS_INFO = 2;
    const ACTIVE_PLAYER_THROWN_CARDS = 6;
  }
  
  abstract class SettingsActivityMessageTypes {
    const BLA = 0;
  }
  
  

?>