package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class SidePoint extends Joint {
	public static final int LEFT = 0;
	public static final int TOP = 90;
	public static final int RIGHT = 180;
	public static final int BOTTOM = -90;

	private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	static {
		paint.setColor(Color.argb(150, 20, 20, 20));
		paint.setStyle(Style.FILL);
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
		canvas.drawRect(-3, -10, 3, 10, paint);
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
