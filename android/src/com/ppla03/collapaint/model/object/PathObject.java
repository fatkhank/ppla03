package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

public class PathObject extends BasicObject {
	private RectF bound;
	protected Path path;
	protected ArrayList<Point> points;
	private static final ControlPoint[] mover = new ControlPoint[] { new ControlPoint(
			ControlPoint.Type.MOVE, 0, 0, 0) };

	private static final ShapeHandler handler = new ShapeHandler(mover);

	public PathObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		path = new Path();
		points = new ArrayList<>();
	}

	public PathObject(int x, int y, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		points = new ArrayList<>();
		points.add(new Point(x, y));
		path = new Path();
		path.moveTo(x, y);
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, fillPaint);
		canvas.drawPath(path, strokePaint);
	}

	public void penTo(int x, int y) {
		path.lineTo(x, y);
		points.add(new Point(x, y));
	}

	public void close() {
		path.close();
	}

	@Override
	public boolean selectedBy(Rect area) {
		if (bound == null) {
			bound = new RectF();
			path.computeBounds(bound, true);
		}
		return (selected = area.contains((int) bound.left, (int) bound.top,
				(int) bound.right, (int) bound.bottom));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		if (bound == null) {
			bound = new RectF();
			path.computeBounds(bound, true);
		}
		if (bound.contains(x, y)) {
			if (fillPaint.getColor() == Color.TRANSPARENT) {
				selected = false;
				int size = points.size();
				int inc = radius >> 1;
				float tol = strokePaint.getStrokeWidth() + radius;
				for (int i = 0; i < size; i += inc) {
					Point p = points.get(i);
					if (Math.abs(p.x - x) < tol && Math.abs(p.y - x) < tol) {
						selected = true;
						break;
					}
				}
			} else {
				Region rg = new Region();
				rg.setPath(path, new Region((int) bound.left, (int) bound.top,
						(int) bound.right, (int) bound.bottom));
				selected = rg.contains(x, y);
			}
		} else
			selected = false;
		return selected;
	}

	@Override
	public void translate(int x, int y) {
		path.offset(x, y);
		int len = points.size();
		for (int i = 0; i < len; i++)
			points.get(i).offset(x, y);
	}

	@Override
	public ShapeHandler getHandlers() {
		RectF bounds = new RectF();
		path.computeBounds(bounds, false);
		mover[0].x = (int) bounds.centerX();
		mover[0].y = (int) bounds.centerY();
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		int dx = point.x - oldX;
		int dy = point.y - oldY;
		path.offset(dx, dy);
	}

}
