package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.Stack;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

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
import android.view.View.OnLongClickListener;

public class CanvasView extends View implements OnLongClickListener {
	public static class Mode {
		public static final int SELECT = 3, DRAW = 5, HAND = 8;
		private static final int EDIT = 16, SELECTION_MODE = 32;
	}

	private int mode;

	public static class ObjectType {
		public static final int LINES = 1, RECT = 2, OVAL = 3, PATH = 4;
		private static final int IMAGE = 5, TEXT = 6;
	}

	private boolean hidden_mode;

	static final int CANVAS_BACKGROUND_COLOR = Color.rgb(80, 80, 80);
	static final int CANVAS_COLOR = Color.WHITE;
	static final int CANVAS_SHADOW_COLOR = Color.argb(150, 40, 40, 40);
	static final int CANVAS_SHADOW_RADIUS = 10;
	static final int CANVAS_SHADOW_DX = 0;
	static final int CANVAS_SHADOW_DY = 0;
	static final int CANVAS_MARGIN = 50;
	static final int SELECTION_COVER_COLOR = Color.argb(100, 0, 0, 0);
	static final int SELECTION_MIN_SIZE = 10;
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
	private TransformMultiple protaTransform;
	private StyleAction protaStyle;
	private ReshapeAction protaShape;

	private ArrayList<CanvasObject> selectedObjects;
	private Stack<UserAction> userActions;
	private Stack<UserAction> redoStack;

	private CanvasListener listener;
	private CanvasSynchronizer syncon;
	private CanvasModel model;
	private int fillColor;
	private int strokeColor;
	private int strokeWidth;
	private int strokeStyle;
	private int textSize;
	private int textFont;
	private boolean makeLoop;

	private Bitmap cacheImage;
	private Bitmap cacheImageSelected;
	private Canvas cacheCanvas;
	private Paint cachePaint;
	private Paint canvasPaint;

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

		fillColor = Color.RED;
		strokeColor = Color.BLUE;
		strokeWidth = 5;
		strokeStyle = StrokeStyle.DOTTED;
		textSize = 30;
		textFont = 0;

		scrollX = CANVAS_MARGIN;
		scrollY = CANVAS_MARGIN;
		canvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		canvasPaint.setStyle(Style.FILL);
		canvasPaint.setColor(CANVAS_COLOR);
		canvasPaint.setShadowLayer(CANVAS_SHADOW_RADIUS, CANVAS_SHADOW_DX,
				CANVAS_SHADOW_DY, CANVAS_SHADOW_COLOR);

		syncon = new CanvasSynchronizer(this);
		// syncon.start();

