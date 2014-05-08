package com.ppla03.collapaint;

import com.ppla03.collapaint.model.object.CanvasObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

public class Capturer {
	protected static final Canvas drawer = new Canvas();
	protected Bitmap bitmap;

	/**
	 * Bagian dari bitmap yang digambar
	 */
	private final Rect source;

	/**
	 * Ukuran gambar akhir
	 */
	private final RectF dest;

	/**
	 * Tinggi dan lebar ukuran yang diinginkan
	 */
	private float width, height;
	private float scale;

	public Capturer(float left, float top, int width, int height) {
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		source = new Rect(0, 0, width, height);
		dest = new RectF(left, top, left + width, top + height);
		this.width = width;
		this.height = height;
		setDimension(left, top, width, height);
	}

	/**
	 * Mengatur ukuran capturer relatif terhadap ukuran objek
	 * @param scale
	 */
	public void setScale(float scale) {
		float w = source.right * scale;
		float h = source.bottom * scale;
		dest.right = dest.left + w;
		dest.bottom = dest.top + h;
		this.scale = scale;
	}

	public void moveTo(float x, float y) {
		dest.offsetTo(x, y);
	}

	public void setDimension(Capturer cap) {
		dest.set(cap.dest);
		this.scale = cap.scale;
	}

	public float offsetX() {
		return dest.left;
	}

	public float offsetY() {
		return dest.top;
	}

	public float getScale() {
		return scale;
	}

	/**
	 * Mengatur dimensi akhir objek.
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public void setDimension(float left, float top, float width, float height) {
		this.width = width;
		this.height = height;

		float objRatio = source.right / source.bottom;
		float contRatio = width / height;
		if (objRatio > contRatio) {
			// object lebih lebar dari container
			float nh = width / objRatio;
			dest.set(left, top, dest.left + width, dest.top + nh);
		} else {
			float nw = objRatio * height;
			dest.set(left, top, dest.left + nw, dest.top + height);
		}

		scale = dest.width() / source.width();
	}

	private static final PointF objOffset = new PointF();
	private static final RectF objBounds = new RectF();

	/**
	 * Mengambil gambar suatu objek kanvas.
	 * @param object
	 * @return
	 */
	public PointF capture(CanvasObject object) {
		// ambil ukuran objek
		object.getWorldBounds(objBounds);
		objOffset.x = object.offsetX() - objBounds.left;
		objOffset.y = object.offsetY() - objBounds.top;

		// pastikan ukuran bitmap mencukupi
		int width = (int) objBounds.width() + 1;
		int height = (int) objBounds.height() + 1;
		if (bitmap.getWidth() < width || bitmap.getHeight() < height) {
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}
		source.set(0, 0, width, height);
		setDimension(dest.left, dest.top, dest.width(), dest.height());

		// gambar objek ke bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawer.translate(-objBounds.left, -objBounds.top);
		object.draw(drawer);
		drawer.translate(objBounds.left, objBounds.top);

		return objOffset;
	}

	protected void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, source, dest, null);
	}
}
