package it.polimi.tiw.projects.thinBeans;

import java.util.ArrayList;

public class ThinTransfer{
	private ArrayList<Float> balancesAfter;
	private float amount;
	private int idBankAccountFrom;
	private int idBankAccountTo;
	private String comments;
	
	public ThinTransfer(ArrayList<Float> balancesAfter, float amount, int idBankAccountFrom, int idBankAccountTo,
			String comments) {
		super();
		this.balancesAfter = balancesAfter;
		this.amount = amount;
		this.idBankAccountFrom = idBankAccountFrom;
		this.idBankAccountTo = idBankAccountTo;
		this.comments = comments;
	}
}