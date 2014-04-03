package com.ppla03.collapaint.model.object;

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

	private static final ControlPoint cp = new ControlPoint(
			ControlPoint.Type.MOVE, 0, 0, 0);
	private static final ControlPoint[] points = new ControlPoint[] { cp };
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
			y += bounds.centerY();
			this.y = y + bounds.height();
		}
		bounds.offsetTo(this.x, y);
	}

	private void calculateBounds() {
		int cx = bounds.centerX();
		int cy = bounds.centerY();
		paint.getTextBounds(text, 0, text.length(), bounds);
		x = cx - bounds.centerX();
		cy += bounds.centerY();
		y = cy + bounds.height();
		bounds.offsetTo(x, cy);
	}

	public void setParameter(int color, int size, int fontStyle) {
		paint.setTextSize(size);
		this.fontStyle = fontStyle;
		paint.setTypeface(FontManager.getFont(fontStyle));
		calculateBounds();
	}

	public void setColor(int color) {
		paint.setColor(color);
	}

	public void setSize(int size) {
		paint.setTextSize(size);
		calculateBounds();
	}

	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
		paint.setTypeface(FontManager.getFont(fontStyle));
		calculateBounds();
	}

	public int getTextColor() {
		return paint.getColor();
	}

	public int getFontSize() {
		return (int) paint.getTextSize();
	}

	public int getFontStyle() {
		return fontStyle;
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		x = param[start++];
		y = param[start];
	}

	@Override
	public int paramLength() {
		return 2;
	}

	@Override
	public int extractShape(int[] data, int start) {
		data[start++] = x;
		data[start] = y;
		return 2;
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
	public void translate(int dx, int dy) {
		this.x += dx;
		this.y += dy;
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		cp.x = bounds.centerX();
		cp.y = bounds.centerY();
		cp.enable = (filter & ShapeHandler.TRANSFORM_ONLY) == ShapeHandler.TRANSFORM_ONLY;
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		this.x += point.x - oldX;
		this.y += point.y - oldY;
		bounds.offsetTo(this.x, this.y - bounds.height());
	}

	@Override
	public Rect getBounds() {
		return new Rect((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom);
	}

}
