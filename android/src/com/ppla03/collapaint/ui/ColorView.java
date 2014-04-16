package com.ppla03.collapaint.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Color View
 * @author hamba v7
 * 
 */
public class ColorView extends View {
	static final int[] colors = new int[] { Color.BLACK, Color.WHITE,
			Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
			Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
			Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
			Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
			Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, };

	static final int SIZE = 70;
	static final Paint paint = new Paint();
	static final Paint rectPaint = new Paint();
	static {
		paint.setStyle(Style.FILL);
		rectPaint.setStyle(Style.STROKE);
		rectPaint.setStrokeWidth(10);
		rectPaint.setColor(Color.BLACK);
	}

	int OFFSET, WIDTH, COL_COUNT;

	static final RectF rect = new RectF();

	int currentColor = Color.BLACK;

	public ColorView(Context context, AttributeSet attr) {
		super(context, attr);
		rect.setEmpty();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		COL_COUNT = w / SIZE;
		WIDTH = COL_COUNT * SIZE;
		OFFSET = (w - WIDTH) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int top = 0, left = OFFSET, right = OFFSET + SIZE, bottom = SIZE;
		for (int i = 0; i < colors.length; i++) {
			paint.setColor(colors[i]);
			canvas.drawRect(left, top, right, bottom, paint);
			left = right;
			right += SIZE;
			if (right > getWidth()) {
				top = bottom;
				bottom += SIZE;
				left = OFFSET;
				right = OFFSET + SIZE;
			}
		}
		canvas.drawRect(rect, rectPaint);
	}

	int tx, ty;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {} else if (act == MotionEvent.ACTION_MOVE) {

		} else if (act == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int col = (x - OFFSET) / SIZE;
			int row = (y / SIZE);
			int c = row * COL_COUNT + col;
			currentColor = colors[c];
			int selX = OFFSET + col * SIZE;
			int selY = row * SIZE;
			rect.set(selX, selY, selX + SIZE, selY + SIZE);
			postInvalidate();
		}
		return true;
	}
}