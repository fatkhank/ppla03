package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;

public class ScaledBitmap {
	private final int PADDING = 5;
	protected Bitmap bitmap;
	protected static final Canvas drawer = new Canvas();
	private float left, top, width, height;
	private float scale;

	public ScaledBitmap(float left, float top, int width, int height) {
		this.left = left;
		this.top = top;
		if (width < 32)
			width = 32;
		if (height < 32)
			height = 32;
		this.width = width;
		this.height = height;
		bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	}

	private final RectF accBounds = new RectF();
	private static final RectF temp = new RectF();

	public void capture(ArrayList<CanvasObject> objects) {
		// ambil ukuran yang diperlukan.
		accBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		for (int i = 0; i < objects.size(); i++) {
			CanvasObject co = objects.get(i);
			co.getWorldBounds(temp);
			accBounds.union(temp);
		}

		accBounds.left -= PADDING;
		accBounds.top -= PADDING;
		accBounds.right += PADDING;
		accBounds.bottom += PADDING;

		// tetapkan offset
		this.left = accBounds.left;
		this.top = accBounds.top;
		this.scale = 1;

		// pastikan ukuran bitmap mencukupi
		int bw = (int) accBounds.width();
		int bh = (int) accBounds.height();
		if (bitmap.getWidth() < bw || bitmap.getHeight() < bw) {
			bitmap = Bitmap.createBitmap(bw, bh, Config.ARGB_8888);
		}

		// gambar tiap objek
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		drawer.save();
		drawer.translate(-this.left, -this.top);
		for (int i = 0; i < objects.size(); i++)
			objects.get(i).draw(drawer);
		drawer.restore();
	}

	public void offsetTo(float x, float y) {
		this.left = x;
		this.top = y;
	}

	public void offset(float dx, float dy) {
		this.left += dx;
		this.top += dy;
	}

	public float offsetX() {
		return this.left;
	}

	public float offsetY() {
		return this.top;
	}

	public float scale() {
		return this.scale;
	}

	public void scaleTo(float scale) {
		this.scale = scale;
	}

	public void copy(ScaledBitmap sb) {
		// pastikan ukuran bitmap mencukupi
		int bw = sb.bitmap.getWidth();
		int bh = sb.bitmap.getHeight();
		if (bitmap.getWidth() < bw || bitmap.getHeight() < bh) {
			bitmap = Bitmap.createBitmap(bw, bh, Config.ARGB_8888);
		}
		this.scale = sb.scale;
		// copy gambar bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		drawer.drawBitmap(sb.bitmap, 0, 0, null);
	}

	public void matchSize(ScaledBitmap sb) {
		this.left = sb.left;
		this.top = sb.top;
		this.width = sb.width;
		this.height = sb.height;
	}

	public float rescaleTo(ScaledBitmap sb) {
		float wScale = sb.width / accBounds.width();
		float hScale = sb.height / accBounds.height();
		return Math.min(wScale, hScale);
	}

	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(left, top);
		canvas.scale(scale, scale);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}
}
