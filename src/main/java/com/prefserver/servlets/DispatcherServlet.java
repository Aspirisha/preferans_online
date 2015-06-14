package com.prefserver.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.prefserver.dao.PlayerDao;
import com.prefserver.dao.PlayerDaoImpl;
import com.prefserver.dao.RoomDao;
import com.prefserver.dao.RoomDaoImpl;
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
	
	static {
		Map<String, Integer> tmp = new HashMap<String, Integer>();
		tmp.put("register", 1);
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
	   
		if (req == "request")
			processRequest(request, response);
		else if (req == "notification")
			processNotification(request, response);
		
		PrintWriter out = response.getWriter();
		if (req == null)
			out.println("Hello World");
		else
			out.println(ipAddress);
		//Player p = playerDao.findPlayerByID(1);
		if (reg_id != null) {
		/*	HttpClient httpclient = HttpClients.custom().build();

			
			ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("message", "Hello bitch!"));
			data.add(new BasicNameValuePair("registration_ids", "[" + reg_id + "]"));
			
			HttpPost httppost = new HttpPost("https://android.googleapis.com/gcm/send");
			httppost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			httppost.setHeader(HttpHeaders.AUTHORIZATION, "key=AIzaSyAhL2EX96bgmKQSgvwKCrCZjTAwsGzrHNM");
			
			
           // httppost.setEntity(new UrlEncodedFormEntity(data));
            HttpResponse resp = httpclient.execute(httppost);*/
            
		}
	}
	
	void processRequest(HttpServletRequest request, HttpServletResponse response) {
		String req = request.getParameter("request");
		if (req == null) {
			return;
		}
		
		switch (requestsHash.get(req)) {
		case 1:
			processRegisterRequest(request, response);
			break;
		default:
			break;
		}
	}
	
	void processRegisterRequest(HttpServletRequest request, HttpServletResponse response) {
		String login = request.getParameter("login");
		String password = request.getParameter("password");
		
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch (regChecker.canBeRegistered(login, password)) {
		case NAME_EXISTS:
			out.println("Name already exists");
			break;
		case NAME_WRONG_FORMAT:
			out.println("Name wrong format");
			break;
		case PASSWORD_WRONG_FORMAT:
			out.println("Password wrong format");
			break;
		case OK:
			Long id = playerDao.addNewPlayer(login, password, DEFAULT_COINS);
			if (id == null)
				out.println("Error while registering");
			else
				out.println("Successfully Registered! ID is " + id.toString());
			break;
		}
	}
	
	void processNotification(HttpServletRequest request, HttpServletResponse response) {
		
	}

}
