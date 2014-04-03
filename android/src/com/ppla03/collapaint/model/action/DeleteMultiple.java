package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class DeleteMultiple extends UserAction {
	public final ArrayList<CanvasObject> objects;

	DeleteMultiple(ArrayList<CanvasObject> objects, UserAction inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	public DeleteMultiple(ArrayList<CanvasObject> objects) {
		this.objects = new ArrayList<>(objects);
		this.inverse = new DrawMultiple(this.objects, this);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return (action instanceof DrawMultiple)
				&& ((DrawMultiple) action).objects.equals(objects);
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action instanceof DrawMultiple) {
			return ((DrawMultiple) action).objects.equals(objects);
		} else if (action instanceof DrawAction) {
			return objects.contains(((DrawAction) action).object);
		}
		return false;
	}

}
