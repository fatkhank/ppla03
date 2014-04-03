package com.ppla03.collapaint.model.object;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PathEffect;

public class StrokeStyle {
	public static final int SOLID = 0;
	public static final int DASHED = 1;
	public static final int DOTTED = 2;

	public static void applyEffect(int strokeStyle, Paint paint) {
		float strokeWidth = paint.getStrokeWidth();
		if (strokeStyle == SOLID) {
			paint.setStrokeCap(Cap.SQUARE);
			paint.setPathEffect(null);
		} else if (strokeStyle == DASHED) {
			paint.setStrokeCap(Cap.BUTT);
			paint.setPathEffect(new DashPathEffect(new float[] {
					strokeWidth*4, strokeWidth}, strokeWidth));
		} else if (strokeStyle == DOTTED) {
			paint.setStrokeCap(Cap.ROUND);
			paint.setPathEffect(new DashPathEffect(new float[] {10,
					strokeWidth*2}, 0));
		}
	}
}
