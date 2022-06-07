package it.polimi.tiw.projects.thinBeans;

public class ThinUser{
	private final String username;
	private final int id;
	
	public ThinUser(String username, int id) {
		this.username = username;
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getId() {
		return id;
	}
}