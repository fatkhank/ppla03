package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Aksi memindahkan kumpulan objek kanvas. Kebalikan dari aksi ini adalah aksi
 * {@link MoveMultiple} pada arah kebalikannya.
 * @author hamba v7
 * 
 */
public class MoveMultiple extends UserAction {
	private static final int OFFSET_X = 0, OFFSET_Y = 1;

	/**
	 * Daftar parameter perpindahan, berisi offsetX dan offsetY.
	 */
	private final int[] trans = new int[2];

	/**
	 * Daftar objek yang dipindah.
	 */
	public final ArrayList<CanvasObject> objects;

	/**
	 * Koordinat x awal saat aksi menggeser dimulai.
	 */
	private static int anchorX;
	/**
	 * Koordinat y awal saat aksi menggeser dimulai.
	 */
	private static int anchorY;
	/**
	 * Koordinat x akhir saat objek selesai digeser.
	 */
	private static int finalX;
	/**
	 * Koordinat y akhir saat objek selesai digeser.
	 */
	private static int finalY;

	/**
	 * Membuat aksi {@link MoveMultiple}. Penyimpanan daftar objek akan merujuk
	 * ke parameter yang diberikan untuk menghemat memori.
	 * @param objects daftar kumpulan objek kanvas yang dikenai aksi.
	 * @param inverse inverse dari aksi ini.
	 */
	private MoveMultiple(ArrayList<CanvasObject> objects, MoveMultiple inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	/**
	 * Membuat aksi {@link MoveMultiple}. Penyimpanan daftar objek akan menyalin
	 * dari parameter yang diberikan, sehingga perubahan pada parameter daftar
	 * objek tidak akan merubah daftar objek yang telah tercatat oleh aksi ini.
	 * @param objects daftar kumpulan objek kanvas yang dikenai aksi.
	 * @param reversible apakah objek bisa dikembalikan seperti semula atau
	 *            tidak. Jika false maka tidak akan dibuatkan inverse dari aksi
	 *            sehingga aksi perubahan bersifat permanen.
	 */
	public MoveMultiple(ArrayList<CanvasObject> objects, boolean reversible) {
		this.objects = new ArrayList<CanvasObject>(objects);
		if (reversible) {
			MoveMultiple tm = new MoveMultiple(this.objects, this);
			tm.trans[OFFSET_X] = -this.trans[OFFSET_X];
			tm.trans[OFFSET_Y] = -this.trans[OFFSET_Y];
			this.inverse = tm;
		}
	}

	/**
	 * Mendapatkan parameter aksi perpindahan dalam bentuk {@link String}.
	 * @return parameter
	 */
	public String getParameter() {
		return MoveAction.encode(trans[OFFSET_X], trans[OFFSET_Y]);
	}

	/**
	 * Menandai posisi sekarang sebagai titik acuan untuk memindah objek.
	 * @param x koordinat x titik acuan.
	 * @param y koordinat y titik acuan.
	 * @return this.
	 */
	public MoveMultiple anchorDown(int x, int y) {
		anchorX = x;
		anchorY = y;
		return this;
	}

	/**
	 * Memindahkan objek ke posisi tertentu.
	 * @param x koordinat x titik tujuan.
	 * @param y koordinat y titik tujuan.
	 * @return this.
	 */
	public MoveMultiple moveTo(int x, int y) {
		finalX = x;
		finalY = y;
		return this;
	}

	/**
	 * Mencatat perpindahan yang telah dilakukan. Aksi ini akan mengakumulasikan
	 * perpindahan yang telah dilakukan sejak aksi ini dibuat.
	 * @return aksi yang menyimpan pergeseran sejak anchorDown().
	 */
	public MoveMultiple anchorUp() {
		int dx = finalX - anchorX;
		int dy = finalY - anchorY;
		trans[OFFSET_X] += dx;
		trans[OFFSET_Y] += dy;
		MoveMultiple mi = (MoveMultiple) inverse;
		mi.trans[OFFSET_X] = -trans[OFFSET_X];
		mi.trans[OFFSET_Y] = -trans[OFFSET_Y];

		MoveMultiple mm = new MoveMultiple(objects, null);
		mm.trans[OFFSET_X] = dx;
		mm.trans[OFFSET_Y] = dy;
		MoveMultiple mmi = new MoveMultiple(objects, mm);
		mmi.trans[OFFSET_X] = -dx;
		mmi.trans[OFFSET_Y] = -dy;
		mm.inverse = mmi;
		return mm;
	}

	public int offsetX() {
		return trans[OFFSET_X];
	}

	public int offsetY() {
		return trans[OFFSET_Y];
	}

	/**
	 * Menjalankan aksi ini pada kumpulan objek yang terdaftar.
	 */
	public void apply() {
		int dx = trans[OFFSET_X];
		int dy = trans[OFFSET_Y];
		int size = objects.size();
		for (int i = 0; i < size; i++)
			objects.get(i).offset(dx, dy);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		if (action == null || !(action instanceof MoveMultiple))
			return false;
		MoveMultiple tm = (MoveMultiple) action;
		if ((tm.trans[OFFSET_X] != -trans[OFFSET_X])
				|| (tm.trans[OFFSET_Y] != -trans[OFFSET_Y]))
			return false;
		return tm.objects.equals(objects);
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null) {
			if (action instanceof MoveMultiple) {
				MoveMultiple tm = (MoveMultiple) action;
				return tm.objects.equals(objects);
			} else if (action instanceof MoveAction) {
				MoveAction ma = (MoveAction) action;
				return objects.contains(ma.object);
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "MM [" + trans[OFFSET_X] + "] [" + trans[OFFSET_Y] + "]";
	}
}
