package com.ppla03.collapaint.model.object;

import android.graphics.Canvas;

/**
 * Titik kontrol untuk memodifikasi bentuk objek suatu objek.
 * @author hamba v7
 * 
 */
public abstract class ControlPoint {
	/**
	 * Koordinat x kanvas.
	 */
	static final int ORI_X = 0;
	/**
	 * Koordinat y kanvas.
	 */
	static final int ORI_Y = 1;
	/**
	 * Koordinat x relatif terhadap offset objek, tapi tidak tergantung rotasi
	 * objek.
	 */
	static final int TRANS_X = 2;
	/**
	 * Koordinat y relatif terhadap offset objek, tapi tidak tergantung rotasi
	 * objek.
	 */
	static final int TRANS_Y = 3;
	/**
	 * Koordinat x relatif terhadap offset dan rotasi objek.
	 */
	static final int OBJ_X = 4;
	/**
	 * Koordinat y relatif terhadap offset dan rotasi objek.
	 */
	static final int OBJ_Y = 5;

	protected static final int DRAW_RADIUS = 15;
	protected static final int GRAB_RADIUS_SQUARED = 30 * 30;

	final int id;

	/**
	 * Menandakan titik sedang digerakkan.
	 */
	protected boolean grabbed;

	protected float x, y;

	/**
	 * Menandakan titik dapat dicengkeram.
	 */
	protected boolean enable;

	/**
	 * Membuat titik kontrol baru.
	 * @param type tipe titik kontrol. Lihat {@link Type}.
	 * @param x koordinat x titik kontrol relatif terhadap pusat objek.
	 * @param y koordinat y titik kontrol relatif terhadap pusat objek.
	 * @param id id titik kontrol.
	 */
	protected ControlPoint(float x, float y, int id) {
		this.x = x;
		this.y = y;
		this.id = id;
		enable = true;
	}

	/**
	 * Mencoba mencengkeram titik untuk sehingga bisa digerakkan.
	 * @param x koordinat x cengkeraman relatif terhadap pusat objek.
	 * @param y koordinat y cengkeraman relatif terhadap pusat objek.
	 * @return berhasil atau tidak.
	 */
	public abstract boolean grabbed(float[] point);

	/**
	 * Melepaskan cengkeraman ke titik.
	 */
	public void release() {
		grabbed = false;
	}

	/**
	 * Mencoba menggeser titik ke koordinat tertentu. Perpidahan akan
	 * dikalibrasi berdasarkan posisi terakhir titik berhasil dicengkeram.
	 * Posisi titik akan bergantung dari tipe titik.
	 * @param x koordinat sumbu x.
	 * @param y koordinat sumbu y.
	 */
	abstract void moveTo(float[] point);

	/**
	 * Memindahkan titik ke koordinat tertentu. Tidak tergantung dari tipe titik
	 * ini.
	 * @param x koordinat x relatif terhadap pusat objek.
	 * @param y koordinat y relatif terhadap pusat objek.
	 */
	void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Menggambar titik kontrol pada suatu kanvas.
	 * @param canvas
	 */
	public abstract void draw(Canvas canvas);
}
