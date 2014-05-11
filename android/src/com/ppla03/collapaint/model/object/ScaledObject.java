package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.RectF;

public class ScaledObject {
	protected CanvasObject object;
	protected float scale, left, top, width, height;

	public ScaledObject(CanvasObject object) {
		this.object = object;
		scale = 1;
	}

	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(object.offsetX, object.offsetY);
		canvas.rotate(object.rotation);
		canvas.scale(scale, scale);
		object.drawSelf(canvas);
		canvas.restore();
	}

	public void offset(float x, float y) {
		object.offset(x, y);
	}

	public void offsetTo(float x, float y) {
		object.offsetX += x - this.left;
		object.offsetY += y - this.top;
		this.left = x;
		this.top = y;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	private static final RectF objBounds = new RectF();

	public void placeTo(float left, float top, float width, float height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		object.getWorldBounds(objBounds);
		float widthRatio = width / objBounds.width();
		float heightRatio = height / objBounds.height();

		scale = Math.abs(Math.min(widthRatio, heightRatio));
		objBounds.left -= object.offsetX;
		objBounds.top -= object.offsetY;
		object.offsetTo(left - scale * objBounds.left, top - scale
				* objBounds.top);
	}

	public void lookTo(ScaledObject so) {
		this.left = so.left;
		this.top = so.top;
		this.width = so.width;
		this.height = so.height;
	}

	public void placeTo(ScaledObject so) {
		placeTo(so.left, so.top, so.width, so.height);
	}

	public void setObject(CanvasObject object) {
		this.object = object;
		placeTo(this.left, this.top, this.width, this.height);
	}

	public CanvasObject getObject() {
		return object;
	}
}
