package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasCloseListener;
import com.ppla03.collapaint.FontManager;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasLoadListener;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.BasicObject;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.LineObject;
import com.ppla03.collapaint.model.object.TextObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
			handler = new Handler();
			CanvasSynchronizer.getInstance().loadCanvas(LoaderActivity.this);
		} else {
			// tutup kanvas
			info.setText(R.string.l_close);
			CanvasSynchronizer.getInstance().closeCanvas(this);
		}
	}

	@Override
	public void onCanvasLoaded(final CanvasModel model, int status) {
		if (status == ServerConnector.SUCCESS) {
			new AsyncTask<Void, Void, Integer>() {
				static final int SUCCESS = 1, FONT_FAILED = 3;

				@Override
				protected Integer doInBackground(Void... params) {
					if (!FontManager.readFontAsset(getAssets()))
						return FONT_FAILED;

					// ambil warna objek di kanvas
					ColorPallete.resetColor();
					for (int i = 0; i < model.objects.size(); i++) {
						CanvasObject co = model.objects.get(i);
						if (co instanceof BasicObject) {
							BasicObject bo = (BasicObject) co;
							ColorPallete.addColor(bo.getStrokeColor());
							ColorPallete.addColor(bo.getFillColor());
						} else if (co instanceof LineObject) {
							LineObject lo = (LineObject) co;
							ColorPallete.addColor(lo.getColor());
						} else if (co instanceof TextObject) {
							TextObject to = (TextObject) co;
							ColorPallete.addColor(to.getTextColor());
						}
					}
					return SUCCESS;
				}

				@Override
				protected void onPostExecute(Integer result) {
					Intent intent;
					if (result == SUCCESS) {
						intent = new Intent(LoaderActivity.this,
								WorkspaceActivity.class);
					} else {
						CollaDialog.toast(LoaderActivity.this,
								R.string.l_font_failed, Toast.LENGTH_SHORT);
						intent = new Intent(LoaderActivity.this,
								BrowserActivity.class);
					}
					startActivity(intent);
					finish();
				}

			}.execute();
		} else {
			if (status == ServerConnector.CONNECTION_PROBLEM)
				CollaDialog.toast(this, R.string.check_connection,
						Toast.LENGTH_LONG);
			else
				CollaDialog.toast(this, R.string.l_load_failed,
						Toast.LENGTH_LONG);
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