package com.ppla03.collapaint.model.object;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Objek di kanvas yang memiliki pinggiran dan isian.
 * @author hamba v7
 * 
 */
public abstract class BasicObject extends CanvasObject {
	/**
	 * {@link Paint} pinggiran objek.
	 */
	protected Paint strokePaint;
	
	/**
	 * {@link Paint} isian objek.
	 */
	protected Paint fillPaint;
	
	/**
	 * Jenis dekorasi garis pinggiran. Lihat {@link StrokeStyle}.
	 */
	private int strokeStyle;

	/**
	 * 
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWidth
	 * @param strokeStyle
	 */
	protected BasicObject(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setStyle(Style.FILL);
		fillPaint.setColor(fillColor);
		this.strokeStyle = strokeStyle;
		strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setColor(strokeColor);
		strokePaint.setStrokeWidth(strokeWidth);
		StrokeStyle.applyEffect(strokeStyle, strokePaint);
	}

	/**
	 * Mengatur warna isian objek.
	 * @param filled apakah objek memiliki isian atau tidak. Jika false, maka
	 *            parameter color diabaikan.
	 * @param color warna isian. Lihat {@link Color}.
	 */
	public void setFillMode(boolean filled, int color) {
		fillPaint.setColor(filled ? color : Color.TRANSPARENT);
	}

	/**
	 * Mengubah warna pinggiran objek
	 * @param color warna pinggiran. Lihat {@link Color}.
	 */
	public void setStrokeColor(int color) {
		strokePaint.setColor(color);
	}

	/**
	 * Mengatur tebal garis pinggiran objek.
	 * @param width tebal garis.
	 */
	public void setStrokeWidth(int width) {
		strokePaint.setStrokeWidth(width);
	}

	/**
	 * Mengubah jenis dekorasi garis pinggiran objek.
	 * @param style jenis dekorasi. Lihat {@link StrokeStyle}.
	 */
	public void setStrokeStyle(int style) {
		strokeStyle = style;
		StrokeStyle.applyEffect(style, strokePaint);
	}

	/**
	 * Mengambil warna isian objek.
	 * @return warna isian. Lihat {@link Color}.
	 */
	public int getFillColor() {
		return fillPaint.getColor();
	}

	/**
	 * Mengambil jenis dekorasi garis pinggiran objek.
	 * @return jenis dekorasi. Lihat {@link StrokeStyle}.
	 */
	public int getStrokeStyle() {
		return this.strokeStyle;
	}

	/**
	 * Mengambil warna garis pinggiran objek.
	 * @return warna garis. Lihat {@link Color}.
	 */
	public int getStrokeColor() {
		return strokePaint.getColor();
	}

	/**
	 * Mengambil tebal garis pinggiran objek.
	 * @return tebal garis.
	 */
	public int getStrokeWidth() {
		return (int) strokePaint.getStrokeWidth();
	}

	/**
	 * Menyalin properti <i>style</i> objek ini ke suatu {@link BasicObject}
	 * lain.
	 * @param other objek tujuan.
	 */
	protected void changeStyles(BasicObject other) {
		other.fillPaint.set(this.fillPaint);
		other.strokePaint.set(this.strokePaint);
		other.strokeStyle = this.strokeStyle;
	}
}
