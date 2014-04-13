package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.FontManager;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WorkspaceActivity extends Activity implements OnClickListener, CanvasListener, ColorChangeListener {

	private ToggleButton select, draw, hand, color, stroke, image;
	private Button approve, cancel;
	private Button cut, copy, move, delete;
	private Button rect, oval, poly, line, free, text;
	private Button strokeStyle, strokeWidth;
	
	private CanvasView canvas;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);
		
		select = (ToggleButton) findViewById(R.id.w_main_select);
		draw = (ToggleButton) findViewById(R.id.w_main_draw);
		hand = (ToggleButton) findViewById(R.id.w_main_hand);
		color = (ToggleButton) findViewById(R.id.w_main_color);
		stroke = (ToggleButton) findViewById(R.id.w_main_stroke);
		image = (ToggleButton) findViewById(R.id.w_main_image);
		
		approve = (Button) findViewById(R.id.w_app);
		cancel = (Button) findViewById(R.id.w_ccl);
		
		cut = (Button) findViewById(R.id.w_sel_cut);
		copy = (Button) findViewById(R.id.w_sel_copy);
		move = (Button) findViewById(R.id.w_sel_move);
		delete = (Button) findViewById(R.id.w_sel_del);
		
		rect = (Button) findViewById(R.id.w_draw_rect);
		oval = (Button) findViewById(R.id.w_draw_oval);
		poly = (Button) findViewById(R.id.w_draw_polygon);
		line = (Button) findViewById(R.id.w_draw_line);
		free = (Button) findViewById(R.id.w_draw_free);
		text = (Button) findViewById(R.id.w_draw_text);
		
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

		//main-toolbar
		if (v == select) {
			canvas.setMode(CanvasView.Mode.SELECT);
		} else if (v == draw) {
			//tampilkan button2 draw -> rect, oval, line, etc
		} else if (v == hand) {
			canvas.setMode(CanvasView.Mode.HAND);
		} else if (v == color) {
			//tampilin color dialog
		} else if (v == stroke) {
			//tampilkan button2 stroke -> style + width
		} else if (v == image) {	
			Toast.makeText(this, "not avaiable", Toast.LENGTH_SHORT).show();
		}

		//approve + cancel action
		else if (v == approve) {
			canvas.approveAction();
		} else if (v == cancel) {
			canvas.cancelAction();
		}	
			
		//select-additional-toolbar	
		else if (v == cut) {
			canvas.copySelectedObjects();
			canvas.deleteSelectedObjects();
		} else if (v == copy) {
			canvas.copySelectedObjects();
		} else if (v == move) {
			canvas.moveSelectedObject();
		} else if (v == delete) {
			canvas.deleteSelectedObjects();
		} 

		//draw-additional-toolbar
		else if (v == rect) {
			canvas.insertPrimitive(CanvasView.ObjectType.RECT);
		} else if (v == oval) {
			canvas.insertPrimitive(CanvasView.ObjectType.OVAL);
		} else if (v == poly) {
			canvas.insertPolygon(6);
		} else if (v == line) {
			canvas.insertPrimitive(ObjectType.LINE);
		} else if (v == free) {
			canvas.insertPrimitive(CanvasView.ObjectType.FREE);
		} else if (v == text) {
			canvas.insertText("Sample Text");
		
		//stroke-additional-toolbar
		} else if (v == strokeWidth) {
			//belum
		} else if (v == strokeStyle) {
			//belum
		}
	}
}
