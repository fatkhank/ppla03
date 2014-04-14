package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.object.StrokeStyle;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WorkspaceActivity extends Activity implements OnClickListener,
		CanvasListener, ColorChangeListener, OnSeekBarChangeListener,
		OnItemSelectedListener {

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

	private static final int[] STROKE_STYLES = new int[] { StrokeStyle.SOLID,
			StrokeStyle.DASHED, StrokeStyle.DOTTED };
	private static final String[] STR_STYLES_NAMES = new String[] { "Solid",
			"Dashed", "Dotted" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);

		// --- main bar ---
		select = (ToggleButton) findViewById(R.id.w_main_select);
		draw = (ToggleButton) findViewById(R.id.w_main_draw);
		hand = (ToggleButton) findViewById(R.id.w_main_hand);
		color = (ImageButton) findViewById(R.id.w_main_color);
		stroke = (ToggleButton) findViewById(R.id.w_main_stroke);
		image = (ImageButton) findViewById(R.id.w_main_image);
		currentMain = select;

		select.setOnClickListener(this);
		draw.setOnClickListener(this);
		hand.setOnClickListener(this);
		color.setOnClickListener(this);
		stroke.setOnClickListener(this);
		image.setOnClickListener(this);

		select.setChecked(true);
		canvas.setMode(CanvasView.Mode.SELECT);

		// --- approve ---
		approve = (ImageButton) findViewById(R.id.w_app);
		cancel = (ImageButton) findViewById(R.id.w_ccl);
		approve.setOnClickListener(this);
		cancel.setOnClickListener(this);
		setApproveBar(false);

		// --- select ---
		cut = (ImageButton) findViewById(R.id.w_sel_cut);
		copy = (ImageButton) findViewById(R.id.w_sel_copy);
		move = (ImageButton) findViewById(R.id.w_sel_move);
		delete = (ImageButton) findViewById(R.id.w_sel_del);
		cut.setOnClickListener(this);
		copy.setOnClickListener(this);
		move.setOnClickListener(this);
		delete.setOnClickListener(this);
		setSelectAdditionalBar(false);

		// --- draw ---
		rect = (ImageButton) findViewById(R.id.w_draw_rect);
		oval = (ImageButton) findViewById(R.id.w_draw_oval);
		poly = (ImageButton) findViewById(R.id.w_draw_polygon);
		line = (ImageButton) findViewById(R.id.w_draw_line);
		free = (ImageButton) findViewById(R.id.w_draw_free);
		text = (ImageButton) findViewById(R.id.w_draw_text);
		rect.setOnClickListener(this);
		oval.setOnClickListener(this);
		poly.setOnClickListener(this);
		line.setOnClickListener(this);
		free.setOnClickListener(this);
		rect.setOnClickListener(this);
		setDrawAdditionalBar(false);

		// --- stroke ---
		strokeStyle = (Spinner) findViewById(R.id.w_stroke_style);
		strokeWidth = (SeekBar) findViewById(R.id.w_stroke_width);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, STR_STYLES_NAMES);
		strokeStyle.setAdapter(adapter);
		strokeStyle.setOnItemSelectedListener(this);
		strokeWidth.setOnSeekBarChangeListener(this);
		setStrokeAdditionalBar(false);

		// --- prepare canvas ---
		canvas = (CanvasView) findViewById(R.id.w_canvas);
		UserModel user = new UserModel();
		CanvasModel model = new CanvasModel(user, "untitle", 800, 500);
		canvas.open(model);
	}

	@Override
	public void onColorChanged(int color) {
		canvas.setStrokeColor(color);
		onClick(select);
	}

	@Override
	public void onCanvasModelLoaded(CanvasModel model, int status) {
		// TODO after canvas loaded
	}

	@Override
	public void onHideModeChange(boolean hidden) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectionEvent(boolean success) {
		// TODO Auto-generated method stub
		if (success)
			setSelectAdditionalBar(true);
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// main-toolbar

		if (v == select) {
			currentMain.setChecked(false);
			currentMain = select;
			// canvas.setMode(CanvasView.Mode.SELECT);

			setSelectAdditionalBar(true);
			setDrawAdditionalBar(false);
			setStrokeAdditionalBar(false);

		} else if (v == draw) {
			currentMain.setChecked(false);
			currentMain = draw;
			setSelectAdditionalBar(false);
			setStrokeAdditionalBar(false);
			setDrawAdditionalBar(true);
		} else if (v == hand) {
			currentMain.setChecked(false);
			currentMain = hand;
			// canvas.setMode(CanvasView.Mode.HAND);
		} else if (v == color) {
			currentMain.setChecked(false);
			colorDialog.show();
		} else if (v == stroke) {
			currentMain.setChecked(false);
			currentMain = stroke;
			setSelectAdditionalBar(false);
			setDrawAdditionalBar(false);
			setStrokeAdditionalBar(true);
		} else if (v == image) {
			currentMain.setChecked(false);
			Toast.makeText(this, "not avaiable", Toast.LENGTH_SHORT).show();
		}

		// approve + cancel action
		else if (v == approve) {
			canvas.approveAction();
		} else if (v == cancel) {
			canvas.cancelAction();
		}

		// select-additional-toolbar
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

		// draw-additional-toolbar
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
		}
	}

	/**
	 * Atur visibility approve dan cancel
	 * @param visible
	 */
	void setApproveBar(boolean visible) {
		if (visible) {
			approve.setVisibility(View.VISIBLE);
			cancel.setVisibility(View.VISIBLE);
		} else {
			approve.setVisibility(View.GONE);
			cancel.setVisibility(View.GONE);
		}
	}

	/**
	 * Atur visibility select-additional-toolbar
	 * @param visible
	 */
	void setSelectAdditionalBar(boolean visible) {
		if (visible) {
			cut.setVisibility(View.VISIBLE);
			copy.setVisibility(View.VISIBLE);
			move.setVisibility(View.VISIBLE);
			delete.setVisibility(View.VISIBLE);
		} else {
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
	void setDrawAdditionalBar(boolean visible) {
		if (visible) {
			rect.setVisibility(View.VISIBLE);
			oval.setVisibility(View.VISIBLE);
			poly.setVisibility(View.VISIBLE);
			line.setVisibility(View.VISIBLE);
			free.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
		} else {
			rect.setVisibility(View.GONE);
			oval.setVisibility(View.GONE);
			poly.setVisibility(View.GONE);
			line.setVisibility(View.GONE);
			free.setVisibility(View.GONE);
			text.setVisibility(View.GONE);
		}
	}

	/**
	 * Atur visibility stroke-additional-toolbar
	 * @param visible
	 */
	void setStrokeAdditionalBar(boolean visible) {
		if (visible) {
			strokeStyle.setVisibility(View.VISIBLE);
			strokeWidth.setVisibility(View.VISIBLE);
		} else {
			strokeStyle.setVisibility(View.GONE);
			strokeWidth.setVisibility(View.GONE);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		if (fromUser) {
			if (seekBar == strokeWidth) {
				if (progress <= 0) {
					seekBar.setProgress(1);
					progress = 1;
				}
				canvas.setStrokeWidth(progress);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (view == strokeStyle) {
			canvas.setStrokeStyle(STROKE_STYLES[position]);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}
