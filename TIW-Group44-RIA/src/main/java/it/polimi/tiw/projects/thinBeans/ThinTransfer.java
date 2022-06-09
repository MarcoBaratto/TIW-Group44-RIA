package it.polimi.tiw.projects.thinBeans;

import java.util.ArrayList;

public class ThinTransfer{
	private ArrayList<Float> balancesAfter;
	private float amount;
	private int idBankAccountFrom;
	private int idBankAccountTo;
	private int idOwnerTo;
	private String comments;
	
	public ThinTransfer(ArrayList<Float> balancesAfter, float amount, int idBankAccountFrom, int idBankAccountTo, int idOwnerTo, 
			String comments) {
		super();
		this.balancesAfter = balancesAfter;
		this.amount = amount;
		this.idBankAccountFrom = idBankAccountFrom;
		this.idBankAccountTo = idBankAccountTo;
		this.idOwnerTo = idOwnerTo;
		this.comments = comments;
	}

	public ArrayList<Float> getBalancesAfter() {
		return balancesAfter;
	}

	public float getAmount() {
		return amount;
	}

	public int getIdBankAccountFrom() {
		return idBankAccountFrom;
	}


	public int getIdBankAccountTo() {
		return idBankAccountTo;
	}
	
	public int getIdOwnerTo() {
		return idOwnerTo;
	}

	public String getComments() {
		return comments;
	}

	
}