package com.prefserver.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.jboss.logging.Logger;

import com.prefserver.model.Player;

public class PlayerDaoImpl implements PlayerDao{
	private static SessionFactory factory; 
	private static Configuration configuration;
	private static ServiceRegistry serviceRegistry; 
	private static final Logger log = Logger.getLogger(SessionFactory.class);

	public static void createSessionFactory() {
	    Configuration configuration = new Configuration();
	    configuration.configure();
	    configuration.addClass(Player.class);
	    serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
	            configuration.getProperties()).build();
	    factory = configuration.buildSessionFactory(serviceRegistry);
	}
	
	static {
        createSessionFactory();
    }
	
	public Long addNewPlayer(String name, String password, int coins, String regID) {
		Session session = factory.openSession();
		Transaction tx = null;
		Long playerID = null;
		try{
			tx = session.beginTransaction();
			Player player = new Player(name, password, coins, regID);
			playerID = (Long) session.save(player); 
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
		return playerID;
	}

	public void deletePlayerById(Long id) {
		Session session = factory.openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			Player player = (Player)session.get(Player.class, id); 
			session.delete(player); 
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
	}

	public Player findPlayerByID(int id) {
		Session session = factory.openSession();
		Transaction tx = null;
		Player player = null;
		try{
			tx = session.beginTransaction();
			player = (Player) session.get(Player.class, (long) id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
		return player;
	}

	public Player findPlayerByName(String name) {
		Session session = factory.openSession();
		List players = session.createCriteria(Player.class)
	    .add(Restrictions.eq("name", name))
	    .list();
		System.out.println(name);
		if (players.size() != 1)
			return null;
		return (Player) players.get(0);
	}

	public List<Player> findPlayerByRoomId(long roomId) {
		Session session = factory.openSession();
		List players = session.createCriteria(Player.class)
	    .add(Restrictions.eq("roomId", roomId))
	    .list();
		return players;
	}

	
}
