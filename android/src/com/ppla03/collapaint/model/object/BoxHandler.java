package com.ppla03.collapaint.model.object;

import android.graphics.RectF;

/**
 * Handler yang bisa mengurusi objek yang memiliki bentuk kotak.
 * @author hamba v7
 * 
 */
class BoxHandler extends ShapeHandler {
	private static final RectF rect = new RectF();
	private static final int LEFT_TOP = 0, RIGHT_TOP = 1, LEFT_BOTTOM = 2,
			RIGHT_BOTTOM = 3, ROTATOR = 4, MOVER = 5;
	private static final Rotator rotator = new Rotator(0, 0, 200, 0, ROTATOR);
	private static final Mover mover = new Mover(0, 0, MOVER);
	private static final ControlPoint[] points = { new Joint(0, 0, LEFT_TOP),
			new Joint(0, 0, RIGHT_TOP), new Joint(0, 0, LEFT_BOTTOM),
			new Joint(0, 0, RIGHT_BOTTOM), rotator, mover };

	private static final BoxHandler instance = new BoxHandler();

	private BoxHandler() {
		super(null, points);
		size = 6;
	}

	/**
	 * Ukuran minimum kotak.
	 */
	private static final float MIN_RECT_SIZE = 20;

	/**
	 * Mendapatkan handler dari objek kanvas yang sudah diatur di
	 * {@code handle(CanvasObject, RectF)}.
	 */
	static BoxHandler getHandler(int filter) {
		instance.setEnableAllPoints(false);
		if ((filter & ShapeHandler.SHAPE) == ShapeHandler.SHAPE) {
			points[LEFT_TOP].enable = true;
			points[RIGHT_TOP].enable = true;
			points[LEFT_BOTTOM].enable = true;
			points[RIGHT_BOTTOM].enable = true;
		}
		if ((filter & ROTATE) == ROTATE) {
			rotator.enable = true;
		}
		if ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE) {
			mover.enable = true;
		}
		return instance;
	}

	static void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		if (point.id == MOVER) {
			instance.object.offsetX += point.x;
			instance.object.offsetY += point.y;
			point.setPosition(0, 0);
		} else if (point.id == ROTATOR) {
			instance.object.rotation = rotator.getRotation();
		} else {
			if (point.id == LEFT_TOP) {
				if (rect.right - point.x < MIN_RECT_SIZE)
					point.x = oldX;
				if (rect.bottom - point.y < MIN_RECT_SIZE)
					point.y = oldY;
				rect.left = point.x;
				rect.top = point.y;
			} else if (point.id == RIGHT_TOP) {
				if (point.x - rect.left < MIN_RECT_SIZE)
					point.x = oldX;
				if (rect.bottom - point.y < MIN_RECT_SIZE)
					point.y = oldY;
				rect.right = point.x;
				rect.top = point.y;
			} else if (point.id == LEFT_BOTTOM) {
				if (rect.right - point.x < MIN_RECT_SIZE)
					point.x = oldX;
				if (point.y - rect.top < MIN_RECT_SIZE)
					point.y = oldY;
				rect.left = point.x;
				rect.bottom = point.y;
			} else if (point.id == RIGHT_BOTTOM) {
				if (point.x - rect.left < MIN_RECT_SIZE)
					point.x = oldX;
				if (point.y - rect.top < MIN_RECT_SIZE)
					point.y = oldY;
				rect.right = point.x;
				rect.bottom = point.y;
			}

			float cx = rect.centerX();
			float cy = rect.centerY();
			instance.object.offsetRelative(cx, cy);
			rect.offset(-cx, -cy);
			points[LEFT_TOP].x = rect.left;
			points[LEFT_TOP].y = rect.top;
			points[LEFT_BOTTOM].x = rect.left;
			points[LEFT_BOTTOM].y = rect.bottom;
			points[RIGHT_TOP].x = rect.right;
			points[RIGHT_TOP].y = rect.top;
			points[RIGHT_BOTTOM].x = rect.right;
			points[RIGHT_BOTTOM].y = rect.bottom;
		}
	}

	/**
	 * Perintahkan untuk menangani suatu objek tertentu. Sekaligus memperbaiki
	 * posisi kotak r sehingga offset berada di tengah.
	 * @param object objek yang ditangani
	 * @param r kotak batas-batas objek
	 */
	static void handle(CanvasObject object, RectF r) {
		instance.object = object;
		float cx = r.centerX();
		float cy = r.centerY();
		object.offsetX += cx;
		object.offsetY += cy;
		r.offset(-cx, -cy);
		rect.set(r);
		points[LEFT_TOP].setPosition(rect.left, rect.top);
		points[LEFT_BOTTOM].setPosition(rect.left, rect.bottom);
		points[RIGHT_TOP].setPosition(rect.right, rect.top);
		points[RIGHT_BOTTOM].setPosition(rect.right, rect.bottom);
		cx = r.centerX();
		cy = r.centerY();
		mover.setPosition(cx, cy);
		rotator.setCenter(cx, cy).setRotation(object.rotation);
	}

	/**
	 * Memasukkan hasil perubahan ke suatu variabel kotak
	 * @param r tempat menampung hasil perubahan.
	 */
	static void mapTo(RectF r) {
		r.set(rect);
	}
}
