package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.Participation.Role;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ParticipantAdapter extends BaseAdapter implements OnClickListener {
	public static final String NAME_IF_NULL = "Anonym";
	Dashboard dashboard;
	ArrayList<Participation> participants;

	class ItemView {
		Participation participation;
		TextView name;
		TextView status;
		ImageButton close;
	}

	public ParticipantAdapter(Dashboard dashboard) {
		this.dashboard = dashboard;
		participants = new ArrayList<>();
	}

	public void addAll(ArrayList<Participation> list) {
		participants.addAll(list);
	}

	public void add(Participation p) {
		participants.add(p);
	}

	public void clear() {
		participants.clear();
	}

	@Override
	public int getCount() {
		return participants.size();
	}

	@Override
	public Object getItem(int position) {
		return participants.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = dashboard.workspace.getLayoutInflater().inflate(

			R.layout.list_item_participant, null);
		}
		ItemView iv = ((ItemView) v.getTag());
		if (iv == null) {
			iv = new ItemView();

			iv.participation = participants.get(position);
			iv.name = (TextView) v.findViewById(R.id.p_list_name);
			iv.status = (TextView) v.findViewById(R.id.p_list_status);
			iv.close = (ImageButton) v.findViewById(R.id.p_list_close);
			iv.close.setOnClickListener(ParticipantAdapter.this);
			iv.close.setTag(iv);

			// sembunyikan kick user jika bukan owner
			boolean owner = iv.participation.canvas.owner
					.equals(CollaUserManager.getCurrentUser());
			if (!owner)
				iv.close.setVisibility(View.GONE);
			v.setTag(iv);
		}
		iv.participation = participants.get(position);
		String name = iv.participation.user.name;
		iv.name.setText((name == null || name.isEmpty()) ? NAME_IF_NULL : name);
		if (iv.participation.getRole() == Role.OWNER) {
			iv.status.setText("(owner)");
			iv.close.setVisibility(View.GONE);
		} else if (iv.participation.getRole() == Role.INVITATION) {
			iv.status.setText("(invited)");
		} else
			iv.status.setText("");
		return v;
	}

	@Override
	public void onClick(View v) {
		ItemView tv = (ItemView) v.getTag();
		dashboard.kick(tv.participation);
	}

}
