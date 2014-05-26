package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * Titik untuk mengatur ofset objek kanvas. {@link Mover#grabbed(float[])}
 * selalu memberikan nilai tue, sehingga titik ini selalu dipegang dari semua
 * titik di kanvas.
 * @author hamba v7
 * 
 */
public class Mover extends ControlPoint {
	private static final float MOVER_CROSS_SIZE = 10;
	private static final Paint paint;

	static {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(6);
		paint.setColor(Color.rgb(102, 102, 102));
		paint.setStrokeCap(Cap.ROUND);
	}

	protected static float anchorX, anchorY, refX, refY;

	public Mover(float x, float y, CanvasObject object, int id) {
		super(x, y, id);
		paint.setAlpha(230);
		if (object != null)
			object.getBounds(objBounds);
	}

	public void setObject(CanvasObject object) {
		object.getBounds(objBounds);
	}

	private static final RectF objBounds = new RectF();

	@Override
	public boolean grabbed(float[] points) {
		anchorX = points[TRANS_X];
		anchorY = points[TRANS_Y];

		refX = this.x;
		refY = this.y;
		grabbed = objBounds.contains(points[OBJ_X], points[OBJ_Y]);
		if (grabbed)
			paint.setAlpha(200);
		return grabbed;
	}

	@Override
	void moveTo(float[] points) {
		this.x = refX + (points[TRANS_X] - anchorX);
		this.y = refY + (points[TRANS_Y] - anchorY);
	}

	@Override
	public void release() {
		super.release();
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		float rad;
		if (grabbed) {
			rad = MOVER_CROSS_SIZE + MOVER_CROSS_SIZE;
		} else {
			rad = MOVER_CROSS_SIZE;
		}
		canvas.drawLine(x - rad, y - rad, x + rad, y + rad, paint);
		canvas.drawLine(x - rad, y + rad, x + rad, y - rad, paint);
		canvas.restore();
	}

}
