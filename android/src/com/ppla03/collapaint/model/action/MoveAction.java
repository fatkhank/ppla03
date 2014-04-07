package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

public class MoveAction extends UserAction {
	static final int OFFSET_X = 0, OFFSET_Y = 1;
	private final int[] trans = new int[2];
	public final CanvasObject object;

	public MoveAction(CanvasObject object) {
		this.object = object;
	}

	public String getParameter() {
		return encode(trans[OFFSET_X], trans[OFFSET_Y]);
	}

	public MoveAction setParameter(String param) {
		decodeTo(param, trans);
		return this;
	}

	public void apply() {
		applyTransform(trans[OFFSET_X], trans[OFFSET_Y], object);
	}

	public static String getParameterOf(CanvasObject object) {
		return "";
	}

	static final int[] tempParam = new int[2];

	public static void applyTransform(String param, CanvasObject object) {
		decodeTo(param, tempParam);
		applyTransform(tempParam[0], tempParam[1], object);
	}

	private static void applyTransform(int ofX, int ofY, CanvasObject object) {
		object.setOffset(ofX, ofY);
	}

	private static void decodeTo(String param, int[] res) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		res[0] = ((bs[0] << 24) & 0xff000000) | ((bs[1] << 16) & 0xff0000)
				| ((bs[2] << 8) & 0xff00) | (bs[3] & 0xff);
		res[1] = ((bs[4] << 24) & 0xff000000) | ((bs[5] << 16) & 0xff0000)
				| ((bs[6] << 8) & 0xff00) | (bs[7] & 0xff);
	}

	static final byte[] encByte = new byte[8];

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
