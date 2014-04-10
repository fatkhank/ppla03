package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Objek dalam kanvas yang berbentuk tidak tentu. Objek dibentuk dengan
 * menggerakkan pena ke arah tertentu (menggunakan {@code penTo}). Setelah pena
 * diangkat ({@code penUp}), bentuk objek akan tetap. Setelah objek memiliki
 * bentuk, operasi yang bisa dilakukan adalah translasi dan rotasi, tanpa
 * mengubah bentuk objek lagi. Hanya ada 1 {@link FreeObject} yang bisa dibentuk
 * dalam sekai waktu.
 * @author hamba v7
 * 
 */
public class FreeObject extends BasicObject {
	/**
	 * Batas-batas objek
	 */
	private final RectF bounds;
	private final Path path;

	/**
	 * Daftar koordinat x dari bentuk objek.
	 */
	private float[] xLocs;

	/**
	 * Daftar koordinat y dari bentuk objek.
	 */
	private float[] yLocs;

	/**
	 * Daftar untuk mencatat titik-titik goresan pena, terurut dari yang
	 * pertama.
	 */
	private static final ArrayList<PointF> points = new ArrayList<PointF>();

	private static final int EDIT_MASK = 1, EDITABLE = 1, PERMANENT = 0,
			LOOP_MASK = 16, CLOSED = 16, OPEN = 0;

	/**
	 * Status bentuk objek; NEW = masih dibentuk, OPEN = terbuka (tidak
	 * membentuk loop), CLOSED = membentuk loop.
	 */
	private int state;

	private static final ControlPoint mover = new ControlPoint(
			ControlPoint.Type.MOVE, 0, 0, 0);

	private static final ShapeHandler handler = new ShapeHandler(null,
			new ControlPoint[] { mover });

