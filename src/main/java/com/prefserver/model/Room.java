package com.prefserver.model;


public class Room {

	private long id;
	private String name;
	private String password;
	private int bet;
	private byte gameType;
	private byte playersNumber;
	
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}
	
	public byte getGameType() {
		return gameType;
	}

	public void setGameType(byte game_type) {
		this.gameType = game_type;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + ((password == null) ? 0 : password.hashCode());
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
		Room other = (Room) obj;
		if (id != other.id)
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", name=" + name + ", password="
				+ password + ", bet=" + bet + ", game_type=" +gameType + "]";
	}

	public void setPlayersNumber(byte playersNumber) {
		this.playersNumber = playersNumber;
	}

	public byte getPlayersNumber() {
		return playersNumber;
	}
	
	
	
}
