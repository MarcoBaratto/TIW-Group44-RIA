package it.polimi.tiw.projects.thinBeans;

import java.math.BigDecimal;

public class ThinBankAccount{
	private int id;
	private BigDecimal balance;
	
	public ThinBankAccount(int id, BigDecimal balance) {
		this.id = id;
		this.balance = balance;
	}
	
	public int getId() {
		return id;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}
	
}