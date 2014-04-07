package com.ppla03.collapaint.model.action;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Menangani aksi untuk merubah bentuk suatu objek.
 * @author hamba v7
 * 
 */
public class ReshapeAction extends UserAction {
	/**
	 * Objek kanvas yang diubah.
	 */
	public final CanvasObject object;

	/**
	 * Daftar parameter bentuk suatu objek. Tiga indeks pertama diisi parameter
	 * OFFSET_X, OFFSET_Y, dan ROTATION dari objek secara berurutan. Indeks
	 * berikutnya diisi oleh parameter instrinsik dari bentuk objek.
	 */
	private float[] params;

	private static final int TRANSFORM_LENGTH = 3;
	private static final int OFSX_INDEX = 0, OFSY_INDEX = 1, ROT_INDEX = 2;

	/**
	 * Membuat suatu {@link ReshapeAction} yang memiliki inverse.
	 * @param inverse
	 */
	private ReshapeAction(ReshapeAction inverse) {
		this.object = inverse.object;
		this.inverse = inverse;
		params = new float[inverse.params.length];
	}

	/**
	 * Membuat suatu {@link ReshapeAction} dengan parameter tertentu.
	 * @param object objek kanvas yang diubah bentuknya.
	 * @param reversible apakah bisa dilakukan operasi balik atau tidak. Jika
	 *            true, maka inverse dari objek ini dipastikan selalu tersedia.
	 */
	public ReshapeAction(CanvasObject object, boolean reversible) {
		this.object = object;
		params = new float[object.paramLength() + TRANSFORM_LENGTH];
		params[OFSX_INDEX] = object.offsetX();
		params[OFSY_INDEX] = object.offsetY();
		params[ROT_INDEX] = object.rotation();
		object.extractShape(params, TRANSFORM_LENGTH);
		if (reversible) {
			ReshapeAction ra = new ReshapeAction(object, false);
			ra.inverse = this;
			this.inverse = ra;
		}
	}

	/**
	 * Mengambil parameter bentuk objek dari aksi ini.
	 * @return parameter aksi yang sudah terkodekan dalam {@link String}.
	 */
	public String getParameter() {
		return encode(params, params.length);
	}

	/**
	 * Mengatur parameter bentuk objek dari aksi ini.
	 * @param param parameter aksi dalam bentuk {@link String}.
	 */
	public void setParameter(String param) {
		int size = decodeSize(param);
		if (params.length < size)
			params = new float[size];
		decodeTo(param, params);
	}

	/**
	 * Mengaplikasikan parameter bentuk ke objek.
	 */
	public void apply() {
		object.setOffset(params[OFSX_INDEX], params[OFSY_INDEX]);
		object.setRotation(params[ROT_INDEX]);
		object.setShape(params, TRANSFORM_LENGTH, params.length);
	}

	private static float[] tempPoints = new float[5];

	/**
	 * Mengambil parameter bentuk dari suatu objek.
	 * @param object objek kanvas yang ingin diambil parameternya.
	 * @return parameter yang sudah terkodekan dalam {@link String}.
	 */
	public static String getParameterOf(CanvasObject object) {
		int size = object.paramLength() + TRANSFORM_LENGTH;
		if (tempPoints.length < size)
			tempPoints = new float[size];
		tempPoints[OFSX_INDEX] = object.offsetX();
		tempPoints[OFSY_INDEX] = object.offsetY();
		tempPoints[ROT_INDEX] = object.rotation();
		int n = object.extractShape(tempPoints, TRANSFORM_LENGTH)
				+ TRANSFORM_LENGTH;
		return encode(tempPoints, n);
	}

	private static float[] applyTemp = new float[5];

	/**
	 * Mengaplikasikan suatu parameter bentuk objek ke objek kanvas.
	 * @param param parameter bentuk dalam {@link String}.
	 * @param object objek kanvas.
	 */
	public static void apply(String param, CanvasObject object) {
		int length = object.paramLength() + TRANSFORM_LENGTH;
		if (applyTemp.length < length)
			applyTemp = new float[length];
		int size = decodeTo(param, applyTemp);
		object.setOffset(applyTemp[OFSX_INDEX], applyTemp[OFSY_INDEX]);
		object.setRotation(applyTemp[ROT_INDEX]);
		object.setShape(applyTemp, TRANSFORM_LENGTH, size);
	}

	/**
	 * Mencatat bentuk objek saat ini.
	 * @return aksi yang inverse-nya akan mengembalikan bentuk objek ke bentuk
	 *         terakhir sebelum diCapture.
	 */
	public ReshapeAction capture() {
		ReshapeAction rai = new ReshapeAction(object, false);
		System.arraycopy(params, 0, rai.params, 0, params.length);
		params[OFSX_INDEX] = object.offsetX();
		params[OFSY_INDEX] = object.offsetY();
		params[ROT_INDEX] = object.rotation();
		object.extractShape(params, TRANSFORM_LENGTH);
		ReshapeAction ra = new ReshapeAction(rai);
		System.arraycopy(params, 0, ra.params, 0, params.length);
		rai.inverse = ra;
		return ra;
	}

	/**
	 * Mendapatkan ukuran minimal array yang dibutuhkan untuk menampung hasil
	 * decode suatu parameter bentuk objek dalam bentuk {@link String}.
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
		for (int i = 0; i < count; i++) {
			int p = Float.floatToIntBits(points[i]);
			encByte[c++] = (byte) (p >> 24);
			encByte[c++] = (byte) (p >> 16);
			encByte[c++] = (byte) (p >> 16);
			encByte[c++] = (byte) (p);
		}
		return Base64.encodeToString(encByte, 0, c, Base64.URL_SAFE);
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
		if (action != null && action instanceof ReshapeAction) {
			ReshapeAction ra = (ReshapeAction) action;
			return ra.object.equals(this.object);
		}
		return false;
	}

}
