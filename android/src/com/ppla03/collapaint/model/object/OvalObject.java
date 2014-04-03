package com.ppla03.collapaint.model.object;

import org.apache.http.client.CircularRedirectException;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

public class OvalObject extends BasicObject {
	private RectF bounds;

	public OvalObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		bounds = new RectF();
	}

	/**
	 * @param x
	 * @param y
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWidth
	 * @param strokeStyle
	 */
	public OvalObject(int x, int y, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		bounds = new RectF();
		bounds.left = x;
		bounds.top = y;
		bounds.right = x;
		bounds.bottom = y;
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		bounds.left = param[start++];
		bounds.top = param[start++];
		bounds.right = param[start++];
		bounds.bottom = param[start];
	}

	@Override
	public int paramLength() {
		return 4;
	}

	@Override
	public int extractShape(int[] data, int start) {
		data[start++] = (int) bounds.left;
		data[start++] = (int) bounds.top;
		data[start++] = (int) bounds.right;
		data[start] = (int) bounds.bottom;
		return 4;
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bounds, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bounds, strokePaint);
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = area.contains((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		int tol = (int) strokePaint.getStrokeWidth() + radius;
		if (x < bounds.left - tol || x > bounds.right + tol
				|| y < bounds.top - tol || y > bounds.bottom + tol)
			selected = false;
		else {
			float cx = bounds.centerX();
			float cy = bounds.centerY();
			x -= cx;
			y -= cy;
			float r2 = x * x + y * y;
			float max = Math.max((bounds.right - cx), (bounds.bottom - cy));
			float appr2 = max + tol;
			appr2 *= appr2;
			selected = (r2 <= appr2)
					&& ((fillPaint.getColor() != Color.TRANSPARENT) || r2 > appr2
							- 4 * max * tol);
		}
		return selected;
	}

	@Override
	public void translate(int dx, int dy) {
		bounds.offset(dx, dy);
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		BoxHandler.handle(bounds);
		return BoxHandler.getHandlers(filter);
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		BoxHandler.onHandlerMoved(handler, point, oldX, oldY);
		BoxHandler.mapTo(bounds);
	}

	public void setDimension(int left, int top, int right, int bottom) {
		bounds.left = left;
		bounds.top = top;
		bounds.right = right;
		bounds.bottom = bottom;
	}

	@Override
	public Rect getBounds() {
		return new Rect((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom);
	}

}
