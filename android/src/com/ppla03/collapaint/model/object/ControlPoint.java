package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class ControlPoint {
	public static enum Type {
		MOVE, JOINT
	}

	private static final int DRAW_RADIUS = 15;
	private static final int GRAB_RADIUS_SQUARED = 30 * 30;
	private static final int JOINT_COLOR1 = Color.argb(200, 255, 150, 150);
	private static final int JOINT_COLOR2 = Color.argb(200, 250, 0, 0);
	private static final int MOVER_COLOR1 = Color.argb(200, 150, 150, 150);
	private static final int MOVER_COLOR2 = Color.argb(100, 0, 0, 0);
	private static Paint jointPaint;
	private static Paint moverPaint;

	private static int anchorX, anchorY, refX, refY;

	static {
		jointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		jointPaint.setStyle(Style.FILL);
		jointPaint.setShader(new RadialGradient(5, -5, DRAW_RADIUS,
				JOINT_COLOR1, JOINT_COLOR2, TileMode.CLAMP));
		moverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		moverPaint.setStyle(Style.FILL);
		moverPaint.setShader(new RadialGradient(10, -10, DRAW_RADIUS << 1,
				MOVER_COLOR1, MOVER_COLOR2, TileMode.CLAMP));
	}

	private final Type type;
	private boolean grabbed;
	int x, y;
	final int id;
	boolean enable;

	public ControlPoint(Type type, int x, int y, int id) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.id = id;
		jointPaint.setAlpha(100);
		moverPaint.setAlpha(100);
		enable = true;
	}

	public boolean grabbed(int x, int y) {
		int dx = this.x - x;
		int dy = this.y - y;
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
				moverPaint.setAlpha(255);
		}
		return grabbed;
	}

	public void release() {
		grabbed = false;
		jointPaint.setAlpha(100);
		moverPaint.setAlpha(100);
	}

	public void moveTo(int x, int y) {
		this.x = refX + (x - anchorX);
		this.y = refY + (y - anchorY);
	}

	void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		if (type == Type.JOINT)
			canvas.drawCircle(0, 0, DRAW_RADIUS, jointPaint);
		else
			canvas.drawCircle(0, 0, DRAW_RADIUS << 1, moverPaint);
		canvas.restore();
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
