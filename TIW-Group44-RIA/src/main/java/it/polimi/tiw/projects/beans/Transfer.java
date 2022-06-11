package it.polimi.tiw.projects.beans;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class Transfer{
	private int id;
	private BigDecimal amount;
	private Timestamp date;
	private int idBankAccountFrom;
	private int idBankAccountTo;
	private String comments;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
	public int getIdBankAccountFrom() {
		return idBankAccountFrom;
	}
	public void setIdBankAccountFrom(int idBankAccountFrom) {
		this.idBankAccountFrom = idBankAccountFrom;
	}
	public int getIdBankAccountTo() {
		return idBankAccountTo;
	}
	public void setIdBankAccountTo(int idBankAccountTo) {
		this.idBankAccountTo = idBankAccountTo;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
}