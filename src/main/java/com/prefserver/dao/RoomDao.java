package com.prefserver.dao;

import java.util.List;

import com.prefserver.model.Room;

public interface RoomDao {
	void addNewRoom(Long id, String name, String password, int bet, byte gameType);
	
	List<Room> listAllRooms();
	
	Room findRoomByID(Long id);
	
	Room findRoomByName(Long name);
	
	void deleteRoomByName(String name);
	
	void deleteRoomByID(Long id);
}
