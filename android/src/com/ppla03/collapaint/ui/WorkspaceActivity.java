package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WorkspaceActivity extends Activity implements OnClickListener, CanvasListener, ColorChangeListener {
	
	private ToggleButton currentMain;
	private ToggleButton select, draw, hand, stroke;
	private ImageButton color, image;
	
	private ImageButton approve, cancel;
	private ImageButton cut, copy, move, delete;
	private ImageButton rect, oval, poly, line, free, text;
	private Spinner strokeStyle;
	private SeekBar strokeWidth;
	
	private CanvasView canvas;
	private ColorDialog colorDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);
		
		select = (ToggleButton) findViewById(R.id.w_main_select);
		draw = (ToggleButton) findViewById(R.id.w_main_draw);
		hand = (ToggleButton) findViewById(R.id.w_main_hand);
		color = (ImageButton) findViewById(R.id.w_main_color);
		stroke = (ToggleButton) findViewById(R.id.w_main_stroke);
		image = (ImageButton) findViewById(R.id.w_main_image);
		currentMain = select;
		
		approve = (ImageButton) findViewById(R.id.w_app);
		approve.setVisibility(View.GONE);
		cancel = (ImageButton) findViewById(R.id.w_ccl);
		cancel.setVisibility(View.GONE);
		
		cut = (ImageButton) findViewById(R.id.w_sel_cut);
		cut.setVisibility(View.GONE);
		copy = (ImageButton) findViewById(R.id.w_sel_copy);
		copy.setVisibility(View.GONE);
		move = (ImageButton) findViewById(R.id.w_sel_move);
		move.setVisibility(View.GONE);
		delete = (ImageButton) findViewById(R.id.w_sel_del);
		delete.setVisibility(View.GONE);
		
		rect = (ImageButton) findViewById(R.id.w_draw_rect);
		rect.setVisibility(View.GONE);
		oval = (ImageButton) findViewById(R.id.w_draw_oval);
		oval.setVisibility(View.GONE);
		poly = (ImageButton) findViewById(R.id.w_draw_polygon);
		poly.setVisibility(View.GONE);
		line = (ImageButton) findViewById(R.id.w_draw_line);
		line.setVisibility(View.GONE);
		free = (ImageButton) findViewById(R.id.w_draw_free);
		free.setVisibility(View.GONE);
		text = (ImageButton) findViewById(R.id.w_draw_text);
		text.setVisibility(View.GONE);
		
		strokeStyle = (Spinner) findViewById(R.id.w_stroke_style);
		strokeStyle.setVisibility(View.GONE);
		strokeWidth = (SeekBar) findViewById(R.id.w_stroke_width);
		strokeWidth.setVisibility(View.GONE);
		
//		canvas = findViewById(R.id.ca)
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
			currentMain.setChecked(false);
			canvas.setMode(CanvasView.Mode.SELECT);
			
			setSelectAdditionalBar(true);
			setDrawAdditionalBar(false);
			setStrokeAdditionalBar(false);
		
		} else if (v == draw) {
			setSelectAdditionalBar(false);
			setStrokeAdditionalBar(false);
			setDrawAdditionalBar(true);
		} else if (v == hand) {
			currentMain.setChecked(false);
			canvas.setMode(CanvasView.Mode.HAND);
		} else if (v == color) {
			//tampilin color dialog
			currentMain.setChecked(false);
		} else if (v == stroke) {
			currentMain.setChecked(false);
			setSelectAdditionalBar(false);
			setDrawAdditionalBar(false);
			setStrokeAdditionalBar(true);
		} else if (v == image) {
			currentMain.setChecked(false);
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
	
	/**
	 * Atur visibility approve dan cancel
	 * @param visible
	 */
	void setApproveBar(boolean visible){
		if(visible){
			approve.setVisibility(View.VISIBLE);
			cancel.setVisibility(View.VISIBLE);
		}else{
			approve.setVisibility(View.GONE);
			cancel.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Atur visibility select-additional-toolbar
	 * @param visible
	 */
	void setSelectAdditionalBar(boolean visible){
		if(visible){
			cut.setVisibility(View.VISIBLE);
			copy.setVisibility(View.VISIBLE);
			move.setVisibility(View.VISIBLE);
			delete.setVisibility(View.VISIBLE);
		}else{
			cut.setVisibility(View.GONE);
			copy.setVisibility(View.GONE);
			move.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Atur visibility draw-additional-toolbar
	 * @param visible
	 */
	void setDrawAdditionalBar(boolean visible){
		if(visible){
			rect.setVisibility(View.VISIBLE);
			oval.setVisibility(View.VISIBLE);
			poly.setVisibility(View.VISIBLE);
			line.setVisibility(View.VISIBLE);
			free.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
		}else{
			rect.setVisibility(View.GONE);
			oval.setVisibility(View.GONE);
			poly.setVisibility(View.GONE);
			line.setVisibility(View.GONE);
			free.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Atur visibility stroke-additional-toolbar
	 * @param visible
	 */
	void setStrokeAdditionalBar(boolean visible){
		if(visible){
			strokeStyle.setVisibility(View.VISIBLE);
			strokeWidth.setVisibility(View.VISIBLE);
		}else{
			strokeStyle.setVisibility(View.GONE);
			strokeWidth.setVisibility(View.GONE);
		}
	}
}
