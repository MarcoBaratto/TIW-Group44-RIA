package it.polimi.tiw.projects.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddressBook {
	private int ownerId;
	private Map<Integer, Set<Integer>> contacts = new HashMap<>();
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public Map<Integer, Set<Integer>> getContacts() {
		return new HashMap<>(contacts);
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
	public void addContact(int ownerId, int accountId) {
		if(contacts.containsKey(ownerId)) {
			contacts.get(ownerId).add(accountId);
		}else {
			Set<Integer> set = new HashSet<>();
			set.add(accountId);
			contacts.put(ownerId, set);
		}

	}	
}