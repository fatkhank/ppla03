package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.Formatter.BigDecimalLayoutForm;
import java.util.Stack;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.CopyAction;
import com.ppla03.collapaint.model.action.DeleteMultiple;
import com.ppla03.collapaint.model.action.DeleteAction;
import com.ppla03.collapaint.model.action.DrawAction;
import com.ppla03.collapaint.model.action.MoveAction;
import com.ppla03.collapaint.model.action.ReshapeAction;
import com.ppla03.collapaint.model.action.StyleAction;
import com.ppla03.collapaint.model.action.DrawMultiple;
import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.BasicObject;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.ImageObject;
import com.ppla03.collapaint.model.object.LinesObject;
import com.ppla03.collapaint.model.object.OvalObject;
import com.ppla03.collapaint.model.object.PathObject;
import com.ppla03.collapaint.model.object.PolygonObject;
import com.ppla03.collapaint.model.object.RectObject;
import com.ppla03.collapaint.model.object.ControlPoint;
import com.ppla03.collapaint.model.object.ShapeHandler;
import com.ppla03.collapaint.model.object.TextObject;
import com.ppla03.collapaint.ui.WorkspaceActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
	public static enum Mode {
		DRAW, SELECT, DRAW_EDIT, SELECTION_EDIT, EDIT_SHAPE, HAND
	}

	private Mode mode;

	public static class ObjectType {
		public static int LINES, RECT, OVAL, PATH;
		private static int IMAGE, TEXT;
	}

	private boolean hidden_mode;

	private final int SELECTION_COVER_COLOR = Color.argb(100, 0, 0, 0);
	private int scrollX, scrollY;
	private int limitScrollX, limitScrollY;
	private int anchorX, anchorY;

	private int objectType;
	private CanvasObject currentObject;
	private ImageObject protoImage;
	private TextObject protoText;
	private OvalObject protoOval;
	private RectObject protoRect;
	private PolygonObject protoPoly;
	private PathObject protoPath;
	private LinesObject protoLines;
	private BasicObject protoBasic;

	private ArrayList<CanvasObject> selectedObjects;
	private Stack<UserAction> userActions;
	private Stack<UserAction> redoStack;

	private WorkspaceActivity activity;
	private CanvasSynchronizer syncon;
	private CanvasModel model;
	private int fillColor;
	private int strokeColor;
	private int strokeWidth;
	private int strokeStyle;
	private int textSize;
	private boolean makeLoop;

	private Bitmap cacheImage;
	private Canvas cacheCanvas;
	private Paint cachePaint;

	private Rect selectRect;
	private Paint selectPaint;
	private ShapeHandler handler;
	private ControlPoint grabbedCPoint;

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mode = Mode.SELECT;

		selectedObjects = new ArrayList<>();
		userActions = new Stack<>();
		redoStack = new Stack<>();

		selectRect = new Rect();
		selectPaint = new Paint();
		selectPaint.setStyle(Style.FILL);
		selectPaint.setColor(Color.argb(160, 100, 140, 255));

		fillColor = Color.argb(160, 255, 100, 50);
		strokeColor = Color.argb(200, 100, 250, 40);
		strokeWidth = 5;

		syncon = new CanvasSynchronizer(this);
