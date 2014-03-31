package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class CopyAction extends UserAction {
	public final ArrayList<CanvasObject> objects;
	private int dx, dy;
	
	public CopyAction(ArrayList<CanvasObject> objects) {
		// TODO Auto-generated constructor stub
		this.objects = objects;
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
