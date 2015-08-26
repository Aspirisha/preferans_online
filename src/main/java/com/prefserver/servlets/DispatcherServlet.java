package com.prefserver.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import ru.springcoding.common.CommonEnums.MessageTypes;
import ru.springcoding.common.CommonEnums.RecieverID;
import ru.springcoding.common.RoomInfo;
import ru.springcoding.common.Serializer;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.prefserver.dao.PlayerDao;
import com.prefserver.dao.PlayerDaoImpl;
import com.prefserver.dao.RoomDao;
import com.prefserver.dao.RoomDaoImpl;
import com.prefserver.model.Player;
import com.prefserver.model.Room;
import com.prefserver.utility.HashTables;
import com.prefserver.utility.RegistrationChecker;
/**
 * Servlet implementation class Dispatcher
 */
@WebServlet("/Dispatcher")
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Map<String, Integer> requestsHash;

	private static RegistrationChecker regChecker = new RegistrationChecker();
	private static final int DEFAULT_COINS = 100;
	
	private PlayerDao playerDao = new PlayerDaoImpl();
	private RoomDao roomDao = new RoomDaoImpl();
	private static final String GOOGLE_SERVER_KEY = "AIzaSyAhL2EX96bgmKQSgvwKCrCZjTAwsGzrHNM";
	
	private Serializer serializer = new Serializer();
	
	static {
		Map<String, Integer> tmp = new HashMap<String, Integer>();
		tmp.put("register", 1);
		tmp.put("existing_rooms", 2);
		tmp.put("ping", 0);
		requestsHash = tmp;
	}

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DispatcherServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String reg = request.getParameter("reg_id");
		PrintWriter out = response.getWriter();
		Map<String, String[]> params = request.getParameterMap();
		if (params != null) {
			out.println("not null!");
			for (String k : params.keySet()) {
				for (String v : params.get(k)) {
					out.println(v + " -> " + k);
				}
			}
		}
		if (reg == null)
			out.println("Hello Jesus!");
		else
			out.println(reg);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String req = request.getParameter("request_type");
		String reg_id = request.getParameter("reg_id");
	   //is client behind something?
	   String ipAddress = request.getHeader("X-FORWARDED-FOR");  
	   if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
	   }

		if (req.equals("request")) {
			processRequest(request, response);
		} // TODO fix this shit
		else if (req.equals("notification"))
			processNotification(request, response);
		
		PrintWriter out = response.getWriter();
		if (req == null)
			out.println("Hello World");
		else
			out.println(ipAddress);

		if (reg_id != null) {
            //foo(request, response, reg_id);
		}
	}
	
	void sendData(List<String>regIDs, Map<String, String> data) {
		try {
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).setData(data)
					.build();
			MulticastResult result = sender.send(message, regIDs, 1);
			System.out.println(result.toString());
			//request.setAttribute("pushStatus", result.toString());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			//request.setAttribute("pushStatus",
					//"RegId required: " + ioe.toString());
		} catch (Exception e) {
			e.printStackTrace();
			//request.setAttribute("pushStatus", e.toString());
		}
	}
	
	void sendData(String regID, Map<String, String> data) {
		List<String> regIDs = new ArrayList<String>();
		regIDs.add(regID);
		sendData(regIDs, data);
	}
	
	void sendData(String regID, String msg, RecieverID recID, MessageTypes msgType) {
		List<String> regIDs = new ArrayList<String>();
		regIDs.add(regID);
		Map<String, String> data = new HashMap<String, String>();
		data.put("message", msg);
		data.put("receiver", recID.toString());
		data.put("messageType", msgType.toString());
		sendData(regIDs, data);
	}
	
	void processRequest(HttpServletRequest request, HttpServletResponse response) {
		String req = request.getParameter("request");
		
		if (req == null) 
			return;
		
		System.out.println(req);
		switch (requestsHash.get(req)) {
		case 0:
			processPingRequest(request, response);
			break;
		case 1:
			processRegisterRequest(request, response);
			break;
		case 2:
			processExistingRoomsRequest(request, response);
			break;
		default:
			break;
		}
	}
	
	void processPingRequest(HttpServletRequest request, HttpServletResponse response) {
		String reg_id = request.getParameter("reg_id");
		String password = request.getParameter("password");		
		String login = request.getParameter("login");
		
		sendData(reg_id, "", RecieverID.PING_ANSWER, MessageTypes.KEEP_ALIVE_ANSWER);
	}
		
	
	void processRegisterRequest(HttpServletRequest request, HttpServletResponse response) {
		String login = request.getParameter("login");
		String password = request.getParameter("password");
		String reg_id = request.getParameter("reg_id");
		
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(login);
		switch (regChecker.canBeRegistered(login, password)) {
		case NAME_EXISTS:
			sendData(reg_id, "Name already exists", RecieverID.ENTRY_ACTIVITY, 
					MessageTypes.ENTRY_REGISTRATION_RESULT);
			System.out.println("Name already exists");
			break;
		case NAME_WRONG_FORMAT:
			sendData(reg_id, "Name wrong format", RecieverID.ENTRY_ACTIVITY, 
					MessageTypes.ENTRY_REGISTRATION_RESULT);
			System.out.println("Name wrong format");
			break;
		case PASSWORD_WRONG_FORMAT:
			sendData(reg_id, "Password wrong format", RecieverID.ENTRY_ACTIVITY, 
					MessageTypes.ENTRY_REGISTRATION_RESULT);
			System.out.println("Password wrong format");
			break;
		case OK:
			System.out.println("OK");
			Long id = playerDao.addNewPlayer(login, password, DEFAULT_COINS, reg_id);
			if (id == null)
				sendData(reg_id, "Error while registering", RecieverID.ENTRY_ACTIVITY, 
						MessageTypes.ENTRY_REGISTRATION_RESULT);
			else {
				String data = id.toString() + " " + login + " " + password;
				sendData(reg_id, data, RecieverID.ENTRY_ACTIVITY, 
						MessageTypes.ENTRY_REGISTRATION_RESULT);
			}
			break;
		}
	}
	
	void processExistingRoomsRequest(HttpServletRequest request, 
			HttpServletResponse response) {
		List<Room> rooms = roomDao.listAllRooms();
		int id = Integer.parseInt(request.getParameter("id"));
		String password = request.getParameter("password");
		String reg_id = playerDao.findPlayerByID(id).getRegId();
		LinkedList<RoomInfo> roomsInfo = new LinkedList<RoomInfo>(); 
		System.out.println(reg_id);
		
		assert(rooms != null);
		
		for (Room r : rooms) {
			RoomInfo ri = new RoomInfo();
			ri.bullet = r.getBullet();
			ri.gameType = HashTables.GameTypes.get(r.getGameType());
			ri.hasPassword = (r.getPassword() == null);
			ri.id = (int) r.getId();
			ri.name = r.getName();
			ri.noWhistRaspasyExit = r.isNoWhistRaspExit();
			ri.playersNumber = r.getPlayersNumber();
			ri.raspExit = new int[3];
			
			String[] data = r.getRaspExit().split(" ");
			for (int i = 0; i < 3; i++)
				ri.raspExit[i] = Integer.parseInt(data[i]);
			
			ri.raspProgression = new int[3];
			data = r.getRaspProgression().split(" ");
			for (int i = 0; i < 3; i++)
				ri.raspProgression[i] = Integer.parseInt(data[i]);
			
			ri.stalingrad = r.isStalingrad();
			ri.tenWhist = r.isTenWhist();
			ri.whistCost = r.getWhistCost();
			ri.withoutThree = r.isWithoutThree();
			roomsInfo.add(ri);
		}
		
		int tries = 0;
		boolean sent = false;
		Gson gson = new Gson();
		String s = gson.toJson(roomsInfo);
		
		do {
			System.out.println(s);
			sendData(reg_id, s, RecieverID.ROOMS_ACTIVITY, 
					MessageTypes.ROOMS_EXISTING_ROOMS);
			sent = true;
			System.out.println("Sent rooms data.");
		} while (!sent && tries < 3);
		
		if (!sent) {
			sendData(reg_id, "ERROR", RecieverID.ROOMS_ACTIVITY, 
					MessageTypes.ROOMS_EXISTING_ROOMS);
		}
	}
	
	void processNotification(HttpServletRequest request, 
			HttpServletResponse response) {
		String notification = request.getParameter("notification");
		String playerName = request.getParameter("login");
		String regID = request.getParameter("reg_id");
		if (notification.equals("online")) {
			System.out.println("Player is online!");
		} else if (notification.equals("keep_alive")) {
			System.out.println("keep alive");
			Player p = playerDao.findPlayerByName(playerName);
			String answer = ""; // empty if player is not in room
			
			Long roomId = p.getRoomId();
			if (roomId != null) {
				List<Player> pl = playerDao.findPlayerByRoomId(roomId);
				answer = Long.toString(System.currentTimeMillis()); // TODO change to current server time
				for (int i = 1; i <= 3; i++) {
					for (Player player : pl) {
						if (player.getMyNumber() == i)
							answer += " " + player.getTimeLeft();
					}
				}
			}
			
			sendData(regID, answer, RecieverID.KEEP_ALIVE, 
					MessageTypes.KEEP_ALIVE_ANSWER);
		} else {
			System.out.println("some notification: " + notification);
		}
	}

}
