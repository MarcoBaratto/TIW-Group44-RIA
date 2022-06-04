package it.polimi.tiw.projects.beans;

public class ThinUser{
	private final String username;
	private final int id;
	
	public ThinUser(User u) {
		this.username = u.getUsername();
		this.id = u.getId();
	}
}