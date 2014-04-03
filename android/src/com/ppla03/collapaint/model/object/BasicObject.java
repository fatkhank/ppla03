package com.ppla03.collapaint.model.object;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public abstract class BasicObject extends CanvasObject {
	protected Paint strokePaint;
	protected Paint fillPaint;
	private int strokeStyle;

	protected BasicObject(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setStyle(Style.FILL);
		fillPaint.setColor(fillColor);
		this.strokeStyle = strokeStyle;
		strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setColor(strokeColor);
		strokePaint.setStrokeWidth(strokeWidth);
		StrokeStyle.applyEffect(strokeStyle, strokePaint);
	}

	public void setFillMode(boolean filled, int color) {
		fillPaint.setColor(filled ? color : Color.TRANSPARENT);
	}

	public void setStrokeStyle(int style) {
		strokeStyle = style;
		StrokeStyle.applyEffect(style, strokePaint);
	}

	public void setStrokeColor(int color) {
		strokePaint.setColor(color);
	}

	public void setStrokeWidth(int width) {
		strokePaint.setStrokeWidth(width);
	}

	public int getFillColor() {
		return fillPaint.getColor();
	}

	public int getStrokeStyle() {
		return this.strokeStyle;
	}

	public int getStrokeColor() {
		return strokePaint.getColor();
	}

	public int getStrokeWidth() {
		return (int) strokePaint.getStrokeWidth();
	}
}
