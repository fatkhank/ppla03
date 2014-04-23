package com.ppla03.collapaint.model.action;

import java.util.Arrays;

import android.util.Base64;

import com.ppla03.collapaint.model.object.BasicObject;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.LineObject;
import com.ppla03.collapaint.model.object.TextObject;

/**
 * Aksi mengubah <i>style</i> dari suatu objek kanvas.
 * @author hamba v7
 * 
 */
public class StyleAction extends AtomicAction {
	/**
	 * Objek kanvas yang dikenai aksi.
	 */
	public final CanvasObject object;
	private static final int FILL_COLOR = 0, STROKE_COLOR = 1,
			STROKE_WIDTH = 2, STROKE_STYLE = 3, TEXT_COLOR = STROKE_COLOR,
			TEXT_SIZE = STROKE_WIDTH, TEXT_STYLE = STROKE_STYLE;

	/**
	 * Parameter <i>style</i> objek.
	 */
	protected final int[] styles = new int[4];

	/**
	 * Konstruktor untuk membuat inverse dari styleAction
	 * @param object
	 * @param inverse
	 */
	private StyleAction(CanvasObject object, StyleAction inverse) {
		this.object = object;
		this.inverse = inverse;
	}

	/**
	 * Membuat suatu StyleAction
	 * @param object {@link CanvasObject} yang diubah stylenya.
	 * @param reversible mempunyai inverse atau tidak.
	 */
	public StyleAction(CanvasObject object, boolean reversible) {
		this.object = object;
		if (reversible) {
			StyleAction inv = new StyleAction(object, this);
			extractStyle(object, inv.styles);
			extractStyle(object, styles);
			inverse = inv;
		}
	}

	/**
	 * Menangkap perubahan yang sudah dilakukan terhadap style objek.
	 * @return Aksi perubahan style dari semenjak capture terakhir. Jika tidak
	 *         ada perubahan, maka null;
	 */
	public StyleAction capture() {
		StyleStepper backward = new StyleStepper(this, object, null);
		System.arraycopy(styles, 0, backward.styles, 0, styles.length);
		extractStyle(object, styles);
		if (Arrays.equals(styles, backward.styles))
			return null;
		StyleStepper forward = new StyleStepper(this, object, backward);
		System.arraycopy(styles, 0, forward.styles, 0, styles.length);
		backward.inverse = forward;
		return forward;
	}

	/**
	 * 
	 * @author hamba v7
	 * 
	 */
	static class StyleStepper extends StyleAction {
		private StyleAction parent;

		public StyleStepper(StyleAction parent, CanvasObject object, StyleStepper inverse) {
			super(object, inverse);
			this.parent = parent;
		}

		@Override
		public void applyStyle() {
			System.arraycopy(this.styles, 0, parent.styles, 0,
					this.styles.length);
			super.applyStyle();
		}
	}

	/**
	 * Menjalankan aksi ini.
	 */
	public void applyStyle() {
		applyStyle(styles, object);
	}

	/**
	 * Mendapatkan parameter dari aksi ini dalam bentuk String.
	 * @return parameter.
	 */
	public String getParameter() {
		return encode(styles);
	}

	/**
	 * Mengatur parameter aksi ini dalam bentuk String.
	 * @param param
	 * @return this
	 */
	public StyleAction setParameter(String param) {
		decodeTo(param, styles);
		return this;
	}

	private static final int[] getParamTemp = new int[4];

	/**
	 * Mengambil parameter style dari suatu objek dan mengubahnya ke dalam
	 * bentuk String
	 * @param object
	 * @return
	 */
	public static String getParameterOf(CanvasObject object) {
		extractStyle(object, getParamTemp);
		return encode(getParamTemp);
	}

	private static final int[] styleTemp = new int[4];

	public static void applyStyle(String param, CanvasObject object) {
		if (param != null && !param.isEmpty()) {
			decodeTo(param, styleTemp);
			applyStyle(styleTemp, object);
		}
	}

