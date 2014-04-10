package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.Stack;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
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
		public static final int LINE = 1, RECT = 2, OVAL = 3, FREE = 4;
		private static final int IMAGE = 5, TEXT = 6;
	}

	// Warna latar di luar kertas kanvas
	static final int CANVAS_BACKGROUND_COLOR = Color.rgb(80, 80, 80);
	// Warna kertas kanvas
	static final int CANVAS_COLOR = Color.WHITE;

	/*
	 * Pengaturan drop shadow kertas kanvas
	 */
	static final int CANVAS_SHADOW_COLOR = Color.argb(150, 40, 40, 40);
	static final int CANVAS_SHADOW_RADIUS = 10;
	static final int CANVAS_SHADOW_DX = 0;
	static final int CANVAS_SHADOW_DY = 0;

	// Jarak kertas kanvas ke View.
	static final int CANVAS_MARGIN = 50;
	// Warna untuk memisahkan objek yang tidak diseleksi dan diseleksi. Objek2
	// yang tidak diseleksi akan terkaburkan dengan warna ini, sedangkan objek
	// yang diseleksi tidak akan terpengaruh terhadap warna ini.
	static final int SELECTION_COVER_COLOR = Color.argb(100, 0, 0, 0);
	// Lebar minimal untuk seleksi area. Jika lebar kotak seleksi kurang dari
	// daerah ini, maka seleksi yang dilakukan adalah seleksi titik. Objek yang
	// terseleksi adalah objek yang paling atas tersentuh oleh titik seleksi.
	static final int SELECTION_MIN_SIZE = 10;

	// Posisi scroll kanvas, berisi posisi relatif pojok kiri atas kertas kanvas
	// terhadap pojok kiri atas CanvasView.
	private int scrollX, scrollY;
	// Batasan scroll, dihitung berdasarkan ukuran kanvas dan ukuran CanvasView.
	private int limitScrollX, limitScrollY;
	// Posisi di kanvas yang berada di tengah-tengah CanvasView.
	private int centerX, centerY;
	// Menyimpan koordinat saat touchdown.
	private int anchorX, anchorY;

	/**
	 * Berikut adalah daftar prototip objek yang sedang digambar dan aksi yang
	 * dijalankan. Dijabarkan tiap tipe untuk mengurangi proses casting.
	 */
	private int objectType;
	private CanvasObject currentObject;
	private ImageObject protoImage;
	private TextObject protoText;
	private OvalObject protoOval;
	private RectObject protoRect;
	private PolygonObject protoPoly;
	private FreeObject protoFree;
	private LineObject protoLine;
	private BasicObject protoBasic;
	private MoveMultiple protaMove;
	private StyleAction protaStyle;
	private ReshapeAction protaReshape;

	// Daftar objek yang diseleksi
	private final ArrayList<CanvasObject> selectedObjects = new ArrayList<>();
	// Daftar aksi terakhir, untuk undo
	private final Stack<UserAction> userActions = new Stack<>();
	// Daftar aksi untuk redo
	private final Stack<UserAction> redoStack = new Stack<>();
	// Banyak perubahan yang terjadi terhadap objek sejak diseleksi.
	private int changeCounter;

	/*
	 * Berikut adalah beberapa tempat penyimpanan aksi-aksi yang ditampung
	 * karena aksi tersebut melibatkan objek yang saat ini sedang diedit.
	 * Keputusan apakah aksi dijalankan atau tidak tergantung dari apakah
	 * pengguna membatalkan aksinya atau tidak. Jika pengguna membatalkan
	 * aksinya, maka aksi dalam penampungan ini dijalankan. Jika pengguna
	 * menyetujui aksinya, maka aksi dalam penampungan diabaikan karena ditimpa
	 * oleh aksi pengguna.
	 */
	private final ArrayList<UserAction> pendingStyleActions = new ArrayList<>();
	private final ArrayList<UserAction> pendingReshapeActions = new ArrayList<>();
	private final ArrayList<UserAction> pendingMoveActions = new ArrayList<>();
	private final ArrayList<UserAction> pendingDeleteActions = new ArrayList<>();

	private CanvasListener listener;
	private CanvasSynchronizer synczer;
	private CanvasModel model;
	private ShapeHandler handler;
	private ControlPoint grabbedCPoint;

	private static int fillColor;
	private static int strokeColor;
	private static int strokeWidth;
	private static int strokeStyle;
	private static int textSize;
	private static int textFont;
	private static int imageAlpha;
	private static boolean makeLoop;
	private static boolean hidden_mode;

	// Bitmap untuk menyimpan gambaran yang ada di kanvas, untuk mempercepat
	// proses refresh, tanpa harus menggambar tiap objek satu-persatu.
	private Bitmap cacheImage;
	// Gambar cacheImage
	private static final Paint cachePaint;
	static {
		cachePaint = new Paint();
		cachePaint.setStyle(Style.FILL);
	}

	// Bitmap untuk menyimpan gambar objek-objek yang sedang diseleksi.
	// Digunakan saat kumpulan objek tersebut digeser, penggeseran dilakukan di
	// bitmap, sedangkan penggeseran sesungguhnya dilakukan saat proses
	// penggeseran selesai.
	private Bitmap selectedObjectsCache;
	// Offset posisi selectedObjectsCache
	private int socX, socY;

	// Bertugas melakukan penggambaran ke cacheImage dan ke selectedObjectsCache
	private static final Canvas cacheCanvas = new Canvas();

	// Jarak yang diberikan antara kotak edit agar tidak menempel ke objek.
	private static final int EDIT_BORDER_PADDING = 10;
	// Kotak garis putus-putus di sekitar objek saat diseleksi.
	private static final RectF editRect = new RectF();
	// Cat editRect : garis putus-putus di sekitar objek saat seleksi objek.
	private static final Paint editPaint;
	static {
		editPaint = new Paint();
		editPaint.setStyle(Style.STROKE);
		editPaint.setColor(Color.BLACK);
		editPaint.setStrokeWidth(1);
		StrokeStyle.applyEffect(StrokeStyle.DASHED, editPaint);
	}

	// Kotak seleksi
	private static final RectF selectRect = new RectF();
	// Cat kotak seleksi
	private static final Paint selectPaint;
	static {
		selectPaint = new Paint();
		selectPaint.setStyle(Style.FILL);
		selectPaint.setARGB(100, 0, 30, 200);
	}

	// Cat kanvas putih dengan drop shadow ditepinya.
	private static final Paint canvasPaint;
	static {
		canvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		canvasPaint.setStyle(Style.FILL);
		canvasPaint.setColor(CANVAS_COLOR);
		canvasPaint.setShadowLayer(CANVAS_SHADOW_RADIUS, CANVAS_SHADOW_DX,
				CANVAS_SHADOW_DY, CANVAS_SHADOW_COLOR);
	}

	// Tulisan saat hidden_mode aktif
	private static final String HIDDEN_MODE_TEXT = "HIDE MODE";
	// Gambar tulisan HIDDEN_MODE
	private static final Paint hideModeTextPaint;
	static {
		hideModeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hideModeTextPaint.setColor(Color.GRAY);
		hideModeTextPaint.setTextSize(30);
		hideModeTextPaint.setTypeface(Typeface.create(Typeface.SERIF,
				Typeface.ITALIC));
	}

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mode = Mode.SELECT;

		// default value
		fillColor = Color.TRANSPARENT;
		strokeColor = Color.BLACK;
		strokeWidth = 1;
		strokeStyle = StrokeStyle.SOLID;
		textSize = 30;
		textFont = 0;
		imageAlpha = 255;

		scrollX = CANVAS_MARGIN;
		scrollY = CANVAS_MARGIN;

		synczer = new CanvasSynchronizer(this, context);
		// TODO start synchronizer
		// syncon.start();

		setLayerType(LAYER_TYPE_SOFTWARE, canvasPaint);
		setLongClickable(true);
	}

	/**
	 * Mengatur {@link CanvasListener}
	 * @param listener
	 */
	public void setListener(CanvasListener listener) {
		this.listener = listener;
	}

	/**
	 * Memuat data kanvas dari server. Jika proses sudah selesai, maka akan memanggil {@link CanvasListener#onCanvasModelLoaded(CanvasModel, int)}
	 * @param model model kanvas yang akan dibuka, minimal harus memiliki data
	 *            id kanvas.
	 */
	public void open(CanvasModel model) {
		this.model = model;
		synczer.loadCanvas(model);
		//TODO remove autoload
		onCanvasLoaded(1);
	}

	public void onCanvasLoaded(int status) {
		// ---- reset ----
		selectedObjects.clear();
		userActions.clear();
		redoStack.clear();
		pendingDeleteActions.clear();
		pendingMoveActions.clear();
		pendingReshapeActions.clear();
		pendingStyleActions.clear();
		selectRect.setEmpty();
		editRect.setEmpty();

		cacheImage = Bitmap.createBitmap(model.width, model.height,
				Config.ARGB_8888);
		selectedObjectsCache = Bitmap.createBitmap(model.width, model.height,
				Config.ARGB_8888);
		cacheCanvas.setBitmap(cacheImage);

		reloadCache();
		listener.onCanvasModelLoaded(model, status);
	}

	public CanvasModel getModel() {
		return model;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (model != null) {
			centerX = (w >> 1) - scrollX;
			centerY = (h >> 1) - scrollY;
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
				canvas.drawBitmap(selectedObjectsCache, socX, socY, cachePaint);
			if (currentObject != null) {
				currentObject.draw(canvas);
				currentObject.getWorldBounds(editRect);
				editRect.left -= EDIT_BORDER_PADDING;
				editRect.top -= EDIT_BORDER_PADDING;
				editRect.right += EDIT_BORDER_PADDING;
				editRect.bottom += EDIT_BORDER_PADDING;
				canvas.drawRect(editRect, editPaint);
				if (handler != null)
					handler.draw(canvas);
			} else if (mode == Mode.SELECT)
				canvas.drawRect(selectRect, selectPaint);
			if (hidden_mode)
				canvas.drawText(HIDDEN_MODE_TEXT, 10 - scrollX, 30 - scrollY,
						hideModeTextPaint);
		}

		// /* CHANGE TO LINE COMMENT TO DEBUG
		if (model != null)
			debug(canvas);
		// */
	}

	// /** CHANGE TO LINE COMMENT TO DEBUG
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
		for (int i = (userActions.size() > 5 ? userActions.size() - 5 : 0); i < userActions
				.size(); i++) {
			UserAction ua = userActions.get(i);
			canvas.drawText(
					ua.toString() + " >< " + ua.getInverse().toString(), 10,
					30 + i * 20, debugPaint);
		}
		for (int i = (redoStack.size() > 5 ? redoStack.size() - 5 : 0); i < redoStack
				.size(); i++) {
			UserAction ua = redoStack.get(i);
			canvas.drawText(
					ua.toString() + " >< " + ua.getInverse().toString(), 10,
					getHeight() - 20 - i * 20, debugPaint);
		}
	}

	// */

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
				cacheCanvas.setBitmap(selectedObjectsCache);
				cacheCanvas.drawColor(Color.TRANSPARENT,
						android.graphics.PorterDuff.Mode.CLEAR);
				size = selectedObjects.size();
				editRect.setEmpty();
				for (int i = 0; i < size; i++) {
					CanvasObject co = selectedObjects.get(i);
					co.draw(cacheCanvas);
					co.getWorldBounds(selectRect);
					editRect.union(selectRect);
				}
				editRect.left -= EDIT_BORDER_PADDING;
				editRect.top -= EDIT_BORDER_PADDING;
				editRect.right += EDIT_BORDER_PADDING;
				editRect.bottom += EDIT_BORDER_PADDING;
				selectRect.setEmpty();
				cacheCanvas.drawRect(editRect, editPaint);
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
				socX = 0;
				socY = 0;
				if ((mode & Mode.MOVING) == Mode.MOVING) {
					protaMove.anchorDown(anchorX, anchorY);
				} else if (mode == Mode.SELECT) {
					selectRect.set(anchorX, anchorY, anchorX, anchorY);
				} else if (mode == Mode.DRAW) {
					if (objectType == ObjectType.LINE) {
						protoLine = new LineObject(anchorX, anchorY,
								strokeColor, strokeWidth, strokeStyle);
						currentObject = protoLine;
					} else if (objectType == ObjectType.FREE) {
						protoFree = new FreeObject(makeLoop, fillColor,
								strokeColor, strokeWidth, strokeStyle).penDown(
								anchorX, anchorY);
						protoBasic = protoFree;
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
			} else {
				x -= scrollX;
				y -= scrollY;
				if ((mode & Mode.MOVING) == Mode.MOVING) {
					socX = x - anchorX;
					socY = y - anchorY;
					protaMove.moveTo(x, y);
				} else {
					if (x < 0 && x > model.width && y < 0 && y > model.height)
						return true;
					if (mode == Mode.SELECT) {
						selectRect.left = Math.min(anchorX, x);
						selectRect.top = Math.min(anchorY, y);
						selectRect.right = Math.max(anchorX, x);
						selectRect.bottom = Math.max(anchorY, y);
					} else if (mode == Mode.DRAW) {
						if (objectType == ObjectType.FREE) {
							protoFree.penTo(x, y);
						} else if (objectType == ObjectType.LINE) {
							protoLine.penTo(x, y);
						}
					} else if ((mode & Mode.EDIT) == Mode.EDIT) {
						if (grabbedCPoint != null)
							handler.dragPoint(grabbedCPoint, x, y);
					}
				}
			}
		} else if (act == MotionEvent.ACTION_UP) {
			if ((mode & Mode.HAND) == Mode.HAND) {
				centerX = (getWidth() >> 1) - scrollX;
				centerY = (getHeight() >> 1) - scrollY;
			} else if (mode == Mode.SELECT) {
				float dx = selectRect.right - selectRect.left;
				float dy = selectRect.bottom - selectRect.top;
				boolean selected;
				if (dy < SELECTION_MIN_SIZE && dx < SELECTION_MIN_SIZE) {
					currentObject = selectAt(anchorX, anchorY,
							SELECTION_MIN_SIZE);
					selected = currentObject != null;
					if (selected) {
						selectedObjects.add(currentObject);
						editObject(currentObject, ShapeHandler.SHAPE);
					}
				} else {
					selected = selectArea(selectRect);
					if (selectedObjects.size() == 1)
						editObject(selectedObjects.get(0), ShapeHandler.SHAPE);
				}
				if (selected) {
					mode |= Mode.SELECTION_MODE;
					reloadCache();
				}
				selectRect.setEmpty();
				listener.onSelectionEvent(selected);
			} else if (mode == Mode.DRAW) {
				if (objectType == ObjectType.FREE
						|| objectType == ObjectType.LINE) {
					if (objectType == ObjectType.FREE)
						protoFree.penUp();
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
				socX = 0;
				socY = 0;
				redoStack.clear();
				changeCounter++;
				MoveMultiple mm = protaMove.anchorUp();
				mm.apply();
				pushToUAStack(mm, false);
				reloadCache();
			}
		}
		invalidate();
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
		// TODO debug edit
		Log.d("POS", "id:" + co.privateID + ", gid:" + co.getGlobalID());

		currentObject = co;
		changeCounter = 0;
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
		postInvalidate();
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
				protaStyle = null;
			}
			if (protaReshape != null) {
				execute(protaReshape.getInverse(), true);
				protaReshape = null;
			}
			if ((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE)
				cancelSelect();
		} else if ((mode & Mode.MOVING) == Mode.MOVING) {
			mode &= ~Mode.MOVING;
			if (protaMove != null) {
				execute(protaMove.getInverse(), true);
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
		postInvalidate();
	}

	/**
	 * Mengganti warna pinggiran objek yang sedang aktif atau yang akan
	 * digambar. Berlaku untuk {@link LineObject} dan {@link BasicObject}.
	 * @param color. Lihat {@link Color}.
	 */
	public void setStrokeColor(int color) {
		strokeColor = color;
		if (currentObject != null) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeColor(color);
			else if (currentObject instanceof LineObject)
				protoLine.setColor(color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	/**
	 * Mengganti tebal garis yang sedang aktif atau akan digambar. Berlaku untuk
	 * {@link LineObject} dan {@link BasicObject}
	 * @param width
	 */
	public void setStrokeWidth(int width) {
		strokeWidth = width;
		if (currentObject != null) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeWidth(width);
			else if (currentObject instanceof LineObject)
				protoLine.setWidth(width);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	/**
	 * Mengganti jenis dekorasi pinggiran objek yang sedang aktif atau akan
	 * digambar. Berlaku untuk {@link LineObject} dan {@link BasicObject}.
	 * @param style jenis dekorasi. Lihat {@link StrokeStyle}.
	 */
	public void setStrokeStyle(int style) {
		strokeStyle = style;
		if (currentObject != null) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				protoBasic.setStrokeStyle(strokeStyle);
			else if (currentObject instanceof LineObject)
				protoLine.setStrokeStyle(style);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
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
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
	}

	public void setImageTransparency(int alpha) {
		// TODO image transparency
		imageAlpha = alpha;
		if (protoImage != null)
			protoImage.setTransparency(alpha);
	}

	public void insertText(String text) {
		objectType = ObjectType.TEXT;
		int x = Math.min(model.width, getWidth()) / 2 - scrollX;
		int y = Math.min(model.height, getHeight()) / 2 - scrollY;
		protoText = new TextObject(text, x, y, strokeColor, textFont, textSize);
		setMode(Mode.DRAW);
		editObject(protoText, ShapeHandler.ALL);
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		invalidate();
	}

	public void setTextColor(int color) {
		strokeColor = color;
		if (currentObject != null && currentObject instanceof TextObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			protoText = (TextObject) currentObject;
			protoText.setColor(color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void setFontSize(int size) {
		textSize = size;
		if (currentObject != null && currentObject instanceof TextObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			protoText = (TextObject) currentObject;
			protoText.setSize(size);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void setFontStyle(int font) {
		textFont = font;
		if (currentObject != null && currentObject instanceof TextObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			protoText = (TextObject) currentObject;
			protoText.setFontStyle(font);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
			postInvalidate();
		}
	}

	public void insertPrimitive(int type) {
		objectType = type;
		setMode(Mode.DRAW);
		if (objectType == ObjectType.RECT) {
			protoRect = new RectObject(centerX, centerY, centerY - 20,
					fillColor, strokeColor, strokeWidth, strokeStyle);
			protoBasic = protoRect;
			currentObject = protoBasic;
			editObject(currentObject, ShapeHandler.ALL);
		} else if (objectType == ObjectType.OVAL) {
			protoOval = new OvalObject(centerX, centerY, centerY - 20,
					fillColor, strokeColor, strokeWidth, strokeStyle);
			protoBasic = protoOval;
			currentObject = protoBasic;
			editObject(currentObject, ShapeHandler.ALL);
		}
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		invalidate();
	}

	public void insertPolygon(int corner) {
		protoPoly = new PolygonObject(corner, centerY - 20, centerX, centerY,
				fillColor, strokeColor, strokeWidth, strokeStyle);
		protoBasic = protoPoly;
		setMode(Mode.DRAW);
		editObject(protoPoly, ShapeHandler.ALL);
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		invalidate();
	}

	public void setFillParameter(boolean filled, int color) {
		fillColor = (filled) ? color : Color.TRANSPARENT;
		if (currentObject != null && currentObject instanceof BasicObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			protoBasic = (BasicObject) currentObject;
			protoBasic.setFillMode(filled, color);
			if ((mode & Mode.DRAW) != Mode.DRAW) {
				changeCounter++;
				pushToUAStack(protaStyle.capture(), false);
			}
		}
		postInvalidate();
	}

	public void setMakeLoop(boolean loop) {
		this.makeLoop = loop;
	}

	private boolean selectArea(RectF area) {
		selectedObjects.clear();
		int size = model.objects.size();
		for (int i = 0; i < size; i++) {
			CanvasObject co = model.objects.get(i);
			if (co.selectIn(area))
				selectedObjects.add(co);
		}
		return !selectedObjects.isEmpty();
	}

	private CanvasObject selectAt(int x, int y, int radius) {
		selectedObjects.clear();
		int last = model.objects.size();
		if (last == 0)
			return null;
		last--;
		for (int i = last; i >= 0; i--) {
			CanvasObject co = model.objects.get(i);
			if (co.selectAt(x, y, radius))
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
		ObjectClipboard.put(selectedObjects);
	}

	public void deleteSelectedObjects() {
		DeleteMultiple dm = new DeleteMultiple(selectedObjects);
		model.objects.removeAll(selectedObjects);
		pushToUAStack(dm, !hidden_mode);
		cancelSelect();
	}

	public void cancelSelect() {
		currentObject = null;
		selectRect.setEmpty();
		mode &= ~Mode.SELECTION_MODE;
		int size = selectedObjects.size();
		for (int i = 0; i < size; i++)
			selectedObjects.get(i).deselect();
		if ((mode & Mode.DRAW) == Mode.DRAW)
			cancelAction();
		reloadCache();
		postInvalidate();
	}

	public void pasteFromClipboard() {
		selectedObjects.clear();
		ArrayList<CanvasObject> objs = ObjectClipboard.retrieve();
		for (int i = 0; i < objs.size(); i++)
			selectedObjects.add(objs.get(i).cloneObject());
		DrawMultiple dm = new DrawMultiple(selectedObjects);
		pushToUAStack(dm, !hidden_mode);
		mode |= Mode.SELECTION_MODE;
		socX = 0;
		socY = 0;
		reloadCache();
		postInvalidate();
	}

	private void pushToUAStack(UserAction action, boolean flush) {
		if (action == null)
			return;
		userActions.push(action);
		if (flush)
			synczer.addToBuffer(action);
		listener.onURStatusChange(true, !redoStack.isEmpty());
	}

	public boolean isUndoable() {
		return !userActions.isEmpty();
	}

	public void undo() {
		if (!userActions.isEmpty()) {
			if (changeCounter == 0
					&& (((mode & Mode.SELECTION_MODE) == Mode.SELECTION_MODE) || ((mode & Mode.DRAW) == Mode.DRAW))) {
				cancelAction();
				redoStack.clear();
				listener.onURStatusChange(!userActions.isEmpty(), false);
				mode = Mode.SELECT;
				return;
			}
			UserAction action = userActions.pop();
			UserAction inverse = action.getInverse();
			if (inverse == null)
				return;
			if (inverse instanceof MoveMultiple) {
				socX = 0;
				socY = 0;
			}
			execute(inverse, true);
			if (!hidden_mode)
				synczer.addToBuffer(inverse);
			reloadCache();
			redoStack.push(action);
			changeCounter--;
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

	/**
	 * Mengatur hidden_mode. Jika hidden_mode true, akan tampil tanda hide mode
	 * di pojok kanvas.
	 * @param hidden
	 */
	public void setHideMode(boolean hidden) {
		this.hidden_mode = hidden;
		postInvalidate();
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
				if (handler != null && currentObject != null) {
					if (grabbedCPoint != null) {
						grabbedCPoint.release();
						grabbedCPoint = null;
					}
					handler = currentObject.getHandlers(ShapeHandler.SHAPE);
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
		}
	}

	public void closeCanvas() {
		// TODO on close canvas
	}

	public void onCanvasClosed(int status) {
		// TODO canvas close
	}

	public void test() {
		// TODO test method
		synczer.test();
	}
}
