package com.ppla03.collapaint.model;

/**
 * Representasi partisipasi seorang user pada sebuah kanvas
 * @author hamba v7
 * 
 */
public class Participation {
	/**
	 * Peran seorang user pada kanvas tersebut.
	 * @author hamba v7
	 * 
	 */
	public enum Role {
		/**
		 * User adalah owner dari kanvas.
		 */
		OWNER,
		/**
		 * User merupakan anggota yang tergabung di kanvas.
		 */
		MEMBER,
		/**
		 * User sedang dikirimi undangan untuk bergabung di kanvas.
		 */
		INVITATION
	}

	/**
	 * Aksi yang sedang dilakukan user.
	 * @author hamba v7
	 * 
	 */
	public enum Action {
		/**
		 * User sedang aktif membuka kanvas.
		 */
		OPEN,
		/**
		 * User tidak sedang membuka kanvas.
		 */
		CLOSE
	}

	public final UserModel user;
	public final CanvasModel canvas;
	private Role role;
	private Action action;

	public Participation(UserModel user, CanvasModel canvas) {
		this.user = user;
		this.canvas = canvas;
		this.role = Role.INVITATION;
	}
	
	public Participation(UserModel user, CanvasModel canvas, Role role) {
		this.user = user;
		this.canvas = canvas;
		this.role = role;
	}

	public Participation setRole(Role role) {
		this.role = role;
		return this;
	}

	public Role getRole() {
		return this.role;
	}

	public Participation setAction(Action action) {
		this.action = action;
		return this;
	}

	public Action getAction() {
		return this.action;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Participation) {
			Participation p = (Participation) o;
			return p.canvas.equals(this.canvas) && p.user.equals(this.user);
		}
		return false;
	}
}
