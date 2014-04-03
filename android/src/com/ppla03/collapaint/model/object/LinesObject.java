package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class LinesObject extends CanvasObject {
	private static ControlPoint[] cps = new ControlPoint[] {
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, 0),
			new ControlPoint(ControlPoint.Type.JOINT, 0, 0, 1),
			new ControlPoint(ControlPoint.Type.MOVE, 0, 0, 2) };
	private static final ShapeHandler handler = new ShapeHandler(cps);
	static {
		handler.size = 2;
	}

	private int x1, y1, x2, y2;
	private int strokeStyle;
	protected final Paint paint;

	public LinesObject() {
		this(0, 0, Color.BLACK, 1, StrokeStyle.SOLID);
	}

	public LinesObject(int x, int y, int color, int width, int strokeStyle) {
		super();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setStrokeWidth(width);
		this.strokeStyle = strokeStyle;
		StrokeStyle.applyEffect(strokeStyle, paint);
		this.x1 = x;
		this.y1 = y;
		this.x2 = x;
		this.y2 = y;
	}

	public void penTo(int x, int y) {
		x2 = x;
		y2 = y;
	}

	public void setColor(int color) {
		paint.setColor(color);
	}

	public void setWidth(int width) {
		paint.setStrokeWidth(width);
	}

	public void setStrokeStyle(int style) {
		this.strokeStyle = style;
		StrokeStyle.applyEffect(style, paint);
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		x1 = param[start++];
		y1 = param[start++];
		x2 = param[start++];
		y2 = param[start];
	}

	@Override
	public int paramLength() {
		return 4;
	}

	@Override
	public int extractShape(int[] data, int start) {
		data[start++] = x1;
		data[start++] = y1;
		data[start++] = x2;
		data[start] = y2;
		return 4;
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		handler.setEnableAllPoint(false);
		if ((filter & ShapeHandler.SHAPE_ONLY) == ShapeHandler.SHAPE_ONLY) {
			handler.points[0].moveTo(x1, y1);
			handler.points[0].enable = true;
			handler.points[1].moveTo(x2, y2);
			handler.points[1].enable = true;
		}
		if ((filter & ShapeHandler.TRANSFORM_ONLY) == ShapeHandler.TRANSFORM_ONLY) {
			int mx = (x2 + x1) >> 1;
			int my = (y2 + y1) >> 1;
			handler.points[2].moveTo(mx, my);
			handler.points[2].enable = true;
		}
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint cp, int oldX,
			int oldY) {
		if (cp.id == 0) {
			x1 = cp.x;
			y1 = cp.y;
		} else if (cp.id == 1) {
			x2 = cp.x;
			y2 = cp.y;
		} else {
			int dx = cp.x - oldX;
			int dy = cp.y - oldY;
			x1 += dx;
			y1 += dy;
			x2 += dx;
			y2 += dy;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	@Override
	public boolean selectedBy(Rect area) {
		return (selected = (area.contains(x1, y1) && area.contains(x2, y2)));
	}

	@Override
	public boolean selectedBy(int x, int y, int radius) {
		int tol = (int) (paint.getStrokeWidth() + radius);
		int dx = x2 - x1;
		int dy = y2 - y1;
		if (Math.abs(dy) > Math.abs(dy))
			selected = Math.abs((y1 + dy / dx * (x - x1)) - y) < radius;
		else
			selected = Math.abs((x1 + dx / dy * (y - y1)) - x) < radius;
		return selected;
	}

	@Override
	public void translate(int dx, int dy) {
		x1 += dx;
		y1 += dy;
		x2 += dx;
		y2 += dy;
	}

	public int getColor() {
		return paint.getColor();
	}

	public int getWidth() {
		return (int) paint.getStrokeWidth();
	}

	public int getStrokeStyle() {
		return strokeStyle;
	}

	@Override
	public Rect getBounds() {
		Rect r = new Rect();
		r.left = Math.min(x1, x2);
		r.top = Math.min(y1, y2);
		r.right = Math.max(x1, x2);
		r.bottom = Math.max(y1, y2);
		return r;
	}
}
