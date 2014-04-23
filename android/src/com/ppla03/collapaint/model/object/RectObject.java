package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * Objek dalam kanvas berbentuk kotak.
 * @author hamba v7
 * 
 */
public class RectObject extends BasicObject {
	private final RectF rect;

	/**
	 * Membuat kotak kosong.
	 */
	public RectObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		this.rect = new RectF();
	}

	/**
	 * Membuat {@link RectObject} berbentuk persegi dengan parameter tertentu.
	 * @param worldX koordinat x titik pusat kotak. (koordinat kanvas)
	 * @param worldY koordinat y titik pusat kotak. (koordinat kanvas)
	 * @param size lebar sisi kotak
	 * @param fillColor warna isian objek. Jika warna =
	 *            {@code Color.TRANSPARENT}, dianggap objek tidak memiliki
	 *            isian. Lihat {@link Color}.
	 * @param strokeColor warna pinggiran objek.
	 * @param strokeWidth lebar pinggiran objek.
	 * @param strokeStyle jenis dekorasi pinggiran objek.
	 */
	public RectObject(int worldX, int worldY, int size, int fillColor,
			int strokeColor, int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		this.offsetX = worldX;
		this.offsetY = worldY;
		int half = size >> 1;
		this.rect = new RectF(-half, -half, half, half);
	}

	@Override
	public void drawSelf(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, strokePaint);
	}

	@Override
	public void setGeom(float[] param, int start, int end) {
		rect.right = param[start++];
		rect.bottom = param[start];
		rect.left = -rect.right;
		rect.top = -rect.bottom;
	}

	@Override
	public int geomParamLength() {
		return 2;
	}

	@Override
	public int extractGeom(float[] data, int start) {
		data[start++] = rect.right;
		data[start] = rect.bottom;
		return 2;
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			return rect.contains(x, y);
		float tol = strokePaint.getStrokeWidth() + radius;
		return (x > rect.left - tol && x < rect.right + tol
				&& y > rect.top - tol && y < rect.bottom + tol)
				&& !(x > rect.left + tol && x < rect.right - tol
						&& y > rect.top + tol && y < rect.bottom - tol);
	}

	@Override
	public ShapeHandler getHandler(int filter) {
		BoxHandler.handle(this, rect);
		return BoxHandler.getHandler(filter);
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		BoxHandler.onHandlerMoved(handler, point, oldX, oldY);
		BoxHandler.mapTo(rect);
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(rect);
	}

	@Override
	public CanvasObject cloneObject() {
		RectObject ro = new RectObject();
		ro.rect.set(this.rect);
		copyTransformData(ro);
		modifyStyles(ro);
		return ro;
	}
}
