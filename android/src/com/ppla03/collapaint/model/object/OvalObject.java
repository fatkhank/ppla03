package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

public class OvalObject extends BasicObject {
	private RectF bound;

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
		if (paint.getColor() != Color.TRANSPARENT)
			canvas.drawOval(bound, paint);
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
