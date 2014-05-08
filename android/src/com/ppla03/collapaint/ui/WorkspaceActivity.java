package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.FontManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.FontManager.Font;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.StrokeStyle;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WorkspaceActivity extends Activity implements OnClickListener,
		OnLongClickListener, CanvasListener, ColorChangeListener,
		OnSeekBarChangeListener, OnItemSelectedListener,
		OnCheckedChangeListener, OnEditorActionListener {

	private ImageButton select, hand, undo, redo, showDash;

	private ImageButton showProp, cut, copy, move, delete;

	// property dan dashboard
	private View propertyPane;
	private RelativeLayout topBar;
	private int colorNormal, colorHidden, currentColor;
	private TextView canvasTitle;
	private View colorConsumer;

	// --------- stroke setting ---------
	private RelativeLayout strokePane;
	private Button strokeColor;
	private Spinner strokeStyle;
	private SeekBar strokeWidth;
	private TextView strokeWidthText;

	private static final int[] STROKE_STYLES = new int[] { StrokeStyle.SOLID,
			StrokeStyle.DASHED, StrokeStyle.DOTTED };
	private static final String[] STR_STYLES_NAMES = new String[] { "Solid",
			"Dashed", "Dotted" };

	// --------- fill setting ---------
	private RelativeLayout fillPane;
	private CheckBox fillCheck;
	private Button fillColor;

	// --------- font setting ---------
	private RelativeLayout textPane;
	private Button textColor;
	private EditText textInput;
	private Spinner fontStyles;
	private SeekBar textSize;
	private TextView textSizeText;
	private ToggleButton textBold, textItalic, textUnderline;

	// ------
	private CanvasView canvas;
	private ColorDialog colorDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);
		// --- top bar ---
		select = (ImageButton) findViewById(R.id.w_main_select);
		hand = (ImageButton) findViewById(R.id.w_main_hand);
		undo = (ImageButton) findViewById(R.id.w_undo);
		redo = (ImageButton) findViewById(R.id.w_redo);
		showDash = (ImageButton) findViewById(R.id.w_show_dash);

		select.setOnClickListener(this);
		select.setOnLongClickListener(this);
		hand.setOnClickListener(this);
		undo.setOnClickListener(this);
		undo.setEnabled(false);
		redo.setOnClickListener(this);
		redo.setEnabled(false);
		showDash.setOnClickListener(this);

		// --- select ---
		propertyPane = (View) findViewById(R.id.w_property_scroll);
		showProp = (ImageButton) findViewById(R.id.w_show_property);
		cut = (ImageButton) findViewById(R.id.w_sel_cut);
		copy = (ImageButton) findViewById(R.id.w_sel_copy);
		move = (ImageButton) findViewById(R.id.w_sel_move);
		delete = (ImageButton) findViewById(R.id.w_sel_del);
		showProp.setOnClickListener(this);
		cut.setOnClickListener(this);
		copy.setOnClickListener(this);
		move.setOnClickListener(this);
		delete.setOnClickListener(this);

		// --------- stroke ---------
		strokePane = (RelativeLayout) findViewById(R.id.w_prop_stroke);

		// atur lebar stroke
		strokeWidth = (SeekBar) findViewById(R.id.w_stroke_width);
		strokeWidthText = (TextView) findViewById(R.id.w_stroke_width_text);
		strokeWidth.setOnSeekBarChangeListener(this);
		strokeWidth.setMax(CanvasObject.MAX_STROKE_WIDTH
				- CanvasObject.MIN_STROKE_WIDTH);
		strokeWidth.setProgress(0);

		// atur stroke style
		strokeStyle = (Spinner) findViewById(R.id.w_stroke_style);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, STR_STYLES_NAMES);
		strokeStyle.setAdapter(adapter);
		strokeStyle.setOnItemSelectedListener(this);

		// atur stroke color
		strokeColor = (Button) findViewById(R.id.w_stroke_color);
		strokeColor.setOnClickListener(this);

		// --------- fill ---------
		fillPane = (RelativeLayout) findViewById(R.id.w_prop_fill);
		fillCheck = (CheckBox) findViewById(R.id.w_fill_check);
		fillCheck.setOnCheckedChangeListener(this);
		fillColor = (Button) findViewById(R.id.w_fill_color);
		fillColor.setOnClickListener(this);

		// --------- font ---------
		textPane = (RelativeLayout) findViewById(R.id.w_prop_text);
		textInput = (EditText) findViewById(R.id.w_font_input);
		textInput.setSingleLine(true);
		textInput.setOnEditorActionListener(this);
		textSize = (SeekBar) findViewById(R.id.w_font_size);
		fontStyles = (Spinner) findViewById(R.id.w_font_style);
		textBold = (ToggleButton) findViewById(R.id.w_font_bold);
		textItalic = (ToggleButton) findViewById(R.id.w_font_italic);
		textUnderline = (ToggleButton) findViewById(R.id.w_font_underline);
		textBold.setOnCheckedChangeListener(this);
		textItalic.setOnCheckedChangeListener(this);
		textUnderline.setOnCheckedChangeListener(this);
		textSizeText = (TextView) findViewById(R.id.w_font_size_text);
		textSize.setOnSeekBarChangeListener(this);
		textSize.setMax(FontManager.MAX_FONT_SIZE - FontManager.MIN_FONT_SIZE);
		textSize.setProgress(FontManager.MIN_FONT_SIZE);
		fontStyles.setAdapter(FontManager.getAdapter(this));
		fontStyles.setOnItemSelectedListener(this);
		textColor = (Button) findViewById(R.id.w_font_color);
		textColor.setOnClickListener(this);

		// --- prepare dialog ---
		colorDialog = new ColorDialog(this, this);
		colorDialog.setColor(Color.BLACK);

		// --- prepare canvas ---
		colorNormal = getResources().getColor(R.color.workspace_normal);
		colorHidden = getResources().getColor(R.color.workspace_hidden);
		currentColor = colorNormal;
		topBar = (RelativeLayout) findViewById(R.id.w_topbar);
		topBar.setBackgroundColor(colorNormal);
		canvasTitle = (TextView) findViewById(R.id.w_canvas_name);
		canvas = (CanvasView) findViewById(R.id.w_canvas);
		canvas.setListener(this);
		CanvasSynchronizer.getInstance().setCanvasView(canvas);
		canvasTitle.setText(canvas.getModel().name);
		onClick(select);
	}

	@Override
	public void onBackPressed() {
		// TODO close canvas
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO show dashboard
		return true;
	}

	@Override
	public void onColorChanged(int color) {
		if (colorConsumer == strokeColor) {
			canvas.setStrokeColor(color, true);
			strokeColor.setBackgroundColor(color);
		} else if (colorConsumer == fillColor) {
			canvas.setFillParameter(true, color, true);
			fillColor.setBackgroundColor(color);
		} else if (colorConsumer == textColor) {
			canvas.setTextColor(color, true);
			textColor.setBackgroundColor(color);
		}
	}

	@Override
	public void onHideModeChange(boolean hidden) {
		if (hidden) {
			currentColor = colorHidden;
			canvasTitle.setText(canvas.getModel().name + " (hide mode)");
		} else {
			currentColor = colorNormal;
			canvasTitle.setText(canvas.getModel().name);
		}
	}

	@Override
	public void onSelectionEvent(int state, int param) {
		if (state == CanvasListener.CHANGE_MODE) {
			select.setBackgroundColor(Color.WHITE);
		} else {
			setSelectAdditionalBar(state == CanvasListener.SELECT, param > 1);
			if (canvas.isInSelectionMode())
				select.setBackgroundColor(Color.WHITE);
			else
				select.setBackgroundColor(currentColor);
		}
	}

	private void setSelectAdditionalBar(boolean show, boolean withMove) {
		if (show) {
			showProp.setVisibility(View.VISIBLE);
			cut.setVisibility(View.VISIBLE);
			copy.setVisibility(View.VISIBLE);
			delete.setVisibility(View.VISIBLE);
			if (withMove)
				move.setVisibility(View.VISIBLE);
			else
				move.setVisibility(View.GONE);

			if (canvas.isEditingTextObject()) {
				// jika sedang mengedit teks, tampilkan properti text saja
				textPane.setVisibility(View.VISIBLE);
				fillPane.setVisibility(View.GONE);
				strokePane.setVisibility(View.GONE);
				textInput.setText(canvas.getTextContent());
			} else if (canvas.isEditingBasicObject()) {
				// jika sedang mengedit basic object, tampilkan properti
				// stroke
				// dan fill
				textPane.setVisibility(View.GONE);
				fillPane.setVisibility(View.VISIBLE);
				strokePane.setVisibility(View.VISIBLE);
			} else {// edit line
				fillPane.setVisibility(View.GONE);
				strokePane.setVisibility(View.VISIBLE);
			}
		} else {
			cut.setVisibility(View.GONE);
			copy.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			showProp.setVisibility(View.GONE);
		}
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {
		undo.setEnabled(undoable);
		redo.setEnabled(redoable);
	}

	@Override
	public void onWaitForApproval() {

	}

	@Override
	public void onBeginDraw() {

	}

	@Override
	public void onClick(View v) {
		// main-toolbar
		if (v == select) {
			canvas.setMode(CanvasView.Mode.SELECT);
			if (canvas.isInSelectionMode())
				select.setBackgroundColor(currentColor);
			else
				select.setBackgroundColor(Color.WHITE);
		} else if (v == hand) {
			canvas.setMode(CanvasView.Mode.HAND);
			if (canvas.isInHandMode())
				hand.setBackgroundColor(currentColor);
			else
				hand.setBackgroundColor(Color.WHITE);
		} else if (v == undo) {
			canvas.undo();
		} else if (v == redo) {
			canvas.redo();
		} else if (v == showDash) {
			// TODO show dash
		} else if (v == showProp) {
			if (propertyPane.getVisibility() == View.VISIBLE) {
				propertyPane.setVisibility(View.GONE);
				if (canvas.isInHideMode())
					showProp.setBackgroundColor(colorHidden);
				else
					showProp.setBackgroundColor(colorNormal);
			} else {
				propertyPane.setVisibility(View.VISIBLE);
				showProp.setBackgroundColor(Color.WHITE);
			}
		}

		// select-additional-toolbar
		else if (v == cut) {
			// TODO cut
			canvas.copySelectedObjects();
			canvas.deleteSelectedObjects();
		} else if (v == copy) {
			int count = canvas.copySelectedObjects();
			Toast.makeText(this, count + " objects copied", Toast.LENGTH_SHORT)
					.show();
		} else if (v == move) {
			canvas.moveSelectedObject();
		} else if (v == delete) {
			canvas.deleteSelectedObjects();
		}
		// color
		else if (v == strokeColor) {
			colorConsumer = strokeColor;
			colorDialog.show();
		} else if (v == fillColor) {
			colorConsumer = fillColor;
			colorDialog.show();
		} else if (v == textColor) {
			colorConsumer = textColor;
			colorDialog.show();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v == select && !canvas.hasUnsavedChanges()) {
			int count = canvas.selectAllObject();
			if (count > 0) {
				if (count <= 1)
					move.setVisibility(View.GONE);
				Toast.makeText(this, "All object selected", Toast.LENGTH_SHORT)
						.show();
			}
		}
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		if (button == fillCheck) {
			if (isChecked)
				fillColor.setVisibility(View.VISIBLE);
			else
				fillColor.setVisibility(View.GONE);
		} else if (button == textBold)
			canvas.setFontBold(isChecked, true);
		else if (button == textItalic)
			canvas.setFontItalic(isChecked, true);
		else if (button == textUnderline)
			canvas.setTextUnderline(isChecked, true);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			if (seekBar == strokeWidth) {
				progress += CanvasObject.MIN_STROKE_WIDTH;
				strokeWidthText.setText(String.valueOf(progress));
				canvas.setStrokeWidth(progress, false);
				// TODO poly
				// } else if (seekBar == polySeek) {
				// polyText.setText(String.valueOf(progress
				// + PolygonObject.MIN_CORNER_COUNT));
			} else if (seekBar == textSize) {
				int size = progress + FontManager.MIN_FONT_SIZE;
				canvas.setFontSize(size, false);
				textSizeText.setText(String.valueOf(size));
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar == strokeWidth) {
			canvas.setStrokeWidth(seekBar.getProgress()
					+ CanvasObject.MIN_STROKE_WIDTH, true);
		} else if (seekBar == textSize) {
			int size = seekBar.getProgress() + FontManager.MIN_FONT_SIZE;
			canvas.setFontSize(size, true);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == strokeStyle) {
			canvas.setStrokeStyle(STROKE_STYLES[position], true);
		} else if (parent == fontStyles) {
			Font f = FontManager.getFont(position);
			if (f.hasBold()) {
				textBold.setEnabled(true);
			} else {
				textBold.setEnabled(false);
				canvas.setFontBold(false, false);
			}
			if (f.hasItalic())
				textItalic.setEnabled(true);
			else {
				textItalic.setEnabled(false);
				canvas.setFontItalic(false, false);
			}
			canvas.setFont(position, true);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (v == textInput) {
			canvas.setTextContent(textInput.getText().toString());
			return true;
		}
		return false;
	}
}
