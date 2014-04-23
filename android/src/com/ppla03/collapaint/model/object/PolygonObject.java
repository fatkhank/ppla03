package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Paint.Join;

/**
 * Objek dialam kanvas yang berbentuk poligon, memiliki sisi minimal tiga
 * (segitiga). Jumlah sisinya tetap, namun titik-titik sudutnya bisa digeser
 * sehingga memiliki yang tidak beraturan. Objek bisa dirotasi dan translasi.
 * @author hamba v7
 * 
 */
public class PolygonObject extends BasicObject {

	/**
	 * Jumlah sudut minimal poligon
	 */
	public static final int MIN_CORNER_COUNT = 3;
	/**
	 * Jumlah sudut maksimal poligon
	 */
	public static final int MAX_CORNER_COUNT = 30;
	/**
	 * Minimal radius awal poigon.
	 */
	public static final int MINIMAL_RADIUS = 10;

	/**
	 * Jarak minimal tiap titik poligon atau panjang minimal sisi.
	 */
	public static final int MIN_POINT_DISTANCE = 3;

	private final Path path;

	/**
	 * Koordinat x titik-titik di poligon.
	 */
	private float[] xLocs;

	/**
	 * Koordinat y titik-titik di poigon.
	 */
	private float[] yLocs;

	/**
	 * Batas-batas objek.
	 */
	private final RectF bounds;

	private static final Rotator rotator = new Rotator(0, 0, 150, 0, -1);
	private static final Mover mover = new Mover(0, 0, -2);

	/**
	 * Default handler untuk poligon
	 */
	private static final ShapeHandler handler = new ShapeHandler(null,
			new ControlPoint[] {});

	/**
	 * Membuat objek poligon kosong.
	 */
	public PolygonObject() {
		super(Color.TRANSPARENT, Color.BLACK, 1, StrokeStyle.SOLID);
		path = new Path();
		bounds = new RectF();
		strokePaint.setStrokeJoin(Join.ROUND);
	}

	/**
	 * Membuat objek poligon dengan parameter tertentu.
	 * @param corner jumlah sudut/sisi poligon
	 * @param radius jari-jari poligon. Jika kurang dari {@code MINIMAL_RADIUS},
	 *            jari-jari poligon = {@code MINIMAL_RADIUS}.
	 * @param worldX koordinat x titik tengah poligon (koordinat kanvas).
	 * @param worldY koordinat y titik tengah poligon (koordinat kanvas).
	 * @param fillColor warna isian poligon. Jika warna =
	 *            {@code Color.TRANSPARENT}, poligon dianggap tidak memiliki
	 *            isian. Lihat {@link Color}.
	 * @param strokeColor warna pinggiran poligon. Lihat {@link Color}.
	 * @param strokeWidth tebal garis pinggiran poligon.
	 * @param strokeStyle jenis dekorasi pinggiran poligon.
	 */
	public PolygonObject(int corner, int radius, int worldX, int worldY,
			int fillColor, int strokeColor, int strokeWidth, int strokeStyle) {
		super(fillColor, strokeColor, strokeWidth, strokeStyle);
		if (corner < MIN_CORNER_COUNT)
			corner = MIN_CORNER_COUNT;
		strokePaint.setStrokeJoin(Join.ROUND);
		offsetX = worldX;
		offsetY = worldY;
		xLocs = new float[corner];
		yLocs = new float[corner];
		if (radius <= MINIMAL_RADIUS)
			radius = MINIMAL_RADIUS;
		double inc = (Math.PI + Math.PI) / corner;
		double deg = -Math.PI/2;
		for (int i = 0; i < corner; i++) {
			xLocs[i] = (float) (radius * Math.cos(deg));
			yLocs[i] = (float) (radius * Math.sin(deg));
			deg += inc;
		}
		path = new Path();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 1; i < corner; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		path.close();
		bounds = new RectF();
		path.computeBounds(bounds, true);
	}

	/*
	 * parameter berisi array xLocs, diikuti array yLocs
	 */
	@Override
	public void setGeom(float[] param, int start, int end) {
		int corner = (end - start) >> 1;
		if (xLocs == null) {
			xLocs = new float[corner];
			yLocs = new float[corner];
		}
		System.arraycopy(param, start, xLocs, 0, corner);
		System.arraycopy(param, start + xLocs.length, yLocs, 0, corner);
		path.rewind();
		path.moveTo(xLocs[0], yLocs[0]);
		for (int i = 1; i < corner; i++)
			path.lineTo(xLocs[i], yLocs[i]);
		path.close();
		path.computeBounds(bounds, true);
	}

	@Override
	public int geomParamLength() {
		return xLocs.length << 1;
	}

	@Override
	public int extractGeom(float[] data, int start) {
		System.arraycopy(xLocs, 0, data, start, xLocs.length);
		System.arraycopy(yLocs, 0, data, start + xLocs.length, yLocs.length);
		return xLocs.length << 1;
	}

