package com.prefserver.model;

import java.util.Date;

public class Player {

	private long id;
	private Long roomId = null;
	private String name = "";
	private String password = "";
	private Date joiningDate;
	private int coins;
	private String regId = "";
	private boolean isOnline = false;
	private String cards = "";
	private Integer timeLeft = null;
	private Integer myNumber = null;
	
	public Player() { }
	public Player(String _name, String _password, int _coins, String _regID) {
		name = _name;
		password = _password;
		coins = _coins;
		joiningDate = new Date();
		regId = _regID;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getJoiningDate() {
		return joiningDate;
	}

	public void setJoiningDate(Date joiningDate) {
		this.joiningDate = joiningDate;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", name=" + name + ", joiningDate="
				+ joiningDate + ", coins=" + coins + "]";
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	public void setRegId(String regID) {
		this.regId = regID;
	}
	public String getRegId() {
		return regId;
	}
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public boolean getIsOnline() {
		return isOnline;
	}
	public void setCards(String cards) {
		this.cards = cards;
	}
	public String getCards() {
		return cards;
	}
	public void setTimeLeft(Integer timeLeft) {
		this.timeLeft = timeLeft;
	}
	public Integer getTimeLeft() {
		return timeLeft;
	}
	public void setMyNumber(Integer myNumber) {
		this.myNumber = myNumber;
	}
	public Integer getMyNumber() {
		return myNumber;
	}
	
	
	

}
