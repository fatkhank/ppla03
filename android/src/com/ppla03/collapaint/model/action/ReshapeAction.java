package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

public class ReshapeAction extends UserAction {
	public final CanvasObject object;
	private int[] params;

	private ReshapeAction(ReshapeAction inverse) {
		this.object = inverse.object;
		this.inverse = inverse;
		params = new int[inverse.params.length];
	}

	public ReshapeAction(CanvasObject object, boolean reversible) {
		this.object = object;
		params = new int[object.paramLength()];
		object.extractShape(params, 0);
		if (reversible) {
			ReshapeAction ra = new ReshapeAction(object, false);
			ra.inverse = this;
			this.inverse = ra;
		}
	}

	public String getParameter() {
		return encode(params, params.length);
	}

	public void setParameter(String param) {
		int size = decodeSize(param);
		if (params.length < size)
			params = new int[size];
		decodeTo(param, params);
	}

	public void apply() {
		object.setShape(params, 0, params.length);
	}

	private static int[] tempPoints = new int[4];

	public static String getParameterOf(CanvasObject object) {
		if (tempPoints.length < object.paramLength())
			tempPoints = new int[object.paramLength()];
		int n = object.extractShape(tempPoints, 0);
		return encode(tempPoints, n);
	}

	private static int[] applyTemp = new int[4];

	public static void apply(String param, CanvasObject object) {
		int size = decodeTo(param, applyTemp);
		object.setShape(applyTemp, 0, size);
	}

	public ReshapeAction capture() {
		ReshapeAction rai = new ReshapeAction(object, false);
		System.arraycopy(params, 0, rai.params, 0, params.length);
		object.extractShape(params, 0);
		ReshapeAction ra = new ReshapeAction(rai);
		System.arraycopy(params, 0, ra.params, 0, params.length);
		rai.inverse = ra;
		return ra;
	}

	private static int decodeSize(String param) {
		return ((param.length() * 3) >> 4);
	}

	private static int decodeTo(String param, int[] result) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		int size = bs.length >> 2;
		int c = 0;
		for (int i = 0; i < size; i++)
			result[i] = ((bs[c++] << 24) & 0xff000000)
					| ((bs[c++] << 16) & 0xff0000) | ((bs[c++] << 8) & 0xff00)
					| (bs[c++] & 0xff);
		return size;
	}

	private static byte[] encByte = new byte[4];

	private static String encode(int[] points, int count) {
		int c = count << 2;
		if (encByte.length < c)
			encByte = new byte[c];
		for (int i = 0; i < count; i++) {
			int p = points[i];
			encByte[c++] = (byte) (p >> 24);
			encByte[c++] = (byte) (p >> 16);
			encByte[c++] = (byte) (p >> 16);
			encByte[c++] = (byte) (p);
		}
		return Base64.encodeToString(encByte, 0, c, Base64.URL_SAFE);
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

}
