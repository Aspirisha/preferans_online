package ru.springcoding.prefomega;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import ru.springcoding.prefomega.CommonEnums.GameType;

public class RoomInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7861623482261232546L;

	public static final int roomInfoFields = 13;
	
	public int id;
	public String name;
	public int bullet;
	public float whistCost;
	public GameType gameType;
	public int raspExit[];
	public int raspProgression[];
	public boolean withoutThree;
	public boolean noWhistRaspasyExit;
	public boolean stalingrad;
	public boolean tenWhist;
	public boolean hasPassword;
	public int playersNumber;
	
	private void writeObject(ObjectOutputStream out) {
		
	}
	
	private void readObject(ObjectInputStream in) {
		
	}
}
