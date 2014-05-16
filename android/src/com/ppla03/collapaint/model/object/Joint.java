package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

/**
 * Titik kontrol sambungan garis.
 * @author hamba v7
 * 
 */
public class Joint extends ControlPoint {

	private static final int JOINT_COLOR1 = Color.argb(200, 255, 150, 150);
	private static final int JOINT_COLOR2 = Color.argb(200, 250, 0, 0);
	private static final Paint paint;
	static {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.FILL);
		paint.setShader(new RadialGradient(5, -5, DRAW_RADIUS, JOINT_COLOR1,
				JOINT_COLOR2, TileMode.CLAMP));
	}

	/**
	 * Membuat titik sambungan dengan parameter tertentu.
	 * @param x posisi x
	 * @param y posisi y
	 * @param id id titik
	 */
	public Joint(float x, float y, int id) {
		super(x, y, id);
		paint.setAlpha(255);
	}

	@Override
	public boolean grabbed(float[] points) {
		float dx = this.x - points[OBJ_X];
		float dy = this.y - points[OBJ_Y];
		grabbed = (dx * dx + dy * dy) < GRAB_RADIUS_SQUARED;
		if (grabbed)
			paint.setAlpha(200);
		return grabbed;
	}

	@Override
	void moveTo(float[] points) {
		this.x = points[OBJ_X];
		this.y = points[OBJ_Y];
	}

	@Override
	public void release() {
		super.release();
		paint.setAlpha(255);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		canvas.drawCircle(0, 0, DRAW_RADIUS, paint);
		canvas.restore();
	}

}
