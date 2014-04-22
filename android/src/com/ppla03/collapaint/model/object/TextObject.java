package com.ppla03.collapaint.model.object;

import com.ppla03.collapaint.FontManager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Objek dalam kanvas yang berbentuk teks.
 * @author hamba v7
 * 
 */
public class TextObject extends CanvasObject {
	public static final int MAX_TEXT_LENGTH = 50;

	/**
	 * Batas-batas objek.
	 */
	private final Rect bounds = new Rect();

	/**
	 * Teks yang akan ditampilkan.
	 */
	private String text;

	/**
	 * Jenis font yang digunakan.
	 */
	private int fontCode;

	/**
	 * Paint yang dibutuhkan untuk proses menggambar objek ini.
	 */
	protected final Paint paint;

	private static final Mover mover = new Mover(0, 0, 1);
	private static final Rotator rotator = new Rotator(0, 0, 150, 0, 0);
	private static final ShapeHandler handler = new ShapeHandler(null,
			new ControlPoint[] { rotator, mover });

	/**
	 * Membuat {@link TextObject} kosong.
	 */
	public TextObject() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		text = "";
	}

	/**
	 * Membuat objek {@link TextObject} berdasarkan parameter yang diberikan.
	 * @param text teks yang akan ditampilkan.
	 * @param worldX koordinat x titik tengah objek. (koordinat kanvas)
	 * @param worldY koordinat y titik tengah objek. (koordinat kanvas)
	 * @param color warna teks. Lihat {@link Color}.
	 * @param font jenis huruf. Lihat {@link FontManager}.
	 * @param size ukuran huruf.
	 */
	public TextObject(String text, int worldX, int worldY, int color, int font,
			int size) {
		this.text = text;
		this.fontCode = font;
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		FontManager.apply(fontCode, paint);
		paint.setTextSize(size);
		paint.getTextBounds(text, 0, text.length(), bounds);
		offsetX = worldX;
		offsetY = worldY;
		bounds.offset(-bounds.centerX(), -bounds.centerY());
	}

	/**
	 * Menghitung batas-batas objek
	 */
	private void calculateBounds() {
		paint.getTextBounds(text, 0, text.length(), bounds);
		bounds.offset(-bounds.centerX(), -bounds.centerY());
	}

	/**
	 * Mengatur parameter objek.
	 * @param color warna teks. Lihat {@link Color}.
	 * @param fontCode jenis huruf. Lihat {@link FontManager}.
	 * @param size ukuran huruf
	 */
	public void setParameter(int color, int fontCode, int size) {
		paint.setColor(color);
		paint.setTextSize(size);
		this.fontCode = fontCode;
		FontManager.apply(fontCode, paint);
		calculateBounds();
	}

	/**
	 * Mengubah warna objek
	 * @param color warna. Lihat {@link Color}.
	 */
	public void setColor(int color) {
		paint.setColor(color);
	}

	/**
	 * Mengubah ukuran huruf.
	 * @param size ukuran.
	 */
	public void setSize(int size) {
		paint.setTextSize(size);
		calculateBounds();
	}

	/**
	 * Mengubah jenis huruf.
	 * @param fontCode jenis huruf. Lihat {@link FontManager}.
	 */
	public void setFontCode(int fontCode) {
		this.fontCode = fontCode;
		FontManager.apply(fontCode, paint);
		calculateBounds();
	}

	/**
	 * Mengambil warna objek.
	 * @return warna. Lihat {@link Color}.
	 */
	public int getTextColor() {
		return paint.getColor();
	}

	/**
	 * Mengambil ukuran huruf.
	 * @return ukuran.
	 */
	public int getFontSize() {
		return (int) paint.getTextSize();
	}

	/**
	 * Mengambil jenis huruf.
	 * @return jenis huruf. Lihat {@link FontManager}.
	 */
	public int getFontStyle() {
		return fontCode;
	}

	/*
	 * Integer pertama pada shape parameter berisi panjang karakter. Indeks
	 * berikutnya berisi daftar karakter teks. Tiap integer mengandung dua
	 * karakter.
	 */

	private static char[] setBuffer = new char[16];

	@Override
	public void setShape(float[] param, int start, int end) {
		// hitung jumlah karakter yang dibaca dan pastikan ukuran buffer cukup
		int charLength = (int) param[start++];
		int bufferLength = (charLength + 2) & 0xfffffffe;
		if (setBuffer.length < bufferLength)
			setBuffer = new char[bufferLength];

		// terjemahkan dari param ke daftar buffer karakter
		int c = 0;
		while (start < end) {
			int v = Float.floatToIntBits(param[start++]);
			setBuffer[c++] = (char) (v >> 16);
			setBuffer[c++] = (char) (v);
		}
		text = new String(setBuffer, 0, charLength);
	}

	@Override
	public int paramLength() {
		// indeks pertama adalah jumlah total karakter, ditambah setengah
		// (panjang teks + 1)
		return ((text.length() + 1) >> 1) + 1;
	}

	private static char[] extractBuffer = new char[16];

	@Override
	public int extractShape(float[] data, int start) {
		// pastikan ukuran buffer mencukupi
		int charLength = text.length();
		if (extractBuffer.length < charLength)
			extractBuffer = new char[charLength];

		// masukkan panjang karakter
		data[start++] = charLength;

		// ekstrak karakter ke buffer, kemudian masukkan ke data
		int dataLength = ((charLength + 1) >> 1) + 1;
		text.getChars(0, charLength, extractBuffer, 0);
		int charCtr = 0;
		while (start < dataLength) {
			int c1 = (extractBuffer[charCtr++] << 16) & 0xffff0000;
			int c2 = (charCtr < charLength) ? extractBuffer[charCtr++] : 0;
			data[start++] = Float.intBitsToFloat(c1 | c2);
		}
		return (dataLength >> 1) + 1;
	}

	@Override
	public void drawSelf(Canvas canvas) {
		canvas.drawText(text, bounds.left, bounds.bottom, paint);
	}

	@Override
	public boolean selectedBy(float x, float y, float radius) {
		return bounds.contains((int) x, (int) y);
	}

	@Override
	public ShapeHandler getHandlers(int filter) {
		handler.object = this;
		mover.x = 0;
		mover.y = 0;
		mover.enable = (filter & ShapeHandler.TRANSLATE) == ShapeHandler.TRANSLATE;

		rotator.setCenter(0, 0);
		rotator.setRotation(rotation);
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
	public CanvasObject cloneObject() {
		TextObject to = new TextObject();
		to.text = this.text;
		to.bounds.set(this.bounds);
		copyTransformData(to);
		to.paint.set(this.paint);
		to.fontCode = this.fontCode;
		return to;
	}
}