		setLayerType(LAYER_TYPE_SOFTWARE, canvasPaint);
		setOnLongClickListener(this);
		setLongClickable(true);
	}

	public void setListener(CanvasListener activity) {
		this.listener = activity;
	}

	public void setModel(CanvasModel model) {
		this.model = model;
		cacheImage = Bitmap.createBitmap(model.width, model.height,
				Config.ARGB_8888);
		cacheImageSelected = Bitmap.createBitmap(model.width, model.height,
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
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (model != null) {
			limitScrollX = w - CANVAS_MARGIN - model.width;
			limitScrollY = h - CANVAS_MARGIN - model.height;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(CANVAS_BACKGROUND_COLOR);
		if (model != null) {
			canvas.translate(scrollX, scrollY);
			canvas.drawRect(0, 0, model.width, model.height, canvasPaint);
			if (cacheImage != null)
				canvas.drawBitmap(cacheImage, 0, 0, cachePaint);
			if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
				canvas.drawBitmap(cacheImageSelected, 0, 0, cachePaint);
			if (currentObject != null)
				currentObject.draw(canvas);
			if (mode == Mode.SELECT)
				canvas.drawRect(selectRect, selectPaint);
			else if ((mode & Mode.EDIT) == Mode.EDIT)
				if (handler != null)
					handler.draw(canvas);
		}
	}

	private void reloadCache() {
		int size = model.objects.size();
		cacheCanvas.drawColor(Color.TRANSPARENT,
				android.graphics.PorterDuff.Mode.CLEAR);
		if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE) {
			for (int i = 0; i < size; i++) {
				CanvasObject obj = model.objects.get(i);
				if (!obj.isSelected())
					obj.draw(cacheCanvas);
			}
			if ((mode & Mode.EDIT) != Mode.EDIT) {
				cacheCanvas.drawColor(SELECTION_COVER_COLOR,
						android.graphics.PorterDuff.Mode.XOR);
				cacheCanvas.setBitmap(cacheImageSelected);
				size = selectedObjects.size();
				for (int i = 0; i < size; i++)
					selectedObjects.get(i).draw(cacheCanvas);
				cacheCanvas.setBitmap(cacheImage);
			}
		} else {
			for (int i = 0; i < size; i++)
				model.objects.get(i).draw(cacheCanvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getActionMasked();
		if (act == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if ((mode & Mode.HAND) == Mode.HAND) {
				anchorX = scrollX - x;
				anchorY = scrollY - y;
			} else {
				anchorX = x - scrollX;
				anchorY = y - scrollY;
				if (mode == Mode.SELECT)
					selectRect.set(anchorX, anchorY, anchorX, anchorY);
				else if (mode == Mode.DRAW) {
					if (objectType == ObjectType.LINES) {
						protoLines = new LinesObject(anchorX, anchorY,
								strokeColor, strokeWidth, strokeStyle);
						currentObject = protoLines;
					} else {
						if (objectType == ObjectType.RECT) {
							protoRect = new RectObject(anchorX, anchorY,
									fillColor, strokeColor, strokeWidth,
									strokeStyle);
							protoBasic = protoRect;
						} else if (objectType == ObjectType.OVAL) {
							protoOval = new OvalObject(anchorX, anchorY,
									fillColor, strokeColor, strokeWidth,
									strokeStyle);
							protoBasic = protoOval;
						} else if (objectType == ObjectType.PATH) {
							protoPath = new PathObject(anchorX, anchorY,
									fillColor, strokeColor, strokeWidth,
									strokeStyle);
							protoBasic = protoPath;
						}
						currentObject = protoBasic;
					}
				} else if ((mode & Mode.EDIT) == Mode.EDIT) {
					if (handler != null) {
						grabbedCPoint = handler.grab(anchorX, anchorY);
					}
				}
			}
		} else if (act == MotionEvent.ACTION_MOVE) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if ((mode & Mode.HAND) == Mode.HAND) {
				scrollX = x + anchorX;
				if (scrollX > CANVAS_MARGIN)
					scrollX = CANVAS_MARGIN;
				else if (scrollX < limitScrollX)
					scrollX = limitScrollX;
				scrollY = y + anchorY;
				if (scrollY > CANVAS_MARGIN)
					scrollY = CANVAS_MARGIN;
				else if (scrollY < limitScrollY)
					scrollY = limitScrollY;
			} else {
				x -= scrollX;
				y -= scrollY;
				if (x < 0 && x > model.width && y < 0 && y > model.height)
					return true;
				if (mode == Mode.SELECT) {
					selectRect.left = Math.min(anchorX, x);
					selectRect.top = Math.min(anchorY, y);
					selectRect.right = Math.max(anchorX, x);
					selectRect.bottom = Math.max(anchorY, y);
				} else if (mode == Mode.DRAW) {
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
				} else if ((mode & Mode.EDIT) == Mode.EDIT) {
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
			if (mode == Mode.SELECT) {
				int dx = selectRect.right - selectRect.left;
				int dy = selectRect.bottom - selectRect.top;
				boolean selected;
				if (dy < SELECTION_MIN_SIZE && dx < SELECTION_MIN_SIZE) {
					currentObject = selectAt(anchorX, anchorY,
							SELECTION_MIN_SIZE);
					selected = currentObject != null;
					if (selected)
						editObject(currentObject);
				} else {
					selected = selectArea(selectRect);
					if (selectedObjects.size() == 1)
						editObject(selectedObjects.get(0));
				}
				if (selected) {
					mode |= Mode.SELECTION_MODE;
					reloadCache();
				}
				selectRect.set(0, 0, 0, 0);
				listener.onCanvasSelection(selected);
			} else if (mode == Mode.DRAW) {
				if (objectType == ObjectType.RECT
						|| objectType == ObjectType.OVAL
						|| objectType == ObjectType.PATH
						|| objectType == ObjectType.LINES) {
					if (objectType == ObjectType.PATH && makeLoop)
						protoPath.close();
					editObject(currentObject);
				}
			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				if (grabbedCPoint != null)
					grabbedCPoint.release();
			}
		}
		invalidate();
		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO onlong click
		if ((mode & Mode.SELECT) == Mode.SELECT) {
			currentObject = selectAt(anchorX, anchorY, SELECTION_MIN_SIZE);
			if (currentObject != null) {
				mode |= Mode.SELECTION_MODE;
				selectedObjects.clear();
				selectedObjects.add(currentObject);
				// editObject(currentObject);
			}
		}
		return true;
	}

	public void setMode(int mode) {
		if (mode == Mode.HAND)
			this.mode ^= Mode.HAND;
		else {
			if (mode == Mode.SELECT
					&& (this.mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
				cancelSelect();
			if ((this.mode & Mode.EDIT) == Mode.EDIT)
				cancelAction();
			this.mode = mode;
		}
	}

	private void editObject(CanvasObject co) {
		currentObject = co;
		handler = co.getHandlers();
		mode |= Mode.EDIT;
	}

	public void approveAction() {
		if ((mode & Mode.DRAW) == Mode.DRAW) {
			currentObject.draw(cacheCanvas);
			model.objects.add(currentObject);
			UserAction action = new DrawAction(currentObject);
			userActions.push(action);
			if (!hidden_mode)
				syncon.addToBuffer(action);
		} else {
			if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE) {

			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				if (protaShape != null) {
					userActions.push(protaShape);
					if (!hidden_mode)
						syncon.addToBuffer(protaShape);
				} else if (protaStyle != null) {
					userActions.push(protaStyle);
					if (!hidden_mode)
						syncon.addToBuffer(protaStyle);
				}
				// TODO approve action
			}
			cancelSelect();
		}
		currentObject = null;
		handler = null;
		mode = Mode.SELECT;
		invalidate();
		redoStack.clear();
	}

	public void cancelAction() {
		handler = null;
		mode &= ~Mode.EDIT;
		protaStyle = null;
		protaShape = null;
		protaTransform = null;
		currentObject = null;
		reloadCache();
		invalidate();
	}

	public void setStrokeColor(int color) {
		strokeColor = color;
		if (currentObject != null) {
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeColor(color);
			else if (currentObject instanceof LinesObject)
				protoLines.setLineColor(color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				protaStyle.setStyle(fillColor, color, strokeWidth, strokeStyle);
			}
			postInvalidate();
		}
	}

	public void setStrokeWidth(int width) {
		strokeWidth = width;
		if (currentObject != null) {
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeWidth(width);
			else if (currentObject instanceof LinesObject)
				protoLines.setLineWidth(width);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				protaStyle.setStyle(fillColor, strokeColor, width, strokeStyle);
			}
			postInvalidate();
		}
	}

	public void setStrokeStyle(int style) {
		strokeStyle = style;
		if (currentObject != null) {
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeStyle(strokeStyle);
			else if (currentObject instanceof LinesObject)
				protoLines.setLineStyle(style);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				protaStyle.setStyle(fillColor, strokeColor, strokeWidth, style);
			}
			postInvalidate();
		}
	}

	public void insertImage(Bitmap bitmap) {
		// TODO insert image
		objectType = ObjectType.IMAGE;
	}

	public void setImageTransparency(int alpha) {
		protoImage.setTransparency(alpha);
	}

	public void insertText(String text) {
		objectType = ObjectType.TEXT;
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		protoText = new TextObject(text, x, y, strokeColor, textFont, textSize,
				true);
		mode = Mode.DRAW;
		editObject(protoText);
		invalidate();
	}

	public void setFontSize(int size) {
		textSize = size;
		if (protoText != null) {
			protoText.setParameter(strokeColor, size, textFont);
			invalidate();
		}
	}

	public void setFontType(int font) {
		textFont = font;
		if (protoText != null) {
			protoText.setParameter(strokeColor, textSize, font);
			invalidate();
		}
	}

	public void insertPrimitive(int type) {
		objectType = type;
		mode = Mode.DRAW;
	}

	public void insertPolygon(int corner) {
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		protoPoly = new PolygonObject(corner, x, y, fillColor, strokeColor,
				strokeWidth, strokeStyle);
		protoBasic = protoPoly;
		mode = Mode.DRAW;
		editObject(protoPoly);
		invalidate();
	}

	public void setFillParameter(boolean filled, int color) {
		fillColor = (filled) ? color : Color.TRANSPARENT;
		if (currentObject != null && currentObject instanceof BasicObject) {
			protoBasic.setFillMode(filled, color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				protaStyle.setStyle(fillColor, strokeColor, strokeWidth,
						strokeStyle);
			}
		}
		postInvalidate();
	}

	public void setMakeLoop(boolean loop) {
		this.makeLoop = loop;
		// TODO makeloop
	}

	private boolean selectArea(Rect area) {
		selectedObjects.clear();
		int size = model.objects.size();
		for (int i = 0; i < size; i++) {
			CanvasObject co = model.objects.get(i);
			if (co.selectedBy(selectRect))
				selectedObjects.add(co);
		}
		return !selectedObjects.isEmpty();
	}

	private CanvasObject selectAt(int x, int y, int radius) {
		int last = model.objects.size();
		if (last == 0)
			return null;
		last--;
		for (int i = last; i >= 0; i--) {
			CanvasObject co = model.objects.get(i);
			if (co.selectedBy(x, y, radius))
				return co;
		}
		return null;
	}

	public void moveSelectedObject() {
		// TODO move selected
	}

	public void copySelectedObjects() {
		// TODO copy selected
	}

	public void deleteSelectedObjects() {
		// TODO delete selected
	}

	public void cancelSelect() {
		currentObject = null;
		mode &= ~Mode.SELECTION_MODE;
		int size = selectedObjects.size();
		for (int i = 0; i < size; i++)
			selectedObjects.get(i).deselect();
		reloadCache();
		postInvalidate();
	}

	public boolean isUndoable() {
		return !userActions.isEmpty();
	}

	public void undo() {
		if (!userActions.isEmpty()) {
			UserAction action = userActions.pop();
			UserAction inverse = action.getInverse();
			if (inverse == null)
				return;
			redoStack.push(action);
			execute(inverse);
			if (!hidden_mode)
				syncon.addToBuffer(inverse);
			reloadCache();
			invalidate();
		}
	}

	public boolean isRedoable() {
		return redoStack.isEmpty();
	}

	public void redo() {
		if (!redoStack.isEmpty()) {
			UserAction action = redoStack.pop();
			userActions.push(action);
			execute(action);
			if (!hidden_mode)
				syncon.addToBuffer(action);
			reloadCache();
			invalidate();
		}
	}

	public void setHideMode(boolean hidden) {
		this.hidden_mode = hidden;
	}

	public void execute(ArrayList<UserAction> actions) {
		// TODO execute
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
		} else if (action instanceof TransformAction) {
		} else if (action instanceof ReshapeAction) {
		} else if (action instanceof StyleAction) {
			StyleAction sa = (StyleAction) action;
			sa.applyStyle();
		} else if (action instanceof DeleteAction) {
			DeleteAction da = (DeleteAction) action;
			int idx = model.objects.lastIndexOf(da.object);
			model.objects.remove(idx);
		} else if (action instanceof DeleteMultiple) {
			DeleteMultiple da = (DeleteMultiple) action;
			model.objects.removeAll(da.objects);
		} else if (action instanceof DrawMultiple) {// undelete
			DrawMultiple dm = (DrawMultiple) action;
			model.objects.addAll(dm.objects);
		} else if (action instanceof CopyAction) {
			// TODO copy action
		}
	}

	public void closeCanvas() {
		// TODO on close canvas
	}

	public void onUpdateComplete(int status) {
		// TODO onupdate complete
	}

	public void onCanvasClosed(int status) {
		// TODO canvas close
	}

	public void test() {
		// TODO
		syncon.test();
	}
}
