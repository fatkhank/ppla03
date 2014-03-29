package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

public class TextObject extends CanvasObject {
	private String text;
	private int x;
	private int y;

	private static final ControlPoint[] points = new ControlPoint[] { new ControlPoint(
			ControlPoint.Type.MOVE, 0, 0, 0) };
	private static final ShapeHandler handler = new ShapeHandler(points);

	public TextObject(String text, int x, int y, int color, int size,
			boolean center) {
		super();
		this.text = text;
		paint = new Paint();
		paint.setColor(color);
		paint.setTextSize(size);
		this.y = y;
		this.x = x;
		if (center) {
			int res = (int) paint.measureText(text) / 2;
			this.x -= res;
		}
	}

	public void setPararameter(int size, Typeface font) {
		paint.setTextSize(size);
		paint.setTypeface(font);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawText(text, x, y, paint);
	}

	@Override
	public String getStyleParameter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStyleParam(String param) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String getShapeParameter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setShapeParam(String param) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean selectedBy(Rect area) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void translate(int x, int y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public ShapeHandler getHandlers() {
		points[0].x = this.x + (int) paint.measureText(text) / 2;
		points[0].y = this.y;
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		this.x += point.x - oldX;
		this.y += point.y - oldY;
	}

}
