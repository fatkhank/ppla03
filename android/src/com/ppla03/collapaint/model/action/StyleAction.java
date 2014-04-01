package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.BasicObject;
import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.ImageObject;
import com.ppla03.collapaint.model.object.LinesObject;
import com.ppla03.collapaint.model.object.TextObject;

public class StyleAction extends UserAction {
	public final CanvasObject object;
	private static final int FILL_COLOR = 0, STROKE_COLOR = 1,
			STROKE_WIDTH = 2, STROKE_STYLE = 3, TEXT_COLOR = STROKE_COLOR,
			TEXT_SIZE = STROKE_WIDTH, TEXT_STYLE = STROKE_STYLE;

	private final int[] styles = new int[4];

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
			inverse = inv;
		}
	}

	public void setStyle(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		styles[FILL_COLOR] = fillColor;
		styles[STROKE_COLOR] = strokeColor;
		styles[STROKE_WIDTH] = strokeWidth;
		styles[STROKE_STYLE] = strokeStyle;
	}

	public void setTextStyle(int color, int size, int style) {
		styles[TEXT_COLOR] = color;
		styles[TEXT_SIZE] = size;
		styles[TEXT_STYLE] = style;
	}

	public void applyStyle() {
		applyStyle(styles, object);
	}

	public String getParameter() {
		return encode(styles);
	}

	public void setParameter(String param) {
		parseTo(param, styles);
	}

	static final int[] getParamTemp = new int[4];

	public static String getParameterOf(CanvasObject object) {
		extractStyle(object, getParamTemp);
		return encode(getParamTemp);
	}

	static final int[] styleTemp = new int[4];

	public static void applyStyle(String param, CanvasObject object) {
		parseTo(param, styleTemp);
		applyStyle(styleTemp, object);
	}

	private static void extractStyle(CanvasObject object, int[] style) {
		if (object instanceof BasicObject) {
			BasicObject bo = (BasicObject) object;
			style[FILL_COLOR] = bo.getFillColor();
			style[STROKE_COLOR] = bo.getStrokeColor();
			style[STROKE_WIDTH] = bo.getStrokeWidth();
			style[STROKE_STYLE] = bo.getStrokeStyle();
		} else if (object instanceof LinesObject) {
			LinesObject lo = (LinesObject) object;
			style[STROKE_COLOR] = lo.getStrokeColor();
			style[STROKE_WIDTH] = lo.getStrokeWidth();
			style[STROKE_STYLE] = lo.getStrokeStyle();
		} else if (object instanceof TextObject) {
			TextObject to = (TextObject) object;
			style[TEXT_COLOR] = to.getTextColor();
			style[TEXT_SIZE] = to.getFontSize();
			style[TEXT_STYLE] = to.getFontStyle();
		} else if (object instanceof ImageObject) {
			style[FILL_COLOR] = ((ImageObject) object).getTransparency();
		}
	}

	private static void applyStyle(int[] style, CanvasObject co) {
		if (co instanceof BasicObject) {
			BasicObject bo = (BasicObject) co;
			bo.setFillMode(true, style[FILL_COLOR]);
			bo.setStrokeColor(style[STROKE_COLOR]);
			bo.setStrokeWidth(style[STROKE_WIDTH]);
			bo.setStrokeStyle(style[STROKE_STYLE]);
		}else if(co instanceof LinesObject){
			LinesObject lo = (LinesObject) co;
			lo.setLineColor(style[STROKE_COLOR]);
			lo.setLineWidth(style[STROKE_WIDTH]);
			lo.setLineStyle(style[STROKE_STYLE]);
		} else if (co instanceof TextObject) {
			TextObject to = (TextObject) co;
			to.setParameter(style[TEXT_COLOR], style[TEXT_SIZE],
					style[TEXT_STYLE]);
		} else if (co instanceof ImageObject) {
			((ImageObject) co).setTransparency(style[FILL_COLOR]);
		}
	}

	private static void parseTo(String param, int[] res) {
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

	static final byte[] encByte = new byte[16];

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
