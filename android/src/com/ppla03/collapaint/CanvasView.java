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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;

public class CanvasView extends View implements OnLongClickListener {
	public static class Mode {
		/**
		 * Mode untuk menyeleksi objek yang ada di kanvas.
		 */
		public static final int SELECT = 3;

		/**
		 * Mode untuk menggeser kanvas.
		 */
		public static final int HAND = 8;

		/**
		 * Mode untuk menggambar.
		 */
		private static final int DRAW = 5;

		/**
		 * Mode saat ada satu objek yang sedang diseleksi (current objek dijamin
		 * tidak null). Bisa digabung dengan DRAW atau SELECTION_MODE
		 */
		private static final int EDIT = 16;

		/**
		 * Mode di mana ada objek yang diseleksi (selectedObject tidak kosong)
		 */
		private static int SELECTION_MODE = 32;

		/**
		 * Mode saat ada objek yang dipindah.
		 */
		private static int MOVING = 128;
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
	static final int EDIT_BORDER_PADDING = 10;
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
	private MoveMultiple protaMove;
	private StyleAction protaStyle;
	private ReshapeAction protaReshape;
	private int changeCounter;

	private ArrayList<CanvasObject> selectedObjects;
	private Stack<UserAction> userActions;
	private Stack<UserAction> redoStack;
	private ArrayList<UserAction> pendingStyleActions;
	private ArrayList<UserAction> pendingReshapeActions;
	private ArrayList<UserAction> pendingMoveActions;
	private ArrayList<UserAction> pendingDeleteActions;

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
	private Paint editPaint;
	private int cacheSelX, cacheSelY;

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
		pendingStyleActions = new ArrayList<>();
		pendingReshapeActions = new ArrayList<>();
		pendingMoveActions = new ArrayList<>();
		pendingDeleteActions = new ArrayList<>();

