package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasSynchronizer.CanvasCloseListener;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.CanvasView.Param;
import com.ppla03.collapaint.FontManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.FontManager.Font;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.PolygonObject;
import com.ppla03.collapaint.ui.ColorPane.ColorChangeListener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
		OnCheckedChangeListener, OnEditorActionListener, AnimationListener,
		CanvasCloseListener, AnimatorUpdateListener, AnimatorListener {

	// --------- top bar ---------
	private View topbar;
	private CheckBox select, hand;
	private ImageButton undo, redo;

	// --------- select additional ---------
	private View selectAddButtons;
	private ImageButton cut, copy, move, delete;

	// --------- dashboard ---------
	private CheckBox showDash;
	private View dashboardView;
	Dashboard dashboard;
	private ValueAnimator animDash;

	// --------- property --------
	private CheckBox showProp;

	private int colorNormal, colorHidden, currentThemeColor;
	private TextView canvasTitle;
	private View colorConsumer;

	private ColorPane colorPane;
	private View colorPaneView;
	private View propertyPane;

	private ScaleAnimation animPropShow;
	private ScaleAnimation animPropHide;

	// --------- stroke setting ---------
	private RelativeLayout strokePane;
	private ImageButton strokeColor;
	private Spinner strokeStyle;
	private SeekBar strokeWidth;
	private TextView strokeWidthText;

	// --------- fill setting ---------
	private RelativeLayout fillPane;
	private CheckBox fillCheck;
	private ImageButton fillColor;

	// --------- font setting ---------
	private RelativeLayout textPane;
	private ImageButton textColor;
	private EditText textInput;
	private Spinner fontStyles;
	private SeekBar textSize;
	private TextView textSizeText;
	private CheckBox textBold, textItalic, textUnderline;

	// ---------- poly ------------
	private RelativeLayout shapePane;
	private SeekBar polySeek;
	private TextView polyText;

	// ------
	CanvasView canvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_workspace);
			// --- top bar ---
			topbar = findViewById(R.id.w_topbar);
			select = (CheckBox) findViewById(R.id.w_select);
			hand = (CheckBox) findViewById(R.id.w_hand);
			undo = (ImageButton) findViewById(R.id.w_undo);
			redo = (ImageButton) findViewById(R.id.w_redo);
			showDash = (CheckBox) findViewById(R.id.w_show_dash);

			select.setOnClickListener(this);
			select.setOnLongClickListener(this);
			hand.setOnClickListener(this);
			undo.setOnClickListener(this);
			undo.setEnabled(false);
			redo.setOnClickListener(this);
			redo.setEnabled(false);
			showDash.setOnClickListener(this);

			// --- select ---
			selectAddButtons = (LinearLayout) findViewById(R.id.w_selection_pane);
			selectAddButtons.setVisibility(View.GONE);
			showProp = (CheckBox) findViewById(R.id.w_show_property);
			showProp.setChecked(true);
			cut = (ImageButton) findViewById(R.id.w_sel_cut);
			copy = (ImageButton) findViewById(R.id.w_sel_copy);
			move = (ImageButton) findViewById(R.id.w_sel_move);
			delete = (ImageButton) findViewById(R.id.w_sel_del);
			showProp.setOnClickListener(this);
			cut.setOnClickListener(this);
			copy.setOnClickListener(this);
			move.setOnClickListener(this);
			delete.setOnClickListener(this);

			// --- property pane ---
			propertyPane = (View) findViewById(R.id.w_property_scroll);
			propertyPane.setVisibility(View.GONE);
			animPropShow = new ScaleAnimation(0, 1, 0, 1,
					ScaleAnimation.RELATIVE_TO_SELF, 1,
					ScaleAnimation.RELATIVE_TO_SELF, 0);
			animPropShow.setDuration(300);
			animPropShow.setAnimationListener(this);
			animPropShow.setFillAfter(true);
			animPropHide = new ScaleAnimation(1, 0, 1, 0,
					ScaleAnimation.RELATIVE_TO_SELF, 1,
					ScaleAnimation.RELATIVE_TO_SELF, 0);
			animPropHide.setDuration(300);
			animPropHide.setAnimationListener(this);
			animPropHide.setFillAfter(false);

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
			strokeStyle.setAdapter(new StrokeStyleAdapter(this));
			strokeStyle.setOnItemSelectedListener(this);

			// atur stroke color
			strokeColor = (ImageButton) findViewById(R.id.w_stroke_color);
			strokeColor.setOnClickListener(this);

			// --------- fill ---------
			fillPane = (RelativeLayout) findViewById(R.id.w_prop_fill);
			fillCheck = (CheckBox) findViewById(R.id.w_fill_check);
			fillCheck.setOnCheckedChangeListener(this);
			fillCheck.setChecked(false);
			fillColor = (ImageButton) findViewById(R.id.w_fill_color);
			fillColor.setOnClickListener(this);
			fillColor.setVisibility(View.GONE);

			// --------- font ---------
			textPane = (RelativeLayout) findViewById(R.id.w_prop_text);
			textInput = (EditText) findViewById(R.id.w_font_input);
			textInput.setOnEditorActionListener(this);
			textSize = (SeekBar) findViewById(R.id.w_font_size);
			fontStyles = (Spinner) findViewById(R.id.w_font_style);
			textBold = (CheckBox) findViewById(R.id.w_font_bold);
			textItalic = (CheckBox) findViewById(R.id.w_font_italic);
			textUnderline = (CheckBox) findViewById(R.id.w_font_underline);
			textBold.setOnCheckedChangeListener(this);
			textItalic.setOnCheckedChangeListener(this);
			textUnderline.setOnCheckedChangeListener(this);
			textSizeText = (TextView) findViewById(R.id.w_font_size_text);
			textSize.setOnSeekBarChangeListener(this);
			textSize.setMax(FontManager.MAX_FONT_SIZE
					- FontManager.MIN_FONT_SIZE);
			textSize.setProgress(FontManager.MIN_FONT_SIZE);
			fontStyles.setAdapter(FontManager.getAdapter(this));
			fontStyles.setOnItemSelectedListener(this);
			textColor = (ImageButton) findViewById(R.id.w_font_color);
			textColor.setOnClickListener(this);

			// --------- poly ---------
			shapePane = (RelativeLayout) findViewById(R.id.w_shape_pane);
			polySeek = (SeekBar) findViewById(R.id.w_poly_seek);
			polySeek.setOnSeekBarChangeListener(this);
			polySeek.setMax(PolygonObject.MAX_CORNER_COUNT
					- PolygonObject.MIN_CORNER_COUNT);
			polySeek.setProgress(0);
			polyText = (TextView) findViewById(R.id.w_poly_text);
			polyText.setText(String.valueOf(PolygonObject.MIN_CORNER_COUNT));

			// --- prepare color ---
			colorPaneView = findViewById(R.id.w_color_pane_scroll);
			colorPane = new ColorPane(this, colorPaneView, this);

			// --- prepare canvas ---
			colorNormal = getResources().getColor(R.color.workspace_normal);
			colorHidden = getResources().getColor(R.color.workspace_hidden);
			currentThemeColor = colorNormal;
			canvasTitle = (TextView) findViewById(R.id.w_canvas_name);
			canvas = (CanvasView) findViewById(R.id.w_canvas);
			canvas.setListener(this);
			CanvasSynchronizer.getInstance().setCanvasView(canvas);

			// --- dashboard ---
			dashboardView = findViewById(R.id.dashboard);
			dashboardView.setVisibility(View.GONE);
			dashboard = new Dashboard(savedInstanceState, this, dashboardView);
			animDash = new ValueAnimator();
			animDash.setDuration(500);
			animDash.addUpdateListener(this);
			animDash.addListener(this);
			topbar.bringToFront();

			// --- load ---
			canvasTitle.setText(canvas.getModel().name);
			onClick(select);
		} catch (Exception ex) {
			android.util.Log.d("POS", "e:" + ex);
			for (StackTraceElement s : ex.getStackTrace()) {
				android.util.Log.d("POS", "ex:" + s);
			}
		}

	}

	@Override
	public void onBackPressed() {
		closeCanvas();
	}

	public void closeCanvas() {
		CanvasSynchronizer.getInstance().closeCanvas(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// sama seperti showdash dipencet
		showDash.performClick();
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
			canvasTitle.setText(canvas.getModel().name + " (hide mode)");
			currentThemeColor = colorHidden;
		} else {
			canvasTitle.setText(canvas.getModel().name);
			currentThemeColor = colorNormal;
		}
		topbar.setBackgroundColor(currentThemeColor);
	}

	@Override
	public void onSelectionEvent(int state, int param) {
		if (state == CanvasListener.EDIT_MULTIPLE
				|| state == CanvasListener.EDIT_OBJECT) {
			hand.setChecked(false);
			select.setChecked(false);
			selectAddButtons.setVisibility(View.VISIBLE);
			strokePane.setVisibility(View.GONE);
			fillPane.setVisibility(View.GONE);
			textPane.setVisibility(View.GONE);
			shapePane.setVisibility(View.GONE);

			if (state == CanvasListener.EDIT_MULTIPLE) {
				move.setVisibility(View.VISIBLE);
				showProp.setVisibility(View.GONE);
			} else {
				move.setVisibility(View.GONE);
				showProp.setVisibility(View.VISIBLE);
				if (param == ObjectType.TEXT) {
					// jika sedang mengedit teks, tampilkan properti text saja
					textPane.setVisibility(View.VISIBLE);
				} else if (param == ObjectType.LINE) {
					// edit line
					strokePane.setVisibility(View.VISIBLE);
				} else {
					// jika sedang mengedit basic object, tampilkan properti
					// stroke dan fill
					if ((boolean) canvas.getObjectParam(Param.fillable))
						fillPane.setVisibility(View.VISIBLE);
					strokePane.setVisibility(View.VISIBLE);

					if (param == ObjectType.POLYGON)
						shapePane.setVisibility(View.VISIBLE);
				}
				setPropPaneVisibility(showProp.isChecked());
			}
		} else {
			select.setChecked(canvas.isInSelectionMode());
			selectAddButtons.setVisibility(View.GONE);
			setPropPaneVisibility(false);
		}
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {
		undo.setEnabled(undoable);
		redo.setEnabled(redoable);
	}

	@Override
	public void onWaitForApproval() {}

	@Override
	public void onBeginDraw() {}

	@Override
	public void onClick(View v) {
		// main-toolbar
		if (v == select) {
			canvas.setMode(CanvasView.Mode.SELECT);
			if (canvas.isInSelectionMode()) {
				hand.setChecked(false);
				select.setChecked(true);
			}
		} else if (v == hand) {
			canvas.setMode(CanvasView.Mode.HAND);
			if (canvas.isInHandMode())
				select.setChecked(false);
			else if (canvas.isInSelectionMode() && !canvas.hasSelectedObject())
				select.setChecked(true);
		} else if (v == undo) {
			canvas.undo();
		} else if (v == redo) {
			canvas.redo();
		} else if (v == showDash) {
			if (showDash.isChecked()) {
				// munculkan
				if (dashboardView.getVisibility() == View.GONE)
					dashboardView.setY(-dashboardView.getHeight());
				animDash.setFloatValues(dashboardView.getY(),
						topbar.getHeight());
				
				canvas.approveAction();
				
				hand.setVisibility(View.GONE);
				select.setVisibility(View.GONE);
				undo.setVisibility(View.GONE);
				redo.setVisibility(View.GONE);
				
			} else {
				// sembunyikan
				animDash.setFloatValues(dashboardView.getY(),
						-dashboardView.getHeight());
				
				hand.setVisibility(View.VISIBLE);
				select.setVisibility(View.VISIBLE);
				undo.setVisibility(View.VISIBLE);
				redo.setVisibility(View.VISIBLE);
			}
			animDash.start();
		} else if (v == showProp) {
			setPropPaneVisibility(showProp.isChecked());
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
			colorPane.setColor((int) canvas.getObjectParam(Param.strokeColor));
			colorPane.show();
		} else if (v == fillColor) {
			colorConsumer = fillColor;
			colorPane.setColor((int) canvas.getObjectParam(Param.fillColor));
			colorPane.show();
		} else if (v == textColor) {
			colorConsumer = textColor;
			colorPane.setColor((int) canvas.getObjectParam(Param.textColor));
			colorPane.show();
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
			canvas.setFillParameter(fillCheck.isChecked(),
					(int) canvas.getState(Param.fillColor), true);
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
			} else if (seekBar == polySeek) {
				progress += PolygonObject.MIN_CORNER_COUNT;
				canvas.setPolygonCorner(progress, false);
				polyText.setText(String.valueOf(progress));
			} else if (seekBar == textSize) {
				progress += FontManager.MIN_FONT_SIZE;
				canvas.setFontSize(progress, false);
				textSizeText.setText(String.valueOf(progress));
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
		} else if (seekBar == polySeek) {
			canvas.setPolygonCorner(seekBar.getProgress()
					+ PolygonObject.MIN_CORNER_COUNT, true);
		} else if (seekBar == textSize) {
			int size = seekBar.getProgress() + FontManager.MIN_FONT_SIZE;
			canvas.setFontSize(size, true);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == strokeStyle) {
			canvas.setStrokeStyle(position, true);
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
			canvas.setTextObjContent(textInput.getText().toString());
		}
		return true;
	}

	private void setPropPaneVisibility(boolean visible) {
		int vis = propertyPane.getVisibility();
		if (visible) {
			if (vis == View.GONE) {
				propertyPane.startAnimation(animPropShow);

				if (strokePane.getVisibility() == View.VISIBLE) {
					// warna stroke
					Integer strColor = (Integer) canvas
							.getObjectParam(Param.strokeColor);
					if (strokeColor != null)
						strokeColor.setBackgroundColor(strColor.intValue());
					else
						strokeColor.setBackgroundColor((int) canvas
								.getState(Param.strokeColor));

					// tebal stroke
					Integer strWidth = (Integer) canvas
							.getObjectParam(Param.strokeWidth);
					if (strWidth == null)
						strWidth = (int) canvas.getState(Param.strokeWidth);
					strokeWidth.setProgress(strWidth.intValue()
							- CanvasObject.MIN_STROKE_WIDTH);
					strokeWidthText.setText(String.valueOf(strWidth));

					// style stroke
					Integer strStyle = (Integer) canvas
							.getObjectParam(Param.strokeStyle);
					if (strStyle != null)
						strokeStyle.setSelection(strStyle.intValue());
					else
						strokeStyle.setSelection((int) canvas
								.getState(Param.strokeStyle));
				}

				if (textPane.getVisibility() == View.VISIBLE) {
					// isi teks
					String textContent = (String) canvas
							.getObjectParam(Param.textContent);
					if (textContent != null)
						textInput.setText(textContent);

					// warna teks
					Integer txtColor = (Integer) canvas
							.getObjectParam(Param.textColor);
					if (txtColor != null)
						textColor.setBackgroundColor(txtColor.intValue());
					else
						textColor.setBackgroundColor((int) canvas
								.getState(Param.textColor));

					// ukuran teks
					Integer txtSize = (Integer) canvas
							.getObjectParam(Param.textSize);
					if (txtSize == null)
						txtSize = (Integer) canvas.getState(Param.textSize);
					textSize.setProgress(txtSize.intValue()
							- FontManager.MIN_FONT_SIZE);
					textSizeText.setText(String.valueOf(txtSize));

					// teks bold
					Boolean txtBold = (Boolean) canvas
							.getObjectParam(Param.fontBold);
					if (txtBold != null)
						textBold.setChecked(txtBold.booleanValue());
					else
						textBold.setChecked((boolean) canvas
								.getState(Param.fontBold));

					// teks italic
					Boolean txtItalic = (Boolean) canvas
							.getObjectParam(Param.fontItalic);
					if (txtItalic != null)
						textItalic.setChecked(txtItalic.booleanValue());
					else
						textItalic.setChecked((boolean) canvas
								.getState(Param.fontItalic));

					// teks bergaris bawah
					Boolean txtUline = (Boolean) canvas
							.getObjectParam(Param.textUnderline);
					if (txtUline != null)
						textUnderline.setChecked(txtUline.booleanValue());
					else
						textUnderline.setChecked((boolean) canvas
								.getState(Param.textUnderline));
				}

				if (fillPane.getVisibility() == View.VISIBLE) {
					Boolean filled = (Boolean) canvas
							.getObjectParam(Param.filled);
					if (filled != null)
						fillCheck.setChecked(true);
					else
						fillCheck.setChecked(false);

					Integer fColor = (Integer) canvas
							.getObjectParam(Param.fillColor);
					if (fColor != null)
						fillColor.setBackgroundColor(fColor.intValue());
					else
						fillColor.setBackgroundColor((int) canvas
								.getState(Param.fillColor));

				}

				if (shapePane.getVisibility() == View.VISIBLE) {
					Integer polyCount = (Integer) canvas
							.getObjectParam(Param.polygonCorner);
					if (polyCount == null)
						polyCount = (int) canvas.getState(Param.polygonCorner);

					polySeek.setProgress(polyCount.intValue()
							- PolygonObject.MIN_CORNER_COUNT);
					polyText.setText(String.valueOf(polyCount));
				}
			}
		} else {
			if (vis == View.VISIBLE)
				propertyPane.startAnimation(animPropHide);
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if (animation == animPropShow) {
			propertyPane.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == animPropHide) {
			propertyPane.setVisibility(View.GONE);
			hand.setVisibility(View.VISIBLE);
			select.setVisibility(View.VISIBLE);
			undo.setVisibility(View.VISIBLE);
			redo.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onCanvasClosed(int status) {
		Intent intent = new Intent(this, BrowserActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (animation == animDash) {
			Float f = (Float) animDash.getAnimatedValue();
			dashboardView.setY(f);
		}
	}

	@Override
	public void onAnimationStart(Animator animation) {
		if (animation == animDash) {
			if (showDash.isChecked()) {
				// memulai animasi show dashboard
				dashboard.show();
			}
		}
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		if (animation == animDash) {
			if (!showDash.isChecked()) {
				// selesai animasi hide dashboard
				dashboard.hide();
			}
		}
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		super.onResume();
		dashboard.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		dashboard.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dashboard.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		dashboard.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		dashboard.onSaveInstanceState(outState);
	}
}
