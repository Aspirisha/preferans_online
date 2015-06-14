package com.prefserver.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.prefserver.model.Player;

public class PlayerDaoImpl implements PlayerDao{
	private static SessionFactory factory; 
	
	public Long addNewPlayer(String name, String password, int coins) {
		Session session = factory.openSession();
		Transaction tx = null;
		Long playerID = null;
		try{
			tx = session.beginTransaction();
			Player player = new Player(name, password, coins);
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
			player = (Player) session.get(Player.class, id);
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
		// TODO Auto-generated method stub
		return null;
	}

	public List<Player> findPlayerByRoomId(int roomId) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
