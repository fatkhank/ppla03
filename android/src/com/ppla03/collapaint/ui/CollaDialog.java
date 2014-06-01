package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CollaDialog {
	public static final int YES = 2, NO = 4;

	public static void toast(Context context, String text, int duration) {
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

	public static void toast(Context context, int rid, int duration) {
		toast(context, context.getResources().getString(rid), duration);
	}

	public interface OnClickListener {
		void onClick(int c);
	}

	public static void confirm(Context context, String message, String okText,
			String cancelText, final OnClickListener onOk,
			final OnClickListener onCancel) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_confirm);
		TextView text = (TextView) dialog.findViewById(R.id.confirm_msg);
		text.setText(message);
		final Button okBtn = (Button) dialog.findViewById(R.id.confirm_ok);
		if (okText != null)
			okBtn.setText(okText);
		final Button cancelBtn = (Button) dialog
				.findViewById(R.id.confirm_cancel);
		if (cancelText != null)
			cancelBtn.setText(cancelText);

		View.OnClickListener klik = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (v == okBtn) {
					if (onOk != null)
						onOk.onClick(YES);
				} else if (v == cancelBtn) {
					if (onCancel != null)
						onCancel.onClick(NO);
				}
			}
		};
		okBtn.setOnClickListener(klik);
		cancelBtn.setOnClickListener(klik);

		dialog.show();
	}

	/**
	 * Alert yes - no
	 * @param context
	 * @param message
	 * @param okText
	 * @param cancelText
	 * @param onClick
	 */
	public static void alert(Context context, String message, String okText,
			String cancelText, OnClickListener onClick) {
		confirm(context, message, okText, cancelText, onClick, onClick);
	}

	/**
	 * Alert yes - no
	 * @param context
	 * @param msgId
	 * @param okText
	 * @param cancelText
	 * @param onClick
	 */
	public static void alert(Context context, int msgId, String okText,
			String cancelText, OnClickListener onClick) {
		confirm(context, context.getResources().getString(msgId), okText,
				cancelText, onClick, onClick);
	}

	/**
	 * Konfirmasi OK - batal
	 * @param context
	 * @param msg
	 * @param onClick
	 */
	public static void confirm(Context context, String msg,
			OnClickListener onClick) {
		confirm(context, msg, null, null, onClick, null);
	}

	/**
	 * Konfirmasi OK - batal
	 * @param context
	 * @param msgId
	 * @param onClick
	 */
	public static void confirm(Context context, int msgId,
			OnClickListener onClick) {
		confirm(context, context.getResources().getString(msgId), null, null,
				onClick, null);
	}
}
