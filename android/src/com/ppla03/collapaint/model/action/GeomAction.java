package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi merubah geometri dari objek. Aksi ini tidak bisa dikembalikan(tidak
 * mempunyai inverse).
 * @author hamba v7
 * 
 */
public class GeomAction extends AtomicAction {

	/**
	 * Objek yang dirubah geometrinya.
	 */
	public final CanvasObject object;
	/**
	 * Daftar parameter objek.
	 */
	float[] params;

	GeomAction(CanvasObject object, float[] sourceParam, int start, int length) {
		this.object = object;
		params = new float[object.geomParamLength()];
		System.arraycopy(sourceParam, start, params, 0, length);
	}

	/**
	 * Membuat {@link GeomAction} dengan parameter tertentu
	 * @param object
	 * @param parameter
	 */
	public GeomAction(CanvasObject object, String parameter) {
		this.object = object;
		params = new float[object.geomParamLength()];
		setParameter(parameter);
	}

	/**
	 * Mengambil parameter geometri objek dari aksi ini.
	 * @return parameter aksi yang sudah terkodekan dalam {@link String}.
	 */
	public String getParameter() {
		return encode(params, params.length);
	}

	/**
	 * Mengatur parameter geometri objek dari aksi ini.
	 * @param param parameter aksi dalam bentuk {@link String}.
	 */
	public void setParameter(String param) {
		int size = decodeSize(param);
		if (params.length < size)
			params = new float[size];
		decodeTo(param, params);
	}

	/**
	 * Mengaplikasikan parameter geometri ke objek.
	 */
	public void apply() {
		object.setGeom(params, 0, params.length);
	}

	private static float[] applyTemp = new float[5];

	/**
	 * Mengaplikasikan suatu parameter geometri objek ke objek kanvas.
	 * @param param parameter geometri dalam {@link String}.
	 * @param object objek kanvas.
	 */
	public static void apply(String param, CanvasObject object) {
		int length = decodeSize(param);
		if (applyTemp.length < length)
			applyTemp = new float[length];
		int size = decodeTo(param, applyTemp);
		object.setGeom(applyTemp, 0, size);
	}

	private static float[] tempPoints = new float[5];

	/**
	 * Mengambil parameter geometri dari suatu objek.
	 * @param object objek kanvas yang ingin diambil parameternya.
	 * @return parameter yang sudah terkodekan dalam {@link String}.
	 */
	public static String getParameterOf(CanvasObject object) {
		int size = object.geomParamLength();
		if (tempPoints.length < size)
			tempPoints = new float[size];
		int n = object.extractGeom(tempPoints, 0);
		return encode(tempPoints, n);
	}

	/**
	 * Mendapatkan ukuran minimal array yang dibutuhkan untuk menampung hasil
	 * decode suatu parameter geometri objek dalam bentuk {@link String}.
	 * @param param parameter
	 * @return ukuran array minimal yang dibutuhkan.
	 */

	private static int decodeSize(String param) {
		return ((param.length() * 3) >> 4);
	}

	/**
	 * Membongkar informasi yang ada pada parameter berbentuk string, dan
	 * memindahkannya pada duatu array.
	 * @param param parameter
	 * @param result array penampung
	 * @return banyaknya data yang dipindahkan ke array.
	 */
	private static int decodeTo(String param, float[] result) {
		byte[] bs = Base64.decode(param, Base64.URL_SAFE);
		int size = bs.length >> 2;
		int c = 0;
		for (int i = 0; i < size; i++)
			result[i] = Float.intBitsToFloat(((bs[c++] << 24) & 0xff000000)
					| ((bs[c++] << 16) & 0xff0000) | ((bs[c++] << 8) & 0xff00)
					| (bs[c++] & 0xff));
		return size;
	}

	private static byte[] encByte = new byte[4];

	/**
	 * Mengkodekan parameter berbentuk array ke parameter berbentuk
	 * {@link String}.
	 * @param points array yang menyimpan informasi parameter.
	 * @param count banyaknya data dalam array yang dikodekan.
	 * @return hasil pengkodean.
	 */
	private static String encode(float[] points, int count) {
		int c = count << 2;
		if (encByte.length < c)
			encByte = new byte[c];
		c = 0;
		for (int i = 0; i < count; i++) {
			int p = Float.floatToIntBits(points[i]);
			encByte[c++] = (byte) (p >> 24);
			encByte[c++] = (byte) (p >> 16);
			encByte[c++] = (byte) (p >> 8);
			encByte[c++] = (byte) (p);
		}
		return Base64.encodeToString(encByte, 0, c, Base64.URL_SAFE);
	}

	@Override
	public UserAction getInverse() {
		return null;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		return (action != null)
				&& ((action instanceof GeomAction && ((GeomAction) action).object
						.equals(object)) || ((action instanceof ReshapeAction) && ((ReshapeAction) action).object
						.equals(object)));
	}
}
