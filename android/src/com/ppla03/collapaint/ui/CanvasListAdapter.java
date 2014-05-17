package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.model.CanvasModel;

class CanvasListAdapter extends BaseAdapter {
	private BrowserActivity activity;
	private ArrayList<CanvasModel> models;

	private class ViewHolder {
		TextView canvasName;
		TextView userName;
	}

	public CanvasListAdapter(BrowserActivity context) {
		this.activity = context;
		models = new ArrayList<CanvasModel>();
	}

	public void addAll(ArrayList<CanvasModel> list) {
		models.addAll(list);
	}

	public void clear() {
		models.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = activity.getLayoutInflater().inflate(
					R.layout.list_item_canvas, null);
			ViewHolder holder = new ViewHolder();
			holder.canvasName = (TextView) view
					.findViewById(R.id.lc_canvas);
			holder.userName = (TextView) view.findViewById(R.id.lc_user);
			view.setTag(holder);
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		CanvasModel model = models.get(position);
		holder.canvasName.setText(model.name);
		if (model.owner.equals(CollaUserManager.getCurrentUser()))
			holder.userName.setText("You");
		else
			holder.userName.setText(model.owner.name);
		return view;
	}

	@Override
	public int getCount() {
		return models.size();
	}

	@Override
	public CanvasModel getItem(int position) {
		return models.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}