package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * Objek dalam kanvas yang berbentuk lingkaran, atau elips. Objek ini bisa
 * dirotasi dan translasi.
 * @author hamba v7
 * 
 */
public class OvalObject extends BasicObject {
	private final RectF bounds;

	/**
	 * Membuat lingkaran berdiameter 0, tanpa isian, dengan pinggiran berwarna
	 * hitam, lebar 1 dan garis solid.
	 */
	public OvalObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		bounds = new RectF();
	}

	/**
	 * Membuat objek lingkaran dengan parameter tertentu.
	 * @param worldX koordinat x pusat lingkaran di kanvas.
	 * @param worldY koordinat y pusat lingkaran di kanvas.
	 * @param radius jari-jari lingkaran
	 * @param fillColor warna isian objek. Lihat {@link Color}.
	 * @param strokeColor warna pinggiran objek. Lihat {@link Color}.
	 * @param strokeWidth tebal pinggiran objek.
	 * @param strokeStyle jenis dekorasi pinggiran objek. Lihat
	 *            {@link StrokeStyle}.
	 */
	public OvalObject(float worldX, float worldY, float radius, int fillColor,
			int strokeColor, int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		offsetX = worldX;
		offsetY = worldY;
		bounds = new RectF(-radius, -radius, radius, radius);
	}

	@Override
	public void setGeom(float[] param, int start, int end) {
		bounds.right = param[start++];
		bounds.bottom = param[start];
		bounds.left = -bounds.right;
		bounds.top = -bounds.bottom;
	}

	@Override
	public int geomParamLength() {
		return 2;
	}

	@Override
	public int extractGeom(float[] data, int start) {
		data[start++] = bounds.right;
		data[start] = bounds.bottom;
		return 2;
	}

	@Override
	public void drawSelf(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bounds, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bounds, strokePaint);
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		float tol = strokePaint.getStrokeWidth() + radius;
		// quick reject
		if (x < bounds.left - tol || x > bounds.right + tol
				|| y < bounds.top - tol || y > bounds.bottom + tol)
			return false;
		else {
			// persamaan ellipse -> (x/radx)^2 + (y/rady)^2 = 1
			float x2 = x * x;
			float y2 = y * y;
			float radX = bounds.right + tol;// radius x luar
			radX *= radX;
			float radY = bounds.bottom + tol;// radius y luar
			radY *= radY;
			// jika melebihi radius luar, maka tidak terseleksi
			if ((x2 / radX + y2 / radY) > 1)
				return false;
			// jika objek memiliki isian, cukup dicek sampai sini
			if (fillPaint.getColor() != Color.TRANSPARENT)
				return true;
			radX -= 4 * bounds.right * tol;// radius x dalam
			radY -= 4 * bounds.bottom * tol;// radius y dalam
			//harus berada di luar radius dalam
			return (x2 / radX + y2 / radY) > 1;
		}
	}

	@Override
	public ShapeHandler getHandler(int filter) {
		BoxHandler.handle(this, bounds);
		return BoxHandler.getHandler(filter);
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		BoxHandler.onHandlerMoved(handler, point, oldX, oldY);
		BoxHandler.mapTo(bounds);
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(this.bounds);
	}

	@Override
	public OvalObject cloneObject() {
		OvalObject oo = new OvalObject();
		oo.bounds.set(this.bounds);
		copyTransformData(oo);
		modifyStyles(oo);
		return oo;
	}

}
