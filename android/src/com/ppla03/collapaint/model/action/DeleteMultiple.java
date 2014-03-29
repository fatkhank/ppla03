package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class DeleteMultiple extends UserAction {
	public final ArrayList<CanvasObject> objects;

	DeleteMultiple(ArrayList<CanvasObject> objects) {
		this.objects = objects;
	}

	DeleteMultiple(ArrayList<CanvasObject> objects, UserAction inverse) {
		this.objects = objects;
		this.inverse = inverse;
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
