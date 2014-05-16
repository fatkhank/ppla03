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
			RIGHT_BOTTOM = 3, LEFT = 4, TOP = 5, RIGHT = 6, BOTTOM = 7,
			ROTATOR = 8, MOVER = 9;
	private static final Rotator rotator = new Rotator(0, 0, 200, 0, ROTATOR);
	private static final Mover mover = new Mover(0, 0, null, MOVER);

	private static final Corner cornerLeftTop = new Corner(0, 0, LEFT_TOP,
			Corner.LEFT_TOP);
	private static final Corner cornerRightTop = new Corner(0, 0, RIGHT_TOP,
			Corner.RIGHT_TOP);
	private static final Corner cornerLeftBottom = new Corner(0, 0,
			LEFT_BOTTOM, Corner.LEFT_BOTTOM);
	private static final Corner cornerRightBottom = new Corner(0, 0,
			RIGHT_BOTTOM, Corner.RIGHT_BOTTOM);

	private static final SidePoint sideLeft = new SidePoint(0, 0, LEFT,
			SidePoint.LEFT);
	private static final SidePoint sideTop = new SidePoint(0, 0, TOP,
			SidePoint.TOP);
	private static final SidePoint sideRight = new SidePoint(0, 0, RIGHT,
			SidePoint.RIGHT);
	private static final SidePoint sideBottom = new SidePoint(0, 0, BOTTOM,
			SidePoint.BOTTOM);

	private static final ControlPoint[] points = { cornerLeftTop,
			cornerRightTop, cornerLeftBottom, cornerRightBottom, sideLeft,
			sideTop, sideRight, sideBottom, rotator, mover };

	private static final BoxHandler instance = new BoxHandler();

	private BoxHandler() {
		super(null, points);
		size = 10;
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
			cornerLeftTop.enable = true;
			cornerRightTop.enable = true;
			cornerLeftBottom.enable = true;
			cornerRightBottom.enable = true;
			sideLeft.enable = true;
			sideTop.enable = true;
			sideRight.enable = true;
			sideBottom.enable = true;
		}
		if ((filter & ROTATE) == ROTATE) {
			rotator.radius = rect.bottom + Rotator.MIN_RADIUS;
			rotator.enable = true;
		}
		if ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE) {
			mover.setObject(instance.object);
			mover.enable = true;
		}
		return instance;
	}

	private static final RectF anchorRect = new RectF();

	@Override
	public ControlPoint grab(float worldX, float worldY) {
		ControlPoint cp = super.grab(worldX, worldY);
		if (cp instanceof Corner) {
			// oldDiag = Math.sqrt(rect.width() * rect.width() * 0.25f)
			// + (rect.height() * rect.height() * 0.25f);
			anchorRect.set(rect);
		}
		return cp;
	}

	static void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		if (point == mover) {
			instance.object.offsetX += point.x;
			instance.object.offsetY += point.y;
			point.setPosition(0, 0);
		} else if (point == rotator) {
			instance.object.rotation = rotator.getRotation();
		} else {
			// cek titik pinggir
			if (point == sideLeft) {
				if (rect.right - point.x < MIN_RECT_SIZE)
					point.x = oldX;
				rect.left = point.x;
			} else if (point == sideTop) {
				if (rect.bottom - point.y < MIN_RECT_SIZE)
					point.y = oldY;
				rect.top = point.y;
			} else if (point == sideRight) {
				if (point.x - rect.left < MIN_RECT_SIZE)
					point.x = oldX;
				rect.right = point.x;
			} else if (point == sideBottom) {
				if (point.y - rect.top < MIN_RECT_SIZE)
					point.y = oldY;
				rect.bottom = point.y;
			} else {
				// cek titik pojok
				if (point == cornerLeftTop) {
					if (rect.right - point.x < MIN_RECT_SIZE)
						point.x = oldX;
					if (rect.bottom - point.y < MIN_RECT_SIZE)
						point.y = oldY;
					rect.left = point.x;
					rect.top = point.y;
				} else if (point == cornerRightTop) {
					if (point.x - rect.left < MIN_RECT_SIZE)
						point.x = oldX;
					if (rect.bottom - point.y < MIN_RECT_SIZE)
						point.y = oldY;
					rect.right = point.x;
					rect.top = point.y;
				} else if (point == cornerLeftBottom) {
					if (rect.right - point.x < MIN_RECT_SIZE)
						point.x = oldX;
					if (point.y - rect.top < MIN_RECT_SIZE)
						point.y = oldY;
					rect.left = point.x;
					rect.bottom = point.y;
				} else if (point == cornerRightBottom) {
					if (point.x - rect.left < MIN_RECT_SIZE)
						point.x = oldX;
					if (point.y - rect.top < MIN_RECT_SIZE)
						point.y = oldY;
					rect.right = point.x;
					rect.bottom = point.y;
				}
			}

			float cx = rect.centerX();
			float cy = rect.centerY();
			instance.object.offsetRelative(cx, cy);
			rect.offset(-cx, -cy);
			cornerLeftTop.setPosition(rect.left, rect.top);
			cornerRightTop.setPosition(rect.right, rect.top);
			cornerLeftBottom.setPosition(rect.left, rect.bottom);
			cornerRightBottom.setPosition(rect.right, rect.bottom);
			sideLeft.x = rect.left;
			sideTop.y = rect.top;
			sideRight.x = rect.right;
			sideBottom.y = rect.bottom;
			rotator.radius = rect.bottom + Rotator.MIN_RADIUS;
			mover.setObject(instance.object);
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
		cornerLeftTop.setPosition(rect.left, rect.top);
		cornerRightTop.setPosition(rect.right, rect.top);
		cornerLeftBottom.setPosition(rect.left, rect.bottom);
		cornerRightBottom.setPosition(rect.right, rect.bottom);
		sideLeft.x = rect.left;
		sideTop.y = rect.top;
		sideRight.x = rect.right;
		sideBottom.y = rect.bottom;
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