	/**
	 * Membuat {@link FreeObject} kosong, namun objek tidak dapat dibentuk
	 * menggunakan {@code penUp} dan {@code penDown}
	 */
	public FreeObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		path = new Path();
		state = EDITABLE | OPEN;
		bounds = new RectF();
	}

	/**
	 * Membuat {@link FreeObject} dengan perameter tertentu. Posisi awal
	 * merupakan titik acuan awal yang digunakan untuk menggambar.
	 * @param worldX koordinat x titik acuan awal (koordinat kanvas)
	 * @param worldY koordinat y titik acuan awal (koordinat kanvas)
	 * @param closed jika true maka ketika pena diangkat, akan ditambahkan garis
	 *            yang menghubungkan titik akhir ke titik awal, sehingga objek
	 *            membentuk loop.
	 * @param fillColor warna isian objek. jika warna =
	 *            {@code Color.TRANSPARENT} atau tidak membentuk loop, maka
	 *            dianggap tidak memiliki isian.
	 * @param strokeColor warna pinggiran objek.
	 * @param strokeWidth tebal pinggiran objek.
	 * @param strokeStyle jenis dekorasi pinggiran objek.
	 */
	public FreeObject(boolean closed, int fillColor, int strokeColor,
			int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		if (closed)
			state = EDITABLE | CLOSED;
		else {
			state = EDITABLE | OPEN;
			fillPaint.setColor(Color.TRANSPARENT);
		}
		path = new Path();
		bounds = new RectF();
	}

	@Override
	public void drawSelf(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, fillPaint);
		canvas.drawPath(path, strokePaint);
	}

	/**
	 * Memulai penggambaran bentuk objek.
	 * @param worldX koordinat x titik (koordinat kanvas)
	 * @param worldY koordinat y titik (koordinat kanvas)
	 * @return {@link FreeObject} this
	 */
	public FreeObject penDown(float worldX, float worldY) {
		if ((state & EDIT_MASK) != PERMANENT) {
			offsetX = worldX;
			offsetY = worldY;
			points.clear();
			points.add(new PointF(0, 0));
			path.reset();
			path.moveTo(0, 0);
		}
		return this;
	}

	/**
	 * Mengarahkan pena ke posisi tertentu, menghasilkan garis dari titik
	 * sebelumnya ke titik ini.
	 * @param worldX koordinat x titik (koordinat kanvas)
	 * @param worldY koordinat y titik (koordinat kanvas)
	 * @return {@link FreeObject} this
	 */
	public FreeObject penTo(float worldX, float worldY) {
		if ((state & EDIT_MASK) == EDITABLE) {
			worldX -= offsetX;
			worldY -= offsetY;
			path.lineTo(worldX, worldY);
			points.add(new PointF(worldX, worldY));
		}
		return this;
	}

	/**
	 * Mengangkat pena dan menghasilkan bentuk akhir objek.
	 * @param closed jika true, akan dibuat garis akhir yang menghubungkan titik
	 *            akhir ke titik awal, sehingga membentuk loop.
	 * @return {@link FreeObject} this
	 */
	public FreeObject penUp() {
		state &= ~EDIT_MASK;
		if ((state & LOOP_MASK) == CLOSED)
			path.close();

		// hitung titik tengah
		path.computeBounds(bounds, true);
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		bounds.offset(-cx, -cy);

		// atur sehingga offset berada di tengah2 loop
		offsetX += cx;
		offsetY += cy;
		path.offset(-cx, -cy);
		int size = points.size();
		xLocs = new float[size];
		yLocs = new float[size];
		for (int i = 0; i < size; i++) {
			PointF p = points.get(i);
			xLocs[i] = p.x - cx;
			yLocs[i] = p.y - cy;
		}
		return this;
	}

	/*
	 * Param indeks pertama berisi informasi state objek param. Indeks
	 * berikutnya berisi xLocs, kemudian yLocs dengan ukuran yang sama dengan
	 * xlocs.
	 */

	@Override
	public void setShape(float[] param, int start, int end) {
		// catat daftar titik-titik
		state = (int) param[start++];
		int size = (end - start) >> 1;
		xLocs = new float[size];
		yLocs = new float[size];
		System.arraycopy(param, start, xLocs, 0, size);
		System.arraycopy(param, start + xLocs.length, yLocs, 0, size);

		// pindahkan ke path
		path.rewind();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 1; i < size; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		if ((state & LOOP_MASK) == CLOSED)
			path.close();

		// hitung batas-batas objek
		path.computeBounds(bounds, true);
	}

	@Override
	public int paramLength() {
		// 1 untuk state + panjang xLocs + panjang yLocs
		return 1 + xLocs.length + yLocs.length;
	}

	@Override
	public int extractShape(float[] data, int start) {
		data[start++] = state;
		System.arraycopy(xLocs, 0, data, start, xLocs.length);
		System.arraycopy(yLocs, 0, data, start + xLocs.length, yLocs.length);
		return 1 + xLocs.length + yLocs.length;
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		if (bounds.contains(x, y)) {
			if (fillPaint.getColor() == Color.TRANSPARENT) {
				// jika tidak punya isian cek jarak titik ke titik-titik di path
				int inc = (int) (radius * 0.5f);
				float tol = strokePaint.getStrokeWidth() + radius;
				for (int i = 0; i < xLocs.length; i += inc) {
					if (Math.abs(xLocs[i] - x) < tol
							&& Math.abs(yLocs[i] - y) < tol)
						return true;
				}
			} else {
				Region rg = new Region();
				rg.setPath(path, new Region((int) bounds.left,
						(int) bounds.top, (int) bounds.right,
						(int) bounds.bottom));
				return rg.contains((int) x, (int) y);
			}
		}
		return false;
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		handler.object = this;
		if ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE) {
			mover.x = 0;
			mover.y = 0;
			mover.enable = true;
		} else
			handler.setEnableAllPoint(false);
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		offsetX += point.x - oldX;
		offsetY += point.y - oldY;
		mover.setPosition(0, 0);
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(this.bounds);
	}

	@Override
	public CanvasObject cloneObject() {
		FreeObject fo = new FreeObject();
		fo.path.set(this.path);
		fo.bounds.set(this.bounds);
		fo.xLocs = new float[this.xLocs.length];
		fo.yLocs = new float[this.yLocs.length];
		System.arraycopy(this.xLocs, 0, fo.xLocs, 0, this.xLocs.length);
		System.arraycopy(this.yLocs, 0, fo.yLocs, 0, this.yLocs.length);
		fo.state = this.state;
		copyTransformData(fo);
		changeStyles(fo);
		return fo;
	}
}