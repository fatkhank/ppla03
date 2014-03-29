package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

public class RectObject extends BasicObject {
	private Rect rect;

	public RectObject(int x, int y, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		this.rect = new Rect(x, y, x, y);
	}

	@Override
	public void draw(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, fillPaint);
		if (paint.getColor() != Color.TRANSPARENT)
			canvas.drawRect(rect, paint);
	}

	public void setDimension(int left, int top, int right, int bottom) {
		rect.left = left;
		rect.top = top;
		rect.right = right;
		rect.bottom = bottom;
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
