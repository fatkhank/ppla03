package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

/**
 * Titik di tepi untuk mengatur lebar atau tinggi
 * @author hamba v7
 * 
 */
public class SidePoint extends Joint {
	public static final int LEFT = 0;
	public static final int TOP = 90;
	public static final int RIGHT = 180;
	public static final int BOTTOM = -90;

	private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Path path = new Path();
	static {
		paint.setColor(Color.rgb(204, 0, 0));
		paint.setStyle(Style.FILL);
		path.moveTo(7, 0);
		path.lineTo(3, 5);
		path.lineTo(3, 13);
		path.lineTo(-3, 13);
		path.lineTo(-3, 5);
		path.lineTo(-7, 0);
		path.lineTo(-3, -5);
		path.lineTo(-3, -13);
		path.lineTo(3, -13);
		path.lineTo(3, -5);
		path.close();
	}

	private int type;

	public SidePoint(float x, float y, int id, int type) {
		super(x, y, id);
		this.type = type;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(x, y);
		canvas.rotate(type);
		if (grabbed) {
			paint.setAlpha(180);
			canvas.scale(3f, 3f);
		} else
			paint.setAlpha(255);
		canvas.drawPath(path, paint);
		// canvas.drawRect(-3, -10, 3, 10, paint);
		canvas.restore();
	}

	@Override
	void moveTo(float[] points) {
		if (type == LEFT || type == RIGHT)
			this.x = points[OBJ_X];
		else if (type == TOP || type == BOTTOM)
			this.y = points[OBJ_Y];
	}
}
