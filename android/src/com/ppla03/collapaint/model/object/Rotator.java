package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class Rotator extends ControlPoint {
	private static final int ROTATE_COLOR1 = Color.argb(200, 10, 255, 150);
	private static final int ROTATE_COLOR2 = Color.argb(200, 0, 250, 20);

	private static final Paint paint, linePaint;
	static {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.FILL);
		paint.setShader(new RadialGradient(5, -5, DRAW_RADIUS, ROTATE_COLOR1,
				ROTATE_COLOR2, TileMode.CLAMP));
		linePaint = new Paint();
		linePaint.setStyle(Style.STROKE);
		linePaint.setStrokeWidth(2);
		StrokeStyle.applyEffect(StrokeStyle.SOLID, linePaint);
	}

	private float centerX, centerY;
	private float radius;
	private float rotation;

	public Rotator(float centerX, float centerY, float radius, float degree,
			int id) {
		super(centerX + radius, centerY, id);
		this.radius = radius;
		setCenter(centerX, centerY).setRotation(degree);
		paint.setAlpha(255);
	}

	@Override
	public boolean grabbed(float[] points) {
		float dx = points[TRANS_X] - this.x;
		float dy = points[TRANS_Y] - this.y;
		grabbed = (dx * dx + dy * dy) < GRAB_RADIUS_SQUARED;
		if (grabbed)
			paint.setAlpha(200);
		return grabbed;
	}

	@Override
	void moveTo(float[] points) {
		float dx = points[TRANS_X] - centerX;
		float dy = points[TRANS_Y] - centerY;
		double rad = Math.atan(dy / dx);
		if (dx < 0)
			rad += Math.PI;
		rotation = (float) Math.toDegrees(rad);
		this.x = (float) (centerX + radius * Math.cos(rad));
		this.y = (float) (centerY + radius * Math.sin(rad));
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
		canvas.drawCircle(0, -radius, DRAW_RADIUS, paint);
	}

	public Rotator setCenter(float x, float y) {
		this.centerX = x;
		this.centerY = y;
		double rad = Math.toRadians(rotation);
		this.x = (float) (centerX + Math.cos(rad));
		this.y = (float) (centerY + Math.sin(rad));
		return this;
	}

	public float getRotation() {
		return rotation + 90;
	}

	public Rotator setRotation(float angle) {
		rotation = angle - 90;
		double rad = Math.toRadians(rotation);
		x = (float) (centerX + radius * Math.cos(rad));
		y = (float) (centerY + radius * Math.sin(rad));
		return this;
	}
}
