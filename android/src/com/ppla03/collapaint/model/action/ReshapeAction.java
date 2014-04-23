package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Menangani aksi untuk merubah bentuk suatu objek.
 * @author hamba v7
 * 
 */
public class ReshapeAction extends UserAction {
	/**
	 * Objek kanvas yang diubah.
	 */
	public final CanvasObject object;

	/**
	 * Daftar parameter bentuk suatu objek. Tiga indeks pertama diisi parameter
	 * OFFSET_X, OFFSET_Y, dan ROTATION dari objek secara berurutan. Indeks
	 * berikutnya diisi oleh parameter instrinsik dari bentuk objek.
	 */
	protected float[] params;

	private static final int TRANSFORM_LENGTH = 3;
	private static final int OFSX_INDEX = 0, OFSY_INDEX = 1, ROT_INDEX = 2;

	/**
	 * Membuat suatu {@link ReshapeAction} yang memiliki inverse.
	 * @param inverse
	 */
	private ReshapeAction(ReshapeAction inverse) {
		this.object = inverse.object;
		this.inverse = inverse;
		params = new float[inverse.params.length];
	}

	/**
	 * Membuat suatu {@link ReshapeAction} dengan parameter tertentu.
	 * @param object objek kanvas yang diubah bentuknya.
	 * @param reversible apakah bisa dilakukan operasi balik atau tidak. Jika
	 *            true, maka inverse dari objek ini dipastikan selalu tersedia.
	 */
	public ReshapeAction(CanvasObject object, boolean reversible) {
		this.object = object;
		params = new float[object.paramLength() + TRANSFORM_LENGTH];
		params[OFSX_INDEX] = object.offsetX();
		params[OFSY_INDEX] = object.offsetY();
		params[ROT_INDEX] = object.rotation();
		object.extractShape(params, TRANSFORM_LENGTH);
		if (reversible) {
			ReshapeAction ra = new ReshapeAction(object, false);
			ra.inverse = this;
			this.inverse = ra;
		}
	}

	/**
	 * Mengaplikasikan parameter bentuk ke objek.
	 */
	public void apply() {
		object.offsetTo(params[OFSX_INDEX], params[OFSY_INDEX]);
		object.rotateTo(params[ROT_INDEX]);
		object.setShape(params, TRANSFORM_LENGTH, params.length);
	}

	/**
	 * Mencatat bentuk objek saat ini.
	 * @return aksi yang inverse-nya akan mengembalikan bentuk objek ke bentuk
	 *         terakhir sebelum diCapture.
	 */
	public ReshapeAction capture() {
		Stepper backward = new Stepper(this, object, false);

		System.arraycopy(params, 0, backward.params, 0, params.length);
		params[OFSX_INDEX] = object.offsetX();
		params[OFSY_INDEX] = object.offsetY();
		params[ROT_INDEX] = object.rotation();
		object.extractShape(params, TRANSFORM_LENGTH);
		Stepper forward = new Stepper(backward);
		System.arraycopy(params, 0, forward.params, 0, params.length);
		backward.inverse = forward;
		return forward;
	}

	public static class Stepper extends ReshapeAction {
		ReshapeAction parent;

		public Stepper(Stepper inverse) {
			super(inverse);
			parent = inverse.parent;
		}

		public Stepper(ReshapeAction parent, CanvasObject object,
				boolean reversible) {
			super(object, reversible);
			this.parent = parent;
		}

	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return action == inverse;
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null && action instanceof ReshapeAction) {
			ReshapeAction ra = (ReshapeAction) action;
			return ra.object.equals(this.object);
		}
		return false;
	}

	@Override
	public int insertInAtomic(ArrayList<AtomicAction> list) {
		list.add(new TransformAction(object, false).setOffset(
				params[OFSX_INDEX], params[OFSY_INDEX]).setRotation(
				params[ROT_INDEX]));
		list.add(new GeomAction(object, params, TRANSFORM_LENGTH, params.length
				- TRANSFORM_LENGTH));
		return 2;
	}

}
