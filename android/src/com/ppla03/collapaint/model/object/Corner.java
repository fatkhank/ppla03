package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;

/**
 * Membuat
 * @author hamba v7
 * 
 */
public class Corner extends ControlPoint {
	public static final int LEFT_TOP = 0;
	public static final int RIGHT_TOP = 90;
	public static final int LEFT_BOTTOM = -90;
	public static final int RIGHT_BOTTOM = 180;

	protected static float anchorX, anchorY;

	private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Path path = new Path();
	static {
		paint.setColor(Color.rgb(20,40,255));
		paint.setStyle(Style.FILL);
		path.moveTo(3, 3);
		path.lineTo(20, 3);
		path.lineTo(20, -3);
		path.lineTo(-3, -3);
		path.lineTo(-3, 20);
		path.lineTo(3, 20);
		path.lineTo(3, 0);
	}
	private int type;

	public Corner(float x, float y, int id, int type) {
		super(x, y, id);
		this.type = type;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		canvas.rotate(type);
		canvas.drawPath(path, paint);
		canvas.restore();
	}

	@Override
	public boolean grabbed(float[] points) {
		anchorX = points[OBJ_X];
		anchorY = points[OBJ_Y];
		float dx = anchorX - x;
		float dy = anchorY - y;
		anchorX = this.x;
		anchorY = this.y;
		return (dx * dx + dy * dy) < GRAB_RADIUS_SQUARED;
	}

	@Override
	void moveTo(float[] points) {
		// rasio terhadap posisi awal
		float mx = 1, my = 1;
		if (Math.abs(anchorX) > 0.01f || Math.abs(anchorY) > 0.01f) {
			mx = points[OBJ_X] / anchorX;
			my = points[OBJ_Y] / anchorY;
		}

		// cari rasio yang maksimal
		float mr = Math.max(mx, my);
		this.x = anchorX * mr;
		this.y = anchorY * mr;
	}

}
