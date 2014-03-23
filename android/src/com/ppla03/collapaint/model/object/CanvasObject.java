package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class CanvasObject {
	protected int id;
	protected int globalID;
	protected Paint paint;
	private boolean selected;

	public boolean isSelected() {
		return selected;
	}

	public abstract void draw(Canvas canvas);

	public abstract String getParameter();

	public abstract void setStyleParam(String param);

	public abstract void setShapeParam(String param);

	public abstract boolean selectedBy(Rect area);

	public abstract void translate(int x, int y);
}
