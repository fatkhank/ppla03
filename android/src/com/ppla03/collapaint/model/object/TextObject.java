package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

public class TextObject extends CanvasObject {
	private final Rect bounds = new Rect();
	private String text;
	private int fontStyle;
	private int x;
	private int y;
	protected final Paint paint; 

	private static final ControlPoint[] points = new ControlPoint[] { new ControlPoint(
			ControlPoint.Type.MOVE, 0, 0, 0) };
	private static final ShapeHandler handler = new ShapeHandler(points);

	public TextObject() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		text = "";
	}

	public TextObject(String text, int x, int y, int color, int font, int size,
			boolean center) {
		this.text = text;
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setTypeface(FontManager.getFont(font));
		paint.setTextSize(size);
		paint.getTextBounds(text, 0, text.length(), bounds);
		this.y = y;
		this.x = x;
		if (center) {
			this.x -= bounds.centerX();
			this.y -= bounds.centerY();
		}
		bounds.offsetTo(this.x, this.y);
	}

	public void setParameter(int color,int size, int fontStyle) {
		paint.setTextSize(size);
		this.fontStyle = fontStyle;
		paint.setTypeface(FontManager.getFont(fontStyle));
		int cx = bounds.centerX();
		int cy = bounds.centerY();
		paint.getTextBounds(text, 0, text.length(), bounds);
		x = cx - bounds.centerX();
		y = cy - bounds.centerY();
		bounds.offsetTo(x, y);
	}
	
	public int getTextColor(){
		return paint.getColor();
	}
	
	public int getFontSize(){
		return (int) paint.getTextSize();
	}
	
	public int getFontStyle(){
		return fontStyle;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawText(text, x, y, paint);
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = area.contains(bounds));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		return (selected = bounds.contains(x, y));
	}

	@Override
	public void translate(int x, int y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public ShapeHandler getHandlers() {
		points[0].x = bounds.centerX();
		points[0].y = bounds.centerY();
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		this.x += point.x - oldX;
		this.y += point.y - oldY;
		bounds.offsetTo(this.x, this.y);
	}

}