//		syncon.start();
	}

	public void setModel(CanvasModel model) {
		this.model = model;
		cacheImage = Bitmap.createBitmap(model.width, model.height,
				Config.ARGB_8888);
		cacheCanvas = new Canvas(cacheImage);
		cachePaint = new Paint();
		cachePaint.setStyle(Style.FILL);

		reloadCache();
	}

	public CanvasModel getModel() {
		return model;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (model != null) {
			if (mode == Mode.SELECTION_EDIT) {
				int i = 0;
				int size = model.objects.size();
				for (i = 0; i < size; i++) {
					CanvasObject obj = model.objects.get(i);
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
				if (mode == Mode.SELECT)
					canvas.drawRect(selectRect, selectPaint);
				else if (mode == Mode.EDIT_SHAPE || mode == Mode.DRAW_EDIT)
					if (handler != null)
						handler.draw(canvas);
			}
		}
	}

	private void reloadCache() {
		cacheCanvas.drawColor(Color.WHITE);
		int size = model.objects.size();
		for (int i = 0; i < size; i++)
			model.objects.get(i).draw(cacheCanvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (mode == Mode.SELECT) {
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
					if (objectType == ObjectType.RECT) {
						protoRect = new RectObject(anchorX, anchorY, fillColor,
								strokeColor, strokeWidth, strokeStyle);
						protoBasic = protoRect;
					} else if (objectType == ObjectType.OVAL) {
						protoOval = new OvalObject(anchorX, anchorY, fillColor,
								strokeColor, strokeWidth, strokeStyle);
						protoBasic = protoOval;
					} else if (objectType == ObjectType.PATH) {
						protoPath = new PathObject(anchorX, anchorY, fillColor,
								strokeColor, strokeWidth, strokeStyle);
						protoBasic = protoPath;
					} else if (objectType == ObjectType.LINES) {
						protoLines = new LinesObject(anchorX, anchorY,
								fillColor, strokeColor, strokeWidth,
								strokeStyle);
						protoLines.newLine(anchorX, anchorY);
						protoBasic = protoLines;
					}
					currentObject = protoBasic;
				} else if (mode == Mode.EDIT_SHAPE || mode == Mode.DRAW_EDIT) {
					if (handler != null) {
						grabbedCPoint = handler.grab(anchorX, anchorY);
					}
				}
			}
		} else if (act == MotionEvent.ACTION_MOVE) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (mode == Mode.SELECT) {
				selectRect.left = Math.min(anchorX, x);
				selectRect.top = Math.min(anchorY, y);
				selectRect.right = Math.max(anchorX, x);
				selectRect.bottom = Math.max(anchorY, y);
			} else if (mode == Mode.HAND) {
				// scrollX = x + anchorX;
				// if (scrollX > CANVAS_MARGIN)
				// scrollX = CANVAS_MARGIN;
				// else if (scrollX < minScrollX)
				// scrollX = minScrollX;
				// scrollY = y + anchorY;
				// if (scrollY > CANVAS_MARGIN)
				// scrollY = CANVAS_MARGIN;
				// else if (scrollY < minScrollY)
				// scrollY = minScrollY;
			} else {
				x -= scrollX;
				y -= scrollY;
				if (mode == Mode.DRAW) {
					if (objectType == ObjectType.PATH) {
						protoPath.penTo(x, y);
					} else if (objectType == ObjectType.LINES) {
						protoLines.penTo(x, y);
					} else {
						int left = Math.min(anchorX, x);
						int top = Math.min(anchorY, y);
						int right = Math.max(anchorX, x);
						int bottom = Math.max(anchorY, y);
						if (objectType == ObjectType.RECT) {
							protoRect.setDimension(left, top, right, bottom);
						} else if (objectType == ObjectType.OVAL) {
							protoOval.setDimension(left, top, right, bottom);
						}
					}
				} else if (mode == Mode.EDIT_SHAPE || mode == Mode.DRAW_EDIT) {
					if (grabbedCPoint != null) {
						int ox = grabbedCPoint.getX();
						int oy = grabbedCPoint.getY();
						grabbedCPoint.setPosition(x, y);
						currentObject.onHandlerMoved(handler, grabbedCPoint,
								ox, oy);
					}
				}
			}
		} else if (act == MotionEvent.ACTION_UP) {
			if (mode == Mode.DRAW) {
				if (objectType == ObjectType.RECT
						|| objectType == ObjectType.OVAL
						|| objectType == ObjectType.PATH
						|| objectType == ObjectType.LINES) {
					if (objectType == ObjectType.PATH && makeLoop)
						protoPath.close();
					handler = currentObject.getHandlers();
					mode = Mode.DRAW_EDIT;
				}
			} else if (mode == Mode.DRAW || mode == Mode.DRAW_EDIT) {
				if (grabbedCPoint != null)
					grabbedCPoint.release();
			}
		}
		invalidate();
		return true;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void approveAction() {
		if (mode == Mode.DRAW_EDIT) {
			currentObject.draw(cacheCanvas);
			UserAction action = new DrawAction(currentObject);
			userActions.push(action);
			if (!hidden_mode)
				syncon.addToBuffer(action);
			currentObject = null;
			handler = null;
			mode = Mode.DRAW;
			invalidate();
		}
		redoStack.clear();
	}

	public void cancelAction() {
		// TODO
	}

	public void setColor(int color) {
		// TODO
	}

	public void setStrokeWidth(int width) {
		// TODO
		strokeWidth = width;
		if (objectType == ObjectType.RECT || objectType == ObjectType.OVAL
				|| objectType == ObjectType.PATH) {
			protoBasic.setStrokeMode(strokeColor, strokeWidth, strokeStyle);
		}
		invalidate();
	}

	public void setStrokeStyle(int style) {
		// TODO
	}

	public void insertImage(Bitmap bitmap) {
		// TODO
		objectType = ObjectType.IMAGE;
	}

	public void setImageTransparency(int alpha) {
		protoImage.setTransparency(alpha);
	}

	public void insertText(String text) {
		objectType = ObjectType.TEXT;
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		protoText = new TextObject(text, x, y, strokeColor, textSize, true);
		currentObject = protoText;
		mode = Mode.DRAW_EDIT;
		handler = protoText.getHandlers();
		invalidate();
	}

	public void setTextParameter(int size, Typeface font) {
		textSize = size;
		if (protoText != null) {
			protoText.setPararameter(size, font);
			invalidate();
		}
	}

	public void insertPrimitive(int type) {
		objectType = type;
	}

	public void insertPolygon(int corner) {
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		protoPoly = new PolygonObject(corner, x, y, fillColor, strokeColor,
				strokeWidth, strokeStyle);
		protoBasic = protoPoly;
		currentObject = protoBasic;
		mode = Mode.DRAW_EDIT;
		handler = protoPoly.getHandlers();
		invalidate();
	}

	public void setFillParameter(boolean filled, int color) {
		// TODO
		if (objectType == ObjectType.RECT || objectType == ObjectType.OVAL
				|| objectType == ObjectType.PATH)
			protoBasic.setFillMode(filled, color);
		invalidate();
	}

	public void setMakeLoop(boolean loop) {
		this.makeLoop = loop;
		// TODO
	}

	private boolean selectArea(Rect area) {
		// TODO
		return false;
	}

	private void selectAt(int x, int y, int radius) {
		// TODO
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

	public boolean isUndoable() {
		return !userActions.isEmpty();
	}

	public void undo() {
		// TODO
		UserAction action = userActions.pop();
		redoStack.push(action);
		UserAction inverse = action.getInverse();
		execute(inverse);
		if (!hidden_mode)
			syncon.addToBuffer(inverse);
		reloadCache();
		invalidate();
	}

	public boolean isRedoable() {
		return redoStack.isEmpty();
	}

	public void redo() {
		// TODO
		UserAction action = redoStack.pop();
		userActions.push(action);
		execute(action);
		if (!hidden_mode)
			syncon.addToBuffer(action);
		reloadCache();
		invalidate();
	}

	public void setHideMode(boolean hidden) {
		this.hidden_mode = hidden;
	}

	public void execute(ArrayList<UserAction> actions) {
		// TODO
		int size = actions.size();
		for (int i = 0; i < size; i++)
			execute(actions.get(i));
		reloadCache();
		invalidate();
	}

	private void execute(UserAction action) {
		if (action instanceof DrawAction) {
			DrawAction da = (DrawAction) action;
			da.object.draw(cacheCanvas);
			model.objects.add(da.object);
		} else if (action instanceof MoveAction) {
		} else if (action instanceof ReshapeAction) {
		} else if (action instanceof StyleAction) {
		} else if (action instanceof DeleteAction) {
			DeleteAction da = (DeleteAction) action;
			int idx = model.objects.lastIndexOf(da.object);
		} else if (action instanceof DeleteMultiple) {
			DeleteMultiple da = (DeleteMultiple) action;
			model.objects.removeAll(da.objects);
		} else if (action instanceof DrawMultiple) {// undelete
			DrawMultiple dm = (DrawMultiple) action;
			model.objects.addAll(dm.objects);
		} else if (action instanceof CopyAction) {

		}
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
	
	
	public void test(){
		//TODO
		syncon.test();
	}
}
