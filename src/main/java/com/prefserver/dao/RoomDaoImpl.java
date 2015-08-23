package com.prefserver.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.jboss.logging.Logger;

import com.prefserver.model.Player;
import com.prefserver.model.Room;


public class RoomDaoImpl implements RoomDao {
	private static SessionFactory factory; 
	private static Configuration configuration;
	private static ServiceRegistry serviceRegistry; 
	private static final Logger log = Logger.getLogger(SessionFactory.class);

	public static void createSessionFactory() {
	    Configuration configuration = new Configuration();
	    configuration.configure();
	    configuration.addClass(Room.class);
	    serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
	            configuration.getProperties()).build();
	    factory = configuration.buildSessionFactory(serviceRegistry);
	}
	
	static {
        createSessionFactory();
    }
	
	public void addNewRoom(Long id, String name, String password, int bet,
			byte gameType) {
		// TODO Auto-generated method stub
		
	}

	public List<Room> listAllRooms() {
		Session session = factory.openSession();
		List<Room> rooms = session.createCriteria(Room.class).list();
		
		return rooms;
	}

	public Room findRoomByID(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Room findRoomByName(Long name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteRoomByName(String name) {
		// TODO Auto-generated method stub
		
	}

	public void deleteRoomByID(Long id) {
		// TODO Auto-generated method stub
		
	}

}
