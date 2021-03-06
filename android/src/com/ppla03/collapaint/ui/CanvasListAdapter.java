package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.model.CanvasModel;

class CanvasListAdapter extends BaseAdapter implements OnClickListener {
	private BrowserActivity activity;
	private ArrayList<CanvasModel> models;

	private class ViewHolder {
		TextView canvasName;
		TextView userName;
		ImageButton delete;
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
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.canvasName = (TextView) view.findViewById(R.id.lc_canvas);
			holder.canvasName.setOnClickListener(openClick);
			holder.userName = (TextView) view.findViewById(R.id.lc_user);
			holder.delete = (ImageButton) view.findViewById(R.id.lc_delete);
			holder.delete.setOnClickListener(this);
			view.setTag(holder);
			view.setOnClickListener(openClick);
		}
		CanvasModel model = models.get(position);
		holder.canvasName.setText(model.name);
		if (model.owner.equals(CollaUserManager.getCurrentUser()))
			holder.userName.setText("You");
		else
			holder.userName.setText(model.owner.name);

		holder.delete.setTag(model);
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

	private final OnClickListener openClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			if (holder != null && holder.delete != null) {
				CanvasModel model = (CanvasModel) holder.delete.getTag();
				activity.openCanvas(model);
			}
		}
	};

	@Override
	public void onClick(View v) {
		CanvasModel model = (CanvasModel) v.getTag();
		if (model.owner.equals(CollaUserManager.getCurrentUser())) {
			// kanvas adalah milik user
			activity.deleteCanvas(model);
		} else {
			// user hanya sebagai member
			activity.removeParticipation(model);
		}
	}

}