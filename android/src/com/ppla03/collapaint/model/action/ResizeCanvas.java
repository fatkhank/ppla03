package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.CanvasModel;

/**
 * Mengubah ukuran kanvas
 * @author hamba v7
 * 
 */
public class ResizeCanvas extends AtomicAction {
	public int width, height, left, top;

	public ResizeCanvas(CanvasModel model, int width, int height, int top,
			int left, boolean reversible) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
		if (reversible) {
			this.inverse = new ResizeCanvas(model, model.getWidth(),
					model.getHeight(), model.getTop(), model.getLeft(), false);
			this.inverse.inverse = this;
		}
	}

	public ResizeCanvas(String param) {
		setParameter(param);
	}

	public void setParameter(String param) {
		decodeTo(param, params);
		width = params[0];
		height = params[1];
		top = params[2];
		left = params[3];
	}

	private static int[] params = new int[4];

	public String getParameter() {
		params[0] = width;
		params[1] = height;
		params[2] = top;
		params[3] = left;
		return encode(params);
	}

	private static void decodeTo(String param, int[] res) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		res[0] = ((bs[0] << 24) & 0xff000000) | ((bs[1] << 16) & 0xff0000)
				| ((bs[2] << 8) & 0xff00) | (bs[3] & 0xff);
		res[1] = ((bs[4] << 24) & 0xff000000) | ((bs[5] << 16) & 0xff0000)
				| ((bs[6] << 8) & 0xff00) | (bs[7] & 0xff);
		res[2] = ((bs[8] << 24) & 0xff000000) | ((bs[9] << 16) & 0xff0000)
				| ((bs[10] << 8) & 0xff00) | (bs[11] & 0xff);
		res[3] = ((bs[12] << 24) & 0xff000000) | ((bs[13] << 16) & 0xff0000)
				| ((bs[14] << 8) & 0xff00) | (bs[15] & 0xff);
	}

	private static final byte[] encByte = new byte[16];

	private static String encode(int[] par) {
		int a = par[0];
		encByte[0] = (byte) (a >> 24);
		encByte[1] = (byte) (a >> 16);
		encByte[2] = (byte) (a >> 16);
		encByte[3] = (byte) (a);
		int b = par[1];
		encByte[4] = (byte) (b >> 24);
		encByte[5] = (byte) (b >> 16);
		encByte[6] = (byte) (b >> 16);
		encByte[7] = (byte) (b);
		int c = par[2];
		encByte[8] = (byte) (c >> 24);
		encByte[9] = (byte) (c >> 16);
		encByte[10] = (byte) (c >> 16);
		encByte[11] = (byte) (c);
		int d = par[3];
		encByte[12] = (byte) (d >> 24);
		encByte[13] = (byte) (d >> 16);
		encByte[14] = (byte) (d >> 16);
		encByte[15] = (byte) (d);

		return Base64.encodeToString(encByte, Base64.URL_SAFE);
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
		return (action instanceof ResizeCanvas);
	}

}
