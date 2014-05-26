package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * Objek dalam kanvas yang berbentuk sebuah garis tunggal. Objek ini hanya bisa
 * ditranslasi atau diubah posisi kedua ujung garis. Rotasi tidak bisa dilakukan
 * terhadap objek ini.
 * @author hamba v7
 */
public class LineObject extends CanvasObject {
	private static Mover mover = new Mover(0, 0, null, 2);

	private static ControlPoint[] cps = { new Joint(0, 0, 0),
			new Joint(0, 0, 1), mover };
	private static final ShapeHandler handler = new ShapeHandler(null, cps);
	static {
		handler.size = 2;
	}

	/**
	 * Panjang minimal sebuah garis.
	 */
	public static final float MIN_LENGTH = 10;

	/*
	 * x2 dan y2 adalah titik ujung garis. Titik pangkal garis berada di (0,0).
	 * Garis akan digambar dari titik (0,0) ke titik (x2,y2).
	 */
	private float x2, y2;

	private final Paint paint;

	private int strokeStyle;

	public LineObject() {
		this(0, 0, Color.BLACK, 1, StrokeStyle.SOLID);
	}

	public LineObject(int worldX, int worldY, int color, int width,
			int strokeStyle) {
		super();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStrokeJoin(Join.ROUND);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(width);
		this.strokeStyle = strokeStyle;
		StrokeStyle.applyEffect(strokeStyle, paint);
		offsetX = worldX;
		offsetY = worldY;
		x2 = MIN_LENGTH;
	}

	/**
	 * Mengubah posisi titik acuan garis. Apabila panjang garis menjadi kurang
	 * dari MIN_LENGTH maka garis akan mempertahankan posisinya.
	 * @param worldX koordinat x titik acuan (koordinat kanvas)
	 * @param worldY koordinat y titik acuan (koordinat kanvas)
	 */
	public void penTo(float worldX, float worldY) {
		float nx = worldX - offsetX;
		float ny = worldY - offsetY;
		if (nx > MIN_LENGTH || ny > MIN_LENGTH) {
			x2 = nx;
			y2 = ny;
		}
	}

	/**
	 * Mengubah warna garis.
	 * @param color warna garis. Lihat {@link Color}.
	 */
	public void setColor(int color) {
		paint.setColor(color);
	}

	/**
	 * Mengubah ketebalan garis.
	 * @param width tebal garis.
	 */
	public void setWidth(int width) {
		paint.setStrokeWidth(width);
		StrokeStyle.applyEffect(strokeStyle, paint);
	}

	/**
	 * Mengubah jenis dekorasi garis.
	 * @param style jenis dekorasi. Lihat {@link StrokeStyle}.
	 */
	public void setStrokeStyle(int style) {
		this.strokeStyle = style;
		StrokeStyle.applyEffect(style, paint);
	}

	/**
	 * Mendapatkan warna dari garis.
	 * @return warna garis. Lihat {@link Color}
	 */
	public int getColor() {
		return paint.getColor();
	}

	/**
	 * Mendapatkan tebal dari garis.
	 * @return tebal garis.
	 */
	public int getWidth() {
		return (int) paint.getStrokeWidth();
	}

	/**
	 * Mendapatkan jenis dekorasi garis.
	 * @return jenis dekorasi garis. Ligat {@link StrokeStyle}.
	 */
	public int getStrokeStyle() {
		return strokeStyle;
	}

	@Override
	public void setGeom(float[] param, int start, int end) {
		x2 = param[start++];
		y2 = param[start];
	}

	@Override
	public int geomParamLength() {
		return 2;
	}

	@Override
	public int extractGeom(float[] data, int start) {
		data[start++] = x2;
		data[start] = y2;
		return 2;
	}

	@Override
	public ShapeHandler getHandler(int filter) {
		handler.object = this;
		handler.setEnableAllPoints(false);
		if ((filter & ShapeHandler.SHAPE) == ShapeHandler.SHAPE) {
			cps[0].setPosition(0, 0);
			cps[0].enable = true;
			cps[1].setPosition(x2, y2);
			cps[1].enable = true;
		}
		if ((filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE) {
			mover.setPosition(0, 0);
			mover.enable = true;
			mover.setObject(this);
		}
		return handler;
	}

	@Override
	public void onHandlerMoved(ShapeHandler handler, ControlPoint cp,
			float oldX, float oldY) {
		if (cp.id == 1) {
			// jika yang diubah titik ujung
			x2 = cp.x;
			y2 = cp.y;
			mover.setObject(this);
		} else {
			// jika yang diubah titik pangkal, ubah translasi
			offsetX += cp.x;
			offsetY += cp.y;
			if (cp.id == 0) {
				// jika yang dilakukan bukan menggeser keseluruhan baris, ubah
				// posisi relatif titik ujung. sehingga posisi titik ujung di
				// kanvas tetap.
				x2 -= cp.x;
				y2 -= cp.y;
				cps[1].x -= cp.x;
				cps[1].y -= cp.y;
			}
			cp.setPosition(0, 0);
		}
		// cegah panjang garis kurang dari minimum
		if (Math.abs(x2) < MIN_LENGTH && Math.abs(y2) < MIN_LENGTH) {
			x2 = MIN_LENGTH;
			y2 = MIN_LENGTH;
			cps[0].setPosition(x2, y2);
		}
	}

	@Override
	public void drawSelf(Canvas canvas) {
		canvas.drawLine(0, 0, x2, y2, paint);
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		// quick reject
		if ((x + radius) < Math.min(-radius, x2)
				|| (x - radius) > Math.max(0, x2)
				|| (y + radius) < Math.min(0, y2)
				|| (y - radius) > Math.max(0, y2))
			return false;
		// aproksimasi jarak titik ke garis
		float tol = paint.getStrokeWidth() + radius;
		if (Math.abs(x2) > Math.abs(y2))
			return Math.abs((y2 / x2 * x) - y) < tol;
		else
			return Math.abs((x2 / y2 * y) - x) < tol;
	}

	@Override
	public void getBounds(RectF bounds) {
		bounds.left = Math.min(0, x2);
		bounds.top = Math.min(0, y2);
		bounds.right = Math.max(0, x2);
		bounds.bottom = Math.max(0, y2);
	}

	@Override
	public LineObject cloneObject() {
		LineObject lo = new LineObject();
		lo.x2 = this.x2;
		lo.y2 = this.y2;
		copyTransformData(lo);
		lo.paint.set(this.paint);
		return lo;
	}
}