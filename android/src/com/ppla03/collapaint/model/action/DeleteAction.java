package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

public class DeleteAction extends UserAction {
	public final CanvasObject object;

	public DeleteAction(CanvasObject object) {
		this.object = object;
	}

	@Override
	public UserAction getInverse() {
		if (inverse == null) {
			inverse = new DrawAction(object);
			inverse.inverse = this;
		}
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		if (action instanceof DrawAction)
			return ((DrawAction) action).object.equals(this.object);
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		return ((DeleteAction) action).object.equals(this.object);
	}

}
