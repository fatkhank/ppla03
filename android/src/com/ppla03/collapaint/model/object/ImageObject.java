package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;

public class ImageObject extends CanvasObject {
	private Bitmap bitmap;
	private Rect srcRect;
	private Rect destRect;
	private String imageID;
	protected Paint paint;
	
	public ImageObject() {
		super();
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, srcRect, destRect, paint);
	}

	public void setTransparency(int alpha) {
		paint.setAlpha(alpha);
	}
	
	public int getTransparency(){
		return paint.getAlpha();
	}

	@Override
	public boolean selectedBy(Rect area) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean selectedBy(int x, int y, int radius) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void translate(int x, int y) {
		destRect.offset(x, y);
	}

	@Override
	public ShapeHandler getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		// TODO Auto-generated method stub

	}
}
