package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class CaptureObject extends CanvasObject {
	protected static final Canvas drawer = new Canvas();
	protected Bitmap bitmap;

	/**
	 * Ukuran asli gambar
	 */
	private RectF objBounds = new RectF();

	/**
	 * Ukuran gambar akhir
	 */
	private final RectF dest;
	private float scaleX, scaleY;

	public CaptureObject(float left, float top, int width, int height) {
		this.offsetX = left;
		this.offsetY = top;
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		dest = new RectF(0, 0, width, height);
		scaleX = 1;
		scaleY = 1;
	}

	public static enum Mode {
		OBJECT_PERCENTAGE, EXACT
	}

	public void setDimension(Mode mode, float widthParam, float heightParam) {
		
	}

	/**
	 * Mengatur dimensi akhir objek.
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public void setDimension(float left, float top, float width, float height) {
		this.offsetX = left;
		this.offsetY = top;
		dest.set(0, 0, width, height);
		scaleX = dest.width() / objBounds.width();
		scaleY = dest.height() / objBounds.height();
	}

	/**
	 * Perbandingan lebar gambar akhir dengan gambar asli
	 * @return
	 */
	public float scaleX() {
		return this.scaleX;
	}

	/**
	 * Perbandingan tinggi gambar asli dengan gambar akhir
	 * @return
	 */
	public float scaleY() {
		return this.scaleY;
	}

	/**
	 * Mengambil gambar suatu objek kanvas.
	 * @param object
	 * @return
	 */
	public CaptureObject capture(CanvasObject object) {
		// ambil ukuran objek
		object.getWorldBounds(objBounds);
		scaleX = dest.width() / objBounds.width();
		scaleY = dest.height() / objBounds.height();

		// pastikan ukuran bitmap mencukupi
		int width = (int) objBounds.width() + 1;
		int height = (int) objBounds.height() + 1;
		if (bitmap.getWidth() < width || bitmap.getHeight() < height) {
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}

		// gambar objek ke bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawer.translate(-objBounds.left, -objBounds.top);
		object.draw(drawer);
		drawer.translate(objBounds.left, objBounds.top);
		return this;
	}

	private static final RectF accBounds = new RectF();

	public CaptureObject capture(ArrayList<CaptureObject> objects, int start,
			int end) {
		// hitung ukuran bitmap yang dibutuhkan
		objBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		for (int i = start; i < end; i++) {
			CanvasObject obj = objects.get(i);
			obj.getWorldBounds(accBounds);
			objBounds.union(accBounds);
		}

		scaleX = dest.width() / objBounds.width();
		scaleY = dest.height() / objBounds.height();

		// pastikan ukuran bitmap mencukupi
		int width = (int) objBounds.width() + 1;
		int height = (int) objBounds.height() + 1;
		if (bitmap.getWidth() < width || bitmap.getHeight() < height) {
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}

		// gambar semua objek ke bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawer.translate(-accBounds.left, -accBounds.top);
		for (; start < end; start++) {
			CanvasObject co = objects.get(0);
			co.draw(drawer);
		}
		drawer.translate(accBounds.left, accBounds.top);
		return this;
	}

	public CaptureObject capture(Drawable object) {
		// ambil ukuran objek
		objBounds.set(object.getBounds());
		scaleX = dest.width() / objBounds.width();
		scaleY = dest.height() / objBounds.height();

		// pastikan ukuran bitmap mencukupi
		int width = (int) objBounds.width() + 1;
		int height = (int) objBounds.height() + 1;
		if (bitmap.getWidth() < width || bitmap.getHeight() < height) {
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}

		// gambar objek ke bitmap
		drawer.setBitmap(bitmap);
		drawer.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawer.translate(-objBounds.left, -objBounds.top);
		object.draw(drawer);
		drawer.translate(objBounds.left, objBounds.top);
		return this;
	}

	public CaptureObject capture(CaptureObject object) {
		return this;
	}

	@Override
	protected void drawSelf(Canvas canvas) {
		canvas.drawBitmap(bitmap, null, dest, null);
	}

	@Override
	protected boolean selectedBy(float x, float y, float radius) {
		return (x > dest.left - radius) && (x < dest.right + radius)
				&& (y > dest.top - radius) && (y < dest.bottom + radius);
	}

	@Override
	public void setGeom(float[] param, int start, int end) {
		// nothing
	}

	@Override
	public int geomParamLength() {
		return 0;
	}

	@Override
	public int extractGeom(float[] target, int start) {
		return 0;
	}

	@Override
	public ShapeHandler getHandler(int filter) {
		BoxHandler.handle(this, dest);
		return BoxHandler.getHandler(filter);
	}

	@Override
	void onHandlerMoved(ShapeHandler handler, ControlPoint point, float oldX,
			float oldY) {
		BoxHandler.onHandlerMoved(handler, point, oldX, oldY);
		BoxHandler.mapTo(dest);
	}

	@Override
	protected void getBounds(RectF bounds) {
		bounds.set(dest);
	}

	@Override
	public CaptureObject cloneObject() {
		CaptureObject co = new CaptureObject(offsetX, offsetY,
				bitmap.getWidth(), bitmap.getHeight());
		co.bitmap = this.bitmap.copy(Bitmap.Config.ARGB_8888, true);
		co.dest.set(this.dest);
		return co;
	}

}
