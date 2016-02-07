package ru.springcoding.prefomega;

public class CommonEnums {

public enum RecieverID {
	    ENTRY_ACTIVITY,
	    NEW_ROOM_ACTIVITY,
	    ROOMS_ACTIVITY,
	    GAME_ACTIVITY,
	    SETTINGS_ACTIVITY,
	    PING,
	    KEEP_ALIVE,
	    PING_ANSWER
	}

/// message type is for messages, that server sends to client
public enum MessageTypes {
	ENTRY_ONLINE_NOTIFICATION_ANSWER,
	ENTRY_REGISTRATION_RESULT,
	ENTRY_LOGIN_RESULT,
	
	ROOMS_EXISTING_ROOMS,
	ROOMS_NEW_ROOM_CREATION_RESULT,
	ROOMS_CONNECTION_RESULT,

	KEEP_ALIVE_ANSWER,
	NEED_REGID_UPDATE,
	AUTHORIZATION_RESULT
}

public enum GameType {
	SOCHI,
	LENINGRAD,
	ROSTOV
}

/// Notification types are for notifications, that client
/// sends to server
public enum Notifications {
	ONLINE,
	KEEP_ALIVE,
	LOGOUT,
	EXITED_ROOM
}

/// Requests type are for requests, that client sends to 
/// server. Difference between notification and request is that 
/// when sending request, we suppose server will answer smth,
/// while notification just notifies server
public enum Requests {
	PING,
	REGISTER,
	TRY_REGISTER,
	TRY_LOGIN,
	EXISTING_ROOMS,
	CONNECT_TO_EXISTING_ROOM,
	
}

}