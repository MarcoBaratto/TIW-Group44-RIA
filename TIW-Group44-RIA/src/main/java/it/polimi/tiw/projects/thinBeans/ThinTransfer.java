package it.polimi.tiw.projects.thinBeans;

import java.math.BigDecimal;

public class ThinTransfer{
	private BigDecimal[] balancesAfter;
	private BigDecimal[] balancesBefore;
	private BigDecimal amount;
	private int idBankAccountFrom;
	private int idBankAccountTo;
	private int idOwnerTo;
	private String comments;
	
	public ThinTransfer(BigDecimal[] balancesBefore, BigDecimal[] balancesAfter, BigDecimal amount, int idBankAccountFrom, int idBankAccountTo, int idOwnerTo, 
			String comments) {
		super();
		this.balancesAfter = balancesAfter;
		this.balancesBefore = balancesBefore;
		this.amount = amount;
		this.idBankAccountFrom = idBankAccountFrom;
		this.idBankAccountTo = idBankAccountTo;
		this.idOwnerTo = idOwnerTo;
		this.comments = comments;
	}
	
	public BigDecimal[] getBalancesBefore() {
		return balancesBefore;
	}
	
	public BigDecimal[] getBalancesAfter() {
		return balancesAfter;
	}

	public BigDecimal getAmount() {
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