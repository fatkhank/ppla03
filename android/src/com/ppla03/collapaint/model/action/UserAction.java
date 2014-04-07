package com.ppla03.collapaint.model.action;

public abstract class UserAction {
	
	protected UserAction inverse;
	
	public abstract UserAction getInverse();

	public abstract boolean inverseOf(UserAction action);

	public abstract boolean overwrites(UserAction action);
}
