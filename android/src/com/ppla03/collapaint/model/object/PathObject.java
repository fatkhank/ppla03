package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class PathObject extends BasicObject {
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
		canvas.drawPath(path, paint);
	}

	public void penTo(int x, int y) {
		path.lineTo(x, y);
		points.add(new Point(x, y));
	}

	public void close() {
		path.close();
	}
	
	@Override
	public void setShapeParam(ArrayList<Point> param) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean selectedBy(Rect area) {
		// TODO Auto-generated method stub
		return false;
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
