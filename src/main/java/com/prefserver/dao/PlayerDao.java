package com.prefserver.dao;

import java.util.List;

import com.prefserver.model.Player;

public interface PlayerDao {

	Long addNewPlayer(String name, String password, int coins, String regID);
	
	void deletePlayerById(Long id);
	
	Player findPlayerByID(int id);
	
	Player findPlayerByName(String name);
	
	List<Player> findPlayerByRoomId(long roomId);
}
