package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.projects.beans.User;


/**
 * Class UserDAO handles database interactions relative to the User table
 * @author marco
 *
 */
public class UserDAO {
	private Connection con;
/**
 * Creates a new UserDAO object
 * @param connection db connection
 */
	public UserDAO(Connection connection) {
		this.con = connection;
	}
/**
 * Checks if the credentials submitted are correct
 * @param usrn the username to check
 * @param pwd the password to check
 * @return null if the check failed, a User bean if the check succeeds
 * @throws SQLException if an error is encountered during the interaction with the db
 */
	public User checkCredentials(String usrn, String pwd) throws SQLException {
		String query = "SELECT  id, username, name, surname, mail FROM user  WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setMail(result.getString("mail"));
					return user;
				}
			}
		}
	}
	
	/**
	 * Checks if the username is unique
	 * @param usrn the username to check
	 * @return true if the username is unique
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public boolean checkUniqueNickName(String usrn) throws SQLException {
		String query = "SELECT id FROM user WHERE username = ?"; 
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(1, usrn);
			try (ResultSet result = pstatement.executeQuery();){
				//if there are no results (no one has that username) isBeforeFirst returns false -> the username is unique
				return !result.isBeforeFirst();		
			}
		}
	}
	/**
	 * Create a new Entry in the User Table
	 * @param usrn value of Username field
	 * @param psw value of Password field
	 * @param name value of Name field
	 * @param surname value of Surname field
	 * @param mail value of Mail field
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public void createUser(String usrn, String psw, String name, String surname, String mail) throws SQLException{
		String query = "INSERT INTO User VALUES(id, ?,?,?,?,?)";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(3, usrn);
			pstatement.setString(4, psw);
			pstatement.setString(1, name);
			pstatement.setString(2, surname);
			pstatement.setString(5, mail);
			pstatement.executeUpdate();
		}
	}
	
	}

