package com.ppla03.collapaint.model;

public class UserModel {
	public int collaID;
	public String accountID;
	public String nickname;

	public UserModel() {
		collaID = -1;
		accountID = "";
		nickname = "";
	}

	public UserModel(int collaID, String accountID, String nickname) {
		this.collaID = collaID;
		this.accountID = accountID;
		this.nickname = nickname;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof UserModel) {
			UserModel um = (UserModel) o;
			return (collaID == -1 && accountID.equals(um.accountID))
					|| (um.collaID == this.collaID);
		}
		return false;
	}
}
