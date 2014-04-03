package com.ppla03.collapaint.model.object;

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

	public int getTransparency() {
		return paint.getAlpha();
	}

	@Override
	public void setShape(int[] param, int start, int end) {
		destRect.left = param[start++];
		destRect.top = param[start++];
		destRect.right = param[start++];
		destRect.bottom = param[start++];
	}

	@Override
	public int paramLength() {
		return 4;
	}

	@Override
	public int extractShape(int[] data, int start) {
		data[start++] = destRect.left;
		data[start++] = destRect.top;
		data[start++] = destRect.right;
		data[start++] = destRect.bottom;
		return 4;
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
	public void translate(int dx, int dy) {
		destRect.offset(dx, dy);
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		// TODO image shape handler
		return null;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			int oldX, int oldY) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rect getBounds() {
		return destRect;
	}
}
