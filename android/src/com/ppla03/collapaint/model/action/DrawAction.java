package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

public class DrawAction extends UserAction {
	public final CanvasObject object;

	public DrawAction(CanvasObject object) {
		this.object = object;
	}

	@Override
	public UserAction getInverse() {
		if (inverse == null) {
			inverse = new DeleteAction(object);
			inverse.inverse = this;
		}
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		// TODO Auto-generated method stub
		if (action instanceof DeleteMultiple)
			return ((DeleteAction) action).object.equals(this.object);
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		return false;
	}

}
