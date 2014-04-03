package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public abstract class CanvasObject {
	private static int idCounter = 1;
	public final int id;
	protected int globalID = -1;
	protected boolean selected;

	public CanvasObject() {
		id = idCounter++;
	}

	public void setGlobaID(int id) {
		globalID = id;
	}

	public int getGlobalID() {
		return globalID;
	}

	public boolean isSelected() {
		return selected;
	}

	public void deselect() {
		selected = false;
	}

	public abstract void draw(Canvas canvas);

	public abstract boolean selectedBy(Rect area);

	public abstract boolean selectedBy(int x, int y, int radius);

	public abstract void setShape(int[] param, int start, int end);

	public abstract int paramLength();

	public abstract int extractShape(int[] data, int start);

	public abstract void translate(int dx, int dy);

	public abstract ShapeHandler getHandlers(int filter);

	public abstract void onHandlerMoved(ShapeHandler handler,
			ControlPoint point, int oldX, int oldY);

	public abstract Rect getBounds();

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CanvasObject))
			return false;
		CanvasObject co = (CanvasObject) o;
		return (this.globalID == co.globalID && this.globalID != -1)
				|| this.id == co.id;
	}
}
