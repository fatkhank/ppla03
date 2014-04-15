package com.ppla03.collapaint.model;

public class UserModel {
	public int id;
	public String username;
	
	public UserModel() {
		id = -1;
		username = "";
	}
	
	public UserModel(int id, String username){
		this.id = id;
		this.username = username;
	}
}
