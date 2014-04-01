package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

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
	public boolean selectedBy(Rect area) {
		return (selected = area.contains(rect));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			return rect.contains(x, y);
		int tol = (int) strokePaint.getStrokeWidth() + radius;
		return (selected = (x > rect.left - tol && x < rect.right + tol
				&& y > rect.top - tol && y < rect.bottom + tol)
				&& !(x > rect.left + tol && x < rect.right - tol
						&& y > rect.top + tol && y < rect.bottom - tol));
	}

	@Override
	public void translate(int x, int y) {
		rect.offset(x, y);
	}

	@Override
	public ShapeHandler getHandlers() {
		BoxTool.handle(rect);
		return BoxTool.getHandlers();
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		BoxTool.onHandlerMoved(handler, point, oldX, oldY);
		BoxTool.mapTo(rect);
	}
}
