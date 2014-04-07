package com.ppla03.collapaint.model.object;

import com.ppla03.collapaint.model.object.ControlPoint.Type;

import android.graphics.Canvas;

/**
 * Berfungsi untuk mengontrol bentuk dari objek, menggunakan titik-titik kontrol
 * yang bisa digerakkan.
 * @author hamba v7
 * 
 */
public class ShapeHandler {
	public static final int SHAPE = 1, TRANSLATE = 2, ROTATE = 4, ALL = SHAPE
			| TRANSLATE | ROTATE;

	/**
	 * Daftar {@link ControlPoint}.
	 */
	ControlPoint[] points;

	/**
	 * Jumlah {@link ControlPoint} di daftar point yang bisa digunakan.
	 */
	int size;

	/**
	 * Objek kanvas yang sedang ditangani oleh handler ini.
	 */
	CanvasObject object;

	/**
	 * Membuat objek handler baru.
	 * @param object
	 * @param points
	 */
	public ShapeHandler(CanvasObject object, ControlPoint[] points) {
		this.points = points;
		size = points.length;
		this.object = object;
	}

	/**
	 * Menggambar handler beserta titik-titik kontrol yang dimiliki ke dalam
	 * suatu kanvas.
	 * @param canvas kanvas tempat menggambar handler.
	 */
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(object.offsetX, object.offsetY);
		canvas.rotate(object.rotation);
		for (int i = 0; i < size; i++) {
			ControlPoint cp = points[i];
			if (cp.enable)
				cp.draw(canvas);
		}
		canvas.restore();
	}

	/**
	 * Mencoba menangkap titik kontrol yang ada di posisi tertentu.
	 * @param worldX koordinat x
	 * @param worldY koordinat y
	 * @return titik kontrol yang berhasil ditangkap, atau <code>null</code>
	 *         jika tidak ada titik kontrol pada posisi tersebut.
	 */
	public ControlPoint grab(int worldX, int worldY) {
		worldX -= object.offsetX;
		worldY -= object.offsetY;
		double deg = Math.toRadians(-object.rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		worldX = (int) (worldX * cos + worldY * -sin);
		worldY = (int) (worldX * sin + worldY * cos);
		for (int i = 0; i < size; i++) {
			ControlPoint cp = points[i];
			if (cp.enable && cp.grabbed(worldX, worldY))
				return cp;
		}
		return null;
	}

	/**
	 * Mengatur keaktifan dari semua titik kontrol.
	 * @param enable
	 */
	void setEnableAllPoint(boolean enable) {
		for (int i = 0; i < points.length; i++)
			points[i].enable = enable;
	}

	/**
	 * Mencoba menggeser suatu titik kontrol ke arah tertentu.
	 * @param cp titik kontrol yang digeser.
	 * @param worldX koordinat x titik tujuan.
	 * @param worldY koordinat y titik tujuan.
	 */
	public void dragPoint(ControlPoint cp, float worldX, float worldY) {
		float ox = cp.x;
		float oy = cp.y;
		worldX -= object.offsetX;
		worldY -= object.offsetY;
		double deg = Math.toRadians(-object.rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		worldX = (float) (worldX * cos - worldY * sin);
		worldY = (float) (worldX * sin + worldY * cos);
		if (cp.type == Type.MOVE)
			cp.moveTo(worldX, worldY);
		else
			cp.setPosition(worldX, worldY);
		object.onHandlerMoved(this, cp, ox, oy);
	}
}
