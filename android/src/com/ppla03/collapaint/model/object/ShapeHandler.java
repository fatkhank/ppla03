package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

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
	protected ControlPoint[] points;

	/**
	 * Jumlah {@link ControlPoint} di daftar point yang bisa digunakan.
	 */
	protected int size;

	/**
	 * Objek kanvas yang sedang ditangani oleh handler ini.
	 */
	protected CanvasObject object;

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
	 * Mengatur kembali posisi handler karena ada parameter objek yang berubah
	 * dari luar handler.
	 */
	public void refresh() {
		object.getBounds(bounds);
	}

	protected static final RectF bounds = new RectF();

	/**
	 * Menggambar handler beserta titik-titik kontrol yang dimiliki ke dalam
	 * suatu kanvas.
	 * @param canvas kanvas tempat menggambar handler.
	 */
	public void draw(Canvas canvas, Paint selector) {
		canvas.save();
		canvas.translate(object.offsetX, object.offsetY);
		canvas.rotate(object.rotation);
		canvas.drawRect(bounds, selector);
		for (int i = 0; i < size; i++) {
			ControlPoint cp = points[i];
			if (cp.enable)
				cp.draw(canvas);
		}
		canvas.restore();
	}

	protected static final float[] grabPoints = new float[6];

	/**
	 * Mencoba menangkap titik kontrol yang ada di posisi tertentu.
	 * @param worldX koordinat x
	 * @param worldY koordinat y
	 * @return titik kontrol yang berhasil ditangkap, atau <code>null</code>
	 *         jika tidak ada titik kontrol pada posisi tersebut.
	 */
	public ControlPoint grab(float worldX, float worldY) {
		grabPoints[ControlPoint.ORI_X] = worldX;
		grabPoints[ControlPoint.ORI_Y] = worldY;
		worldX -= object.offsetX;
		worldY -= object.offsetY;
		grabPoints[ControlPoint.TRANS_X] = worldX;
		grabPoints[ControlPoint.TRANS_Y] = worldY;
		double deg = Math.toRadians(-object.rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		grabPoints[ControlPoint.OBJ_X] = (float) (worldX * cos + worldY * -sin);
		grabPoints[ControlPoint.OBJ_Y] = (float) (worldX * sin + worldY * cos);
		for (int i = 0; i < size; i++) {
			ControlPoint cp = points[i];
			if (cp.enable && cp.grabbed(grabPoints))
				return cp;
		}
		return null;
	}

	/**
	 * Mengatur keaktifan dari semua titik kontrol.
	 * @param enable
	 */
	void setEnableAllPoints(boolean enable) {
		for (int i = 0; i < size; i++)
			points[i].enable = enable;
	}

	/**
	 * Menginisiasi handler.
	 */
	public void init() {
		object.getBounds(bounds);
	}

	protected static final float[] movePoints = new float[6];

	/**
	 * Mencoba menggeser suatu titik kontrol ke arah tertentu.
	 * @param cp titik kontrol yang digeser.
	 * @param worldX koordinat x titik tujuan.
	 * @param worldY koordinat y titik tujuan.
	 */
	public void dragPoint(ControlPoint cp, float worldX, float worldY) {
		float ox = cp.x;
		float oy = cp.y;
		movePoints[ControlPoint.ORI_X] = worldX;
		movePoints[ControlPoint.ORI_Y] = worldY;
		worldX -= object.offsetX;
		worldY -= object.offsetY;
		movePoints[ControlPoint.TRANS_X] = worldX;
		movePoints[ControlPoint.TRANS_Y] = worldY;
		double deg = Math.toRadians(-object.rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		movePoints[ControlPoint.OBJ_X] = (float) (worldX * cos - worldY * sin);
		movePoints[ControlPoint.OBJ_Y] = (float) (worldX * sin + worldY * cos);
		cp.moveTo(movePoints);
		object.onHandlerMoved(this, cp, ox, oy);
		object.getBounds(bounds);
	}

	/**
	 * Melepaskan titik kontrol yang dipegang.
	 * @param cp
	 */
	public void releasePoint(ControlPoint cp) {
		object.onHandlerRelease(cp);
		cp.release();
	}
}
