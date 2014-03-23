package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.Formatter.BigDecimalLayoutForm;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.ImageObject;
import com.ppla03.collapaint.model.object.OvalObject;
import com.ppla03.collapaint.model.object.RectObject;
import com.ppla03.collapaint.model.object.ShapeHandler;
import com.ppla03.collapaint.model.object.TextObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
	public static enum Mode {
		DRAW, SELECTION, DRAW_EDIT, SELECTION_EDIT, EDIT_SHAPE, HAND
	}

	private Mode mode;
	private boolean hidden_mode;

	private final int SELECTION_COVER_COLOR = Color.argb(100, 0, 0, 0);
	private int scrollX, scrollY;
	private int limitScrollX, limitScrollY;
	private int anchorX, anchorY;

	private CanvasObject currentObject;
	private ImageObject protoImage;
	private OvalObject protoOval;
	private RectObject protoRect;
	private TextObject protoText;

	private ArrayList<CanvasObject> selectedObjects;

	private CanvasModel model;
	private int fillColor;
	private int strokeColor;
	private int strokeWidth;
	private int strokeStyle;

	private Bitmap cacheImage;
	private Canvas cacheCanvas;
	private Paint cachePaint;

	private Rect selectRect;
	private Paint selectPaint;
	private ShapeHandler[] shapeHandlers;
	private ShapeHandler grabbedHandler;

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void setModel(CanvasModel model) {
		this.model = model;
		cacheImage = Bitmap.createBitmap(model.getWidth(), model.getHeight(),
				Config.ARGB_8888);
		cacheCanvas = new Canvas(cacheImage);
		cachePaint = new Paint();
		cachePaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (model != null) {
			if (mode == Mode.SELECTION_EDIT) {
				int i = 0;
				int size = model.getObjects().size();
				for (i = 0; i < size; i++) {
					CanvasObject obj = model.getObjects().get(i);
					if (!obj.isSelected())
						obj.draw(canvas);
				}
				canvas.drawColor(SELECTION_COVER_COLOR);
				size = selectedObjects.size();
				for (i = 0; i < size; i++)
					selectedObjects.get(i).draw(canvas);
			} else {
				canvas.translate(scrollX, scrollY);
				if (cacheImage != null)
					canvas.drawBitmap(cacheImage, 0, 0, cachePaint);
				if (currentObject != null)
					currentObject.draw(canvas);
				if (mode == Mode.SELECTION)
					canvas.drawRect(selectRect, selectPaint);
				else if (mode == Mode.EDIT_SHAPE && shapeHandlers != null)
					for (int i = 0; i < shapeHandlers.length; i++)
						shapeHandlers[i].draw(canvas);
			}
		}
	}

	private void reloadCache() {
		cacheCanvas.drawColor(Color.WHITE);
		int size = model.getObjects().size();
		for (int i = 0; i < size; i++)
			model.getObjects().get(i).draw(cacheCanvas);
	}

	private void pushObjectToCache() {
		currentObject.draw(cacheCanvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (mode == Mode.SELECTION) {
				anchorX = x;
				anchorY = y;
				selectRect.left = x;
				selectRect.top = y;
				selectRect.right = x;
				selectRect.bottom = y;

			} else if (mode == Mode.HAND) {
				anchorX = scrollX - x;
				anchorY = scrollY - y;
			} else {
				anchorX = x - scrollX;
				anchorY = y - scrollY;
				if (mode == Mode.DRAW) {
					
				} else if (mode == Mode.EDIT_SHAPE) {
					if (shapeHandlers != null) {
						grabbedHandler = null;
						for (int i = 0; i < shapeHandlers.length; i++) {
							ShapeHandler sh = shapeHandlers[i];
							if (sh.grabbed(anchorX, anchorY)) {
								grabbedHandler = sh;
							}
						}
					}
				}
			}
		} else if (act == MotionEvent.ACTION_MOVE) {

		} else if (act == MotionEvent.ACTION_UP) {

		}
		return true;
	}

	// private void pushT

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void approveAction() {
		// TODO
	}

	public void cancelAction() {
		// TODO
	}

	public void setColor(int color) {
		// TODO
	}

	public void setStrokeWidth(int width) {
		// TODO
	}

	public void setStrokeStyle(int style) {
		// TODO
	}

	public void insertImage(Bitmap bitmap) {
		// TODO
	}

	public boolean selectArea(Rect area) {
		// TODO
		return false;
	}

	public void moveSelectedObject() {
		// TODO
	}

	public void copySelectedObjects() {
		// TODO
	}

	public void deleteSelectedObjects() {
		// TODO
	}

	public void cancelSelect() {
		// TODO
	}

	public void insertObjects(ArrayList<CanvasObject> objects) {
		// TODO
	}

	public boolean isUndoable() {
		// TODO
		return false;
	}

	public void undo() {
		// TODO
	}

	public boolean isRedoable() {
		// TODO
		return false;
	}

	public void redo() {
		// TODO
	}

	public void setHideMode(boolean hidden) {
		this.hidden_mode = hidden;
	}

	public void execute(ArrayList<UserAction> actions) {
		// TODO
	}

	public void closeCanvas() {
		// TODO
	}

	public void onUpdateComplete(int status) {
		// TODO
	}

	public void onCanvasClosed(int status) {
		// TODO
	}

}
