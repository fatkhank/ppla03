package com.ppla03.collapaint.model.object;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Paint.Join;

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
	static {
		points.ensureCapacity(128);
	}

	private static final int EDIT_MASK = 1, EDITABLE = 1, PERMANENT = 0,
			LOOP_MASK = 16, CLOSED = 16, OPEN = 0;

	private static final float MAKE_LOOP_DIST = 20;

	/**
	 * Status bentuk objek; NEW = masih dibentuk, OPEN = terbuka (tidak
	 * membentuk loop), CLOSED = membentuk loop.
	 */
	private int state;

	private static final Mover mover = new Mover(0, 0, null, 1);
	private static final Rotator rotator = new Rotator(0, 0, 200, 0, 0);

	private static final ShapeHandler handler = new ShapeHandler(null,
			new ControlPoint[] { rotator, mover });

	/**
	 * Membuat {@link FreeObject} kosong, namun objek tidak dapat dibentuk
	 * menggunakan {@code penUp} dan {@code penDown}
	 */
	public FreeObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		strokePaint.setStrokeJoin(Join.ROUND);
		path = new Path();
		state = EDITABLE | OPEN;
		bounds = new RectF();
	}

	/**
	 * Membuat {@link FreeObject} dengan perameter tertentu. Posisi awal
	 * merupakan titik acuan awal yang digunakan untuk menggambar.
	 * @param worldX koordinat x titik acuan awal (koordinat kanvas)
	 * @param worldY koordinat y titik acuan awal (koordinat kanvas)
	 * @param fillColor warna isian objek. jika warna =
	 *            {@code Color.TRANSPARENT} atau tidak membentuk loop, maka
	 *            dianggap tidak memiliki isian.
	 * @param strokeColor warna pinggiran objek.
	 * @param strokeWidth tebal pinggiran objek.
	 * @param strokeStyle jenis dekorasi pinggiran objek.
	 */
	public FreeObject(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		strokePaint.setStrokeJoin(Join.ROUND);
		path = new Path();
		state = EDITABLE | OPEN;
		bounds = new RectF();
	}

	@Override
	public void drawSelf(Canvas canvas) {
		if (state == CLOSED)
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
		// if ((state & EDIT_MASK) != PERMANENT) {
		offsetX = worldX;
		offsetY = worldY;
		points.clear();
		points.add(new PointF(0, 0));
		path.reset();
		path.moveTo(0, 0);
		path.lineTo(0, 0);
		// }
		return this;
	}

	/**
	 * Mengarahkan pena ke posisi tertentu, menghasilkan garis dari titik
	 * sebelumnya ke titik ini.
	 * @param worldX koordinat x titik (koordinat kanvas)
	 * @param worldY koordinat y titik (koordinat kanvas)
	 * @return {@link FreeObject} this
	 */
	public void penTo(float worldX, float worldY) {
		// if ((state & EDIT_MASK) == EDITABLE) {
		worldX -= offsetX;
		worldY -= offsetY;
		path.lineTo(worldX, worldY);
		points.add(new PointF(worldX, worldY));
		// }
		// return this;
	}

	/**
	 * Mengangkat pena dan menghasilkan bentuk akhir objek.
	 * @param closed jika true, akan dibuat garis akhir yang menghubungkan titik
	 *            akhir ke titik awal, sehingga membentuk loop.
	 * @return {@link FreeObject} this
	 */
	public FreeObject penUp() {
		state &= ~EDIT_MASK;// buat jadi permanen

		// tentukan apakah membentuk loop atau tidak
		PointF first = points.get(0);
		PointF last = points.get(points.size() - 1);
		float dx = Math.abs(first.x - last.x);
		float dy = Math.abs(first.y - last.y);
		if (dx < MAKE_LOOP_DIST && dy < MAKE_LOOP_DIST) {
			state = CLOSED;
			path.close();
		}

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

	@Override
	public void setFillMode(boolean filled, int color) {
		if (state == CLOSED)
			super.setFillMode(filled, color);
	}

	/**
	 * Mengetahui apakah objek ini bisa diberi isian atau tidak (membentuk loop
	 * atau tidak).
	 * @return
	 */
	public boolean fillable() {
		return state == CLOSED;
	}

	/*
	 * Param indeks pertama berisi informasi state objek param. Indeks
	 * berikutnya berisi xLocs, kemudian yLocs dengan ukuran yang sama dengan
	 * xlocs.
	 */
	@Override
	public void setGeom(float[] param, int start, int end) {
		// catat daftar titik-titik
		state = Float.floatToIntBits(param[start++]);
		int size = (end - start) >> 1;
		xLocs = new float[size];
		yLocs = new float[size];
		System.arraycopy(param, start, xLocs, 0, size);
		System.arraycopy(param, start + xLocs.length, yLocs, 0, size);

		// pindahkan ke path
		path.rewind();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 0; i < size; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		if ((state & LOOP_MASK) == CLOSED)
			path.close();

		// hitung batas-batas objek
		path.computeBounds(bounds, true);
	}

	@Override
	public int geomParamLength() {
		// 1 untuk state + panjang xLocs + panjang yLocs
		return 1 + xLocs.length + yLocs.length;
	}

	@Override
	public int extractGeom(float[] data, int start) {
		// data[start++] = Float.intBitsToFloat(state);
		System.arraycopy(xLocs, 0, data, start, xLocs.length);
		System.arraycopy(yLocs, 0, data, start + xLocs.length, yLocs.length);
		return 1 + xLocs.length + yLocs.length;
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		if (bounds.contains(x, y)) {
			if (fillPaint.getColor() == Color.TRANSPARENT) {
				// jika tidak punya isian cek jarak titik ke titik-titik di path
				int inc = 1;// (int) (radius * 0.5f);
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
	public ShapeHandler getHandler(int filter) {
		handler.object = this;

		mover.x = 0;
		mover.y = 0;
		mover.setObject(this);
		mover.enable = ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE);

		rotator.setCenter(0, 0);
		rotator.setRotation(rotation);
		rotator.radius = bounds.bottom + Rotator.MIN_RADIUS;
		rotator.enable = ((filter & ShapeHandler.ROTATE) == ShapeHandler.ROTATE);

		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		if (point == mover) {
			offsetX += point.x - oldX;
			offsetY += point.y - oldY;
			mover.setPosition(0, 0);
		} else if (point == rotator)
			rotation = rotator.getRotation();
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(this.bounds);
	}

	@Override
	public FreeObject cloneObject() {
		FreeObject fo = new FreeObject();
		fo.path.set(this.path);
		fo.bounds.set(this.bounds);
		fo.xLocs = new float[this.xLocs.length];
		fo.yLocs = new float[this.yLocs.length];
		System.arraycopy(this.xLocs, 0, fo.xLocs, 0, this.xLocs.length);
		System.arraycopy(this.yLocs, 0, fo.yLocs, 0, this.yLocs.length);
		fo.state = this.state;
		copyTransformData(fo);
		modifyStyles(fo);
		return fo;
	}
}