package com.ppla03.collapaint.model;

/**
 * Merepresentasikan akun bayangan collapaint.
 * @author hamba v7
 * 
 */
public class UserModel {
	/**
	 * id collapaint
	 */
	public int collaID;
	/**
	 * Alamat email.
	 */
	public String accountID;
	/**
	 * Nama akun.
	 */
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
