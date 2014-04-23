package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Objek yang dapat dimasukkan ke dalam kanvas.
 * @author hamba v7
 * 
 */
public abstract class CanvasObject {
	private static int idCounter = 1;

	/**
	 * Id privat yang menandakan identitas objek. Unik untuk semua objek yang
	 * belum memiliki id global. Id ini akan menggantikan id global sebelum
	 * objek memiliki id global.
	 */
	public final int privateID;

	/**
	 * Id global objek. Dipastikan unik untuk semua objek yang telah memiliki id
	 * ini. Objek yang belum memiliki id global akan memiliki nilai -1
	 * (default), yang dapat berarti bahwa objek belum disinkronisasikan.
	 */
	protected int globalID = -1;

	/**
	 * Menandakan objek sedang diseleksi atau tidak.
	 */
	protected boolean selected;

	/**
	 * Pergeseran pusat objek dari pojok kiri atas kanvas pada sumbu x.
	 */
	protected float offsetX;

	/**
	 * Pergeseran pusat objek dari pojok kiri atas kanvas pada sumbu y.
	 */
	protected float offsetY;

	/**
	 * Besarnya rotasi objek dalam derajat.
	 */
	protected float rotation;

	/**
	 * Membuat objek dengan id baru yang unik.
	 */
	protected CanvasObject() {
		privateID = idCounter++;
	}

	/**
	 * Mengubah id global objek.
	 * @param id id yang baru.
	 */
	public final void setGlobaID(int id) {
		globalID = id;
	}

	/**
	 * Mendapatkan id global objek.
	 * @return id gobal
	 */
	public final int getGlobalID() {
		return globalID;
	}

