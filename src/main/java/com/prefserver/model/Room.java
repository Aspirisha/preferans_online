package com.prefserver.model;


public class Room {

	private long id;
	private String name;
	private String password;
	private int bullet;
	private byte gameType;
	private byte playersNumber;
	private float whistCost;
	private String raspExit;
	private String raspProgression;
	private boolean withoutThree;
	private boolean noWhistRaspExit;
	private boolean stalingrad;
	private boolean tenWhist;
	
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

	public int getBullet() {
		return bullet;
	}

	public void setBullet(int bullet) {
		this.bullet = bullet;
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
				+ password + ", bullet=" + bullet + ", game_type=" +gameType + "]";
	}

	public void setPlayersNumber(byte playersNumber) {
		this.playersNumber = playersNumber;
	}

	public byte getPlayersNumber() {
		return playersNumber;
	}

	public void setWhistCost(float whistCost) {
		this.whistCost = whistCost;
	}

	public float getWhistCost() {
		return whistCost;
	}

	public void setRaspExit(String raspExit) {
		this.raspExit = raspExit;
	}

	public String getRaspExit() {
		return raspExit;
	}

	public void setRaspProgression(String raspProgression) {
		this.raspProgression = raspProgression;
	}

	public String getRaspProgression() {
		return raspProgression;
	}

	public void setWithoutThree(boolean withoutThree) {
		this.withoutThree = withoutThree;
	}

	public boolean isWithoutThree() {
		return withoutThree;
	}

	public void setNoWhistRaspExit(boolean noWhistRaspExit) {
		this.noWhistRaspExit = noWhistRaspExit;
	}

	public boolean isNoWhistRaspExit() {
		return noWhistRaspExit;
	}

	public void setStalingrad(boolean stalingrad) {
		this.stalingrad = stalingrad;
	}

	public boolean isStalingrad() {
		return stalingrad;
	}

	public void setTenWhist(boolean tenWhist) {
		this.tenWhist = tenWhist;
	}

	public boolean isTenWhist() {
		return tenWhist;
	}
	
	
	
}
