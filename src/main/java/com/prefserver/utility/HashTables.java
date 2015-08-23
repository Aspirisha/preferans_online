package com.prefserver.utility;

import java.util.HashMap;
import ru.springcoding.common.*;
public class HashTables {
	public static final HashMap<Integer, CommonEnums.GameType> 
	GameTypes = new HashMap<Integer, CommonEnums.GameType>();
	
	static {
		GameTypes.put(0, CommonEnums.GameType.LENINGRAD);
		GameTypes.put(1, CommonEnums.GameType.ROSTOV);
		GameTypes.put(2, CommonEnums.GameType.SOCHI);
	}
}