	private static void extractStyle(CanvasObject object, int[] style) {
		if (object instanceof BasicObject) {
			BasicObject bo = (BasicObject) object;
			style[FILL_COLOR] = bo.getFillColor();
			style[STROKE_COLOR] = bo.getStrokeColor();
			style[STROKE_WIDTH] = bo.getStrokeWidth();
			style[STROKE_STYLE] = bo.getStrokeStyle();
		} else if (object instanceof LineObject) {
			LineObject lo = (LineObject) object;
			style[STROKE_COLOR] = lo.getColor();
			style[STROKE_WIDTH] = lo.getWidth();
			style[STROKE_STYLE] = lo.getStrokeStyle();
		} else if (object instanceof TextObject) {
			TextObject to = (TextObject) object;
			style[TEXT_COLOR] = to.getTextColor();
			style[TEXT_SIZE] = to.getFontSize();
			style[TEXT_STYLE] = to.getFontCode();
		}
	}

	private static void applyStyle(int[] style, CanvasObject co) {
		if (co instanceof BasicObject) {
			BasicObject bo = (BasicObject) co;
			bo.setFillMode(true, style[FILL_COLOR]);
			bo.setStrokeColor(style[STROKE_COLOR]);
			bo.setStrokeWidth(style[STROKE_WIDTH]);
			bo.setStrokeStyle(style[STROKE_STYLE]);
		} else if (co instanceof LineObject) {
			LineObject lo = (LineObject) co;
			lo.setColor(style[STROKE_COLOR]);
			lo.setWidth(style[STROKE_WIDTH]);
			lo.setStrokeStyle(style[STROKE_STYLE]);
		} else if (co instanceof TextObject) {
			((TextObject) co).setParameter(style[TEXT_COLOR],
					style[TEXT_STYLE], style[TEXT_SIZE]);
		}
	}

	private static void decodeTo(String param, int[] res) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		res[0] = ((bs[0] << 24) & 0xff000000) | ((bs[1] << 16) & 0xff0000)
				| ((bs[2] << 8) & 0xff00) | (bs[3] & 0xff);
		res[1] = ((bs[4] << 24) & 0xff000000) | ((bs[5] << 16) & 0xff0000)
				| ((bs[6] << 8) & 0xff00) | (bs[7] & 0xff);
		res[2] = ((bs[8] << 24) & 0xff000000) | ((bs[9] << 16) & 0xff0000)
				| ((bs[10] << 8) & 0xff00) | (bs[11] & 0xff);
		res[3] = ((bs[12] << 24) & 0xff000000) | ((bs[13] << 16) & 0xff0000)
				| ((bs[14] << 8) & 0xff00) | (bs[15] & 0xff);
	}

	private static final byte[] encByte = new byte[16];

	private static String encode(int[] style) {
		int a = style[0];
		encByte[0] = (byte) (a >> 24);
		encByte[1] = (byte) (a >> 16);
		encByte[2] = (byte) (a >> 16);
		encByte[3] = (byte) (a);
		int b = style[1];
		encByte[4] = (byte) (b >> 24);
		encByte[5] = (byte) (b >> 16);
		encByte[6] = (byte) (b >> 16);
		encByte[7] = (byte) (b);
		int c = style[2];
		encByte[8] = (byte) (c >> 24);
		encByte[9] = (byte) (c >> 16);
		encByte[10] = (byte) (c >> 16);
		encByte[11] = (byte) (c);
		int d = style[3];
		encByte[12] = (byte) (d >> 24);
		encByte[13] = (byte) (d >> 16);
		encByte[14] = (byte) (d >> 16);
		encByte[15] = (byte) (d);

		return Base64.encodeToString(encByte, Base64.URL_SAFE);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return action == inverse;
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null && action instanceof StyleAction) {
			StyleAction sa = (StyleAction) action;
			return sa.object.equals(this.object);
		}
		return false;
	}
}
