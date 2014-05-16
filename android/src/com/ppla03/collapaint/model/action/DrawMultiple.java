package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi menggambar kumpulan objek kanvas. Kebalikan dari aksi ini adalah aksi
 * {@link DeleteMultiple}.
 * @author hamba v7
 * 
 */
public class DrawMultiple extends UserAction {
	/**
	 * Daftar objek yang digambar.
	 */
	public final ArrayList<CanvasObject> objects;

	/**
	 * Membuat {@link DrawMultiple}.
	 * @param objects daftar objek yang digambar. Perubahan pada parameter ini
	 *            dapat merubah daftar objek yang tersimpan oleh aksi ini.
	 * @param inverse inverse dari aksi ini.
	 */
	DrawMultiple(ArrayList<CanvasObject> objects, UserAction inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	/**
	 * Membuat {@link DrawMultiple}
	 * @param objects daftar objek yang digambar. Perubahan pada parameter ini
	 *            setelah digunakan tidak akan mengubah daftar objek yang
	 *            tersimpan oleh aksi ini.
	 */
	public DrawMultiple(ArrayList<CanvasObject> objects) {
		this.objects = new ArrayList<CanvasObject>(objects);
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

	@Override
	public int insertInAtomic(ArrayList<AtomicAction> list) {
		int size = objects.size();
		for (int i = 0; i < size; i++)
			list.add(new DrawAction(objects.get(i)));
		return size;
	}
}
