package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.projects.beans.AddressBook;

public class AddressBookDAO{
	private Connection con;
	
	public AddressBookDAO(Connection con) {
		this.con = con;
	}
	
	public void createContact(int ownerId, int accountId) throws SQLException, Exception {
		if(checkUniqueContact(ownerId,accountId)) {
				String query = "INSERT INTO CONTACT VALUES (?,?)";
				try(PreparedStatement pstatement = con.prepareStatement(query);){
					pstatement.setInt(1, ownerId);
					pstatement.setInt(2, accountId);
					pstatement.executeUpdate();
					
					con.commit();
				}
			}else {
				throw new Exception("Contact already present");
			}
	}
	
	
	public AddressBook getAddressBook(int ownerId) throws SQLException {
		String query = "SELECT CONTACTACCOUNT,A.USERID AS U FROM CONTACT C JOIN BANKACCOUNT A ON C.CONTACTACCOUNT=A.ID WHERE C.USERID=?";
		
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, ownerId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) 
					return null;
				else {
					AddressBook addressBook = new AddressBook();
					addressBook.setOwnerId(ownerId);
					while(result.next()) {
						addressBook.addContact(result.getInt("U"), result.getInt("CONTACTACCOUNT"));
					}
					
					return addressBook;
				}
			}
		}
	}
	
	public boolean checkUniqueContact(int ownerId, int accountId) throws SQLException {
		String query = "SELECT * FROM contact WHERE userId=? AND contactAccount=?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, ownerId);
			pstatement.setInt(2, accountId);
			try (ResultSet result = pstatement.executeQuery();){
				return !result.isBeforeFirst();		
			}
		}
	}
}