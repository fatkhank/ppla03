package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import com.ppla03.collapaint.CanvasExporter;
import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.R.id;
import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.conn.ManageParticipantListener;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.object.ObjectClipboard;
import com.ppla03.collapaint.model.object.StrokeStyle;
import com.ppla03.collapaint.ui.ColorDialog.ColorChangeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WorkspaceActivity extends Activity implements OnClickListener,
		CanvasListener, ColorChangeListener, OnSeekBarChangeListener,
		OnItemSelectedListener, DialogInterface.OnClickListener,
		ManageParticipantListener {

	private ToggleButton currentMain;
	private ToggleButton select, draw, hand, stroke;
	private ImageButton color, image;

	private ImageButton approve, cancel;
	private ImageButton cut, copy, move, delete;
	private ImageButton rect, oval, poly, line, free, text;

	private Spinner strokeStyle;
	private SeekBar strokeWidth;
	private TextView strokeStyleLabel, strokeWidthLabel, strokeWidthText;

	private CanvasView canvas;
	private ColorDialog colorDialog;
	private AlertDialog insertTextDialog;
	private EditText insertTextInput;

	private AlertDialog polyDialog;
	private EditText polyInput;

	private ProgressBar progress;
	private View cover;

	// --- list participants ---
	private AlertDialog participantsDialog;
	private ArrayAdapter<String> participantAdapter;

	// --- export
	private AlertDialog downloadDialog;
	private Spinner edFormat;
	private CheckBox edCropped;
	private static final CompressFormat[] EXPORT_FORMATS = new CompressFormat[] {
			CompressFormat.PNG, CompressFormat.JPEG };
	private static final String[] EXPORT_FORMAT_TEXT = new String[] { "PNG",
			"JPG" };

	private static final int[] STROKE_STYLES = new int[] { StrokeStyle.SOLID,
			StrokeStyle.DASHED, StrokeStyle.DOTTED };
	private static final String[] STR_STYLES_NAMES = new String[] { "Solid",
			"Dashed", "Dotted" };

	private CanvasConnector connector;

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

		// --- approve ---
		approve = (ImageButton) findViewById(R.id.w_app);
		cancel = (ImageButton) findViewById(R.id.w_ccl);
		approve.setOnClickListener(this);
		cancel.setOnClickListener(this);
		showApproveBar(false);

		// --- select ---
		cut = (ImageButton) findViewById(R.id.w_sel_cut);
		copy = (ImageButton) findViewById(R.id.w_sel_copy);
		move = (ImageButton) findViewById(R.id.w_sel_move);
		delete = (ImageButton) findViewById(R.id.w_sel_del);
		cut.setOnClickListener(this);
		copy.setOnClickListener(this);
		move.setOnClickListener(this);
		delete.setOnClickListener(this);
		showSelectAdditionalBar(false);

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
		text.setOnClickListener(this);
		showDrawAdditionalBar(false);

		// --- stroke ---
		strokeStyleLabel = (TextView) findViewById(R.id.w_stroke_style_label);
		strokeStyle = (Spinner) findViewById(R.id.w_stroke_style);
		strokeWidth = (SeekBar) findViewById(R.id.w_stroke_width);
		strokeWidthText = (TextView) findViewById(R.id.w_stroke_width_text);
		strokeWidthLabel = (TextView) findViewById(R.id.w_stroke_width_label);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, STR_STYLES_NAMES);
		strokeStyle.setAdapter(adapter);
		strokeStyle.setOnItemSelectedListener(this);
		strokeWidth.setOnSeekBarChangeListener(this);
		showStrokeAdditionalBar(false);

		// --- prepare dialog ---
		colorDialog = new ColorDialog(this, this);
		colorDialog.setColor(Color.BLACK);

		// --- insert text dialog ---
		AlertDialog.Builder textADB = new AlertDialog.Builder(this);
		textADB.setPositiveButton("OK", this);
		textADB.setNegativeButton("Cancel", this);
		textADB.setMessage(getResources().getText(R.string.w_insert_text));
		insertTextInput = new EditText(this);
		insertTextInput.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
		textADB.setView(insertTextInput);
		insertTextDialog = textADB.create();

		// --- download image dialog ---
		AlertDialog.Builder exportDB = new AlertDialog.Builder(this);
		exportDB.setPositiveButton("Download", this);
		exportDB.setNegativeButton("Cancel", this);
		View exportDV = getLayoutInflater().inflate(R.layout.dialog_export,
				null);
		exportDB.setView(exportDV);
		edCropped = (CheckBox) exportDV.findViewById(R.id.ed_cropped);
		edFormat = (Spinner) exportDV.findViewById(R.id.ed_format);
		ArrayAdapter<String> exportFmtAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, EXPORT_FORMAT_TEXT);
		edFormat.setAdapter(exportFmtAdapter);
		downloadDialog = exportDB.create();

		// --- list participants dialog ---
		AlertDialog.Builder lisParDB = new AlertDialog.Builder(this);
		lisParDB.setTitle("Participant List");
		lisParDB.setPositiveButton("OK", this);
		participantAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1);
		lisParDB.setAdapter(participantAdapter, this);
		participantsDialog = lisParDB.create();

		// --- polygon dialog ---
		AlertDialog.Builder pdb = new AlertDialog.Builder(this);
		polyInput = new EditText(this);
		polyInput.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		polyInput.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
		pdb.setView(polyInput);
		pdb.setPositiveButton("Insert", this);
		pdb.setNegativeButton("Cancel", this);
		polyDialog = pdb.create();

		// --- progress ---
		progress = (ProgressBar) findViewById(R.id.w_progress);
		progress.setVisibility(View.GONE);
		cover = findViewById(R.id.w_cover);
		cover.setVisibility(View.GONE);

		// --- prepare canvas ---
		canvas = (CanvasView) findViewById(R.id.w_canvas);
		canvas.setListener(this);
		CanvasSynchronizer.getInstance().setCanvasView(canvas);
		connector = CanvasConnector.getInstance().setManageParticipantListener(
				this);

		onClick(select);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.wm_hide).setVisible(!canvas.isInHideMode());
		menu.findItem(R.id.wm_unhide).setVisible(canvas.isInHideMode());
		menu.findItem(R.id.wm_paste).setVisible(ObjectClipboard.hasObject());
		menu.findItem(R.id.wm_redo).setVisible(canvas.isRedoable());
		menu.findItem(R.id.wm_undo).setVisible(canvas.isUndoable());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.wm_hide:
			canvas.setHideMode(true);
			break;
		case R.id.wm_unhide:
			canvas.setHideMode(false);
			break;
		case R.id.wm_undo:
			canvas.undo();
			break;
		case R.id.wm_redo:
			canvas.redo();
			break;
		case R.id.wm_paste:
			canvas.pasteFromClipboard();
			break;
		case R.id.wm_download:
			downloadDialog.show();
			break;
		case R.id.wm_participant:
			progress.setVisibility(View.VISIBLE);
			cover.setVisibility(View.VISIBLE);
			connector.getParticipants(canvas.getModel());
			break;
		}
		return true;
	}

	@Override
	public void onColorChanged(int color) {
		canvas.setStrokeColor(color);
		canvas.setTextColor(color);
	}

	@Override
	public void onHideModeChange(boolean hidden) {}

	@Override
	public void onSelectionEvent(boolean success) {
		if (success)
			showSelectAdditionalBar(true);
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {}

	@Override
	public void onWaitForApproval() {
		showApproveBar(true);
		showDrawAdditionalBar(false);
	}

	@Override
	public void onBeginDraw() {
		showApproveBar(true);
		showDrawAdditionalBar(false);
	}

	@Override
	public void onClick(View v) {
		// main-toolbar
		if (v == select) {
			currentMain.setChecked(false);
			currentMain = select;
			canvas.setMode(CanvasView.Mode.SELECT);
			showSelectAdditionalBar(false);
			showApproveBar(false);
			showDrawAdditionalBar(false);
			showStrokeAdditionalBar(false);
		} else if (v == draw) {
			currentMain.setChecked(false);
			currentMain = draw;
			showSelectAdditionalBar(false);
			showStrokeAdditionalBar(false);
			showDrawAdditionalBar(true);
		} else if (v == hand) {
			currentMain.setChecked(false);
			currentMain = hand;
			canvas.setMode(CanvasView.Mode.HAND);
		} else if (v == color) {
			currentMain.setChecked(false);
			colorDialog.show();
		} else if (v == stroke) {
			currentMain.setChecked(false);
			currentMain = stroke;
			showSelectAdditionalBar(false);
			showDrawAdditionalBar(false);
			showStrokeAdditionalBar(true);
		} else if (v == image) {
			currentMain.setChecked(false);
			Toast.makeText(this, "not avaiable", Toast.LENGTH_SHORT).show();
		}

		// approve + cancel action
		else if (v == approve) {
			showApproveBar(false);
			canvas.approveAction();
			onClick(select);
		} else if (v == cancel) {
			showApproveBar(false);
			showStrokeAdditionalBar(false);
			canvas.cancelAction();
			if (canvas.isInDrawingMode())
				showDrawAdditionalBar(true);
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
			showSelectAdditionalBar(false);
			canvas.deleteSelectedObjects();
		}

		// draw-additional-toolbar
		else if (v == rect) {
			canvas.insertPrimitive(CanvasView.ObjectType.RECT);
		} else if (v == oval) {
			canvas.insertPrimitive(CanvasView.ObjectType.OVAL);
		} else if (v == poly) {
			polyDialog.show();
		} else if (v == line) {
			Toast.makeText(this, "Drag to make line.", Toast.LENGTH_SHORT)
					.show();
			canvas.insertPrimitive(ObjectType.LINE);
		} else if (v == free) {
			Toast.makeText(this, "Drag to make path.", Toast.LENGTH_SHORT)
					.show();
			canvas.insertPrimitive(CanvasView.ObjectType.FREE);
		} else if (v == text) {
			android.util.Log.d("POS", "text");
			insertTextDialog.show();
		}
	}

	/**
	 * Atur visibility approve dan cancel
	 * @param visible
	 */
	void showApproveBar(boolean visible) {
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
	void showSelectAdditionalBar(boolean visible) {
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
	void showDrawAdditionalBar(boolean visible) {
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
	void showStrokeAdditionalBar(boolean visible) {
		if (visible) {
			strokeStyleLabel.setVisibility(View.VISIBLE);
			strokeStyle.setVisibility(View.VISIBLE);
			strokeWidth.setVisibility(View.VISIBLE);
			strokeWidthLabel.setVisibility(View.VISIBLE);
			strokeWidthText.setVisibility(View.VISIBLE);
		} else {
			strokeStyleLabel.setVisibility(View.GONE);
			strokeStyle.setVisibility(View.GONE);
			strokeWidth.setVisibility(View.GONE);
			strokeWidthLabel.setVisibility(View.GONE);
			strokeWidthText.setVisibility(View.GONE);
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
				strokeWidthText.setText(String.valueOf(progress));
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
		if (parent == strokeStyle) {
			canvas.setStrokeStyle(STROKE_STYLES[position]);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == insertTextDialog) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				String text = insertTextInput.getText().toString();
				if (!text.isEmpty())
					canvas.insertText(text);
			}
		} else if (dialog == downloadDialog) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				boolean cropped = edCropped.isChecked();
				CompressFormat format = EXPORT_FORMATS[edFormat
						.getSelectedItemPosition()];
				if (CanvasExporter.export(canvas.getModel(), format, cropped) == CanvasExporter.SUCCESS) {
					MediaScannerConnection.scanFile(this,
							new String[] { CanvasExporter.getResultFile()
									.toString() }, null, null);
					Toast.makeText(this,
							"Downloaded to " + CanvasExporter.getResultFile(),
							Toast.LENGTH_LONG).show();
				} else
					Toast.makeText(this, "Download fail ", Toast.LENGTH_LONG)
							.show();
			}
		} else if (dialog == polyDialog) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				int corner = Integer.parseInt(polyInput.getText().toString());
				if (corner < 3)
					corner = 3;
				canvas.insertPolygon(corner);
			}
		}
	}

	@Override
	public void onParticipantFetched(CanvasModel canvas, UserModel owner,
			ArrayList<UserModel> participants) {
		participantAdapter.clear();
		participantAdapter.add(owner.username);
		for (int i = 0; i < participants.size(); i++)
			participantAdapter.add(participants.get(i).username);
		progress.setVisibility(View.GONE);
		cover.setVisibility(View.GONE);
		participantsDialog.show();
	}

	@Override
	public void onParticipationFetchedFailed(CanvasModel model, int status) {
		if (status == ServerConnector.CONNECTION_PROBLEM) {
			Toast.makeText(this, "Failed to fetch list. Connection problem.",
					Toast.LENGTH_SHORT).show();
		}
	}
}
