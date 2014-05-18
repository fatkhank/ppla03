package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ParticipantManager.InviteResponse;
import com.ppla03.collapaint.model.CanvasModel;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class InvitationAdapter extends BaseAdapter {
	private BrowserActivity activity;
	private ArrayList<CanvasModel> models;

	private class ViewHolder {
		TextView canvasName;
		TextView ownerName;
		ImageButton accept;
		ImageButton decline;
	}

	public InvitationAdapter(BrowserActivity context) {
		this.activity = context;
		models = new ArrayList<>();
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
					R.layout.list_item_invitation, null);
			ViewHolder holder = new ViewHolder();
			holder.canvasName = (TextView) view
					.findViewById(R.id.b_invite_name);
			holder.ownerName = (TextView) view.findViewById(R.id.b_invite_owner);
			holder.accept = (ImageButton) view
					.findViewById(R.id.b_invite_accept);
			holder.accept.setOnClickListener(acceptInvitation);
			holder.decline = (ImageButton) view
					.findViewById(R.id.b_invite_decline);
			holder.decline.setOnClickListener(declineInvitation);
			view.setTag(holder);
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		CanvasModel model = models.get(position);
		holder.canvasName.setText(model.name);
		holder.ownerName.setText(model.owner.name);
		holder.accept.setTag(model);
		holder.decline.setTag(model);
		return view;
	}

	OnClickListener acceptInvitation = new OnClickListener() {

		@Override
		public void onClick(View v) {
			CanvasModel model = (CanvasModel) v.getTag();
			activity.responseInvitation(model, InviteResponse.ACCEPT);
		}
	};

	OnClickListener declineInvitation = new OnClickListener() {

		@Override
		public void onClick(View v) {
			CanvasModel model = (CanvasModel) v.getTag();
			activity.responseInvitation(model, InviteResponse.DECLINE);
		}
	};

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
