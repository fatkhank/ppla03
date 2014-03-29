package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class CanvasObject {
	private static int idCounter = 1;
	public final int id;
	protected int globalID = -1;
	protected Paint paint;
	private boolean selected;
	
	public CanvasObject() {
		id = idCounter++;
	}
	
	public void setGlobaID(int id){
		globalID = id;
	}
	
	public int getGlobalID(){
		return globalID;
	}

	public boolean isSelected() {
		return selected;
	}

	public abstract void draw(Canvas canvas);

	public abstract String getStyleParameter();

	public abstract void setStyleParam(String param);
	
	public abstract String getShapeParameter();

	public abstract void setShapeParam(String param);

	public abstract boolean selectedBy(Rect area);

	public abstract void translate(int x, int y);

	public abstract ShapeHandler getHandlers();

	public abstract void onHandlerMoved(ShapeHandler handler,
			ControlPoint point, int oldX, int oldY);

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CanvasObject))
			return false;
		CanvasObject co = (CanvasObject) o;
		return this.globalID == co.globalID || this.id == co.id;
	}
}
