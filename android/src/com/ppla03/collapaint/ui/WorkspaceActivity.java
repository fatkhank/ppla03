package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.Mode;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.model.CanvasModel;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class WorkspaceActivity extends Activity implements OnClickListener {
	private Button shape, fill, ok, send;
	private CanvasView canvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);

		canvas = (CanvasView) findViewById(R.id.w_canvas);
		canvas.setMode(Mode.DRAW);
		CanvasModel cmod = new CanvasModel("njajal", 800, 400);
		canvas.setModel(cmod);

		shape = (Button) findViewById(R.id.w_shape);
		shape.setOnClickListener(this);
		fill = (Button) findViewById(R.id.w_fill);
		fill.setOnClickListener(this);
		ok = (Button) findViewById(R.id.w_ok);
		ok.setOnClickListener(this);
		send = (Button) findViewById(R.id.w_send);
		send.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v == shape) {
			canvas.insertPolygon(5);
		} else if (v == fill) {
			if (turn) {
				canvas.setTextParameter(30, Typeface.MONOSPACE);
			} else {
				canvas.setTextParameter(35, Typeface.SERIF);
			}
			turn = !turn;
		} else if (v == ok) {
			canvas.approveAction();
		} else if (v == send) {
			canvas.test();
		}
	}

	boolean turn;
}
