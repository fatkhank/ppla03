package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.StrokeStyle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class StrokeStyleAdapter extends BaseAdapter {

	Context context;
	int styles[] = { StrokeStyle.SOLID, StrokeStyle.DASHED, StrokeStyle.DOTTED };

	public StrokeStyleAdapter(Activity context) {
		this.context = context;
	}

	class StrokeView extends View {
		Paint paint = new Paint();

		public StrokeView(Context context, int style) {
			super(context);
			paint.setStrokeWidth(6);
			StrokeStyle.applyEffect(style, paint);
			setLayerType(LAYER_TYPE_SOFTWARE, paint);
		}

		public StrokeView setStyle(int style) {
			StrokeStyle.applyEffect(style, paint);
			return this;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			float mid = getHeight() / 2;
			canvas.drawLine(0, mid, getWidth(), mid, paint);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int parent = MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(parent, 58);
		}
	}

	public void addAll(ArrayList<CanvasModel> list) {}

	public void clear() {}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			return convertView = new StrokeView(context, styles[position]);
		return ((StrokeView) convertView).setStyle(styles[position]);
	}

	@Override
	public int getCount() {
		return styles.length;
	}

	@Override
	public Object getItem(int position) {
		return styles[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
