package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.FontManager;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasLoadListener;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoaderActivity extends Activity implements CanvasLoadListener {

	ProgressBar progress;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acivity_loader);
		if (FontManager.readFontAsset(getAssets())) {
			progress = (ProgressBar) findViewById(R.id.l_progress);
			handler = new Handler();
			CanvasSynchronizer.getInstance().loadCanvas(LoaderActivity.this);
		} else {
			Toast.makeText(this, "Cannot load font assets", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onCanvasLoaded(CanvasModel model, int status) {
		if (status == ServerConnector.SUCCESS) {
			Intent intent = new Intent(this, WorkspaceActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(this, "Cannot load canvas", Toast.LENGTH_LONG)
					.show();
			final Intent intent = new Intent(this, BrowserActivity.class);
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					startActivity(intent);
				}
			}, 500);
		}
	}
}