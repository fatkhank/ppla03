package com.ppla03.collapaint.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPallete extends View {
	public static final int DARK = Color.rgb(102, 102, 102);

	static final int[] color1 = new int[] {
			// abu
			Color.rgb(255, 255, 255),
			Color.rgb(225, 225, 225),
			Color.rgb(200, 200, 200),
			Color.rgb(170, 170, 170),
			Color.rgb(130, 130, 130),
			Color.rgb(100, 100, 100),
			// hitam
			Color.rgb(50, 50, 50),
			Color.rgb(35, 35, 35),
			Color.rgb(25, 25, 25),
			Color.rgb(15, 15, 15),
			Color.rgb(5, 5, 5),
			Color.rgb(0, 0, 0),
			// coklat
			Color.rgb(238, 236, 225),
			Color.rgb(210, 205, 174),
			Color.rgb(185, 178, 132),
			Color.rgb(137, 128, 77),
			Color.rgb(75, 70, 43),
			Color.rgb(66, 62, 38),
			// biru tua
			Color.rgb(198, 217, 240),
			Color.rgb(141, 179, 226),
			Color.rgb(84, 141, 212),
			Color.rgb(31, 73, 125),
			Color.rgb(23, 54, 93),
			Color.rgb(15, 36, 62),
			// biru muda
			Color.rgb(219, 229, 241),
			Color.rgb(184, 204, 228),
			Color.rgb(149, 179, 215),
			Color.rgb(79, 129, 189),
			Color.rgb(45, 96, 146),
			Color.rgb(36, 64, 97),
			// merah
			Color.rgb(242, 220, 219),
			Color.rgb(229, 185, 183),
			Color.rgb(217, 150, 148),
			Color.rgb(192, 80, 77),
			Color.rgb(149, 55, 52),
			Color.rgb(99, 36, 35),
			// hijau
			Color.rgb(235, 241, 221),
			Color.rgb(215, 227, 188),
			Color.rgb(192, 214, 155),
			Color.rgb(155, 187, 89),
			Color.rgb(118, 146, 60),
			Color.rgb(79, 97, 40),
			// ungu
			Color.rgb(229, 224, 236),
			Color.rgb(204, 193, 217),
			Color.rgb(178, 162, 199),
			Color.rgb(128, 100, 162),
			Color.rgb(95, 73, 122),
			Color.rgb(63, 49, 81),
			// toska
			Color.rgb(219, 238, 243), Color.rgb(183, 221, 232),
			Color.rgb(146, 205, 220), Color.rgb(75, 172, 198),
			Color.rgb(49, 133, 155),
			Color.rgb(32, 88, 103),
			// orange
			Color.rgb(253, 234, 218), Color.rgb(251, 213, 181),
			Color.rgb(250, 192, 143), Color.rgb(247, 150, 70),
			Color.rgb(227, 108, 9), Color.rgb(151, 72, 6),
	// end
	};

	static final int color2[] = { Color.BLACK, Color.WHITE, Color.RED,
			Color.GREEN, Color.BLUE, Color.YELLOW, Color.GRAY, Color.MAGENTA,
			Color.CYAN };

	private static int[] colors;
	private static int count;
	private static final Paint paint = new Paint();
	private static final Paint rectPaint = new Paint();
	static {
		paint.setStyle(Style.FILL);
		rectPaint.setStyle(Style.STROKE);
		rectPaint.setStrokeWidth(10);
		rectPaint.setColor(Color.WHITE);
		resetColor();
	}

	ColorPane dialog;
	private static ColorPallete instance;

	/**
	 * Setengah jarak antar kotak
	 */
	private float margin = 10;
	/**
	 * Lebar kotak
	 */
	private float size = 70;
	/**
	 * Lebar grid (lebar kotak + 2*MARGIN)
	 */
	private float gridSize = size + margin + margin;

	private static float minScroll = 0, scroll = 0, maxScroll = 10;
	static float offsetX;
	private boolean press;
	private static final RectF rect = new RectF();

	/**
	 * lebar pallete
	 */
	private float width;
	/**
	 * tinggi pallete
	 */
	private float height = gridSize;

	private static final int COL_COUNT = 5;
	private static int rowCount;

	static int currentColor = Color.BLACK, currentCol = 0, currentRow = 0,
			currentId = 0;

	public ColorPallete(Context context, AttributeSet attr) {
		super(context, attr);
		setLayerType(LAYER_TYPE_SOFTWARE, paint);
		instance = this;
		rowCount = (count + COL_COUNT - 1) / COL_COUNT;
	}

	static void resetColor() {
		count = color1.length;
		if (colors == null)
			colors = new int[count];
		System.arraycopy(color1, 0, colors, 0, count);
		rowCount = (count + COL_COUNT - 1) / COL_COUNT;
		if (instance != null)
			instance.calcHeight();
	}

	static void addColor(int c) {
		// cek sudah ada belum
		for (int i = 0; i < count; i++) {
			if (colors[i] == c)
				return;
		}
		// pastikan ukuran mencukupi
		if (colors.length <= count) {
			int[] nc = new int[count + 8];
			System.arraycopy(colors, 0, nc, 0, count);
			colors = nc;
		}
		colors[count++] = c;
		rowCount = (count + COL_COUNT - 1) / COL_COUNT;
		if (instance != null)
			instance.calcHeight();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		gridSize = (w - 20) / COL_COUNT;
		margin = (float) (gridSize * 0.1);
		size = gridSize - margin - margin;
		width = (COL_COUNT * gridSize);
		offsetX = (float) ((w - width) * 0.1);
		calcHeight();
	}

	private void calcHeight() {
		height = rowCount * gridSize;
		minScroll = getHeight() - height - margin;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		// gambar scroll
		paint.setColor(DARK);
		float right = getWidth() - 4;
		canvas.drawLine(right, 0, right, getHeight(), paint);
		float top = (getHeight() - 30) * (maxScroll - scroll)
				/ (maxScroll - minScroll);
		canvas.drawRect(right - 10, top, right, top + 30, paint);

		//gambar warna
		canvas.translate(offsetX, scroll);
		top = margin;
		right = margin + size;
		float left = margin;
		float bottom = margin + size;
		for (int i = 0; i < count; i++) {
			paint.setColor(colors[i]);
			canvas.drawRect(left, top, right, bottom, paint);
			left += gridSize;
			right += gridSize;
			if (right > width) {
				top += gridSize;
				bottom += gridSize;
				left = margin;
				right = margin + size;
			}
		}
		if (press)
			canvas.drawRect(rect, rectPaint);
	}

	float tx, ty, lastY;

	static final float MIN_MOV = 10;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {
			press = true;
			tx = (int) event.getX();
			ty = (int) event.getY();
			lastY = scroll - ty;
			currentCol = (int) ((tx - offsetX) / gridSize);
			currentRow = (int) ((ty - scroll) / gridSize);
			float selX = currentCol * gridSize + margin;
			float selY = currentRow * gridSize + margin;
			rect.set(selX, selY, selX + size, selY + size);
		} else if (act == MotionEvent.ACTION_MOVE) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (Math.abs(x - tx) > MIN_MOV || Math.abs(y - ty) > MIN_MOV) {
				float ns = y + lastY;
				if ((ns < scroll && ns > minScroll)
						|| (ns > scroll && ns < maxScroll))
					scroll = ns;
			}
		} else if (act == MotionEvent.ACTION_UP) {
			press = false;
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (Math.abs(x - tx) < MIN_MOV && Math.abs(y - ty) < MIN_MOV) {
				currentId = currentRow * COL_COUNT + currentCol;
				currentColor = colors[currentId];
				dialog.palleteChange(currentColor);
			}
		}
		postInvalidate();
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parent = MeasureSpec.getSize(widthMeasureSpec);
		int h = (int) Math.min(MeasureSpec.getSize(heightMeasureSpec), rowCount
				* gridSize);
		setMeasuredDimension(parent, h);
	}
}