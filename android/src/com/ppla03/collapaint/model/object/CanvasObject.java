package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public abstract class CanvasObject {
	private static int idCounter = 1;
	public final int id;
	protected int globalID = -1;
	protected Paint paint;
	private boolean selected;
	private int offsetX, offsetY;

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

	public abstract void draw(Canvas canvas);

	public abstract void setShapeParam(ArrayList<Point> param);

	public abstract boolean selectedBy(Rect area);
	
	public int getXOffset(){return offsetX;}
	
	public int getYOffset(){return offsetY;}

	public abstract void translate(int x, int y);

	public abstract ShapeHandler getHandlers();

	public abstract void onHandlerMoved(ShapeHandler handler,
			ControlPoint point, int oldX, int oldY);

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CanvasObject))
			return false;
		CanvasObject co = (CanvasObject) o;
		return (this.globalID == co.globalID && this.globalID != -1)
				|| this.id == co.id;
	}
}
