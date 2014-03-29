package com.ppla03.collapaint.model.object;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public abstract class BasicObject extends CanvasObject {
	protected Paint fillPaint;

	protected BasicObject(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		super();
		fillPaint = new Paint();
		fillPaint.setStyle(Style.FILL);
		fillPaint.setColor(fillColor);
		fillPaint.setAntiAlias(true);
		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(strokeColor);
		paint.setStrokeWidth(strokeWidth);
		paint.setPathEffect(StrokeStyle.getEffect(strokeStyle, strokeWidth));
		paint.setAntiAlias(true);
	}

	public void setFillMode(boolean filled, int color) {
		if (filled)
			fillPaint.setColor(color);
		else
			fillPaint.setColor(Color.TRANSPARENT);
	}

	public void setStrokeMode(int color, int strokeWidth, int strokeStyle) {
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		paint.setPathEffect(StrokeStyle.getEffect(strokeStyle, strokeWidth));
	}
}
