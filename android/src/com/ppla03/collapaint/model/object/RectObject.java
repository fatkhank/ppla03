package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.nfc.cardemulation.OffHostApduService;
import android.util.Log;
import android.view.animation.Transformation;

public class RectObject extends BasicObject {
	private Rect rect;

	public RectObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		this.rect = new Rect();
	}

	public RectObject(int x, int y, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		this.rect = new Rect(x, y, x, y);
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, strokePaint);
	}

	public void setDimension(int left, int top, int right, int bottom) {
		rect.left = left;
		rect.top = top;
		rect.right = right;
		rect.bottom = bottom;
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		rect.left = param[start++];
		rect.top = param[start++];
		rect.right = param[start++];
		rect.bottom = param[start];
	}

	@Override
	public int paramLength() {
		return 4;
	}

	@Override
	public int extractShape(int[] data, int start) {
		data[start++] = rect.left;
		data[start++] = rect.top;
		data[start++] = rect.right;
		data[start] = rect.bottom;
		return 4;
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = area.contains(rect));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			return (selected = rect.contains(x, y));
		int tol = (int) strokePaint.getStrokeWidth() + radius;
		return (selected = (x > rect.left - tol && x < rect.right + tol
				&& y > rect.top - tol && y < rect.bottom + tol)
				&& !(x > rect.left + tol && x < rect.right - tol
						&& y > rect.top + tol && y < rect.bottom - tol));
	}

	@Override
	public void translate(int dx, int dy) {
		rect.offset(dx, dy);
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		BoxHandler.handle(rect);
		return BoxHandler.getHandlers(filter);
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		BoxHandler.onHandlerMoved(handler, point, oldX, oldY);
		BoxHandler.mapTo(rect);
	}

	@Override
	public Rect getBounds() {
		return rect;
	}
}
