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
	private RectF bounds;
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
	public void setShape(int[] param, int start, int end) {
		this.points.clear();
		while (start < end)
			this.points.add(new Point(param[start++], param[start++]));
		path.rewind();
		Point p = this.points.get(0);
		path.moveTo(p.x, p.y);
		int size = this.points.size();
		for (int i = 1; i < size; i++) {
			p = this.points.get(i);
			path.lineTo(p.x, p.y);
		}
		path.close();
		if (bounds == null)
			bounds = new RectF();
		path.computeBounds(bounds, true);
	}

	@Override
	public int paramLength() {
		return points.size() << 1;
	}

	@Override
	public int extractShape(int[] data, int start) {
		int size = this.points.size();
		for (int i = 0; i < size; i++) {
			Point p = points.get(i);
			data[start++] = p.x;
			data[start++] = p.y;
		}
		return size << 1;
	}

	@Override
	public boolean selectedBy(Rect area) {
		if (bounds == null) {
			bounds = new RectF();
			path.computeBounds(bounds, true);
		}
		return (selected = area.contains((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		if (bounds == null) {
			bounds = new RectF();
			path.computeBounds(bounds, true);
		}
		if (bounds.contains(x, y)) {
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
				rg.setPath(path, new Region((int) bounds.left, (int) bounds.top,
						(int) bounds.right, (int) bounds.bottom));
				selected = rg.contains(x, y);
			}
		} else
			selected = false;
		return selected;
	}

	@Override
	public void translate(int dx, int dy) {
		path.offset(dx, dy);
		int len = points.size();
		for (int i = 0; i < len; i++)
			points.get(i).offset(dx, dy);
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		if ((filter & ShapeHandler.TRANSFORM_ONLY) == ShapeHandler.TRANSFORM_ONLY) {
			RectF bounds = new RectF();
			path.computeBounds(bounds, false);
			mover[0].x = (int) bounds.centerX();
			mover[0].y = (int) bounds.centerY();
		} else
			handler.setEnableAllPoint(false);
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		int dx = point.x - oldX;
		int dy = point.y - oldY;
		path.offset(dx, dy);
	}

	@Override
	public Rect getBounds() {
		return new Rect((int) bounds.left, (int) bounds.top, (int) bounds.right,
				(int) bounds.bottom);
	}

}
