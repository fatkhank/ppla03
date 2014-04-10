package com.ppla03.collapaint.model.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageObject extends CanvasObject {
	private Bitmap bitmap;
	private final Rect srcRect;
	private final RectF destRect;
	private String imageID;
	protected Paint paint;

	public ImageObject() {
		super();
		srcRect = new Rect();
		destRect = new RectF();
	}
	
	public ImageObject(Bitmap bitmap, float worldX, float worldY, int width, int height, int alpha){
		this.bitmap = bitmap;
		srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		float halfWidth = width * 0.5f;
		float halfHeight = height * 0.5f;
		destRect = new RectF(-halfWidth,-halfHeight, halfWidth, halfHeight);
	}

	@Override
	public void drawSelf(Canvas canvas) {
		canvas.drawBitmap(bitmap, srcRect, destRect, paint);
	}

	public void setTransparency(int alpha) {
		paint.setAlpha(alpha);
	}

	public int getTransparency() {
		return paint.getAlpha();
	}

	@Override
	public void setShape(float[] param, int start, int end) {
		destRect.right = param[start++];
		destRect.bottom = param[start++];
		destRect.left = -destRect.right;
		destRect.top = -destRect.bottom;
	}

	@Override
	public int paramLength() {
		return 4;
	}

	@Override
	public int extractShape(float[] data, int start) {
		data[start++] = destRect.left;
		data[start++] = destRect.top;
		data[start++] = destRect.right;
		data[start++] = destRect.bottom;
		return 4;
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		// TODO image shape handler
		return null;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(destRect);
	}

	@Override
	public CanvasObject cloneObject() {
		// TODO clone image
		return null;
	}
}