		selectRect = new Rect();
		selectPaint = new Paint();
		selectPaint.setStyle(Style.FILL);
		selectPaint.setColor(Color.argb(160, 100, 140, 255));
		editPaint = new Paint();
		editPaint.setStyle(Style.STROKE);
		editPaint.setColor(Color.BLACK);
		editPaint.setStrokeWidth(1);
		StrokeStyle.applyEffect(StrokeStyle.DASHED, editPaint);

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
				canvas.drawBitmap(cacheImageSelected, cacheSelX, cacheSelY,
						cachePaint);
			if (currentObject != null) {
				currentObject.draw(canvas);
				Rect r = currentObject.getBounds();
				canvas.drawRect(r.left - EDIT_BORDER_PADDING, r.top
						- EDIT_BORDER_PADDING, r.right + EDIT_BORDER_PADDING,
						r.bottom + EDIT_BORDER_PADDING, editPaint);
			}
			if (mode == Mode.SELECT)
				canvas.drawRect(selectRect, selectPaint);
			else if ((mode & Mode.EDIT) == Mode.EDIT)
				if (handler != null)
					handler.draw(canvas);
			debug(canvas);
		}
	}

	static Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	static {
		debugPaint.setColor(Color.RED);
		debugPaint.setTextSize(20);
		debugPaint.setTypeface(Typeface.MONOSPACE);
	}

	void debug(Canvas canvas) {
		canvas.translate(-scrollX, -scrollY);
		canvas.drawText("U[" + userActions.size() + "]", 250, 20, debugPaint);
		canvas.drawText("R[" + redoStack.size() + "]", 330, 20, debugPaint);
		canvas.drawText("C[" + changeCounter + "]", 410, 20, debugPaint);
		if (handler != null)
			canvas.drawText("[hnd]", getWidth() - 370, 45, debugPaint);
		if (grabbedCPoint != null)
			canvas.drawText("[gcb]", getWidth() - 310, 45, debugPaint);
		if (protaStyle != null)
			canvas.drawText("[sty]", getWidth() - 250, 45, debugPaint);
		if (protaReshape != null)
			canvas.drawText("[shp]", getWidth() - 190, 45, debugPaint);
		if (protaMove != null)
			canvas.drawText("[mov]", getWidth() - 130, 45, debugPaint);
		if (currentObject != null)
			canvas.drawText("[cob]", getWidth() - 70, 45, debugPaint);
		String res = "|";
		if ((mode & Mode.SELECT) == Mode.SELECT)
			res += "SE|";
		if ((mode & Mode.DRAW) == Mode.DRAW)
			res += "DW|";
		if ((mode & Mode.EDIT) == Mode.EDIT)
			res += "ED|";
		if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
			res += "SM|";
		if ((mode & Mode.HAND) == Mode.HAND)
			res += "HD|";
		if ((mode & Mode.MOVING) == Mode.MOVING)
			res += "MV|";
		canvas.drawText(res, getWidth() - 300, 20, debugPaint);
		for (int i = 0; i < userActions.size(); i++) {
			UserAction ua = userActions.get(i);
			canvas.drawText(ua.getClass().getSimpleName() + " >< "
					+ ua.getInverse().getClass().getSimpleName(), 10,
					30 + i * 20, debugPaint);
		}
		for (int i = 0; i < redoStack.size(); i++) {
			UserAction ua = redoStack.get(i);
			canvas.drawText(ua.getClass().getSimpleName() + " >< "
					+ ua.getInverse().getClass().getSimpleName(), 10,
					getHeight() - 20 - i * 20, debugPaint);
		}
	}

	private void reloadCache() {
		int size = model.objects.size();
		cacheCanvas.drawColor(Color.WHITE,
				android.graphics.PorterDuff.Mode.CLEAR);
		if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE) {
			for (int i = 0; i < size; i++) {
				CanvasObject obj = model.objects.get(i);
				if (!obj.isSelected())
					obj.draw(cacheCanvas);
			}
			if ((mode & Mode.EDIT) != Mode.EDIT) {
				cacheCanvas.drawColor(SELECTION_COVER_COLOR);
				cacheCanvas.setBitmap(cacheImageSelected);
				cacheCanvas.drawColor(Color.TRANSPARENT,
						android.graphics.PorterDuff.Mode.CLEAR);
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
			} else if ((mode & Mode.MOVING) == Mode.MOVING) {
				anchorX = cacheSelX - x;
				anchorY = cacheSelY - y;
				protaMove.anchorDown(x - scrollX, y - scrollY);
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
					if (handler != null)
						grabbedCPoint = handler.grab(anchorX, anchorY);
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
			} else if ((mode & Mode.MOVING) == Mode.MOVING) {
				cacheSelX = x + anchorX;
				cacheSelY = y + anchorY;
				x -= scrollX;
				y -= scrollY;
				protaMove.moveTo(x, y);
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
						grabbedCPoint.moveTo(x, y);
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
					if (selected) {
						selectedObjects.add(currentObject);
						editObject(currentObject, ShapeHandler.SHAPE_ONLY);
					}
				} else {
					selected = selectArea(selectRect);
					if (selectedObjects.size() == 1)
						editObject(selectedObjects.get(0),
								ShapeHandler.SHAPE_ONLY);
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
					redoStack.clear();
					listener.onURStatusChange(true, false);
					editObject(currentObject, ShapeHandler.ALL);
				}
			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				if (grabbedCPoint != null) {
					grabbedCPoint.release();
					grabbedCPoint = null;
					if (protaReshape != null) {
						changeCounter++;
						redoStack.clear();
						pushToUAStack(protaReshape.capture(), false);
					}
				}
			} else if ((mode & Mode.MOVING) == Mode.MOVING) {
				redoStack.clear();
				pushToUAStack(protaMove.anchorUp(), false);
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

	private void editObject(CanvasObject co, int filter) {
		currentObject = co;
		handler = co.getHandlers(filter);
		if ((mode & Mode.DRAW) != Mode.DRAW)
			protaReshape = new ReshapeAction(co, true);
		mode |= Mode.EDIT;
	}

	public void approveAction() {
		while (changeCounter-- > 0)
			userActions.pop();
		if ((mode & Mode.DRAW) == Mode.DRAW) {
			currentObject.draw(cacheCanvas);
			model.objects.add(currentObject);
			UserAction action = new DrawAction(currentObject);
			pushToUAStack(action, !hidden_mode);
			reloadCache();
		} else {
			int size = pendingDeleteActions.size();
			for (int i = 0; i < size; i++)
				execute(pendingDeleteActions.get(i), false);
			pendingDeleteActions.clear();
			if ((mode & Mode.MOVING) == Mode.MOVING) {
				if (protaMove != null) {
					protaMove.apply();
					pushToUAStack(protaMove, !hidden_mode);
					protaMove = null;
					pendingMoveActions.clear();
				}
			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				if (protaReshape != null) {
					pushToUAStack(protaReshape, !hidden_mode);
					protaReshape = null;
					pendingReshapeActions.clear();
				}
				if (protaStyle != null) {
					pushToUAStack(protaStyle, !hidden_mode);
					protaStyle = null;
					pendingStyleActions.clear();
				}
			}
			cancelSelect();
		}
		changeCounter = 0;
		currentObject = null;
		handler = null;
		mode = Mode.SELECT;
		invalidate();
		redoStack.clear();
	}

	public void cancelAction() {
		handler = null;
		currentObject = null;
		while (changeCounter-- > 0)
			userActions.pop();
		if ((mode & Mode.EDIT) == Mode.EDIT) {
			mode &= ~Mode.EDIT;
			if (protaStyle != null) {
				execute(protaStyle.getInverse(), true);
				if (!hidden_mode)
					syncon.addToBuffer(protaStyle);
				protaStyle = null;
			}
			if (protaReshape != null) {
				execute(protaReshape.getInverse(), true);
				if (!hidden_mode)
					syncon.addToBuffer(protaReshape);
				protaReshape = null;
			}
			if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
				cancelSelect();
		} else if ((mode & Mode.MOVING) == Mode.MOVING) {
			if (protaMove != null) {
				execute(protaMove.getInverse(), true);
				if (!hidden_mode)
					syncon.addToBuffer(protaStyle);
				protaMove = null;
			}
		} else
			return;
		for (int i = 0; i < pendingStyleActions.size(); i++)
			execute(pendingStyleActions.get(i), true);
		for (int i = 0; i < pendingReshapeActions.size(); i++)
			execute(pendingReshapeActions.get(i), true);
		for (int i = 0; i < pendingMoveActions.size(); i++)
			execute(pendingMoveActions.get(i), true);
		for (int i = 0; i < pendingDeleteActions.size(); i++)
			execute(pendingDeleteActions.get(i), true);
		pendingStyleActions.clear();
		pendingReshapeActions.clear();
		pendingMoveActions.clear();
		pendingDeleteActions.clear();
		reloadCache();
		invalidate();
	}

	public void setStrokeColor(int color) {
		strokeColor = color;
		if (currentObject != null) {
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeColor(color);
			else if (currentObject instanceof LinesObject)
				protoLines.setColor(color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
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
				protoLines.setWidth(width);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
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
				protoLines.setStrokeStyle(style);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void insertImage(Bitmap bitmap) {
		// TODO insert image
		objectType = ObjectType.IMAGE;
		setMode(Mode.DRAW);
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
		setMode(Mode.DRAW);
		editObject(protoText, ShapeHandler.ALL);
		invalidate();
	}

	public void setTextColor(int color) {
		strokeColor = color;
		if (currentObject != null && currentObject instanceof TextObject) {
			protoText = (TextObject) currentObject;
			protoText.setColor(color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void setFontSize(int size) {
		textSize = size;
		if (currentObject != null && currentObject instanceof TextObject) {
			protoText = (TextObject) currentObject;
			protoText.setSize(size);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void setFontStyle(int font) {
		textFont = font;
		if (currentObject != null && currentObject instanceof TextObject) {
			protoText = (TextObject) currentObject;
			protoText.setFontStyle(font);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void insertPrimitive(int type) {
		objectType = type;
		setMode(Mode.DRAW);
	}

	public void insertPolygon(int corner) {
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		int radius = getWidth() >> 1 - 20;
		protoPoly = new PolygonObject(corner, radius, x, y, fillColor,
				strokeColor, strokeWidth, strokeStyle);
		protoBasic = protoPoly;
		setMode(Mode.DRAW);
		editObject(protoPoly, ShapeHandler.ALL);
		invalidate();
	}

	public void setFillParameter(boolean filled, int color) {
		fillColor = (filled) ? color : Color.TRANSPARENT;
		if (currentObject != null && currentObject instanceof BasicObject) {
			protoBasic = (BasicObject) currentObject;
			protoBasic.setFillMode(filled, color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				if (protaStyle == null)
					protaStyle = new StyleAction(currentObject, true);
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
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
		if (((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
				&& ((mode & Mode.DRAW) != Mode.DRAW)) {
			if (protaMove == null) {
				protaMove = new MoveMultiple(selectedObjects, true);
				mode |= Mode.MOVING;
			}
		}
	}

	public void copySelectedObjects() {
		// TODO copy selected
	}

	public void deleteSelectedObjects() {
		DeleteMultiple dm = new DeleteMultiple(selectedObjects);
		model.objects.removeAll(selectedObjects);
		pushToUAStack(dm, !hidden_mode);
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

	private void pushToUAStack(UserAction action, boolean flush) {
		if (action == null)
			return;
		userActions.push(action);
		if (flush)
			syncon.addToBuffer(action);
		listener.onURStatusChange(true, !redoStack.isEmpty());
	}

	public boolean isUndoable() {
		return !userActions.isEmpty();
	}

	public void undo() {
		if (!userActions.isEmpty()) {
			if (changeCounter <= 0
					&& (((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE) || ((mode & Mode.DRAW) == Mode.DRAW))) {
				cancelAction();
				return;
			}
			UserAction action = userActions.pop();
			UserAction inverse = action.getInverse();
			if (inverse == null)
				return;
			redoStack.push(action);
			execute(inverse, true);
			changeCounter--;
			if (!hidden_mode)
				syncon.addToBuffer(inverse);
			reloadCache();
			invalidate();
			listener.onURStatusChange(!userActions.isEmpty(), true);
		}
	}

	public boolean isRedoable() {
		return redoStack.isEmpty();
	}

	public void redo() {
		if (!redoStack.isEmpty()) {
			UserAction action = redoStack.pop();
			execute(action, true);
			pushToUAStack(action, !hidden_mode);
			changeCounter++;
			reloadCache();
			invalidate();
		}
	}

	public void setHideMode(boolean hidden) {
		this.hidden_mode = hidden;
	}

	public void execute(ArrayList<UserAction> actions) {
		int size = actions.size();
		for (int i = 0; i < size; i++)
			execute(actions.get(i), false);
		reloadCache();
		invalidate();
	}

	/**
	 * Mengeksekusi suatu perintah.
	 * @param action aksi yang dieksekusi
	 * @param forced jika true -> aksi langsung dijalankan. jika false -> aksi
	 *            akan ditunda jika objek yang berkaitan dengan aksi tersebut
	 *            sedang diedit.
	 */
	private void execute(UserAction action, boolean forced) {
		if (action instanceof DrawAction) {
			DrawAction da = (DrawAction) action;
			da.object.draw(cacheCanvas);
			model.objects.add(da.object);
		} else if (action instanceof MoveAction) {
			MoveAction ma = (MoveAction) action;
			if (!forced && (protaMove != null) && protaMove.overwrites(ma))
				pendingMoveActions.add(ma);
			else
				ma.apply();
		} else if (action instanceof ReshapeAction) {
			ReshapeAction ra = (ReshapeAction) action;
			if (!forced && protaReshape != null && protaReshape.overwrites(ra))
				pendingReshapeActions.add(ra);
			else {
				ra.apply();
				if (handler != null) {
					if (grabbedCPoint != null) {
						grabbedCPoint.release();
						grabbedCPoint = null;
					}
					handler = currentObject
							.getHandlers(ShapeHandler.SHAPE_ONLY);
				}
			}
		} else if (action instanceof StyleAction) {
			StyleAction sa = (StyleAction) action;
			if (!forced && (protaStyle != null) && protaStyle.overwrites(sa))
				pendingStyleActions.add(sa);
			else
				sa.applyStyle();
		} else if (action instanceof DeleteAction) {
			DeleteAction da = (DeleteAction) action;
			if (!forced && currentObject != null
					&& da.object.equals(currentObject))
				pendingDeleteActions.add(da);
			else {
				int idx = model.objects.lastIndexOf(da.object);
				model.objects.remove(idx);
			}
		} else if (action instanceof MoveMultiple) {
			((MoveMultiple) action).apply();
		} else if (action instanceof DeleteMultiple) {
			DeleteMultiple da = (DeleteMultiple) action;
			if (!forced && currentObject != null
					&& da.objects.contains(currentObject))
				pendingDeleteActions.add(da);
			else
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

	public void onCanvasClosed(int status) {
		// TODO canvas close
	}
}
