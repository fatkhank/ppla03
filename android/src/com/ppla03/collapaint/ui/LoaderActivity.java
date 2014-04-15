package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasLoadListener;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.object.FontManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoaderActivity extends Activity implements CanvasLoadListener {
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acivity_loader);
		progress = (ProgressBar) findViewById(R.id.l_progress);
		try {
			FontManager.readAsset(getAssets());
			UserModel user = new UserModel();
			
			CanvasModel model = new CanvasModel(user, "untitle", 800, 500);
			CanvasSynchronizer.getInstance().setCanvas(model).loadCanvas(this);
		} catch (Exception e) {
			Toast.makeText(this, "Cannot load font assets", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onCanvasLoaded(CanvasModel model, int status) {
		if (status == ServerConnector.SUCCESS) {
			Intent intent = new Intent(this, WorkspaceActivity.class);
			startActivity(intent);
		} else {
			Toast.makeText(this, "Cannot load canvas", Toast.LENGTH_SHORT)
					.show();
			// TODO failed load canvas
		}
	}
}