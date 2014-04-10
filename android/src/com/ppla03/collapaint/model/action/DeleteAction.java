package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi menghapus suatu objek kanvas. Aksi ini merupakan lawan dari {@link DrawAction}.
 * @author hamba v7
 * 
 */
public class DeleteAction extends UserAction {
	/**
	 * Objek kanvas yang dihapus.
	 */
	public final CanvasObject object;

	/**
	 * Membuat aksi {@link DeleteAction}.
	 * @param object objek yang dihapus.
	 */
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
		return ((action instanceof DrawAction) && ((DrawAction) action).object
				.equals(this.object));
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null) {
			if (action instanceof DeleteAction)
				return ((DeleteAction) action).object.equals(this.object);
			else if (action instanceof DeleteMultiple)
				return ((DeleteMultiple) action).objects.contains(object);
		}
		return false;
	}

}