	@Override
	public void drawSelf(Canvas canvas) {
		if (fillPaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, fillPaint);
		if (strokePaint.getColor() != Color.TRANSPARENT)
			canvas.drawPath(path, strokePaint);
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		if (bounds.contains(x, y)) {// quick reject
			if (fillPaint.getColor() == Color.TRANSPARENT) {
				int last = xLocs.length - 1;
				float x1 = xLocs[last];
				float y1 = yLocs[last];
				float tol = strokePaint.getStrokeWidth() + radius;
				for (int i = 0; i <= last; i++) {
					float x2 = xLocs[i];
					float y2 = yLocs[i];
					float dx = x2 - x1;
					float dy = y2 - y1;
					if (Math.abs(dx) > Math.abs(dy)) {
						float py = (y1 + dy / dx * (x - x1));
						if (Math.abs(py - y) < tol)
							return true;
					} else {
						float px = (x1 + dx / dy * (y - y1));
						if (Math.abs(px - x) < tol)
							return true;
					}
					x1 = x2;
					y1 = y2;
				}
			} else {
				Region rg = new Region();
				rg.setPath(path, new Region((int) bounds.left,
						(int) bounds.top, (int) bounds.right,
						(int) bounds.bottom));
				selected = rg.contains((int) x, (int) y);
			}
		}
		return false;
	}

	@Override
	public ShapeHandler getHandler(int filter) {
		handler.object = this;

		// pastikan jumlah ControlPoint pada handler mencukupi
		handler.size = xLocs.length + 2;
		if (handler.size > handler.points.length) {
			handler.points = new ControlPoint[handler.size];
			int i = 0;
			for (; i < xLocs.length; i++)
				handler.points[i] = new Joint(xLocs[i], yLocs[i], i);
			handler.points[i] = rotator;
			handler.points[++i] = mover;
		}
		handler.setEnableAllPoints(false);

		// jika menampilkan pengatur shape
		if ((filter & ShapeHandler.SHAPE) == ShapeHandler.SHAPE) {
			for (int i = 0; i < xLocs.length; i++) {
				ControlPoint cp = handler.points[i];
				cp.x = xLocs[i];
				cp.y = yLocs[i];
				cp.enable = true;
			}
		}

		// jika menampilkan pengatur rotasi
		if ((filter & ShapeHandler.ROTATE) == ShapeHandler.ROTATE) {
			rotator.enable = true;
		}

		// jika menampilkan pengatur translasi
		if ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE) {
			mover.setPosition(0, 0);
			mover.enable = true;
		}
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY) {
		if (point instanceof Mover) {
			// jika yang berpindah adalah pengatur translasi
			offsetX += point.x - oldX;
			offsetY += point.y - oldY;
			point.setPosition(0, 0);
		} else if (point instanceof Rotator) {
			rotation = rotator.getRotation();
		} else {
			// jika yang berpindah adalah pengatur sudut
			int prev = (point.id == 0) ? xLocs.length - 1 : point.id - 1;
			int next = (point.id == xLocs.length - 1) ? 0 : point.id + 1;

			// abaikan jika jarak dengan titik sebelumnya atau sesudahnya kurang
			// dari jarak minimum
			if (((Math.abs(xLocs[prev] - point.x) < MIN_POINT_DISTANCE) && (Math
					.abs(yLocs[prev] - point.y) < MIN_POINT_DISTANCE))
					|| ((Math.abs(xLocs[next] - point.x) < MIN_POINT_DISTANCE) && (Math
							.abs(yLocs[next] - point.y) < MIN_POINT_DISTANCE))) {
				point.x = oldX;
				point.y = oldY;
			} else {
				xLocs[point.id] = point.x;
				yLocs[point.id] = point.y;

				path.rewind();
				path.moveTo(xLocs[0], yLocs[0]);
				for (int i = 1; i < xLocs.length; i++)
					path.lineTo(xLocs[i], yLocs[i]);
				path.close();
				path.computeBounds(bounds, true);

				float cx = -bounds.centerX();
				float cy = -bounds.centerY();
				path.offset(cx, cy);
				bounds.offset(cx, cy);
				for (int i = 0; i < xLocs.length; i++) {
					xLocs[i] += cx;
					yLocs[i] += cy;
					handler.points[i].setPosition(xLocs[i], yLocs[i]);
				}

				offsetRelative(-cx, -cy);
				mover.setPosition(0, 0);
			}
		}
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.set(this.bounds);
	}

	@Override
	public CanvasObject cloneObject() {
		PolygonObject po = new PolygonObject();
		po.path.set(this.path);
		po.xLocs = new float[this.xLocs.length];
		po.yLocs = new float[this.yLocs.length];
		System.arraycopy(this.xLocs, 0, po.xLocs, 0, this.xLocs.length);
		System.arraycopy(this.yLocs, 0, po.yLocs, 0, this.yLocs.length);
		po.bounds.set(this.bounds);
		copyTransformData(po);
		modifyStyles(po);
		return po;
	}
}
