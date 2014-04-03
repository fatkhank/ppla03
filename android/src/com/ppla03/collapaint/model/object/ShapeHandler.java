package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;

public class ShapeHandler {
	public static final int SHAPE_ONLY = 1, TRANSFORM_ONLY = 2,
			ALL = SHAPE_ONLY | TRANSFORM_ONLY;

	int size;
	ControlPoint[] points;

	public ShapeHandler(ControlPoint[] points) {
		this.points = points;
		size = points.length;
	}

	public void draw(Canvas canvas) {
		for (int i = 0; i < size; i++) {
			ControlPoint cp = points[i];
			if (cp.enable)
				cp.draw(canvas);
		}
	}

	public ControlPoint grab(int x, int y) {
		for (int i = 0; i < size; i++) {
			ControlPoint sh = points[i];
			if (sh.enable && sh.grabbed(x, y))
				return sh;
		}
		return null;
	}

	public int size() {
		return size;
	}

	void setEnableAllPoint(boolean en) {
		for (int i = 0; i < points.length; i++)
			points[i].enable = en;
	}
}
