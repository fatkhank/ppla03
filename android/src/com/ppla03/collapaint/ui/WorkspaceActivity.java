package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class WorkspaceActivity extends Activity implements OnClickListener, CanvasListener, ColorChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);
		
		
	}

	@Override
	public void onColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCanvasModelLoaded(CanvasModel model, int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHideModeChange(boolean hidden) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectionEvent(boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
