package com.prefserver.utility;

import com.prefserver.dao.PlayerDao;
import com.prefserver.dao.PlayerDaoImpl;
import com.prefserver.dao.RoomDao;
import com.prefserver.dao.RoomDaoImpl;

public class RegistrationChecker {
	public enum Problems {
		NAME_EXISTS,
		NAME_WRONG_FORMAT,
		PASSWORD_WRONG_FORMAT,
		OK
	}
	
	PlayerDao playerDao = new PlayerDaoImpl();
	RoomDao roomDao = new RoomDaoImpl();
	static final int MAX_NAME_LENGTH = 100;
	static final int MIN_PASSWORD_LENGTH = 8;
	static final int MAX_PASSWORD_LENGTH = 20;
	
	
	public RegistrationChecker() { }
	
	public Problems canBeRegistered(String name, String password) {
		if (!isValidName(name))
			return Problems.NAME_WRONG_FORMAT;
		if (!isValidPassword(password))
			return Problems.PASSWORD_WRONG_FORMAT;
		if (playerDao.findPlayerByName(name) != null) 
			return Problems.NAME_EXISTS;
		return Problems.OK;
	}
	
	private boolean isValidName(String name) {
		if (name == null)
			return false;
		if (name.isEmpty() || name.length() > MAX_NAME_LENGTH)
			return false;
		return true;
	}
	
	private boolean isValidPassword(String password) {
		if (password == null)
			return false;
		int len = password.length();
		if (len < MIN_PASSWORD_LENGTH || len > MAX_PASSWORD_LENGTH)
			return false;
		return true;
	}
}
