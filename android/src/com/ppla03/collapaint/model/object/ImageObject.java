package com.ppla03.collapaint.model.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ImageObject extends CanvasObject {
	private Bitmap bitmap;
	private Rect srcRect;
	private Rect destRect;
	private String imageID;

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, srcRect, destRect, paint);
	}

	@Override
	public String getParameter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStyleParam(String param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShapeParam(String param) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean selectedBy(Rect area) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void translate(int x, int y) {
		destRect.offset(x, y);
	}

}
