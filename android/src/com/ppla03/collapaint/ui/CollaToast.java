package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

public class CollaToast {
	public static void show(Context context, String text, int duration) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button b = (Button) inflater.inflate(R.layout.toast, null)
				.findViewById(R.id.toast_button);
		b.setText(text);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 32);
		toast.setView(b);
		toast.setDuration(duration);
		toast.show();
	}

	public static void show(Context context, int rid, int duration) {
		show(context, context.getResources().getString(rid), duration);
	}
}
