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
		paint.setColor(Color.rgb(20, 20, 20));
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
		// anchorX = points[TRANS_X];
		// anchorY = points[TRANS_Y];

		anchorX = points[TRANS_X];
		anchorY = points[TRANS_Y];

		refX = this.x;
		refY = this.y;

		paint.setAlpha(200);
		return objBounds.contains(points[OBJ_X], points[OBJ_Y]);

		// float dx = anchorX - x;
		// float dy = anchorY - y;
		// return (dx * dx + dy * dy) < radius_sq;
	}

	@Override
	void moveTo(float[] points) {
		this.x = refX + (points[TRANS_X] - anchorX);
		this.y = refY + (points[TRANS_Y] - anchorY);
	}

	@Override
	public void release() {
		super.release();
		paint.setAlpha(230);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		canvas.drawLine(x - MOVER_CROSS_SIZE, y - MOVER_CROSS_SIZE, x
				+ MOVER_CROSS_SIZE, y + MOVER_CROSS_SIZE, paint);
		canvas.drawLine(x - MOVER_CROSS_SIZE, y + MOVER_CROSS_SIZE, x
				+ MOVER_CROSS_SIZE, y - MOVER_CROSS_SIZE, paint);
		canvas.restore();
	}

}
