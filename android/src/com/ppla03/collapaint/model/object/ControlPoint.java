package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

/**
 * Titik kontrol suatu objek.
 * @author hamba v7
 * 
 */
public class ControlPoint {
	public static enum Type {
		MOVE, JOINT, ROTATE
	}

	private static final int DRAW_RADIUS = 15;
	private static final int GRAB_RADIUS_SQUARED = 30 * 30;
	private static final int JOINT_COLOR1 = Color.argb(200, 255, 150, 150);
	private static final int JOINT_COLOR2 = Color.argb(200, 250, 0, 0);
	private static final float MOVER_CROSS_SIZE = 10;
	private static Paint jointPaint;
	private static Paint moverPaint;

	private static float anchorX, anchorY, refX, refY;

	static {
		jointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		jointPaint.setStyle(Style.FILL);
		jointPaint.setShader(new RadialGradient(5, -5, DRAW_RADIUS,
				JOINT_COLOR1, JOINT_COLOR2, TileMode.CLAMP));
		moverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		moverPaint.setStyle(Style.STROKE);
		moverPaint.setStrokeWidth(1);
		moverPaint.setColor(Color.BLACK);
	}

	final Type type;
	final int id;

	/**
	 * Menandakan titik sedang digerakkan.
	 */
	boolean grabbed;

	float x, y;

	/**
	 * Menandakan titik dapat dicengkeram.
	 */
	boolean enable;

	/**
	 * Membuat titik kontrol baru.
	 * @param type tipe titik kontrol. Lihat {@link Type}.
	 * @param x koordinat x titik kontrol relatif terhadap pusat objek.
	 * @param y koordinat y titik kontrol relatif terhadap pusat objek.
	 * @param id id titik kontrol.
	 */
	public ControlPoint(Type type, float x, float y, int id) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.id = id;
		jointPaint.setAlpha(100);
		moverPaint.setAlpha(200);
		enable = true;
	}

	/**
	 * Mencoba mencengkeram titik untuk sehingga bisa digerakkan.
	 * @param x koordinat x cengkeraman relatif terhadap pusat objek.
	 * @param y koordinat y cengkeraman relatif terhadap pusat objek.
	 * @return berhasil atau tidak.
	 */
	public boolean grabbed(float x, float y) {
		float dx = this.x - x;
		float dy = this.y - y;
		grabbed = (type == Type.MOVE)
				|| (dx * dx + dy * dy) < GRAB_RADIUS_SQUARED;
		if (grabbed) {
			anchorX = x;
			anchorY = y;
			refX = this.x;
			refY = this.y;
			if (type == Type.JOINT)
				jointPaint.setAlpha(255);
			else
				moverPaint.setAlpha(250);
		}
		return grabbed;
	}

	/**
	 * Melepaskan cengkeraman ke titik.
	 */
	public void release() {
		grabbed = false;
		jointPaint.setAlpha(100);
		moverPaint.setAlpha(200);
	}

	/**
	 * Mencoba menggeser titik ke koordinat tertentu. Perpidahan akan
	 * dikalibrasi berdasarkan posisi terakhir titik berhasil dicengkeram.
	 * Posisi titik akan bergantung dari tipe titik.
	 * @param dx perpindahan dalam sumbu x.
	 * @param dy perpindahan dalam sumbu y.
	 */
	void moveTo(float dx, float dy) {
		this.x = refX + (dx - anchorX);
		this.y = refY + (dy - anchorY);
	}

	/**
	 * Memindahkan titik ke koordinat tertentu. Tidak tergantung dari tipe titik
	 * ini.
	 * @param x koordinat x relatif terhadap pusat objek.
	 * @param y koordinat y relatif terhadap pusat objek.
	 */
	void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Menggambar titik kontrol pada suatu kanvas.
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		if (type == Type.JOINT)
			canvas.drawCircle(0, 0, DRAW_RADIUS, jointPaint);
		else {
			canvas.drawLine(x - MOVER_CROSS_SIZE, y - MOVER_CROSS_SIZE, x
					+ MOVER_CROSS_SIZE, y + MOVER_CROSS_SIZE, moverPaint);
			canvas.drawLine(x - MOVER_CROSS_SIZE, y + MOVER_CROSS_SIZE, x
					+ MOVER_CROSS_SIZE, y - MOVER_CROSS_SIZE, moverPaint);
		}
		canvas.restore();
	}
}
