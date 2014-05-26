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
	private float realWidth, realHeight;
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

	private static final RectF accBounds = new RectF();
	private static final RectF temp = new RectF();

	/**
	 * Menangkap gambar kumpulan objek kanvas. Offset akan berubah; skala
	 * menjadi 1;
	 * @param objects
	 */
	public void capture(ArrayList<CanvasObject> objects) {
		// ambil ukuran yang diperlukan.
		accBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		for (int i = 0; i < objects.size(); i++) {
			CanvasObject co = objects.get(i);
			co.getWorldBounds(temp);
			accBounds.union(temp);
		}

		// beri jarak objek dari batas
		accBounds.left -= PADDING;
		accBounds.top -= PADDING;
		accBounds.right += PADDING;
		accBounds.bottom += PADDING;

		// tetapkan offset
		this.left = accBounds.left;
		this.top = accBounds.top;
		this.scale = 1;

		// pastikan ukuran bitmap mencukupi
		realWidth = accBounds.width();
		realHeight = accBounds.height();
		int bw = (int) realWidth;
		int bh = (int) realHeight;
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

	/**
	 * Mengatur ofset
	 * @param x
	 * @param y
	 */
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

	/**
	 * Mengatur skala objek
	 * @param scale
	 * @param center jika true -> pivot ada di tengah; offset akan bergeser ika
	 *            skala berubah. Jika false -> pivot ada di pojok kiri atas,
	 *            skala tidak menyebabkan offset berubah.
	 */
	public void scaleTo(float scale, boolean center) {
		if (center) {
			float offsetRatio = (scale - this.scale) / 2;
			this.left -= offsetRatio * realWidth;
			this.top -= offsetRatio * realHeight;
		}
		this.scale = scale;
	}

	/**
	 * Menyalin gambar dari objek lain, ukuran dan offset tidak berubah.
	 * @param sb
	 */
	public void copy(ScaledBitmap sb) {
		// pastikan ukuran bitmap mencukupi
		int bw = sb.bitmap.getWidth();
		int bh = sb.bitmap.getHeight();
		if (bitmap.getWidth() < bw || bitmap.getHeight() < bh) {
			bitmap = Bitmap.createBitmap(bw, bh, Config.ARGB_8888);
		}
		this.scale = sb.scale;
		this.realWidth = sb.realWidth;
		this.realHeight = sb.realHeight;
		// copy gambar bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		drawer.drawBitmap(sb.bitmap, 0, 0, null);
	}

	/**
	 * Mengubah posisi dan ukuran sesuai dengan objek lain. Skala objek otomatis
	 * menyesuaikan ukuran yang tersedia.
	 * @param sb objek yang ditiru.
	 */
	public void matchSize(ScaledBitmap sb) {
		this.left = sb.left;
		this.top = sb.top;
		this.width = sb.width;
		this.height = sb.height;
		this.scale = rescaleTo(sb);
	}

	/**
	 * Mengitung skala objek agar muat di bound objek lain.
	 * @param sb objek lain yang dicek ukurannya.
	 * @return skala seharusnya.
	 */
	public float rescaleTo(ScaledBitmap sb) {
		float wScale = sb.width / realWidth;
		float hScale = sb.height / realHeight;
		return Math.min(wScale, hScale);
	}

	/**
	 * Menggambar objek.
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(left, top);
		canvas.scale(scale, scale);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}
}
