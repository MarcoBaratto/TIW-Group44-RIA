package it.polimi.tiw.projects.thinBeans;

public class ThinBankAccount{
	private int id;
	private float balance;
	
	public ThinBankAccount(int id, float balance) {
		this.id = id;
		this.balance = balance;
	}
	
	public int getId() {
		return id;
	}
	
	public float getBalance() {
		return balance;
	}
	
}