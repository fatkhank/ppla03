package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.CanvasListener;
import com.ppla03.collapaint.CanvasView;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.CanvasView.Mode;
import com.ppla03.collapaint.CanvasView.ObjectType;
import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.FontManager;
import com.ppla03.collapaint.model.object.StrokeStyle;

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
import android.widget.Toast;
import android.os.Build;

public class TesterActivity extends Activity implements OnClickListener,
		CanvasListener {
	private Button select, hand, fill, strokeWidth, strokeStyle, textSize,
			textFont, approve, cancel, undo, redo, move, copy, delete;
	private Button rect, oval, poly, lines, path, text, image, test;
	private CanvasView canvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tester);

		canvas = (CanvasView) findViewById(R.id.w_canvas);
		canvas.setMode(Mode.SELECT);
		CanvasModel cmod = new CanvasModel("njajal", 1000, 400);
		canvas.setModel(cmod);
		canvas.setListener(this);

		select = (Button) findViewById(R.id.w_select);
		hand = (Button) findViewById(R.id.w_hand);
		fill = (Button) findViewById(R.id.w_fill);
		strokeWidth = (Button) findViewById(R.id.w_stroke_width);
		strokeStyle = (Button) findViewById(R.id.w_stroke_style);
		textSize = (Button) findViewById(R.id.w_text_size);
		textFont = (Button) findViewById(R.id.w_text_font);
		approve = (Button) findViewById(R.id.w_approve);
		cancel = (Button) findViewById(R.id.w_cancel);
		undo = (Button) findViewById(R.id.w_undo);
		redo = (Button) findViewById(R.id.w_redo);
		move = (Button) findViewById(R.id.w_select_move);
		copy = (Button) findViewById(R.id.w_select_copy);
		delete = (Button) findViewById(R.id.w_select_del);

		rect = (Button) findViewById(R.id.w_draw_rect);
		oval = (Button) findViewById(R.id.w_draw_oval);
		path = (Button) findViewById(R.id.w_draw_path);
		poly = (Button) findViewById(R.id.w_draw_poly);
		lines = (Button) findViewById(R.id.w_draw_lines);
		text = (Button) findViewById(R.id.w_draw_text);
		image = (Button) findViewById(R.id.w_draw_image);
		test = (Button) findViewById(R.id.w_test);

		select.setOnClickListener(this);
		hand.setOnClickListener(this);
		fill.setOnClickListener(this);
		strokeWidth.setOnClickListener(this);
		strokeStyle.setOnClickListener(this);
		textSize.setOnClickListener(this);
		textFont.setOnClickListener(this);
		approve.setOnClickListener(this);
		cancel.setOnClickListener(this);
		undo.setOnClickListener(this);
		redo.setOnClickListener(this);
		move.setOnClickListener(this);
		copy.setOnClickListener(this);
		delete.setOnClickListener(this);

		rect.setOnClickListener(this);
		oval.setOnClickListener(this);
		poly.setOnClickListener(this);
		lines.setOnClickListener(this);
		path.setOnClickListener(this);
		text.setOnClickListener(this);
		image.setOnClickListener(this);
		test.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v == select) {
			canvas.setMode(CanvasView.Mode.SELECT);
		} else if (v == hand) {
			canvas.setMode(CanvasView.Mode.HAND);
		} else if (v == rect) {
			canvas.insertPrimitive(CanvasView.ObjectType.RECT);
		} else if (v == oval) {
			canvas.insertPrimitive(CanvasView.ObjectType.OVAL);
		} else if (v == poly) {
			canvas.insertPolygon(6);
		} else if (v == lines) {
			canvas.insertPrimitive(ObjectType.LINES);
		} else if (v == path) {
			canvas.insertPrimitive(CanvasView.ObjectType.PATH);
		} else if (v == text) {
			canvas.insertText("Sample Text");
		} else if (v == image) {
			Toast.makeText(this, "not avaiable", Toast.LENGTH_SHORT).show();
		} else if (v == fill) {
			if (++fillTurn >= fillColors.length)
				fillTurn = 0;
			canvas.setFillParameter(true, fillColors[fillTurn]);
		} else if (v == strokeWidth) {
			if (++strWidthTurn > 12)
				strWidthTurn = 1;
			canvas.setStrokeWidth(strWidthTurn);
		} else if (v == strokeStyle) {
			if (++strStyleTurn >= strokeStyles.length)
				strStyleTurn = 0;
			canvas.setStrokeStyle(strokeStyles[strStyleTurn]);
		} else if (v == textSize) {
			fontSizeTurn += 5;
			if (fontSizeTurn > 60)
				fontSizeTurn = 15;
			canvas.setFontSize(fontSizeTurn);
		} else if (v == textFont) {
			if (++fontTypeTurn >= FontManager.size())
				fontTypeTurn = 0;
			canvas.setFontStyle(fontTypeTurn);
		} else if (v == approve) {
			canvas.approveAction();
		} else if (v == cancel) {
			canvas.cancelAction();
		} else if (v == undo) {
			canvas.undo();
		} else if (v == redo) {
			canvas.redo();
		} else if (v == copy) {
			canvas.copySelectedObjects();
		} else if (v == delete) {
			canvas.deleteSelectedObjects();
		} else if (v == move) {
			canvas.moveSelectedObject();
		} else if (v == test) {
			canvas.invalidate();
		}
	}

	int fillTurn, strStyleTurn, strWidthTurn = 1, fontSizeTurn, fontTypeTurn;
	int[] fillColors = new int[] { Color.TRANSPARENT,
			Color.argb(120, 250, 100, 20), Color.argb(120, 100, 250, 20),
			Color.argb(120, 25, 100, 250) };
	int[] strokeStyles = new int[] { StrokeStyle.SOLID, StrokeStyle.DASHED,
			StrokeStyle.DOTTED };

	@Override
	public void onCanvasSelection(boolean success) {
		if (!success)
			Toast.makeText(this, "No object selected", Toast.LENGTH_SHORT)
					.show();
	}

	@Override
	public void onURStatusChange(boolean undoable, boolean redoable) {
		undo.setEnabled(undoable);
		redo.setEnabled(redoable);
	}
}
