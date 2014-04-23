package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi mengubah parameter transformasi sebuah objek kanvas.
 * @author hamba v7
 * 
 */
public class TransformAction extends AtomicAction {
	static final int OFFSET_X = 0, OFFSET_Y = 1, ROTATION = 2;
	/**
	 * Parameter transformasi
	 */
	private final float[] trans = new float[3];

	/**
	 * Objek kanvas yang dipindah.
	 */
	public final CanvasObject object;

	/**
	 * Membuat aksi {@link TransformAction}.
	 */
	public TransformAction(CanvasObject object, boolean reversable) {
		this.object = object;
		trans[OFFSET_X] = object.offsetX();
		trans[OFFSET_Y] = object.offsetY();
		trans[ROTATION] = object.rotation();
		if (reversable) {
			TransformAction inv = new TransformAction(object, false);
			inv.inverse = this;
			inverse = inv;
		}
	}

	/**
	 * Mendapatkan parameter aksi.
	 * @return parameter dalam bentuk String.
	 */
	public String getParameter() {
		return encode(trans);
	}

	/**
	 * Mengatur parameter aksi ini.
	 * @param param parameter dalam bentuk String.
	 * @return this.
	 */
	public TransformAction setParameter(String param) {
		decodeTo(param, trans);
		return this;
	}

	/**
	 * Mengatur parameter translasi
	 * @param ofX offset x
	 * @param ofY offset y
	 * @return this
	 */
	public TransformAction setOffset(float ofX, float ofY) {
		trans[OFFSET_X] = ofX;
		trans[OFFSET_Y] = ofY;
		return this;
	}

	/**
	 * Mengatur parameter rotasi
	 * @param rotation sudut rotasi dalam derajat
	 * @return this
	 */
	public TransformAction setRotation(float rotation) {
		trans[ROTATION] = rotation;
		return this;
	}

	private static final float[] tempGet = new float[3];

	/**
	 * Offset parameter of object
	 * @param object
	 * @return
	 */
	public static String getParameterOf(CanvasObject object) {
		tempGet[OFFSET_X] = object.offsetX();
		tempGet[OFFSET_Y] = object.offsetY();
		tempGet[ROTATION] = object.rotation();
		return encode(tempGet);
	}

	/**
	 * Menerapkan perpindahan pada objek yang terdaftar.
	 */
	public void apply() {
		applyTransform(trans, object);
	}

	private static final float[] tempParam = new float[3];

	/**
	 * Menerapkan suatu perubahan pada suatu objek.
	 * @param param
	 * @param object
	 */
	public static void applyTransform(String param, CanvasObject object) {
		if (param != null && !param.isEmpty()) {
			decodeTo(param, tempParam);
			applyTransform(tempParam, object);
		}
	}

	/**
	 * Menerapkan suatu aksi pada object tertentu.
	 * @param param parameter tansformasi
	 * @param object objek yang digeser
	 */
	private static void applyTransform(float[] param, CanvasObject object) {
		object.offsetTo(param[OFFSET_X], param[OFFSET_Y]);
	}

	/**
	 * Membongkar informasi dari parameter berbentuk string ke array integer
	 * @param param parameter yang akan dibongkar
	 * @param res tujuan
	 */
	private static void decodeTo(String param, float[] res) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		res[0] = Float.intBitsToFloat(((bs[0] << 24) & 0xff000000)
				| ((bs[1] << 16) & 0xff0000) | ((bs[2] << 8) & 0xff00)
				| (bs[3] & 0xff));
		res[1] = Float.intBitsToFloat(((bs[4] << 24) & 0xff000000)
				| ((bs[5] << 16) & 0xff0000) | ((bs[6] << 8) & 0xff00)
				| (bs[7] & 0xff));
		res[2] = Float.intBitsToFloat(((bs[8] << 24) & 0xff000000)
				| ((bs[9] << 16) & 0xff0000) | ((bs[10] << 8) & 0xff00)
				| (bs[11] & 0xff));
	}

	static final byte[] encByte = new byte[12];

	/**
	 * Mengubah dari parameter bentuk normal ke bentuk String.
	 * @param ofX pergeseran dalam sumbu x.
	 * @param ofY pergeseran dalam sumbu y.
	 * @return paramter dalam bentuk String.
	 */
	static String encode(float[] param) {
		int ofX = Float.floatToIntBits(param[OFFSET_X]);
		encByte[0] = (byte) (ofX >> 24);
		encByte[1] = (byte) (ofX >> 16);
		encByte[2] = (byte) (ofX >> 8);
		encByte[3] = (byte) (ofX);
		int ofY = Float.floatToIntBits(param[OFFSET_Y]);
		encByte[4] = (byte) (ofY >> 24);
		encByte[5] = (byte) (ofX >> 16);
		encByte[6] = (byte) (ofX >> 8);
		encByte[7] = (byte) (ofX);
		int rot = Float.floatToIntBits(param[ROTATION]);
		encByte[8] = (byte) (rot >> 24);
		encByte[9] = (byte) (rot >> 16);
		encByte[10] = (byte) (rot >> 8);
		encByte[11] = (byte) (rot);

		return Base64.encodeToString(encByte, Base64.URL_SAFE);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		if (action == null || !(action instanceof TransformAction))
			return false;
		TransformAction ta = (TransformAction) action;
		return ((ta.trans[OFFSET_X] == -trans[OFFSET_X])
				&& (ta.trans[OFFSET_Y] == -trans[OFFSET_Y]) && ta.object
					.equals(object));
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null) {
			if (action instanceof TransformAction) {
				TransformAction ta = (TransformAction) action;
				return ta.object.equals(this.object);
			}
		}
		return false;
	}
}
