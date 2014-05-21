package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.Stack;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.action.MoveMultiple.MoveStepper;
import com.ppla03.collapaint.model.object.*;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ColorFilter;
import android.graphics.Paint.Style;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

public class CanvasView extends View implements View.OnLongClickListener {
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
		private static final int HAS_SELECTION = 32;

		/**
		 * Mode saat ada objek yang dipindah.
		 */
		private static final int MOVING = 128;

	}

	private int mode;

	public static class ObjectType {
		public static final int NONE = 0, LINE = 1, FREE = 2, RECT = 3,
				OVAL = 4, POLYGON = 5, TEXT = 6, MULTIPLE = 7;
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
	static final int CANVAS_MARGIN = 150;
	// Warna untuk memisahkan objek yang tidak diseleksi dan diseleksi. Objek2
	// yang tidak diseleksi akan terkaburkan dengan warna ini, sedangkan objek
	// yang diseleksi tidak akan terpengaruh terhadap warna ini.
	static final int SELECTION_COVER_COLOR = Color.argb(50, 0, 0, 0);
	// Lebar minimal untuk seleksi area. Jika lebar kotak seleksi kurang dari
	// daerah ini, maka seleksi yang dilakukan adalah seleksi titik. Objek yang
	// terseleksi adalah objek yang paling atas tersentuh oleh titik seleksi.
	static final int SELECTION_MIN_SIZE = 10;

	// Batas agar objek tidak keluar dari kanvas. Asalkan ada bagian objek yang
	// masuk dalam kanvas sejauh batas ini.
	static final int OBJECT_LIMIT = 30;
	// Menampung bound objek untuk melakukan pembatasan gerakkan objek
	static final RectF limiter = new RectF();

	// Posisi scroll kanvas, berisi posisi relatif pojok kiri atas kertas kanvas
	// terhadap pojok kiri atas CanvasView.
	private int scrollX, scrollY;
	// Batasan scroll, dihitung berdasarkan ukuran kanvas dan ukuran CanvasView.
	private int minScrollX, minScrollY, maxScrollX, maxScrollY;
	// Posisi di kanvas yang berada di tengah-tengah CanvasView.
	private int centerX, centerY;
	// Menyimpan koordinat saat touchdown.
	private int anchorX, anchorY;

	private int defaultRectSize, defaultOvalRadius, defaultPolyRadius;

	/**
	 * Berikut adalah daftar objek yang sedang digambar dan aksi yang
	 * dijalankan. Dijabarkan tiap tipe untuk mengurangi proses casting.
	 */
	private int objectType;
	private CanvasObject currentObject;
	private TextObject currentText;
	private OvalObject currentOval;
	private RectObject currentRect;
	private PolygonObject currentPoly;
	private FreeObject currentFree;
	private LineObject currentLine;
	private BasicObject currentBasic;
	private int currentType;
	private MoveMultiple protaMove;
	private StyleAction protaStyle;
	private ReshapeAction protaReshape;

	// Daftar objek yang diseleksi
	private final ArrayList<CanvasObject> selectedObjects = new ArrayList<CanvasObject>();
	// Daftar aksi terakhir, untuk undo
	private final Stack<UserAction> userActions = new Stack<UserAction>();
	// Daftar aksi untuk redo
	private final Stack<UserAction> redoStack = new Stack<UserAction>();
	// Daftar aksi yang dilakukan saat hide_mode
	private final ArrayList<UserAction> revertList = new ArrayList<>();
	// Banyak perubahan yang terjadi terhadap objek sejak diseleksi.
	private int checkpoint;

	/*
	 * Berikut adalah beberapa tempat penyimpanan aksi-aksi yang ditampung
	 * karena aksi tersebut melibatkan objek yang saat ini sedang diedit.
	 * Keputusan apakah aksi dijalankan atau tidak tergantung dari apakah
	 * pengguna membatalkan aksinya atau tidak. Jika pengguna membatalkan
	 * aksinya, maka aksi dalam penampungan ini dijalankan. Jika pengguna
	 * menyetujui aksinya, maka aksi dalam penampungan diabaikan karena ditimpa
	 * oleh aksi pengguna.
	 */
	private final ArrayList<GeomAction> pendingGeomActions = new ArrayList<>();
	private final ArrayList<TransformAction> pendingTransformActions = new ArrayList<>();

	private CanvasListener listener;
	private CanvasSynchronizer synczer;
	private CanvasModel model;
	private ShapeHandler handler;
	private ControlPoint grabbedCPoint;

	private static int fillColor;
	private static int fillColorOri;
	private static boolean filled;
	private static int strokeColor;
	private static int strokeWidth;
	private static int strokeStyle;
	private static int textSize;
	private static int textFont;
	private static int textColor;
	private static String textContent;
	private static boolean textUnderline;
	private static boolean fontBold;
	private static boolean fontItalic;
	private static int polyCorner;
	private static boolean hide_mode;
	private boolean continueDraw;

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
		editPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		editPaint.setStyle(Style.STROKE);
		editPaint.setColor(Color.BLACK);
		editPaint.setStrokeWidth(1);
		StrokeStyle.applyEffect(StrokeStyle.DOTTED, editPaint);
	}

	// Kotak seleksi
	private static final RectF selectRect = new RectF();
	private static final int SELECT_COLOR = Color.argb(100, 0, 30, 200);
	// Cat kotak seleksi
	private static final Paint selectPaint;
	static {
		selectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selectPaint.setStyle(Style.FILL);
		selectPaint.setColor(SELECT_COLOR);
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

	// ---------------------- proto area ---------------
	private static int PROTO_AREA_WIDTH = 200, PROTO_AREA_TOP_MARGIN = 0;
	private static final int DS_NONE = 0,// tidak ada
			DS_CLICK = 1, // sedang di klik, tapi belum drag
			DS_DRAG = 2,// ada objek yang sedang didrag
			DS_PROTO = 4,// objek yang didrag adalah proto
			DS_PASTE = 8, // objek yang didrag adalah paste
			DS_MASK_PROPAS = DS_PROTO | DS_PASTE,// masking proto atau paste
			DS_ANIM_FINISH = 16,// animasi selesai
			DS_INFLATE_FLAG = 32,// menggembungkan objek
			DS_DEFLATE_FLAG = 64,// objek sedang dikempeskan
			// animasi penggembungan selesai
			DS_INFLATED = DS_INFLATE_FLAG | DS_ANIM_FINISH,
			// batal menggambar objek
			DS_DESTROY_FLAG = 128,
			// sedang menggambar
			DS_DRAW = 256,
			// free objek
			DS_FREE = 512,
			// objek garis
			DS_LINE = 1024,
			// mendrag objek dari mainbar, ukuran masih kecil
			DS_DRAG_PROTO = DS_DRAG | DS_PROTO,
			// mengatur posisi paste objek
			DS_DRAG_PASTE = DS_DRAG | DS_PASTE,
			// animasi menyimpan hasil copy
			DS_PASTE_DEFLATING = DS_DEFLATE_FLAG | DS_PASTE,
			// sedang menggambar free objek
			DS_DRAW_FREE = DS_DRAW | DS_FREE,
			// sedang menggambar garis
			DS_DRAW_LINE = DS_DRAW | DS_LINE;

	/**
	 * Aksi yang dilakukan pada objek di mainbar
	 */
	private int dragStatus, protoY;
	private static final int MIN_DRAG_DISTANCE = 10, PROTO_STROKE_WIDTH = 3;
	private static final float SCALE_THRESHOLD = .0002f;
	// ketebalan stroke yang terlihat objek di mainbar
	private static final int PROTO_TEXT_SIZE = 20;
	/**
	 * ikon delete di main bar
	 */
	private Drawable iconBin;
	private int COLOR_THEME_NORMAL, COLOR_THEME_HIDDEN, COLOR_DESTROY = Color
			.argb(100, 255, 0, 0), COLOR_HIGHLIGHT = Color.argb(100, 0, 0, 0);

	// ---------------------- proto objects -------------
	private ScaledObject captureObject;
	private ScaledObject captureLine;
	private ScaledObject captureFree;
	private ScaledObject captureRect;
	private ScaledObject captureOval;
	private ScaledObject capturePoly;
	private ScaledObject captureText;
	private static ScaledBitmap capturePaste;
	private ScaledBitmap draggedPaste;
	private CanvasObject protoObject;
	private LineObject protoLine;
	private FreeObject protoFree;
	private RectObject protoRect;
	private OvalObject protoOval;
	private PolygonObject protoPoly;
	private TextObject protoText;

	private static final Path pathHighLight = new Path();

	private static ValueAnimator animator = new ValueAnimator();
	private static OvershootInterpolator interOvershoot = new OvershootInterpolator();
	private static LinearInterpolator interLinear = new LinearInterpolator();
	private static final int INFLATE_DURATION = 500, DEFLATE_DURATION = 300;
	private static float pasteOfsX, pasteOfsY, pasteScale;

	public void initProtoArea() {
		PROTO_AREA_WIDTH = (getHeight() - PROTO_AREA_TOP_MARGIN) / 7;

		int icon_left = (int) (0.2 * PROTO_AREA_WIDTH);
		int icon_right = PROTO_AREA_WIDTH - icon_left;
		int icon_width = icon_right - icon_left;
		int center = PROTO_AREA_WIDTH / 2;

		// atur highligh ikon main bar

		pathHighLight.rewind();
		pathHighLight.moveTo(0, 0);
		pathHighLight.lineTo(PROTO_AREA_WIDTH, 0);
		pathHighLight.lineTo(PROTO_AREA_WIDTH, PROTO_AREA_WIDTH);
		pathHighLight.lineTo(0, PROTO_AREA_WIDTH);
		pathHighLight.close();

		// gambar lama
		// RectF fr = new RectF(4, 4, PROTO_AREA_WIDTH - 4, PROTO_AREA_WIDTH -
		// 4);
		// pathHighLight.moveTo(4, 4);
		// pathHighLight.addRoundRect(fr, icon_width / 2, icon_width / 2,
		// Direction.CCW);

		int y = PROTO_AREA_TOP_MARGIN;
		protoLine = new LineObject(icon_left, icon_left, strokeColor,
				PROTO_STROKE_WIDTH, strokeStyle);
		protoLine.penTo(icon_right, icon_right);
		captureLine = new ScaledObject(protoLine);
		captureLine.placeTo(icon_left, y + icon_left, icon_width, icon_width);

		// object free
		y += PROTO_AREA_WIDTH;
		protoFree = new FreeObject(fillColor, strokeColor, strokeWidth,
				strokeStyle);
		protoFree.penDown(icon_left, y + icon_left);
		protoFree.penTo(icon_right, y + center);
		protoFree.penTo(icon_left, y + icon_right);
		protoFree.penTo(icon_right, y + icon_right + icon_right);
		protoFree.penUp();
		captureFree = new ScaledObject(protoFree);
		captureFree.placeTo(icon_left, y + icon_left, icon_width, icon_width);
		// tentukan ukuran stroke agar terlihat jelas
		float w = PROTO_STROKE_WIDTH
				/ (captureFree.getScale() > SCALE_THRESHOLD ? captureFree
						.getScale() : 1);
		protoFree.setStrokeWidth((int) (w));

		// atur objek rect
		y += PROTO_AREA_WIDTH;
		defaultRectSize = centerY;
		protoRect = new RectObject(center, y + center, defaultRectSize,
				fillColor, strokeColor, PROTO_STROKE_WIDTH, strokeStyle);
		captureRect = new ScaledObject(protoRect);
		captureRect.placeTo(icon_left, y + icon_left, icon_width, icon_width);
		// tentukan ukuran stroke agar terlihat jelas
		w = PROTO_STROKE_WIDTH
				/ (captureRect.getScale() > SCALE_THRESHOLD ? captureRect
						.getScale() : 1);
		protoRect.setStrokeWidth((int) (w));

		// atur objek oval
		y += PROTO_AREA_WIDTH;
		defaultOvalRadius = centerY >> 1;
		protoOval = new OvalObject(center, y + center, defaultOvalRadius,
				fillColor, strokeColor, PROTO_STROKE_WIDTH, strokeStyle);
		captureOval = new ScaledObject(protoOval);
		captureOval.placeTo(icon_left, y + icon_left, icon_width, icon_width);
		// tentukan ukuran stroke agar terlihat jelas
		w = PROTO_STROKE_WIDTH
				/ (captureOval.getScale() > SCALE_THRESHOLD ? captureOval
						.getScale() : 1);
		protoOval.setStrokeWidth((int) (w));

		// atur objek poly
		y += PROTO_AREA_WIDTH;
		defaultPolyRadius = defaultOvalRadius;
		protoPoly = new PolygonObject(3, defaultPolyRadius, center, y + center,
				fillColor, strokeColor, PROTO_STROKE_WIDTH, strokeStyle);
		capturePoly = new ScaledObject(protoPoly);
		capturePoly.placeTo(icon_left, y + icon_left, icon_width, icon_width);
		// tentukan ukuran stroke agar terlihat jelas
		w = PROTO_STROKE_WIDTH
				/ (capturePoly.getScale() > SCALE_THRESHOLD ? capturePoly
						.getScale() : 1);
		protoPoly.setStrokeWidth((int) w);

		// atur objek text
		y += PROTO_AREA_WIDTH;
		int fontCode = FontManager.getFontCode(textFont, fontBold, fontItalic,
				textUnderline);
		protoText = new TextObject("Abc", icon_left, y + icon_left,
				strokeColor, fontCode, PROTO_TEXT_SIZE);
		captureText = new ScaledObject(protoText);
		captureText.placeTo(icon_left, y + icon_left, icon_width, icon_width);

		// atur objek paste
		y += PROTO_AREA_WIDTH;
		if (capturePaste == null)
			capturePaste = new ScaledBitmap(icon_left, y + icon_left,
					icon_width, icon_width);
		draggedPaste = new ScaledBitmap(icon_left, y + icon_left, icon_width,
				icon_width);

		// atur animasi mendrag dan membesarkan protoObject
		captureObject = new ScaledObject(protoRect);
		animator.addUpdateListener(animProtoUpdate);
		animator.addListener(animProtoListener);
		animator.setDuration(INFLATE_DURATION);
		animator.setInterpolator(interOvershoot);

		iconBin.setBounds(0, getHeight() - PROTO_AREA_WIDTH, PROTO_AREA_WIDTH,
				getHeight());
	}

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mode = Mode.SELECT;

		// default value
		filled = false;
		fillColorOri = Color.BLACK;
		fillColor = (filled) ? fillColorOri : Color.TRANSPARENT;
		strokeColor = Color.BLACK;
		strokeWidth = 5;
		strokeStyle = StrokeStyle.SOLID;
		textSize = 35;
		textFont = 0;
		textColor = Color.BLACK;
		fontBold = false;
		fontItalic = false;
		textUnderline = false;

		scrollX = 20;
		scrollY = 20;

		synczer = CanvasSynchronizer.getInstance();

		setLayerType(LAYER_TYPE_SOFTWARE, canvasPaint);

		if (isInEditMode()) {
			PROTO_AREA_TOP_MARGIN = 48;
			iconBin = new ShapeDrawable();
			COLOR_THEME_NORMAL = Color.BLUE;
			COLOR_THEME_HIDDEN = Color.RED;
		} else {
			PROTO_AREA_TOP_MARGIN = (int) context.getResources().getDimension(
					R.dimen.w_topbar_height);
			iconBin = context.getResources().getDrawable(
					R.drawable.ic_action_discard_dark);
			COLOR_THEME_NORMAL = context.getResources().getColor(
					R.color.workspace_normal);
			COLOR_THEME_HIDDEN = context.getResources().getColor(
					R.color.workspace_hidden);
		}

		setOnLongClickListener(this);
	}

	/**
	 * Mengatur {@link CanvasListener}
	 * 
	 * @param listener
	 */
	public void setListener(CanvasListener listener) {
		this.listener = listener;
	}

	/**
	 * Memuat data kanvas dari server. Jika proses sudah selesai, maka akan
	 * memanggil {@link CanvasListener#onCanvasModelLoaded(CanvasModel, int)}
	 * 
	 * @param model model kanvas yang akan dibuka, minimal harus memiliki data
	 *            id kanvas.
	 */
	public void open(CanvasModel model) {
		this.model = model;
		selectedObjects.clear();
		userActions.clear();
		redoStack.clear();
		revertList.clear();
		pendingGeomActions.clear();
		pendingTransformActions.clear();
		selectRect.setEmpty();
		editRect.setEmpty();
		// ---- reset ----
		cacheImage = Bitmap.createBitmap(model.getWidth(), model.getHeight(),
				Config.ARGB_8888);
		selectedObjectsCache = Bitmap.createBitmap(model.getWidth(),
				model.getHeight(), Config.ARGB_8888);
		cacheCanvas.setBitmap(cacheImage);

		reloadCache();
		calcScrollBounds(getWidth(), getHeight());
	}

	/**
	 * Mengambil model kanvas yang sedang aktif
	 * 
	 * @return
	 */
	public CanvasModel getModel() {
		return model;
	}

	public void resizeCanvas(int width, int height, int top, int left) {
		ResizeCanvas rc = new ResizeCanvas(model, width, height, top, left,
				true);
		model.setDimension(width, height, top, left);
		cacheImage = Bitmap.createBitmap(model.getWidth(), model.getHeight(),
				Config.ARGB_8888);
		selectedObjectsCache = Bitmap.createBitmap(model.getWidth(),
				model.getHeight(), Config.ARGB_8888);
		cacheCanvas.setBitmap(cacheImage);
		pushToUAStack(rc, true);
		reloadCache();
		postInvalidate();
	}

	/**
	 * Menghitung batas scroll.
	 * @param w lebar layar
	 * @param h tinggi layar
	 */
	void calcScrollBounds(int w, int h) {
		// jika lebar kanvas lebih dari layar
		if (model.getWidth() >= w) {
			minScrollX = w - CANVAS_MARGIN - model.getWidth();
			maxScrollX = CANVAS_MARGIN;

			// jika lebar kanvas kurang dari layar
		} else {
			minScrollX = 0;
			maxScrollX = w - model.getWidth();
		}

		// jika tinggi kanvas lebih dari layar
		if (model.getHeight() >= h) {
			minScrollY = h - CANVAS_MARGIN - model.getHeight();
			maxScrollY = CANVAS_MARGIN;

			// jika tinggi kanvas kurang dari layar
		} else {
			minScrollY = 0;
			maxScrollY = h - model.getHeight();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		centerX = (w >> 1) - scrollX;
		centerY = (h >> 1) - scrollY;
		if (model != null)
			calcScrollBounds(w, h);

		initProtoArea();
	}

	/**
	 * Menggambar objek2 di mainbar
	 * 
	 * @param canvas
	 */
	private void drawProtoArea(Canvas canvas) {
		selectPaint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, PROTO_AREA_WIDTH, getHeight(), selectPaint);
		if ((dragStatus & DS_DRAG) != DS_DRAG) {
			// tampilkan highligh kalau diklik
			if (protoY > PROTO_AREA_TOP_MARGIN || continueDraw) {
				if ((dragStatus & DS_DRAW) == DS_DRAW) {
					selectPaint.setColor(hide_mode ? COLOR_THEME_HIDDEN
							: COLOR_THEME_NORMAL);
					// bawa ke posisi ikon yang sedang menggambar
					canvas.translate(0, protoY);
					canvas.drawPath(pathHighLight, selectPaint);
					canvas.translate(0, -protoY);
				}
				if ((dragStatus & DS_CLICK) == DS_CLICK) {
					selectPaint.setColor(COLOR_HIGHLIGHT);
					canvas.translate(0, protoY);
					canvas.drawPath(pathHighLight, selectPaint);
					canvas.translate(0, -protoY);
				}
			}
			// tampilkan ikon objek
			captureLine.draw(canvas);
			captureFree.draw(canvas);
			captureRect.draw(canvas);
			captureOval.draw(canvas);
			capturePoly.draw(canvas);
			captureText.draw(canvas);
			if (ObjectClipboard.hasObject()
					&& ((dragStatus & DS_PASTE) != DS_PASTE))
				capturePaste.draw(canvas);
		} else {
			if ((dragStatus & DS_DESTROY_FLAG) == DS_DESTROY_FLAG) {
				selectPaint.setColor(COLOR_DESTROY);
				canvas.drawRect(0, 0, PROTO_AREA_WIDTH, getHeight(),
						selectPaint);
			}
			iconBin.draw(canvas);
		}

		// gambar pemisah mainbar dengan kanvas
		selectPaint.setColor((hide_mode) ? COLOR_THEME_HIDDEN
				: COLOR_THEME_NORMAL);
		canvas.drawRect(PROTO_AREA_WIDTH, 0, PROTO_AREA_WIDTH + 3, getHeight(),
				selectPaint);
		selectPaint.setColor(SELECT_COLOR);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// gambar background kanvas
		canvas.drawColor(CANVAS_BACKGROUND_COLOR);

		canvas.translate(scrollX, scrollY);
		if (model != null) {
			// gambar lembaran kanvas
			canvas.drawRect(0, 0, model.getWidth(), model.getHeight(),
					canvasPaint);
			if (cacheImage != null)
				canvas.drawBitmap(cacheImage, 0, 0, cachePaint);
		}
		if ((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION)
			canvas.drawBitmap(selectedObjectsCache, socX, socY, cachePaint);

		if (currentObject != null) {
			currentObject.draw(canvas);
			if (handler != null)
				handler.draw(canvas, editPaint);
		} else if (mode == Mode.SELECT)
			canvas.drawRect(selectRect, selectPaint);

		canvas.translate(-scrollX, -scrollY);
		drawProtoArea(canvas);

		// tampilkan hasil clone objek
		if (((dragStatus & DS_DRAG) == DS_DRAG)
				&& ((dragStatus & DS_PROTO) == DS_PROTO)
				&& ((dragStatus & DS_INFLATED) != DS_INFLATED)) {
			captureObject.draw(canvas);
		} else if ((dragStatus & DS_PASTE) == DS_PASTE)
			draggedPaste.draw(canvas);

		// DEBUG
		// if (model != null)
		// debug(canvas);

	}

	// /** DEBUG
	static Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	static {
		debugPaint.setColor(Color.RED);
		debugPaint.setTextSize(20);
		debugPaint.setTypeface(Typeface.MONOSPACE);
	}

	void debug(Canvas canvas) {
		canvas.translate(-scrollX, -scrollY);
		canvas.drawText("U:" + userActions.size(), getWidth() - 460,
				getHeight(), debugPaint);
		canvas.drawText("R:" + redoStack.size(), getWidth() - 380, getHeight(),
				debugPaint);
		canvas.drawText("C:" + checkpoint, getWidth() - 300, getHeight(),
				debugPaint);

		canvas.drawText("G[" + pendingGeomActions.size() + "]",
				getWidth() - 180, getHeight(), debugPaint);
		canvas.drawText("T[" + pendingTransformActions.size() + "]",
				getWidth() - 120, getHeight(), debugPaint);
		if (currentObject != null) {
			canvas.drawText(String.format(
					"(%d, %d)->(%d, %4.0f, %4.0f, %3.1f)",
					currentObject.privateID, currentObject.getGlobalID(),
					currentObject.geomParamLength(), currentObject.offsetX(),
					currentObject.offsetY(), currentObject.rotation()),
					getWidth() - 460, getHeight() - 50, debugPaint);
		}

		if (handler != null)
			canvas.drawText("[hnd]", getWidth() - 370, getHeight() - 25,
					debugPaint);
		if (grabbedCPoint != null)
			canvas.drawText("[gcb]", getWidth() - 310, getHeight() - 25,
					debugPaint);
		if (protaStyle != null)
			canvas.drawText("[sty]", getWidth() - 250, getHeight() - 25,
					debugPaint);
		if (protaReshape != null)
			canvas.drawText("[shp]", getWidth() - 190, getHeight() - 25,
					debugPaint);
		if (protaMove != null)
			canvas.drawText("[mov]", getWidth() - 130, getHeight() - 25,
					debugPaint);
		if (currentObject != null)
			canvas.drawText("[cob]", getWidth() - 70, getHeight() - 25,
					debugPaint);
		String res = "|";
		if ((mode & Mode.SELECT) == Mode.SELECT)
			res += "SE|";
		if ((mode & Mode.DRAW) == Mode.DRAW)
			res += "DW|";
		if ((mode & Mode.EDIT) == Mode.EDIT)
			res += "ED|";
		if ((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION)
			res += "SM|";
		if ((mode & Mode.HAND) == Mode.HAND)
			res += "HD|";
		if ((mode & Mode.MOVING) == Mode.MOVING)
			res += "MV|";
		canvas.drawText(res, getWidth() - 460, getHeight() - 25, debugPaint);
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

	/**
	 * Menggambar ulang daftar objek ke {@link #cacheImage}. Jika sedang
	 * menyeleksi objek, maka objek yang tidak diseleksi akan digambar di
	 * {@link #cacheImage} yang disamarkan dengan warna
	 * {@link #SELECTION_COVER_COLOR}, kemudian menggambar objek-objek yang
	 * sedang diseleksi akan digambar di {@link #selectedObjectsCache} dengan
	 * garis putus-putus seleksi di sekitar objek.
	 */
	private void reloadCache() {
		int size = model.objects.size();
		cacheCanvas.drawColor(Color.WHITE,
				android.graphics.PorterDuff.Mode.CLEAR);
		if ((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION) {
			for (int i = 0; i < size; i++) {
				CanvasObject obj = model.objects.get(i);
				if (!obj.isSelected())
					obj.draw(cacheCanvas);
			}
			if ((mode & Mode.EDIT) != Mode.EDIT)
				cacheCanvas.drawColor(SELECTION_COVER_COLOR);
			cacheCanvas.setBitmap(selectedObjectsCache);
			cacheCanvas.drawColor(Color.TRANSPARENT,
					android.graphics.PorterDuff.Mode.CLEAR);
			if ((mode & Mode.EDIT) != Mode.EDIT) {
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
				if (size > 1)
					cacheCanvas.drawRect(editRect, editPaint);
				if ((mode & Mode.MOVING) == Mode.MOVING) {
					cacheCanvas.drawLine(editRect.left, editRect.top,
							editRect.right, editRect.bottom, editPaint);
					cacheCanvas.drawLine(editRect.left, editRect.bottom,
							editRect.right, editRect.top, editPaint);
				}
			}
			cacheCanvas.setBitmap(cacheImage);
		} else {
			for (int i = 0; i < size; i++)
				model.objects.get(i).draw(cacheCanvas);
		}
	}

	AnimatorUpdateListener animProtoUpdate = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			Float progress = (Float) animation.getAnimatedValue();
			if (dragStatus == DS_PASTE_DEFLATING) {
				// animasi hasil copy
				float ofx = capturePaste.offsetX() + progress * pasteOfsX;
				float ofy = capturePaste.offsetY() + progress * pasteOfsY;
				draggedPaste.offsetTo(ofx, ofy);
				float scale = pasteScale + (1 - pasteScale) * progress;
				draggedPaste.scaleTo(scale);
			} else if ((dragStatus & DS_MASK_PROPAS) == DS_PROTO) {
				// inflate proto objek
				captureObject.setScale(progress);
			} else if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE) {
				// inflate paste
				draggedPaste.scaleTo(progress);
			}
			CanvasView.this.postInvalidate();
		}
	};
	Animator.AnimatorListener animProtoListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {}

		@Override
		public void onAnimationRepeat(Animator animation) {}

		@Override
		public void onAnimationEnd(Animator animation) {
			// jika user masih melakukan drag
			if ((dragStatus & DS_DRAG) == DS_DRAG) {
				// jika yang dianimasi objek proto
				currentObject = protoObject;
				currentObject.offset(-scrollX, -scrollY);
				dragStatus |= DS_ANIM_FINISH;
			} else {
				if ((dragStatus & DS_DESTROY_FLAG) == DS_DESTROY_FLAG) {
					// jika objek ditandai untuk dihapus
					currentObject = null;

				} else if ((dragStatus & DS_MASK_PROPAS) == DS_PROTO) {
					// jika yang dianimasi objek proto
					currentObject = protoObject;
					currentObject.offset(-scrollX, -scrollY);
					// jika objek sudah dilepas oleh user ->langsung edit
					mode = Mode.DRAW;
					editObject(currentObject, ShapeHandler.ALL);
				} else if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE) {
					if ((dragStatus & DS_INFLATE_FLAG) == DS_INFLATE_FLAG) {
						// jika animasi mempaste
						placePaste();
					} else {
						// jika animasi mengkopi
						dragStatus = DS_NONE;
						capturePaste.copy(draggedPaste);
					}
				}
				dragStatus = DS_NONE;
			}
			CanvasView.this.postInvalidate();
		}

		@Override
		public void onAnimationCancel(Animator animation) {}
	};

	private void placePaste() {
		pasteFromClipboard();// paste objek
		// hitung pergeseran dari saat dicopy
		float ofx = draggedPaste.offsetX() - capturePaste.offsetX() - pasteOfsX;
		float ofy = draggedPaste.offsetY() - capturePaste.offsetY() - pasteOfsY;
		// atur penempatan objek2 yang sudah di paste
		for (int i = 0; i < selectedObjects.size(); i++)
			selectedObjects.get(i).offset(ofx, ofy);
	}

	/**
	 * Mengecek aksi user terhadap objek di mainbar
	 * 
	 * @param event
	 * @param act
	 * @return
	 */
	private boolean onTouchProtoArea(MotionEvent event, int act) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (act == MotionEvent.ACTION_DOWN) {
			// saat layar mulai dipencet -> abaikan jika sedang ada aksi lain
			if (dragStatus != DS_NONE)
				return false;
			// jika user memasuki area mainbar -> kemungkinan akan menggambar
			if (x < PROTO_AREA_WIDTH && y > PROTO_AREA_TOP_MARGIN) {
				approveAction();// setujui semua aksinya dulu

				continueDraw = false;
				mode = Mode.SELECT;
				anchorX = x;
				anchorY = y;
				// tentukan objek yang diklik oleh user
				int id = (y - PROTO_AREA_TOP_MARGIN) / PROTO_AREA_WIDTH;
				if (id >= 6) {
					// tandai bahwa yang diklik adalah paste
					if (ObjectClipboard.hasObject())
						dragStatus = DS_CLICK | DS_PASTE;// klik paste
				} else {
					dragStatus = DS_CLICK | DS_PROTO;// sedang mengklik proto
					if (id == 0) {
						protoObject = protoLine;
						captureObject.lookTo(captureLine);
					} else if (id == 1) {
						protoObject = protoFree;
						captureObject.lookTo(captureFree);
					} else if (id == 2) {
						protoObject = protoRect;
						captureObject.lookTo(captureRect);
					} else if (id == 3) {
						protoObject = protoOval;
						captureObject.lookTo(captureOval);
					} else if (id == 4) {
						protoObject = protoPoly;
						captureObject.lookTo(capturePoly);
					} else if (id == 5) {
						protoObject = protoText;
						captureObject.lookTo(captureText);
					}
				}
				// tandai posisi ikon
				protoY = PROTO_AREA_TOP_MARGIN + id * PROTO_AREA_WIDTH;
				postInvalidate();
				return true;
			}
			return false;
		} else {
			if (dragStatus == DS_NONE) // abaikan jika tak ada aksi
				return false;
			if (act == MotionEvent.ACTION_MOVE) {
				float dx = x - anchorX;
				float dy = y - anchorY;
				if ((dragStatus & DS_CLICK) == DS_CLICK) {
					// jika sudah melewati batas drag -> tandai drag
					if (Math.abs(dx) > MIN_DRAG_DISTANCE
							|| Math.abs(dy) > MIN_DRAG_DISTANCE) {
						dragStatus &= ~DS_CLICK;// matikan klik

						// hilangkan fitur drag saat paste
						if ((dragStatus & DS_PASTE) != DS_PASTE)
							dragStatus |= DS_DRAG;// tandai drag

						if (dragStatus == DS_DRAG_PROTO) {
							// sedang mendrag proto -> clone objek
							protoObject = protoObject.cloneObject();
							captureObject.setObject(protoObject);
						} else if (dragStatus == DS_DRAG_PASTE) {
							// sedang mendrag paste -> clone capture paste
							draggedPaste.copy(capturePaste);
							draggedPaste.matchSize(capturePaste);
						}
					}
				} else {
					// drag objek berdasarkan jenisnya (proto atau paste) dan
					// sudah digembungkan atau belum
					if ((dragStatus & DS_MASK_PROPAS) == DS_PROTO) {
						// typenya proto
						if ((dragStatus & DS_ANIM_FINISH) == DS_ANIM_FINISH) {
							// sedang mendrag objek yang sudah digembungkan
							currentObject.offset(dx, dy);
						} else
							// sedang mendrag proto
							captureObject.offset(dx, dy);
					} else if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE) {
						draggedPaste.offset(dx, dy);
					}
					anchorX = x;
					anchorY = y;

					// yg belum digembungkan dan keluar mainbar -> gembungkan
					if ((dragStatus & DS_INFLATE_FLAG) != DS_INFLATE_FLAG
							&& x > PROTO_AREA_WIDTH) {
						dragStatus |= DS_INFLATE_FLAG;
						if ((dragStatus & DS_MASK_PROPAS) == DS_PROTO) {
							// animasikan penggembungan
							animator.setFloatValues(captureObject.getScale(), 1);
							// sesuaikan ketebalan garis sebenarnya, ambil
							// gambarnya, kembalikan seperti semula
							if (protoObject instanceof BasicObject) {
								BasicObject bo = (BasicObject) protoObject;
								bo.setStrokeWidth(strokeWidth);
							} else if (protoObject instanceof LineObject) {
								((LineObject) protoObject)
										.setWidth(strokeWidth);
							} else if (protoObject instanceof TextObject) {
								((TextObject) protoObject).setSize(textSize);
							}
						} else if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE) {
							float sc = capturePaste.scale();
							animator.setFloatValues(sc, 1);
							draggedPaste.scaleTo(sc);
						}
						animator.setInterpolator(interOvershoot);
						animator.setDuration(INFLATE_DURATION);
						animator.start();
					}

					// jika masuk lagi ke mainbar, tandai objek akan dihapus
					if (x < PROTO_AREA_WIDTH)
						dragStatus |= DS_DESTROY_FLAG;
					else
						dragStatus &= ~DS_DESTROY_FLAG;
				}
			} else if (act == MotionEvent.ACTION_UP) {
				// ------ user hanya mengklik tombol ------
				if ((dragStatus & DS_CLICK) == DS_CLICK) {
					if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE) {
						// diklik paste -> paste objek dari kanvas
						pasteFromClipboard();
						dragStatus = DS_NONE;
					} else {
						if (protoObject instanceof FreeObject) {
							// jika objek free, ubah ke mode gambar biasa
							insertPrimitive(ObjectType.FREE);
							Toast.makeText(getContext(), "Drag to make path",
									Toast.LENGTH_SHORT).show();
							dragStatus = DS_DRAW_FREE;
							continueDraw = true;
						} else if (protoObject instanceof LineObject) {
							insertPrimitive(ObjectType.LINE);
							Toast.makeText(getContext(), "Drag to make line",
									Toast.LENGTH_SHORT).show();
							dragStatus = DS_DRAW_LINE;
						}
					}
					// --- user sudah mendrag ---
				} else if ((dragStatus & DS_DESTROY_FLAG) == DS_DESTROY_FLAG) {
					// jika objek ditandai untuk dihapus
					currentObject = null;
					dragStatus = DS_NONE;

				} else if ((dragStatus & DS_ANIM_FINISH) == DS_ANIM_FINISH) {
					// jika proses animasi sudah selesai
					if ((dragStatus & DS_MASK_PROPAS) == DS_PROTO) {
						// objek sudah diklon
						mode = Mode.DRAW;
						editObject(currentObject, ShapeHandler.ALL);
					} else if ((dragStatus & DS_MASK_PROPAS) == DS_PASTE)
						placePaste();
					dragStatus = DS_NONE;
				} else {
					// animasi belum selesai
					// tandai bahwa objek sudah dilepas oleh user
					dragStatus &= ~DS_DRAG;
				}
			}
			postInvalidate();
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getActionMasked();
		if (onTouchProtoArea(event, act))
			return true;
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
						currentLine = new LineObject(anchorX, anchorY,
								strokeColor, strokeWidth, strokeStyle);
						currentObject = currentLine;
					} else if (objectType == ObjectType.FREE) {
						currentFree = new FreeObject(fillColor, strokeColor,
								strokeWidth, strokeStyle).penDown(anchorX,
								anchorY);
						currentBasic = currentFree;
						currentObject = currentBasic;
					}
				} else if ((mode & Mode.EDIT) == Mode.EDIT) {
					if (handler != null) {
						grabbedCPoint = handler.grab(anchorX, anchorY);
						if (grabbedCPoint != null) {
							if ((mode & Mode.DRAW) != Mode.DRAW
									&& protaReshape == null)
								protaReshape = new ReshapeAction(currentObject,
										true);
						} else
							approveAction();
					}
				}
			}
		} else if (act == MotionEvent.ACTION_MOVE) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			// geser kanvas
			if ((mode & Mode.HAND) == Mode.HAND) {
				int nsx = x + anchorX;
				if ((nsx > scrollX && nsx < maxScrollX)
						|| (nsx <= scrollX && nsx > minScrollX))
					scrollX = nsx;

				int nsy = y + anchorY;
				if ((nsy > scrollY && nsy < maxScrollY)
						|| (nsy <= scrollY && nsy > minScrollY))
					scrollY = nsy;
			} else {
				x -= scrollX;
				y -= scrollY;
				if ((mode & Mode.MOVING) == Mode.MOVING) {
					socX = x - anchorX;
					socY = y - anchorY;
					protaMove.moveTo(x, y);
				} else {
					if (x < 0 && x > model.getWidth() && y < 0
							&& y > model.getHeight())
						return true;
					if (mode == Mode.SELECT) {
						selectRect.left = Math.min(anchorX, x);
						selectRect.top = Math.min(anchorY, y);
						selectRect.right = Math.max(anchorX, x);
						selectRect.bottom = Math.max(anchorY, y);
					} else if (mode == Mode.DRAW) {
						if (objectType == ObjectType.FREE) {
							currentFree.penTo(x, y);
							// listener.onBeginDraw();
						} else if (objectType == ObjectType.LINE) {
							currentLine.penTo(x, y);
							// listener.onBeginDraw();
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
						// menyeleksi satu objek
						selectedObjects.add(currentObject);
						editObject(currentObject, ShapeHandler.SHAPE);
					}
				} else {
					selected = selectArea(selectRect);
					if (selected) {
						// menyeleksi satu objek
						if (selectedObjects.size() == 1)
							editObject(selectedObjects.get(0),
									ShapeHandler.SHAPE);
						else {
							// menyeleksi banyak objek
							listener.onSelectionEvent(
									CanvasListener.EDIT_MULTIPLE,
									selectedObjects.size());
							currentType = ObjectType.MULTIPLE;
						}
					}
				}
				if (selected) {
					mode |= Mode.HAS_SELECTION;
					reloadCache();
				}
				selectRect.setEmpty();
				if (!selected)
					listener.onSelectionEvent(CanvasListener.ENTER_MODE,
							selectedObjects.size());
			} else if (mode == Mode.DRAW) {
				// saat pena diangkat
				if (objectType == ObjectType.FREE
						|| objectType == ObjectType.LINE) {
					if (objectType == ObjectType.FREE) {
						currentFree.penUp();
						protoFree = currentFree.cloneObject();
						captureFree.setObject(protoFree);

						// hitung ukuran stroke agar kelihatan jelas di main bar
						float w = captureFree.getScale();
						if (w > SCALE_THRESHOLD)
							w = PROTO_STROKE_WIDTH / w;
						else
							w = PROTO_STROKE_WIDTH;
						protoFree.setStrokeWidth((int) w);
					}
					redoStack.clear();
					listener.onURStatusChange(true, false);
					if (!continueDraw)
						editObject(currentObject, ShapeHandler.ALL);
					else {
						model.objects.add(currentObject);
						reloadCache();
					}
				}
			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				if (grabbedCPoint != null) {
					currentObject.getWorldBounds(limiter);
					if ((limiter.right < OBJECT_LIMIT)
							|| (limiter.left > model.getWidth() - OBJECT_LIMIT)
							|| (limiter.bottom < OBJECT_LIMIT)
							|| (limiter.top > model.getHeight() - OBJECT_LIMIT))
						// kalau keluar dari kanvas, kembalikan ke tempat semula
						handler.dragPoint(grabbedCPoint, anchorX, anchorY);
					else if (protaReshape != null) {
						// kalau tidak keluar dari kanvas, baru disimpan
						// perubahannya
						redoStack.clear();
						pushToUAStack(protaReshape.capture(), false);
						listener.onWaitForApproval();
					}
					handler.releasePoint(grabbedCPoint);
					grabbedCPoint = null;
				}
			} else if ((mode & Mode.MOVING) == Mode.MOVING) {
				socX = 0;
				socY = 0;
				redoStack.clear();
				MoveStepper ms = protaMove.anchorUp();
				if (ms != null) {
					ms.execute();
					pushToUAStack(ms, false);
					reloadCache();
				}
			}
		}
		invalidate();
		return true;
	}

	/**
	 * Mengubah mode kanvas. Mode {@link Mode#HAND} bisa dibuat toogle. Jika
	 * mode yang dipilih = {@link Mode#SELECT} dan ada objek yan gsedang
	 * diseleksi, maka akan menghilangkan seleksi.
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		if (mode == Mode.HAND)
			this.mode ^= Mode.HAND;
		else {
			if (mode == Mode.SELECT
					&& (this.mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION)
				cancelSelect();
			if ((this.mode & Mode.EDIT) == Mode.EDIT)
				cancelAction();
			this.mode = mode;
		}
	}

	/**
	 * Apakah sedang di mode seleksi atau tidak. Belum tentu ada objek yang
	 * diseleksi, namun bisa saja masih encoba untuk menyeleksi .objek
	 * 
	 * @return
	 */
	public boolean isInSelectionMode() {
		return (mode & Mode.SELECT) == Mode.SELECT;
	}

	public boolean isInDrawingMode() {
		return (mode & Mode.DRAW) == Mode.DRAW;
	}

	public boolean isInHandMode() {
		return (mode & Mode.HAND) == Mode.HAND;
	}

	public int currentObject() {
		return objectType;
	}

	public enum Param {
		fillColor, strokeColor, strokeWidth, strokeStyle, textSize, textFont, textColor, textContent, textUnderline, fontBold, fontItalic, polygonCorner, fillable, filled
	}

	/**
	 * Mengambil variabel state kanvas saat ini.
	 * 
	 * @param param parameter yang akan diambil.
	 * @return
	 */
	public Object getState(Param param) {
		switch (param) {
		case fillColor:
			return fillColorOri;
		case strokeColor:
			return strokeColor;
		case strokeWidth:
			return strokeWidth;
		case strokeStyle:
			return strokeStyle;
		case textSize:
			return textSize;
		case textFont:
			return textFont;
		case textColor:
			return textColor;
		case textContent:
			return textContent;
		case textUnderline:
			return textUnderline;
		case fontBold:
			return fontBold;
		case fontItalic:
			return fontItalic;
		case polygonCorner:
			return polyCorner;
		case fillable:
			return (currentObject == currentBasic)
					&& (currentBasic != currentFree || currentFree.fillable());
		case filled:
			return filled;
		default:
			return null;
		}
	}

	/**
	 * Mengambil parameter objek yang sedang terseleksi.
	 * 
	 * @param param
	 * @return null jika tidak ada objek yang diseleksi atau objek yang
	 *         diseleksi tidak memiliki parameter tersebut
	 */
	public Object getObjectParam(Param param) {
		switch (param) {
		case fillColor:
			if (currentObject == currentBasic)
				return currentBasic.getFillColor();
			break;
		case strokeColor:
			if (currentObject == currentBasic)
				return currentBasic.getStrokeColor();
			if (currentObject == currentLine)
				return currentLine.getColor();
			break;
		case strokeWidth:
			if (currentObject == currentBasic)
				return currentBasic.getStrokeWidth();
			if (currentObject == currentLine)
				return currentLine.getWidth();
			break;
		case strokeStyle:
			if (currentObject == currentBasic)
				return currentBasic.getStrokeStyle();
			if (currentObject == currentLine)
				return currentLine.getStrokeStyle();
			break;
		case textSize:
			if (currentObject == currentText)
				return currentText.getFontSize();
			break;
		case textFont:
			if (currentObject == currentText)
				return FontManager.fontId(currentText.getFontCode());
			break;
		case textColor:
			if (currentObject == currentText)
				return currentText.getTextColor();
			break;
		case textContent:
			if (currentObject == currentText)
				return currentText.getText();
			break;
		case textUnderline:
			if (currentObject == currentText)
				return FontManager.isUnderline(currentText.getFontCode());
			break;
		case fontBold:
			if (currentObject == currentText)
				return FontManager.isBold(currentText.getFontCode());
			break;
		case fontItalic:
			if (currentObject == currentText)
				return FontManager.isItalic(currentText.getFontCode());
			break;
		case polygonCorner:
			if (currentObject == currentPoly)
				return currentPoly.corner();
			break;
		case filled:
			if (currentObject == currentBasic)
				return currentBasic.getFillColor() != Color.TRANSPARENT;
			break;
		case fillable:
			return (currentObject == currentBasic)
					&& (currentBasic != currentFree || currentFree.fillable());
		}
		return null;
	}

	/**
	 * Mengubah state canvas ke parameter objek yang sedang aktif.
	 */
	public void captureObjectState() {
		if (currentObject == currentBasic) {
			fillColor = currentBasic.getFillColor();
			filled = fillColor == Color.TRANSPARENT;
			fillColorOri = fillColor + 0xff000000;
			strokeColor = currentBasic.getStrokeColor();
			strokeStyle = currentBasic.getStrokeStyle();
			strokeWidth = currentBasic.getStrokeWidth();
		} else if (currentObject == currentText) {
			textColor = currentText.getTextColor();
			textSize = currentText.getFontSize();
			fontBold = FontManager.isBold(currentText.getFontCode());
			fontItalic = FontManager.isItalic(currentText.getFontCode());
			textUnderline = FontManager.isUnderline(currentText.getFontCode());
			textFont = FontManager.fontId(currentText.getFontCode());
		} else if (currentObject == currentLine) {
			strokeColor = currentLine.getColor();
			strokeWidth = currentLine.getWidth();
			strokeStyle = currentLine.getStrokeStyle();
		}
	}

	/**
	 * Mengubah isi objek teks yang sedang aktif
	 * 
	 * @param text
	 * @return
	 */
	public boolean setTextObjContent(String text) {
		protoText.setText((text.length() > 3) ? text.substring(0, 4) : text);
		textContent = text;
		if (currentObject != null && currentObject == currentText) {
			currentText.setText(text);
			postInvalidate();
			return true;
		}
		postInvalidate();
		return false;
	}

	/**
	 * Mengubah jumlah sudut poligon yang sedat diedit
	 * 
	 * @param corner
	 * @param save
	 */
	public void setPolygonCorner(int corner, boolean save) {
		CanvasView.polyCorner = corner;
		protoPoly.changeShape(corner, defaultPolyRadius);
		if (currentObject != null && currentObject == currentPoly) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaReshape == null)
				protaReshape = new ReshapeAction(currentObject, true);
			currentPoly.changeShape(corner, defaultPolyRadius);
			if (save) {
				if (handler != null && grabbedCPoint != null)
					handler.releasePoint(grabbedCPoint);
				handler = currentPoly.getHandler(ShapeHandler.ALL);
				handler.init();
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaReshape.capture(), false);
			} else
				handler = null;
		}
		postInvalidate();
	}

	/**
	 * Mengetahui apakah ada objek yang sedang deseleksi atau tidak.
	 * 
	 * @return
	 */
	public boolean hasSelectedObject() {
		return (mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION;
	}

	/**
	 * Mengetahui apakah ada perubahan yang belum disimpan dari terakhir
	 * menyeleksi objek atau tidak.
	 * 
	 * @return
	 */
	public boolean hasUnsavedChanges() {
		return protaReshape != null || protaStyle != null || protaMove != null;
	}

	/**
	 * Mengedit satu buah objek.
	 * 
	 * @param co
	 * @param filter filter Handler
	 */
	private void editObject(CanvasObject co, int filter) {
		// Cast berdasarkan tipe objek untuk memudahkan operasi
		currentObject = co;
		if (co instanceof BasicObject) {
			currentBasic = (BasicObject) co;
			if (currentBasic instanceof RectObject) {
				currentRect = (RectObject) currentBasic;
				currentType = ObjectType.RECT;
			} else if (currentBasic instanceof OvalObject) {
				currentOval = (OvalObject) currentBasic;
				currentType = ObjectType.OVAL;
			} else if (currentBasic instanceof PolygonObject) {
				currentPoly = (PolygonObject) currentBasic;
				currentType = ObjectType.POLYGON;
			} else if (currentBasic instanceof FreeObject) {
				currentFree = (FreeObject) currentBasic;
				currentType = ObjectType.FREE;
			}
			fillColor = currentBasic.getFillColor();
			fillColorOri = fillColor + 0xff000000;
			strokeColor = currentBasic.getStrokeColor();
		} else if (co instanceof LineObject) {
			currentLine = (LineObject) co;
			currentType = ObjectType.LINE;
			strokeColor = currentLine.getColor();
		} else if (co instanceof TextObject) {
			currentText = (TextObject) co;
			currentType = ObjectType.TEXT;
			textColor = currentText.getTextColor();
		}
		selectedObjects.clear();
		selectedObjects.add(co);
		// checkpoint untuk perubahan minor, akan hilang saat approveAction atau
		// cancelAction
		checkpoint = userActions.size();
		handler = co.getHandler(ShapeHandler.ALL);
		handler.init();
		listener.onWaitForApproval();
		mode |= Mode.EDIT;
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		listener.onSelectionEvent(CanvasListener.EDIT_OBJECT, currentType);
	}

	/**
	 * Menyetujui aksi edit yang dilakukan pengguna.
	 */
	public void approveAction() {
		// hilangkan perubahan termporal
		while (userActions.size() > checkpoint)
			userActions.pop();
		if ((mode & Mode.DRAW) == Mode.DRAW) {
			if (currentObject != null) {
				model.objects.add(currentObject);
				UserAction action = new DrawAction(currentObject);
				pushToUAStack(action, !hide_mode);
				reloadCache();
			}
		} else {
			// aksi move user meng-overwrite aksi transformasi user lain
			if ((mode & Mode.MOVING) == Mode.MOVING) {
				mode &= Mode.MOVING;
				if (protaMove != null) {
					protaMove.apply();
					pushToUAStack(protaMove, !hide_mode);
					pendingTransformActions.clear();
				}
			} else if ((mode & Mode.EDIT) == Mode.EDIT) {
				mode &= ~Mode.EDIT;
				// aksi reshape user meng-overwrite aksi transformasi dan geom
				// user lain
				if (protaReshape != null) {
					protaReshape.apply();
					pushToUAStack(protaReshape, !hide_mode);
					pendingTransformActions.clear();
					pendingGeomActions.clear();
				}
				// aksi style user meng-overwrite aksi style user lain
				if (protaStyle != null) {
					protaStyle.applyStyle();
					pushToUAStack(protaStyle, !hide_mode);
				}
			}
			cancelSelect();
		}
		checkpoint = userActions.size();
		currentObject = null;
		handler = null;
		protaMove = null;
		protaStyle = null;
		protaReshape = null;
		mode = Mode.SELECT;
		postInvalidate();
		redoStack.clear();
	}

	/**
	 * Membatalkan aksi pengguna.<br/>
	 * Jika sedang mengedit satu objek, maka perubahan yang dilakukan akan
	 * dibatalkan dan seleksi pada objek dihilangkan.<br/>
	 * Jika sedang memindah banyak objek, maka aksi dibatalkan. Namun
	 * objek-objek tersebut masih diseleksi.
	 */
	public void cancelAction() {
		handler = null;
		currentObject = null;
		// hilangkan perubahan temporal dari stack
		while (userActions.size() > checkpoint)
			userActions.pop();

		// kembalikan keadaan objek seperti sebelumnya
		if ((mode & Mode.EDIT) == Mode.EDIT) {
			mode &= ~Mode.EDIT;
			if (protaStyle != null)
				execute(protaStyle.getInverse(), true);
			if (protaReshape != null)
				execute(protaReshape.getInverse(), true);
			if ((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION)
				cancelSelect();
		} else if ((mode & Mode.MOVING) == Mode.MOVING) {
			mode &= ~Mode.MOVING;
			if (protaMove != null)
				execute(protaMove.getInverse(), true);
		} else
			return;
		protaMove = null;
		protaStyle = null;
		protaReshape = null;

		// jalankan aksi dari user lain yang sempat tertunda
		for (int i = 0; i < pendingGeomActions.size(); i++)
			execute(pendingGeomActions.get(i), true);
		for (int i = 0; i < pendingTransformActions.size(); i++)
			execute(pendingTransformActions.get(i), true);
		pendingGeomActions.clear();
		pendingTransformActions.clear();
		redoStack.clear();
		listener.onURStatusChange(!userActions.isEmpty(), false);
		reloadCache();
		postInvalidate();
	}

	/**
	 * Mengganti warna pinggiran objek yang sedang aktif atau yang akan
	 * digambar. Berlaku untuk {@link LineObject} dan {@link BasicObject}.
	 * 
	 * @param color . Lihat {@link Color}.
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah tampilan objek.
	 */
	public void setStrokeColor(int color, boolean save) {
		strokeColor = color;
		// ubah warna objek di mainbar
		protoLine.setColor(color);
		protoFree.setStrokeColor(color);
		protoRect.setStrokeColor(color);
		protoOval.setStrokeColor(color);
		protoPoly.setStrokeColor(color);

		if (currentObject != null) {
			// ubah objek yang sedang diedit
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				currentBasic.setStrokeColor(color);
			else if (currentObject instanceof LineObject)
				currentLine.setColor(color);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
		}
		postInvalidate();
	}

	/**
	 * Mengganti tebal garis yang sedang aktif atau akan digambar. Berlaku untuk
	 * {@link LineObject} dan {@link BasicObject}
	 * 
	 * @param width
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah tampilan objek.
	 */
	public void setStrokeWidth(int width, boolean save) {
		strokeWidth = width;
		if (currentObject != null) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				currentBasic.setStrokeWidth(width);
			else if (currentObject instanceof LineObject)
				currentLine.setWidth(width);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
			postInvalidate();
		}
	}

	/**
	 * Mengganti jenis dekorasi pinggiran objek yang sedang aktif atau akan
	 * digambar. Berlaku untuk {@link LineObject} dan {@link BasicObject}.
	 * 
	 * @param style jenis dekorasi. Lihat {@link StrokeStyle}.
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah tampilan objek.
	 */
	public void setStrokeStyle(int style, boolean save) {
		strokeStyle = style;
		// ubah objek di main bar
		protoLine.setStrokeStyle(style);
		protoFree.setStrokeStyle(style);
		protoRect.setStrokeStyle(style);
		protoOval.setStrokeStyle(style);
		protoPoly.setStrokeStyle(style);

		if (currentObject != null) {
			// ubah objek yang sedang diedit
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			if (currentObject instanceof BasicObject)
				currentBasic.setStrokeStyle(strokeStyle);
			else if (currentObject instanceof LineObject)
				currentLine.setStrokeStyle(style);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
		}
		postInvalidate();
	}

	/**
	 * Memasukkan objek teks ke dalam kanvas.
	 * 
	 * @param text teks
	 */
	public void insertText(String text) {
		if (text == null || text.isEmpty())
			return;
		objectType = ObjectType.TEXT;
		int x = Math.min(model.getWidth(), getWidth()) / 2 - scrollX;
		int y = Math.min(model.getHeight(), getHeight()) / 2 - scrollY;
		currentText = new TextObject(text, x, y, strokeColor, textFont,
				textSize);
		setMode(Mode.DRAW);
		editObject(currentText, ShapeHandler.ALL);
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		invalidate();
	}

	/**
	 * Mengatur warna teks.
	 * 
	 * @param color warna, lihat {@link Color}
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah tampilan objek.
	 */
	public void setTextColor(int color, boolean save) {
		textColor = color;
		protoText.setColor(color);
		if (currentObject != null && currentObject instanceof TextObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			currentText.setColor(color);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
		}
		postInvalidate();
	}

	/**
	 * Mengubah ukuran huruf.
	 * 
	 * @param size ukuran
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah ukuran teks.
	 */
	public void setFontSize(int size, boolean save) {
		textSize = size;
		if (currentObject != null && currentObject == currentText) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			currentText.setSize(size);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
			postInvalidate();
		}
	}

	/**
	 * Mengubah dekorasi teks.
	 * 
	 * @param font indeks huruf di {@link FontManager}
	 * @param bold teks tebal atau tidak
	 * @param italic teks miring atau tidak
	 * @param save jika true, aksi langsung disimpan di stack, jika false, aksi
	 *            hanya merubah tampilan objek.
	 */
	public void setFontStyle(int font, boolean bold, boolean italic,
			boolean underline, boolean save) {
		textFont = font;
		fontBold = bold;
		fontItalic = italic;
		textUnderline = underline;
		int fontCode = FontManager.getFontCode(font, bold, italic, underline);
		protoText.setFontCode(fontCode);
		if (currentObject != null && currentObject instanceof TextObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			currentText.setFontCode(fontCode);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
			postInvalidate();
		}
	}

	/**
	 * Mengubah jenis huruf
	 * 
	 * @param font
	 * @param save
	 */
	public void setFont(int font, boolean save) {
		setFontStyle(font, fontBold, fontItalic, textUnderline, save);
	}

	/**
	 * Mengubah ketebalan teks.
	 * 
	 * @param bold tebal atau tidak
	 * @param save
	 */
	public void setFontBold(boolean bold, boolean save) {
		setFontStyle(textFont, bold, fontItalic, textUnderline, save);
	}

	/**
	 * Mengubah kemiringan teks
	 * 
	 * @param italic teks miring atau tidak
	 * @param save
	 */
	public void setFontItalic(boolean italic, boolean save) {
		setFontStyle(textFont, fontBold, italic, textUnderline, save);
	}

	/**
	 * memberi garis bawah pada teks
	 * 
	 * @param underline
	 * @param save
	 */
	public void setTextUnderline(boolean underline, boolean save) {
		setFontStyle(textFont, fontBold, fontItalic, underline, save);
	}

	/**
	 * Memasukkan objek primitive ke kanvas. Objek yang sedang digambar dan
	 * belum disimpan akan tertimpa.
	 * 
	 * @param type {@link ObjectType#RECT}, {@link ObjectType#OVAL},
	 *            {@link ObjectType#LINE} atau {@link ObjectType#FREE}
	 */
	public void insertPrimitive(int type) {
		objectType = type;
		setMode(Mode.DRAW);
		if (objectType == ObjectType.RECT) {
			currentRect = new RectObject(centerX, centerY, defaultRectSize,
					fillColor, strokeColor, strokeWidth, strokeStyle);
			currentBasic = currentRect;
			currentObject = currentBasic;
			editObject(currentObject, ShapeHandler.ALL);
		} else if (objectType == ObjectType.OVAL) {
			currentOval = new OvalObject(centerX, centerY, defaultOvalRadius,
					fillColor, strokeColor, strokeWidth, strokeStyle);
			currentBasic = currentOval;
			currentObject = currentBasic;
			editObject(currentObject, ShapeHandler.ALL);
		}
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		listener.onSelectionEvent(CanvasListener.EXIT_MODE, 0);
		invalidate();
	}

	/**
	 * Memasukkan objek poligon ke kanvas, objek yang sedang digambar dan belum
	 * disimpan akan tertimpa.
	 * 
	 * @param corner jumah sudut, minimum 3
	 */
	public void insertPolygon(int corner) {
		currentPoly = new PolygonObject(corner, defaultPolyRadius, centerX,
				centerY, fillColor, strokeColor, strokeWidth, strokeStyle);
		currentBasic = currentPoly;
		setMode(Mode.DRAW);
		editObject(currentPoly, ShapeHandler.ALL);
		redoStack.clear();
		listener.onURStatusChange(!userActions.empty(), false);
		invalidate();
	}

	/**
	 * Mengubah warna isian dari {@link BasicObject}.
	 * 
	 * @param fill jika true berarti memiliki isian, jika false maka tidak
	 *            memiliki isian dan nilai {@code color} diabaikan.
	 * @param color warna isian, lihat {@link Color}
	 * @param save jika true, aksi langsung disimpan ke stack, jika false, aksi
	 *            hanya merubah parameter objek saja.
	 */
	public void setFillParameter(boolean fill, int color, boolean save) {
		android.util.Log.d("POS", "setFill:" + fill + "," + color + "," + save);
		filled = fill;
		color += 0xff000000;// opacity dimaksimalkan
		fillColorOri = color;
		fillColor = (filled) ? color : Color.TRANSPARENT;
		protoFree.setFillMode(filled, color);
		protoRect.setFillMode(filled, color);
		protoOval.setFillMode(filled, color);
		protoPoly.setFillMode(filled, color);
		if (currentObject != null && currentObject instanceof BasicObject) {
			if ((mode & Mode.DRAW) != Mode.DRAW && protaStyle == null)
				protaStyle = new StyleAction(currentObject, true);
			currentBasic.setFillMode(filled, color);
			if (save) {
				if ((mode & Mode.DRAW) != Mode.DRAW)
					pushToUAStack(protaStyle.capture(), false);
				listener.onWaitForApproval();
			}
		}
		postInvalidate();
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

	/**
	 * Menyeleksi semua objek.
	 * 
	 * @return jumlah objek yang diseleksi, jika 0 maka kanvas tidak masuk mode
	 *         seleksi
	 */
	public int selectAllObject() {
		if (model.objects.isEmpty())
			return 0;
		if (currentObject != null)
			approveAction();
		selectedObjects.clear();
		selectedObjects.addAll(model.objects);
		for (int i = 0; i < selectedObjects.size(); i++)
			selectedObjects.get(i).select();
		mode |= Mode.HAS_SELECTION;
		reloadCache();
		listener.onSelectionEvent(CanvasListener.EDIT_MULTIPLE,
				selectedObjects.size());
		postInvalidate();
		return selectedObjects.size();
	}

	public void moveSelectedObject() {
		if (((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION)
				&& ((mode & Mode.DRAW) != Mode.DRAW)) {
			if (protaMove == null) {
				protaMove = new MoveMultiple(selectedObjects, true);
				mode |= Mode.MOVING;
			}
			reloadCache();
			postInvalidate();
		}
	}

	public int copySelectedObjects() {
		ObjectClipboard.put(selectedObjects);
		draggedPaste.capture(selectedObjects);
		pasteOfsX = draggedPaste.offsetX() - capturePaste.offsetX();
		pasteOfsY = draggedPaste.offsetY() - capturePaste.offsetY();
		pasteScale = draggedPaste.rescaleTo(capturePaste);
		dragStatus = DS_PASTE_DEFLATING;
		animator.setInterpolator(interLinear);
		animator.setFloatValues(1, 0);
		animator.setDuration(DEFLATE_DURATION);
		animator.start();
		postInvalidate();
		return selectedObjects.size();
	}

	public void deleteSelectedObjects() {
		if ((mode & Mode.EDIT) == Mode.EDIT) {
			DeleteAction dm = new DeleteAction(currentObject);
			model.objects.remove(currentObject);
			pushToUAStack(dm, !hide_mode);
		} else {
			DeleteMultiple dm = new DeleteMultiple(selectedObjects);
			model.objects.removeAll(selectedObjects);
			pushToUAStack(dm, !hide_mode);
		}
		cancelSelect();
	}

	/**
	 * Menghilangkan seleksi pada objek, jika ada aksi yang belum tersimpan,
	 * maka akan mengabaikannya.
	 */
	public void cancelSelect() {
		currentObject = null;
		selectRect.setEmpty();
		mode &= ~Mode.HAS_SELECTION;
		int size = selectedObjects.size();
		for (int i = 0; i < size; i++)
			selectedObjects.get(i).deselect();
		if ((mode & Mode.EDIT) == Mode.EDIT)
			cancelAction();
		selectedObjects.clear();
		handler = null;
		currentType = ObjectType.NONE;
		reloadCache();
		postInvalidate();
		listener.onSelectionEvent(CanvasListener.ENTER_MODE, 0);
	}

	/**
	 * Menyalin objek dari clipboard ke kanvas. Kanvas otomatis menyeleksi objek
	 * yang telah terseleksi, kecuali kalau tidak ada objek yang disalin.
	 * 
	 * @return jumlah objek yang disalin
	 */
	public int pasteFromClipboard() {
		if (!ObjectClipboard.hasObject())
			return 0;
		selectedObjects.clear();
		ArrayList<CanvasObject> objs = ObjectClipboard.retrieve();
		for (int i = 0; i < objs.size(); i++) {
			CanvasObject clone = objs.get(i).cloneObject();
			clone.select();
			selectedObjects.add(clone);
			model.objects.add(clone);
		}
		mode |= Mode.HAS_SELECTION;
		if (objs.size() > 1) {
			DrawMultiple dm = new DrawMultiple(selectedObjects);
			pushToUAStack(dm, !hide_mode);
			listener.onSelectionEvent(CanvasListener.EDIT_MULTIPLE,
					selectedObjects.size());
		} else {
			CanvasObject co = selectedObjects.get(0);
			DrawAction da = new DrawAction(co);
			pushToUAStack(da, !hide_mode);
			editObject(co, ShapeHandler.ALL);
		}
		socX = 0;
		socY = 0;
		reloadCache();
		postInvalidate();
		return selectedObjects.size();
	}

	/**
	 * Memasukkan aksi ke stack
	 * 
	 * @param action
	 * @param flush kirim ke synchronizer atau tidak
	 */
	private void pushToUAStack(UserAction action, boolean flush) {
		if (action == null)
			return;
		userActions.push(action);
		// masukkan ke sync
		if (flush)
			synczer.addToBuffer(action);

		// jika sedang hidden_mode, catat untuk dikembalikan
		else if (hide_mode)
			revertList.add(action.getInverse());
		listener.onURStatusChange(true, !redoStack.isEmpty());
	}

	/**
	 * Apakah bisa diundo atau tidak
	 * 
	 * @return
	 */
	public boolean isUndoable() {
		return !userActions.isEmpty();
	}

	/**
	 * Mengembalikan perubahan yang terjadi oleh aksi yang terakhir dilakukan.
	 */
	public void undo() {
		if (!userActions.isEmpty()) {
			if (checkpoint == userActions.size()
					&& (((mode & Mode.HAS_SELECTION) == Mode.HAS_SELECTION) || ((mode & Mode.DRAW) == Mode.DRAW))) {
				cancelAction();
				if ((mode & Mode.DRAW) == Mode.DRAW)
					mode = Mode.SELECT;
				return;
			}
			UserAction action = userActions.pop();
			UserAction inverse = action.getInverse();
			if (inverse == null)
				return;
			execute(inverse, true);
			if (!hide_mode)
				synczer.addToBuffer(inverse);
			reloadCache();
			redoStack.push(action);
			invalidate();
			listener.onURStatusChange(!userActions.isEmpty(), true);
		}
	}

	/**
	 * Apakah bisa dilaukan redo atau tidak
	 * 
	 * @return
	 */
	public boolean isRedoable() {
		return !redoStack.isEmpty();
	}

	public void redo() {
		if (!redoStack.isEmpty()) {
			UserAction action = redoStack.pop();
			execute(action, true);
			pushToUAStack(action, !hide_mode);
			reloadCache();
			invalidate();
		}
	}

	/**
	 * Mengatur hidden_mode. Jika {@code hidden} true, akan tampil tanda hide
	 * mode di pojok kanvas.
	 * 
	 * @param hidden
	 */
	public void setHideMode(boolean hidden) {

		if (hidden) {
			if (!hide_mode) {
				// berubah dari tidak hide menjadi hide
				synczer.markCheckpoint();
				revertList.clear();
				synczer.stop();
			}

		} else if (hide_mode) {
			// kembalikan perubahan oleh user
			synczer.revert();
			synczer.start();
			execute(revertList, true);
		}
		hide_mode = hidden;
		postInvalidate();
		listener.onHideModeChange(hidden);
	}

	/**
	 * Mengembalikan apakah sedang hide mode atau tidak.
	 * 
	 * @return
	 */
	public boolean isInHideMode() {
		return hide_mode;
	}

	/**
	 * Mengeksekusi kumpulan aksi secara berurutan.
	 * 
	 * @param actions
	 */
	public void execute(ArrayList<UserAction> actions) {
		execute(actions, false);
	}

	/**
	 * Mengeksekusi kumpulan aksi secara berurutan.
	 * 
	 * @param actions
	 * @param forced jika true berarti langsung dijalankan, jika false dan
	 *            sedang hide mode, maka akan dimasukkan ke revertList
	 */
	private void execute(ArrayList<UserAction> actions, boolean forced) {
		for (int i = 0; i < actions.size(); i++) {
			UserAction ua = actions.get(i);
			execute(ua, forced);
		}
		reloadCache();
		invalidate();
	}

	/**
	 * Mengeksekusi suatu perintah.
	 * 
	 * @param action aksi yang dieksekusi
	 * @param forced jika true -> aksi langsung dijalankan. jika false -> aksi
	 *            akan ditunda jika objek yang berkaitan dengan aksi tersebut
	 *            sedang diedit.
	 */
	private void execute(UserAction action, boolean forced) {
		if (action instanceof DrawAction) {
			// aksi draw tidak ditunda
			DrawAction da = (DrawAction) action;
			model.objects.add(da.object);
		} else if (action instanceof TransformAction) {
			// aksi transform akan ditunda jika user sedang melakukan reshape
			// atau move pada objek tersebut
			TransformAction ta = (TransformAction) action;
			if (!forced
					&& ((protaMove != null && protaMove.overwrites(ta)) || (protaReshape != null && protaReshape
							.overwrites(ta))))
				pendingTransformActions.add(ta);
			else
				ta.apply();
		} else if (action instanceof ReshapeAction) {
			// aksi reshape tidak akan ditunda
			ReshapeAction ra = (ReshapeAction) action;
			ra.apply();
		} else if (action instanceof GeomAction) {
			// aksi geom ditunda jika user sedang melakukan reshape pada objek
			// yang sama
			GeomAction ga = (GeomAction) action;
			if (!forced && protaReshape != null && protaReshape.overwrites(ga))
				pendingGeomActions.add(ga);
			else
				ga.apply();
		} else if (action instanceof StyleAction) {
			// aksi style tidak ditunda
			StyleAction sa = (StyleAction) action;
			sa.applyStyle();
		} else if (action instanceof DeleteAction) {
			// aksi delete tidak ditunda
			DeleteAction da = (DeleteAction) action;
			// jika objek sedang diedit -> batalkan editan
			if (currentObject != null && currentObject.equals(da.object)) {
				cancelAction();
				cancelSelect();
			} else if (selectedObjects.contains(da.object)) {
				// jika diedit rame2 -> singkirkan objek
				selectedObjects.remove(da.object);
			}
			model.objects.remove(da.object);
		} else if (action instanceof MoveMultiple) {
			// aksi move multiple tidak ditunda
			((MoveMultiple) action).apply();
		} else if (action instanceof DeleteMultiple) {
			// aksi delete multiple tidak ditunda
			DeleteMultiple da = (DeleteMultiple) action;
			model.objects.removeAll(da.objects);
		} else if (action instanceof DrawMultiple) {
			// undelete, tidak ditunda
			DrawMultiple dm = (DrawMultiple) action;
			model.objects.addAll(dm.objects);
		} else if (action instanceof MoveStepper) {
			// move tidak ditunda
			MoveStepper ms = (MoveStepper) action;
			ms.execute();
		} else if (action instanceof ResizeCanvas) {
			// aksi resize canvas tidak ditunda
			ResizeCanvas rc = (ResizeCanvas) action;
			model.setDimension(rc.width, rc.height, rc.top, rc.left);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if ((dragStatus & DS_CLICK) == DS_CLICK) {
			continueDraw = true;
		}
		return false;
	}
}
