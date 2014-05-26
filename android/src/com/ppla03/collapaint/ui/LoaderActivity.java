package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasCloseListener;
import com.ppla03.collapaint.FontManager;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasLoadListener;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class LoaderActivity extends Activity implements CanvasLoadListener,
		CanvasCloseListener {
	static final String ACTION = "action";
	static final int LOAD = 2, CLOSE = 9;

	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acivity_loader);
		TextView info = (TextView) findViewById(R.id.l_text);

		int action = getIntent().getIntExtra(ACTION, LOAD);
		if (action == LOAD) {
			// muat kanvas
			info.setText(R.string.l_loading);
			if (FontManager.readFontAsset(getAssets())) {
				handler = new Handler();
				CanvasSynchronizer.getInstance()
						.loadCanvas(LoaderActivity.this);
			} else {
				CollaToast.show(this, R.string.l_font_failed,
						Toast.LENGTH_SHORT);
			}
		} else {
			// tutup kanvas
			info.setText(R.string.l_close);
			CanvasSynchronizer.getInstance().closeCanvas(this);
		}
	}

	@Override
	public void onCanvasLoaded(CanvasModel model, int status) {
		if (status == ServerConnector.SUCCESS) {
			Intent intent = new Intent(this, WorkspaceActivity.class);
			startActivity(intent);
			finish();
		} else {
			if (status == ServerConnector.CONNECTION_PROBLEM)
				CollaToast.show(this, R.string.check_connection,
						Toast.LENGTH_LONG);
			else
				CollaToast
						.show(this, R.string.l_load_failed, Toast.LENGTH_LONG);
			final Intent intent = new Intent(this, BrowserActivity.class);
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					startActivity(intent);
					finish();
				}
			}, 500);
		}
	}

	@Override
	public void onCanvasClosed(int status) {
		Intent intent = new Intent(this, BrowserActivity.class);
		startActivity(intent);
		finish();
	}
}