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

/**
 * Color View
 * @author hamba v7
 * 
 */
class ColorView extends View {
	static final int[] colors2 = new int[] {
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

	static final int colors[] = { Color.BLACK, Color.WHITE, Color.RED,
			Color.GREEN, Color.BLUE, Color.YELLOW, Color.GRAY, Color.MAGENTA,
			Color.CYAN };

	static int size = 70;
	static final Paint paint = new Paint();
	static final Paint rectPaint = new Paint();
	static {
		paint.setStyle(Style.FILL);
		paint.setShadowLayer(10, 0, 0, Color.BLACK);
		rectPaint.setStyle(Style.FILL);
		rectPaint.setStrokeWidth(10);
		rectPaint.setColor(Color.BLACK);
		rectPaint.setShadowLayer(10, 0, 0, Color.BLACK);
	}
	final int MIN_SCROLL = 10;
	int scroll = 0, maxScroll = 0;
	int OFFSETX, width;
	final int COL_COUNT = 6, ROW_COUNT = colors.length / COL_COUNT + 1;

	static final RectF rect = new RectF();

	static int currentColor = Color.BLACK, currentCol = 0, currentRow = 0,
			currentId = 0;

	public ColorView(Context context, AttributeSet attr) {
		super(context, attr);
		rect.setEmpty();
		setLayerType(LAYER_TYPE_SOFTWARE, paint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		size = (w - 20) / COL_COUNT;
		width = COL_COUNT * size;
		OFFSETX = (w - width) / 2;
		int height = ROW_COUNT * size;
		maxScroll = h - MIN_SCROLL - height;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(OFFSETX, scroll);
		int top = 0, left = 0, right = size, bottom = size;
		for (int i = 0; i < colors.length; i++) {
			paint.setColor(colors[i]);
			canvas.drawRect(left, top, right, bottom, paint);
			left = right;
			right += size;
			if (right > width) {
				top = bottom;
				bottom += size;
				left = 0;
				right = size;
			}
		}
		rectPaint.setColor(currentColor);
		canvas.drawRect(rect, rectPaint);
	}

	int tx, ty, lastY;

	static final int MIN_MOV = 10;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {
			tx = (int) event.getX();
			ty = (int) event.getY();
			lastY = scroll - ty;
		} else if (act == MotionEvent.ACTION_MOVE) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (Math.abs(x - tx) > MIN_MOV || Math.abs(y - ty) > MIN_MOV) {
				scroll = y + lastY;
				if (scroll > MIN_SCROLL)
					scroll = MIN_SCROLL;
				if (scroll < maxScroll)
					scroll = maxScroll;
			}
		} else if (act == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (Math.abs(x - tx) < MIN_MOV && Math.abs(y - ty) < MIN_MOV) {
				currentCol = (x - OFFSETX) / size;
				currentRow = (y - scroll) / size;
				currentId = currentRow * COL_COUNT + currentCol;
				currentColor = colors[currentId];
				int selX = currentCol * size;
				int selY = currentRow * size;
				rect.set(selX - 10, selY - 10, selX + size + 10, selY + size
						+ 10);
			}
		}
		postInvalidate();
		return true;
	}
}