package it.polimi.tiw.projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.projects.beans.BankAccount;


/**
 * Class BankAccountDAO handles database interactions relative to the BankAccount table
 * @author marco
 *
 */
public class BankAccountDAO{
	private Connection con;

	/**
	 * Creates a new BankAccountDAO object
	 * @param con database connection
	 */
	public BankAccountDAO(Connection con) {
		this.con = con;
	}
	
	/**
	 * Creates a new Bank Account entry
	 * @param UserId the owner of the new Bank Account
	 * @param balance the balance of the new Bank Account
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public void createBankAccount(int UserId, BigDecimal balance) throws SQLException{
		String query = "INSERT INTO BankAccount VALUES (ID, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query); ){
			pstatement.setBigDecimal(1, balance);
			pstatement.setInt(2, UserId);
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Finds all Bank Accounts belonging to the selected User
	 * @param UserId the owner of the Bank Accounts
	 * @return a List of all Bank Accounts belonging to the selected User
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public ArrayList<BankAccount> findAccountsByUser(int UserId) throws SQLException{
		ArrayList<BankAccount> accounts = new ArrayList<>();
		String query="SELECT * FROM BankAccount WHERE UserId = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, UserId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					BankAccount account = new BankAccount();
					account.setBalance(result.getBigDecimal("Balance"));
					account.setId(result.getInt("ID"));
					account.setIdUser(result.getInt("UserId"));
					accounts.add(account);
				}
			}
		}
		return accounts;
	}
	
	/**
	 * Ensure if the User owns the bankAccount 
	 * @param UserId the User's Id
	 * @param BankAccountId the BankAccount's Id
	 * @return true if the User owns the BankAccount
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public boolean checkAssociationAccountUser(int UserId, int BankAccountId) throws SQLException{
		String query = "SELECT * FROM BankAccount WHERE ID = ? AND UserId = ?"; 
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, BankAccountId);
			pstatement.setInt(2, UserId);
			try (ResultSet result = pstatement.executeQuery();){
				//if there are no results (no one has that username) isBeforeFirst returns false -> the username is unique
				return !result.isBeforeFirst();		
			}
		}
	}
	
	/**
	 * Retrieves the details of a certain Bank Account
	 * @param BankAccountId the id to search for
	 * @return a BankAccountBean, the search result
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public BankAccount detailsAccount(int BankAccountId) throws SQLException{
		BankAccount account = null;
		String query = "SELECT * FROM BankAccount WHERE ID = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, BankAccountId);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) {
					account = new BankAccount();
					account.setId(result.getInt("ID"));
					account.setBalance(result.getBigDecimal("Balance"));
					account.setIdUser(result.getInt("UserId"));
				}
			}
		}
		return account;
	}
	
	public BigDecimal[] transfer(BigDecimal amount, int idDestination, int bankAccountidOrigin) throws SQLException{
		BigDecimal[] balancesAfter = new BigDecimal[2];
			//TODO split in different methods
			PreparedStatement selectBalanceO = con.prepareStatement("SELECT Balance FROM BankAccount WHERE ID = ?");
			selectBalanceO.setInt(1, bankAccountidOrigin);
			ResultSet result = selectBalanceO.executeQuery();
			if(result.next())
				if(result.getBigDecimal("Balance").compareTo(amount)<0){
					throw new SQLException("Insufficent funds ");
				}else
					balancesAfter[0] = result.getBigDecimal("Balance").subtract(amount);
			
			
			selectBalanceO = con.prepareStatement("SELECT Balance FROM BankAccount WHERE ID = ?");
			selectBalanceO.setInt(1, idDestination);
			result = selectBalanceO.executeQuery();
			if(result.next())
				balancesAfter[1] = result.getBigDecimal("Balance").add(amount);
			
			PreparedStatement ps = con.prepareStatement("update BankAccount set Balance=Balance-? Where ID=?");
			ps.setBigDecimal(1, amount);
			ps.setInt(2, bankAccountidOrigin);
			ps.executeUpdate();
			ps = con.prepareStatement("update BankAccount set Balance=Balance+? Where ID=?");
			ps.setBigDecimal(1, amount);
			ps.setInt(2, idDestination);
			ps.executeUpdate();
		return balancesAfter;
	}
	
	/**
	 * Updates the Account's balance
	 * @param amount the amount to add or subtract from the current balance
	 * @param BankAccountId the Bank Account on which the operation is being performed
	 * @throws SQLException if an error is encountered during the interaction with the db
	 */
	public void updateBalance(BigDecimal amount, int BankAccountId) throws SQLException {
		String query = "UPDATE BankAccount SET Balance = Balance + ? WHERE ID = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setBigDecimal(1, amount);
			pstatement.setInt(2, BankAccountId);
			pstatement.executeUpdate();
		}
	}
	
}