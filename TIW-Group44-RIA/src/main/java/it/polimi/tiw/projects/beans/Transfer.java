package it.polimi.tiw.projects.beans;

import java.util.Date;

public class Transfer{
	private int id;
	private float amount;
	private Date date;
	private int idBankAccountFrom;
	private int idBankAccountTo;
	private String comments;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
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