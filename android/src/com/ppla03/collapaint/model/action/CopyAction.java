package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class CopyAction extends UserAction {
	public final ArrayList<CanvasObject> objects;
	private int dx, dy;

	public CopyAction(ArrayList<CanvasObject> objects) {
		this.objects = new ArrayList<>(objects);
		this.inverse = new DeleteMultiple(this.objects, this);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
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
