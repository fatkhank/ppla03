package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi memindah sebuah objek kanvas ke posisi lain.
 * @author hamba v7
 * 
 */
public class MoveAction extends UserAction {
	static final int OFFSET_X = 0, OFFSET_Y = 1;
	private final int[] trans = new int[2];

	/**
	 * Objek kanvas yang dipindah.
	 */
	public final CanvasObject object;

	/**
	 * Membuat aksi {@link MoveAction}.
	 */
	public MoveAction(CanvasObject object) {
		this.object = object;
	}

	/**
	 * Mendapatkan parameter aksi.
	 * @return parameter dalam bentuk String.
	 */
	public String getParameter() {
		return encode(trans[OFFSET_X], trans[OFFSET_Y]);
	}

	/**
	 * Mengatur parameter aksi ini.
	 * @param param parameter dalam bentuk String.
	 * @return this.
	 */
	public MoveAction setParameter(String param) {
		decodeTo(param, trans);
		return this;
	}

	/**
	 * Empty string
	 * @param object
	 * @return
	 */
	public static String getParameterOf(CanvasObject object) {
		// TODO parameter object
		return "";
	}

	/**
	 * Menerapkan perpindahan pada objek yang terdaftar.
	 */
	public void apply() {
		applyTransform(trans[OFFSET_X], trans[OFFSET_Y], object);
	}

	static final int[] tempParam = new int[2];

	/**
	 * Menerapkan suatu perubahan pada suatu objek.
	 * @param param
	 * @param object
	 */
	public static void applyTransform(String param, CanvasObject object) {
		if (param != null && !param.isEmpty()) {
			decodeTo(param, tempParam);
			applyTransform(tempParam[0], tempParam[1], object);
		}
	}

	/**
	 * Menerapkan suatu aksi pada object tertentu.
	 * @param ofX pergeseran dalam sumbu x.
	 * @param ofY pergeseran dalam sumbu y.
	 * @param object objek yang digeser
	 */
	private static void applyTransform(int ofX, int ofY, CanvasObject object) {
		object.offset(ofX, ofY);
	}

	/**
	 * Membongkar informasi dari parameter berbentuk string ke array integer
	 * @param param parameter yang akan dibongkar
	 * @param res tujuan
	 */
	private static void decodeTo(String param, int[] res) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		res[0] = ((bs[0] << 24) & 0xff000000) | ((bs[1] << 16) & 0xff0000)
				| ((bs[2] << 8) & 0xff00) | (bs[3] & 0xff);
		res[1] = ((bs[4] << 24) & 0xff000000) | ((bs[5] << 16) & 0xff0000)
				| ((bs[6] << 8) & 0xff00) | (bs[7] & 0xff);
	}

	static final byte[] encByte = new byte[8];

	/**
	 * Mengubah dari parameter bentuk normal ke bentuk String.
	 * @param ofX pergeseran dalam sumbu x.
	 * @param ofY pergeseran dalam sumbu y.
	 * @return paramter dalam bentuk String.
	 */
	static String encode(int ofX, int ofY) {
		encByte[0] = (byte) (ofX >> 24);
		encByte[1] = (byte) (ofX >> 16);
		encByte[2] = (byte) (ofX >> 16);
		encByte[3] = (byte) (ofX);
		encByte[4] = (byte) (ofY >> 24);
		encByte[5] = (byte) (ofX >> 16);
		encByte[6] = (byte) (ofX >> 16);
		encByte[7] = (byte) (ofX);

		return Base64.encodeToString(encByte, Base64.URL_SAFE);
	}

	@Override
	public UserAction getInverse() {
		if (inverse == null) {
			MoveAction ta = new MoveAction(object);
			ta.trans[OFFSET_X] = -this.trans[OFFSET_X];
			ta.trans[OFFSET_Y] = -this.trans[OFFSET_Y];
			ta.inverse = this;
			inverse = ta;
		}
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		if (action == null || !(action instanceof MoveAction))
			return false;
		MoveAction ta = (MoveAction) action;
		return ((ta.trans[OFFSET_X] == -trans[OFFSET_X])
				&& (ta.trans[OFFSET_Y] == -trans[OFFSET_Y]) && ta.object
					.equals(object));
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null) {
			if (action instanceof MoveAction) {
				MoveAction ta = (MoveAction) action;
				return ta.object.equals(this.object);
			}
		}
		return false;
	}
}