	/**
	 * Menggambar objek pada suatu kanvas.
	 * @param canvas kanvas tempat objek digambar.
	 */
	public final void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(offsetX, offsetY);
		canvas.rotate(rotation);
		drawSelf(canvas);
		canvas.restore();
	}

	/**
	 * Menggambar objek pada suatu kanvas, tanpa memperhatikan parameter
	 * translasi dan rotasi dari objek.
	 * @param canvas kanvas tempat objek digambar.
	 */
	protected abstract void drawSelf(Canvas canvas);

	private static final Matrix transMat = new Matrix();
	private static final RectF tempRect = new RectF();

	/**
	 * Tebal maksimal garis pinggiran
	 */
	public static final int MAX_STROKE_WIDTH = 100;

	/**
	 * Tebal minimal garis pinggiran
	 */
	public static final int MIN_STROKE_WIDTH = 1;

	/**
	 * Mencoba menyeleksi objek berdasarkan suatu kotak area tertentu. Bila
	 * keseluruhan objek masuk ke area tersebut, maka objek akan otomatis
	 * terseleksi.
	 * @param worldArea kotak area seleksi; koordinat kotak menggunakan
	 *            koordinat kanvas.
	 * @return apakah objek masuk area seleksi atau tidak.
	 */
	public final boolean selectIn(RectF worldArea) {
		getWorldBounds(tempRect);
		selected = worldArea.contains(tempRect);
		return selected;
	}

	/**
	 * Mencoba menyeleksi objek pada titik tertentu. Apabila terdapat bagian
	 * objek yang berada pada jarak tertentu dari titik tersebut, maka objek
	 * akan otomatis terseleksi.
	 * @param worldX koordinat x titik seleksi (koordinat kanvas)
	 * @param worldY koordinat y titik seleksi. (koordinat kanvas)
	 * @param radius toleransi jarak maksimal agar objek dikatakan terseleksi.
	 * @return hasil apakah objek terseleksi atau tidak.
	 */
	public final boolean selectAt(float worldX, float worldY, float radius) {
		double x = worldX - offsetX;
		double y = worldY - offsetY;
		double deg = Math.toRadians(-rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		x = x * cos + y * -sin;
		y = x * sin + y * cos;
		selected = selectedBy((float) x, (float) y, radius);
		return selected;
	}

	/**
	 * Mencoba menyeleksi objek pada titik tertentu.
	 * @param x koordinat x titik seleksi.
	 * @param y koordinat y titik seleksi.
	 * @param radius toleransi jarak seleksi.
	 * @return hasil apakah terseleksi atau tidak.
	 */
	protected abstract boolean selectedBy(float x, float y, float radius);

	/**
	 * Membuat flag seleksi objek true.
	 */
	public final void select() {
		this.selected = true;
	}

	/**
	 * Mengecek apakah objek sedang diseleksi atau tidak.
	 * @return hasil
	 */
	public final boolean isSelected() {
		return selected;
	}

	/**
	 * Melepaskan seleksi objek.
	 */
	public final void deselect() {
		selected = false;
	}

	/**
	 * Mengatur <i>shape parameter</i> dari objek. Informasi dari param akan di
	 * baca dari indeks start sampai end-1.
	 * @param param array yang memuat informasi bentuk objek.
	 * @param start indeks awal pembacaan informasi.
	 * @param end indeks pemberhenti.
	 */
	public abstract void setGeom(float[] param, int start, int end);

	/**
	 * Mengambil berapa panjang array yang dibutuhkan untuk menampung <i>shape
	 * parameter</i> dari objek.
	 * @return panjang array
	 */
	public abstract int geomParamLength();

	/**
	 * Memasukkan informasi <i>shape parameter</i> dari objek ke dalam suatu
	 * array. Parameter ini berisi informasi intrinsik mengenai bentuk objek.
	 * Parameter translasi dan rotasi tidak diikutkan. Tidak dilakukan
	 * pengecekan terhadap panjang array. Gunakan {@code paramLength()} untuk
	 * mengecek berapa panjang array yang dibutuhkan.
	 * @param target array tempat menampung informasi.
	 * @param start indeks awal informasi mulai dimasukkan ke array.
	 * @return banyaknya informasi yang dimasukkan ke dalam array.
	 */
	public abstract int extractGeom(float[] target, int start);

	/**
	 * Mengambil koordinat translasi objek pada sumbu x.
	 * @return
	 */
	public final float offsetX() {
		return offsetX;
	}

	/**
	 * Mengambil koordinat translasi objek pada sumbu y.
	 * @return
	 */
	public final float offsetY() {
		return offsetY;
	}

	/**
	 * Mengatur translasi dari objek.
	 * @param worldX koordinat x kanvas
	 * @param worldY koordinat y kanvas.
	 */
	public final void offsetTo(float worldX, float worldY) {
		offsetX = worldX;
		offsetY = worldY;
	}

	/**
	 * Menggeser objek dengan jarak tertentu. Hasilnya akan diakumulasi dengan
	 * perpindahan objek yang sudah ada.
	 * @param worldX jarak pergeseran sumbu x.
	 * @param worldY jarak pergeseran sumbu y.
	 */
	public final void offset(float worldX, float worldY) {
		this.offsetX += worldX;
		this.offsetY += worldY;
	}

	/**
	 * Menggeser objek dengan jarak tertentu, dengan koordinat objek. Pergeseran
	 * ini otomatis akan diterjemahkan ke offset koordinat kanvas.
	 * @param objX pergeseran x pada koordinat objek
	 * @param objY pergeseran y pada koordinat objek
	 */
	final void offsetRelative(float objX, float objY) {
		double deg = Math.toRadians(rotation);
		double cos = Math.cos(deg);
		double sin = Math.sin(deg);
		offsetX += (float) (objX * cos + objY * -sin);
		offsetY += (float) (objX * sin + objY * cos);
	}

	/**
	 * Mengambil sudut rotasi objek dalam derajat.
	 */
	public final float rotation() {
		return rotation;
	}

	/**
	 * Mengatur rotasi dari objek.
	 * @param angle sudut rotasi dalam derajat.
	 */
	public final void rotateTo(float angle) {
		rotation = angle;
	}

	/**
	 * Mendapatkan {@link ShapeHandler} dari objek.
	 * @param filter jenis {@link ControlPoint} yang diperbolehkan
	 * @return {@link ShapeHandler}
	 */
	public abstract ShapeHandler getHandler(int filter);

	/**
	 * Menginformasikan kepada objek bahwa ada {@link ControlPoint} dari
	 * {@link ShapeHandler}-nya yang telah berubah posisinya.
	 * @param handler {@link ShapeHandler} dari objek.
	 * @param point {@link ControlPoint} yang berubah posisinya.
	 * @param oldX posisi x {@link ControlPoint} yang lama.
	 * @param oldY posisi y {@link ControlPoint} yang lama.
	 */
	abstract void onHandlerMoved(ShapeHandler handler, ControlPoint point,
			float oldX, float oldY);

	/**
	 * Memberitahu objek bahwa ada {@link ControlPoint} yang telah dilepaskan
	 * @param point
	 */
	void onHandlerRelease(ControlPoint point) {}

	/**
	 * Mendapatkan batas-batas dari objek, kemudian memasukkannya ke dalam
	 * variabel bounds. Koordinat batas objek merupakan koordinat kanvas.
	 * @param kotak tempat menyimpan hasi batas-batas objek.
	 */
	public final void getWorldBounds(RectF bounds) {
		getBounds(bounds);
		transMat.reset();
		transMat.preTranslate(offsetX, offsetY);
		transMat.preRotate(rotation);
		transMat.mapRect(bounds);
	}

	/**
	 * Mendapatkan batas-batas dari objek, tanpa memperhatikan parameter rotasi
	 * dan translasi, kemudian memasukannya ke dalam variabel bounds.
	 * @param bounds kotak untuk menampung hasil batas-batas objek.
	 * 
	 */
	protected abstract void getBounds(RectF bounds);

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof CanvasObject))
			return false;
		CanvasObject co = (CanvasObject) o;
		return (this.globalID == co.globalID && this.globalID != -1)
				|| this.privateID == co.privateID;
	}

	/**
	 * Mendapatkan objek kanvas yang merupakan salinan dari objek ini.
	 * Modifikasi yang dilakukan terhadap hasil salinan tidak berpengaruh
	 * terhadap objek ini.
	 * @return hasil salinan.
	 */
	public abstract CanvasObject cloneObject();

	/**
	 * Menyalin data tranformasi objek ke objek lain.
	 * @param target
	 */
	protected void copyTransformData(CanvasObject target) {
		target.offsetX = this.offsetX;
		target.offsetY = this.offsetY;
		target.rotation = this.rotation;
	}

	@Override
	public int hashCode() {
		return globalID * 200 + privateID;
	}
}
