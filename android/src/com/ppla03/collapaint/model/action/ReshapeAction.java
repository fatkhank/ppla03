package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

public class ReshapeAction extends UserAction {
	public final CanvasObject object;
	public int[] x;
	public int[] y;

	public ReshapeAction(CanvasObject object) {
		// TODO Auto-generated constructor stub
		this.object = object;
	}

	public String getparameter() {
		// TODO
		return "";
	}

	public void setParameter(String param) {
		// TODO
	}

	@Override
	public UserAction getInverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		// TODO Auto-generated method stub
		return false;
	}

}
