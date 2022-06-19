package it.polimi.tiw.projects.enums;

public enum ERRORS {
	SQL_ERROR("There was a problem with the database"),
	NO_FUNDS("Insufficient funds!"),
	INCORRECT_PARAMS("Incorrect or missing input"),
	NOT_OWNER("The user isn't the Account owner"),
	LOGIN_ERROR("Incorrect user/password"),
	PSW_NO_MATCH("Passwords don't match"),
	MAIL_ERROR("Incorrect mail format"),
	USERNAME_CHOSEN("Username already in use"),
	SAME_ACCOUNT("Origin and Destination must be different")
	;
	
	ERRORS(String e) {
		this.e = e;
	}

	private String e;
	
	public String toString() {
		return e;
	}
	
}