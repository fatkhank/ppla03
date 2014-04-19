package com.ppla03.collapaint.model.object;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;

/**
 * Jenis-jenis dekorasi dari suatu garis.
 * @author hamba v7
 * 
 */
public class StrokeStyle {
	/**
	 * Garis lurus tanpa terputus
	 */
	public static final int SOLID = 0;

	/**
	 * Garis putus-putus panjang.
	 */
	public static final int DASHED = 1;

	/**
	 * Garis bulat-bulat.
	 */
	public static final int DOTTED = 2;

	/**
	 * Mengaplikasikan suatu jenis dekorasi pada suatu paint
	 * @param strokeStyle index dekorasi
	 * @param paint yang akan diubah dekorasi garisnya
	 */
	public static void applyEffect(int strokeStyle, Paint paint) {
		float strokeWidth = paint.getStrokeWidth();
		if (strokeStyle == SOLID) {
			paint.setStrokeCap(Cap.SQUARE);
			paint.setPathEffect(null);
		} else if (strokeStyle == DASHED) {
			paint.setStrokeCap(Cap.BUTT);
			paint.setPathEffect(new DashPathEffect(new float[] {
					strokeWidth * 4, strokeWidth }, strokeWidth));
		} else if (strokeStyle == DOTTED) {
			paint.setStrokeCap(Cap.ROUND);
			paint.setPathEffect(new DashPathEffect(new float[] { 1,
					strokeWidth + strokeWidth }, 0));
		}
	}
}
