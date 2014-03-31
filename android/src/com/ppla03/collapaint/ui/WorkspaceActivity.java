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
	private Button select, shape, fill, approve, send, undo, redo;
	private CanvasView canvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);

		canvas = (CanvasView) findViewById(R.id.w_canvas);
		canvas.setMode(Mode.DRAW);
		CanvasModel cmod = new CanvasModel("njajal", 800, 400);
		canvas.setModel(cmod);

		select = (Button) findViewById(R.id.w_select);
		shape = (Button) findViewById(R.id.w_shape);
		fill = (Button) findViewById(R.id.w_fill);
		approve = (Button) findViewById(R.id.w_approve);
		send = (Button) findViewById(R.id.w_send);
		undo = (Button) findViewById(R.id.w_undo);
		redo = (Button) findViewById(R.id.w_redo);

		select.setOnClickListener(this);
		shape.setOnClickListener(this);
		fill.setOnClickListener(this);
		approve.setOnClickListener(this);
		send.setOnClickListener(this);
		undo.setOnClickListener(this);
		redo.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if(v == select){
			canvas.setMode(CanvasView.Mode.SELECT);
		}else if (v == shape) {
			canvas.insertPrimitive(CanvasView.ObjectType.RECT);
		} else if (v == fill) {
			if (turn) {
				canvas.setTextParameter(30, Typeface.MONOSPACE);
			} else {
				canvas.setTextParameter(35, Typeface.SERIF);
			}
			turn = !turn;
		} else if (v == approve) {
			canvas.approveAction();
		} else if (v == send) {
			canvas.test();
		}else if(v == undo){
			canvas.undo();
			redo.setEnabled(canvas.isRedoable());
			undo.setEnabled(canvas.isUndoable());
		}else if(v == redo){
			canvas.redo();
			redo.setEnabled(canvas.isRedoable());
			undo.setEnabled(canvas.isUndoable());
		}
	}

	boolean turn;
}
