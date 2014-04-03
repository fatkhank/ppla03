package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

class BoxHandler {

	static final Rect rect = new Rect();
	private static final int LEFT_TOP = 0, RIGHT_TOP = 1, LEFT_BOTTOM = 2,
			RIGHT_BOTTOM = 3, CENTER = 4;
	private static final ControlPoint[] points = new ControlPoint[] {
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, LEFT_TOP),
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, RIGHT_TOP),
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, LEFT_BOTTOM),
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, RIGHT_BOTTOM),
			new ControlPoint(ControlPoint.Type.MOVE, 0, 0, CENTER) };

	private static final ShapeHandler handler = new ShapeHandler(points);

	static final int MIN_RECT_SIZE = 20;

	static ShapeHandler getHandlers(int filter) {
		handler.setEnableAllPoint(false);
		if ((filter & ShapeHandler.SHAPE_ONLY) == ShapeHandler.SHAPE_ONLY) {
			points[LEFT_TOP].enable = true;
			points[RIGHT_TOP].enable = true;
			points[LEFT_BOTTOM].enable = true;
			points[RIGHT_BOTTOM].enable = true;
		}
		if ((filter & ShapeHandler.TRANSFORM_ONLY) == ShapeHandler.TRANSFORM_ONLY) {
			points[CENTER].enable = true;
		}
		return handler;
	}

	static void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		if (point.id == CENTER) {
			rect.offset(point.x - oldX, point.y - oldY);
			points[LEFT_TOP].setPosition(rect.left, rect.top);
			points[RIGHT_TOP].setPosition(rect.right, rect.top);
			points[LEFT_BOTTOM].setPosition(rect.left, rect.bottom);
			points[RIGHT_BOTTOM].setPosition(rect.right, rect.bottom);
		} else {
			if (point.id == LEFT_TOP) {
				if (rect.right - point.x < MIN_RECT_SIZE)
					point.x = oldX;
				if (rect.bottom - point.y < MIN_RECT_SIZE)
					point.y = oldY;
				rect.left = point.x;
				rect.top = point.y;
				points[LEFT_BOTTOM].x = rect.left;
				points[RIGHT_TOP].y = rect.top;
			} else if (point.id == RIGHT_TOP) {
				if (point.x - rect.left < MIN_RECT_SIZE)
					point.x = oldX;
				if (rect.bottom - point.y < MIN_RECT_SIZE)
					point.y = oldY;
				rect.right = point.x;
				rect.top = point.y;
				points[RIGHT_BOTTOM].x = rect.right;
				points[LEFT_TOP].y = rect.top;
			} else if (point.id == LEFT_BOTTOM) {
				if (rect.right - point.x < MIN_RECT_SIZE)
					point.x = oldX;
				if (point.y - rect.top < MIN_RECT_SIZE)
					point.y = oldY;
				rect.left = point.x;
				rect.bottom = point.y;
				points[LEFT_TOP].x = rect.left;
				points[RIGHT_BOTTOM].y = rect.bottom;
			} else if (point.id == RIGHT_BOTTOM) {
				if (point.x - rect.left < MIN_RECT_SIZE)
					point.x = oldX;
				if (point.y - rect.top < MIN_RECT_SIZE)
					point.y = oldY;
				rect.right = point.x;
				rect.bottom = point.y;
				points[RIGHT_TOP].x = rect.right;
				points[LEFT_BOTTOM].y = rect.bottom;
			}
			points[CENTER].setPosition(rect.centerX(), rect.centerY());
		}
	}
	
	static void handle(Rect r) {
		rect.left = r.left;
		rect.top = r.top;
		rect.right = r.right;
		rect.bottom = r.bottom;
		points[LEFT_TOP].x = r.left;
		points[LEFT_TOP].y = r.top;
		points[LEFT_BOTTOM].x = r.left;
		points[LEFT_BOTTOM].y = r.bottom;
		points[RIGHT_TOP].x = r.right;
		points[RIGHT_TOP].y = r.top;
		points[RIGHT_BOTTOM].x = r.right;
		points[RIGHT_BOTTOM].y = r.bottom;
		points[CENTER].x = r.centerX();
		points[CENTER].y = r.centerY();
		handler.size = 5;
	}

	static void handle(RectF r) {
		rect.left = (int) r.left;
		rect.top = (int) r.top;
		rect.right = (int) r.right;
		rect.bottom = (int) r.bottom;
		points[LEFT_TOP].x = rect.left;
		points[LEFT_TOP].y = rect.top;
		points[LEFT_BOTTOM].x = rect.left;
		points[LEFT_BOTTOM].y = rect.bottom;
		points[RIGHT_TOP].x = rect.right;
		points[RIGHT_TOP].y = rect.top;
		points[RIGHT_BOTTOM].x = rect.right;
		points[RIGHT_BOTTOM].y = rect.bottom;
		points[CENTER].x = rect.centerX();
		points[CENTER].y = rect.centerY();
	}

	static void mapTo(Rect r) {
		r.left = rect.left;
		r.top = rect.top;
		r.right = rect.right;
		r.bottom = rect.bottom;
	}

	static void mapTo(RectF r) {
		r.left = rect.left;
		r.top = rect.top;
		r.right = rect.right;
		r.bottom = rect.bottom;
	}
}
