package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Titik untuk mengatur rotasi objek.
 * @author hamba v7
 * 
 */
public class Rotator extends ControlPoint {
	public static final float MIN_RADIUS = 50;

	private static final Paint paint, linePaint;
	static {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.FILL);
		paint.setColor(Color.rgb(153, 204, 0));
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Style.STROKE);
		linePaint.setColor(Color.DKGRAY);
		linePaint.setStrokeWidth(1);
		StrokeStyle.applyEffect(StrokeStyle.DOTTED, linePaint);
	}

	/**
	 * Jarak titik pegangan ke pusat rotasi
	 */
	protected float radius;

	/**
	 * Besar sudut rotasi dalam derajat
	 */
	protected float rotation;

	public Rotator(float centerX, float centerY, float radius, float degree,
			int id) {
		super(centerX, centerY, id);
		this.radius = radius;
		setRotation(degree);
		paint.setAlpha(255);
	}

	@Override
	public boolean grabbed(float[] points) {
		float dx = points[OBJ_X] - this.x;
		float dy = points[OBJ_Y] - this.y + radius;
		grabbed = (dx * dx + dy * dy) < GRAB_RADIUS_SQUARED;
		if (grabbed)
			paint.setAlpha(200);
		return grabbed;
	}

	@Override
	void moveTo(float[] points) {
		float dx = points[TRANS_X] - this.x;
		float dy = points[TRANS_Y] - this.y;
		double rad = Math.atan(dy / dx);
		if (dx < 0)
			rad += Math.PI;
		rotation = (float) Math.toDegrees(rad);
	}

	@Override
	void setPosition(float x, float y) {}

	@Override
	public void release() {
		super.release();
		paint.setAlpha(255);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawLine(0, 0, 0, -radius, linePaint);
		if (grabbed) {
			paint.setAlpha(100);
			canvas.drawCircle(0, -radius, DRAW_RADIUS + DRAW_RADIUS, paint);
		}
		paint.setAlpha(255);
		canvas.drawCircle(0, -radius, DRAW_RADIUS, paint);
	}

	/**
	 * Mengatur titik pusat rotasi
	 * @param x
	 * @param y
	 * @return
	 */
	public Rotator setCenter(float x, float y) {
		this.x = x;
		this.x = y;
		return this;
	}

	/**
	 * Mengambil nilai rotasi dalam derajat.
	 * @return
	 */
	public float getRotation() {
		return rotation + 90;
	}

	/**
	 * Mengatur nilai rotasi.
	 * @param angle rotasi dalam derajat
	 * @return
	 */
	public Rotator setRotation(float angle) {
		rotation = angle - 90;
		return this;
	}
}
