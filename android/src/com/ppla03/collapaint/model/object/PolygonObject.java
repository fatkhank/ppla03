package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

public class PolygonObject extends BasicObject {

	private Path path;
	private int[] xLocs;
	private int[] yLocs;

	private RectF bounds = new RectF();
	private static final ShapeHandler handler = new ShapeHandler(
			new ControlPoint[] {});

	public PolygonObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		xLocs = new int[0];
		yLocs = new int[0];
		path = new Path();
	}

	public PolygonObject(int corner, int radius, int x, int y, int fillColor,
			int strokeColor, int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		xLocs = new int[corner];
		yLocs = new int[corner];
		double inc = (Math.PI + Math.PI) / corner;
		float deg = 0;
		for (int i = 0; i < corner; i++) {
			xLocs[i] = x + (int) (radius * Math.cos(deg));
			yLocs[i] = y + (int) (radius * Math.sin(deg));
			deg += inc;
		}
		path = new Path();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 1; i < corner; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		path.close();
		path.computeBounds(bounds, true);
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		System.arraycopy(param, start, xLocs, 0, xLocs.length);
		System.arraycopy(param, start + xLocs.length, yLocs, 0, yLocs.length);
		path.rewind();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 1; i < xLocs.length; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		path.close();
		path.computeBounds(bounds, true);
	}

	@Override
	public int paramLength() {
		return xLocs.length << 1;
	}

	@Override
	public int extractShape(int[] data, int start) {
		System.arraycopy(xLocs, 0, data, start, xLocs.length);
		System.arraycopy(yLocs, 0, data, start + xLocs.length, yLocs.length);
		return xLocs.length << 1;
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, strokePaint);
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = area.contains((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		selected = false;
		if (bounds.contains(x, y)) {
			if (fillPaint.getColor() == Color.TRANSPARENT) {
				int last = xLocs.length - 1;
				int x1 = xLocs[last];
				int y1 = yLocs[last];
				for (int i = 0; i <= last; i++) {
					int x2 = xLocs[i];
					int y2 = yLocs[i];
					int dx = x2 - x1;
					int dy = y2 - y1;
					if (Math.abs(dy) > Math.abs(dy)) {
						int py = y1 + dy / dx * (x - x1);
						selected = Math.abs(py - y) < radius;
					} else {
						int px = x1 + dx / dy * (y - y1);
						selected = Math.abs(px - x) < radius;
					}
					if (selected)
						break;
					else {
						x1 = x2;
						y1 = y2;
					}
				}
			} else {
				Region rg = new Region();
				rg.setPath(path, new Region((int) bounds.left, (int) bounds.top,
						(int) bounds.right, (int) bounds.bottom));
				selected = rg.contains(x, y);
			}
		}
		return selected;
	}

	@Override
	public void translate(int dx, int dy) {
		path.offset(dx, dy);
		int len = xLocs.length;
		for (int i = 0; i < len; i++) {
			xLocs[i] += dx;
			yLocs[i] += dy;
		}
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		path.computeBounds(bounds, true);
		handler.size = xLocs.length + 1;
		if (xLocs.length > handler.points.length - 1) {
			handler.points = new ControlPoint[xLocs.length + 1];
			for (int i = 0; i < xLocs.length; i++) {
				handler.points[i] = new ControlPoint(ControlPoint.Type.JOINT,
						xLocs[i], yLocs[i], i);
			}
			int midX = (int) bounds.centerX();
			int midY = (int) bounds.centerY();
			handler.points[xLocs.length] = new ControlPoint(
					ControlPoint.Type.MOVE, midX, midY, xLocs.length);
		} else {
			for (int i = 0; i < xLocs.length; i++) {
				ControlPoint cp = handler.points[i];
				cp.x = xLocs[i];
				cp.y = yLocs[i];
			}
			ControlPoint cp = handler.points[xLocs.length];
			cp.x = (int) bounds.centerX();
			cp.y = (int) bounds.centerY();
		}
		handler.setEnableAllPoint(((filter & ShapeHandler.SHAPE_ONLY) == ShapeHandler.SHAPE_ONLY));
		handler.points[xLocs.length].enable = ((filter & ShapeHandler.TRANSFORM_ONLY) == ShapeHandler.TRANSFORM_ONLY);
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		if (point.id == xLocs.length) {
			int dx = point.x - oldX;
			int dy = point.y - oldY;
			for (int i = 0; i < xLocs.length; i++) {
				ControlPoint cp = handler.points[i];
				cp.x += dx;
				cp.y += dy;
				xLocs[i] = cp.x;
				yLocs[i] = cp.y;
			}
			path.offset(dx, dy);
		} else {
			xLocs[point.id] = point.x;
			yLocs[point.id] = point.y;
			path.rewind();
			path.moveTo(xLocs[0], yLocs[0]);
			for (int i = 1; i < xLocs.length; i++)
				path.lineTo(xLocs[i], yLocs[i]);
			path.close();
			path.computeBounds(bounds, true);

			ControlPoint cp = handler.points[xLocs.length];
			cp.x = (int) bounds.centerX();
			cp.y = (int) bounds.centerY();
		}
	}

	@Override
	public Rect getBounds() {
		return new Rect((int) bounds.left, (int) bounds.top, (int) bounds.right,
				(int) bounds.bottom);
	}
}
