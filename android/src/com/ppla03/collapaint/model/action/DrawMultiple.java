package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class DrawMultiple extends UserAction {
	public final ArrayList<CanvasObject> objects;

	DrawMultiple(ArrayList<CanvasObject> objects, UserAction inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	public DrawMultiple(ArrayList<CanvasObject> objects) {
		this.objects = new ArrayList<>(objects);
		this.inverse = new DeleteMultiple(this.objects, this);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return (action instanceof DeleteMultiple)
				&& ((DeleteMultiple) action).objects.equals(objects);
	}

	@Override
	public boolean overwrites(UserAction action) {
		return false;
	}

}
