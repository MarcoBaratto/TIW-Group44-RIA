package it.polimi.tiw.projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import it.polimi.tiw.projects.beans.Transfer;


/**
 * Class TransferDAO handles database interactions relative to the Transfer table
 * @author marco
 *
 */
public class TransferDAO{
	private Connection con;
	
	/**
	 * Creates a new TransferDAO object
	 * @param con db connection
	 */
	public TransferDAO(Connection con){
		this.con=con;
	}
	
	/**
	 * Creates a new entry in the Transfer Table
	 * @param IdOrigin the Id of the Origin account
	 * @param IdDestination the Id of the Destination account
	 * @param balance the value of the transaction
	 * @param comment the transaction comment
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public void createTransfer(int IdOrigin, int IdDestination, BigDecimal balance, String comment) throws SQLException {
		String query = "INSERT INTO Transfer VALUES(ID, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1,IdOrigin);
			pstatement.setInt(2,IdDestination);
			pstatement.setBigDecimal(3,balance);
			pstatement.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
			pstatement.setString(5, comment);
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Finds all Transfers that involve the selected BankAccount
	 * @param BankAccountId the BankAccount being esaminated
	 * @return the list of Transfers where the Id is in the Origin or Destination field
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public ArrayList<Transfer> findTransferById(int BankAccountId) throws SQLException{
		ArrayList<Transfer> transfers = new ArrayList<>();
		
		String query="SELECT * FROM Transfer WHERE Origin = ? OR Destination = ? ORDER BY Date DESC";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, BankAccountId);
			pstatement.setInt(2, BankAccountId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Transfer transfer = new Transfer();
					transfer.setId(result.getInt("ID"));
					transfer.setAmount(result.getBigDecimal("Balance"));
					transfer.setIdBankAccountFrom(result.getInt("Origin"));
					transfer.setIdBankAccountTo(result.getInt("Destination"));
					transfer.setDate(result.getTimestamp("Date"));
					transfer.setComments(result.getString("Comments"));
					
					transfers.add(transfer);
				}
			}
		}
		return transfers;
	}
	
}