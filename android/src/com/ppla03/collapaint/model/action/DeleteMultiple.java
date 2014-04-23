package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi menghapus kumpulan objek kanvas. Kebalikan dari aksi ini adalah
 * {@link DrawMultiple}.
 * @author hamba v7
 * 
 */
public class DeleteMultiple extends UserAction {
	/**
	 * Daftar objek yang dihapus
	 */
	public final ArrayList<CanvasObject> objects;

	/**
	 * Membuat {@link DeleteMultiple}.
	 * @param objects daftar objek yang dihapus. Perubahan pada parameter ini
	 *            akan mengubah isi daftar objek yang dihapus.
	 * @param inverse inverse dari aksi ini.
	 */
	DeleteMultiple(ArrayList<CanvasObject> objects, UserAction inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	/**
	 * Membuat {@link DeleteMultiple}.
	 * @param objects daftar objek yang dihapus. Perubahan pada parameter ini
	 *            tidak akan mempengaruhi daftar objek yang dihapus.
	 */
	public DeleteMultiple(ArrayList<CanvasObject> objects) {
		this.objects = new ArrayList<CanvasObject>(objects);
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

	@Override
	public int insertInAtomic(ArrayList<AtomicAction> list) {
		int size = objects.size();
		for (int i = 0; i < size; i++)
			list.add(new DeleteAction(objects.get(i)));
		return size;
	}

}
