package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

public class ReshapeAction extends UserAction {
	private CanvasObject object;
	private String param;

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
