package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import org.apache.http.client.CircularRedirectException;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;

public class OvalObject extends BasicObject {
	private RectF bound;

	public OvalObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		bound = new RectF();
	}

	public OvalObject(int x, int y, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		bound = new RectF();
		bound.left = x;
		bound.top = y;
		bound.right = x;
		bound.bottom = y;
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bound, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bound, strokePaint);
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = area.contains((int) bound.left, (int) bound.top,
				(int) bound.right, (int) bound.bottom));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		int tol = (int) strokePaint.getStrokeWidth() + radius;
		if (x < bound.left - tol || x > bound.right + tol
				|| y < bound.top - tol || y > bound.top)
			selected = false;
		else {
			float cx = bound.centerX();
			float cy = bound.centerY();
			x -= cx;
			y -= cy;
			float r2 = x * x + y * y;
			float max = Math.max((bound.right - cx), (bound.bottom - cy));
			float appr2 = max + tol;
			appr2 *= appr2;
			selected = (r2 <= appr2)
					&& ((fillPaint.getColor() != Color.TRANSPARENT) || r2 > appr2
							- 4 * max * tol);
		}
		return selected;
	}

	@Override
	public void translate(int x, int y) {
		bound.offset(x, y);
	}

	@Override
	public ShapeHandler getHandlers() {
		BoxTool.handle(bound);
		return BoxTool.getHandlers();
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		BoxTool.onHandlerMoved(handler, point, oldX, oldY);
		BoxTool.mapTo(bound);
	}

	public void setDimension(int left, int top, int right, int bottom) {
		bound.left = left;
		bound.top = top;
		bound.right = right;
		bound.bottom = bottom;
	}

}
