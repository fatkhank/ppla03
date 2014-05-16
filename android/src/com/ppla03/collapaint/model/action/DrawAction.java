package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi menggambar suatu objek kanvas.
 * @author hamba v7
 * 
 */
public class DrawAction extends AtomicAction {
	/**
	 * Objek kanvas yang digambar.
	 */
	public final CanvasObject object;

	/**
	 * Membuat suatu {@link DrawAction}.
	 * @param object objek yang digambar.
	 */
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
		if (action instanceof DeleteAction)
			return ((DeleteAction) action).object.equals(this.object);
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		return ((action instanceof DrawAction) && ((DrawAction) action).object
				.equals(object));
	}
}
