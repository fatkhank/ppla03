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
	/**
	 * Aksi menggerakan kumpulan objek ke arah tertentu. Aksi ini hanya
	 * menyimpan pergeseran yang terjadi, tanpa menyimpan posisi objek yang
	 * sebenarnya.
	 * @author hamba v7
	 * 
	 */
	public static class MoveStepper extends UserAction {
		MoveMultiple parent;
		float ofx;
		float ofy;

		MoveStepper(MoveMultiple main, float dx, float dy) {
			this.parent = main;
			this.ofx = dx;
			this.ofy = dy;
		}

		@Override
		public UserAction getInverse() {
			return inverse;
		}

		@Override
		public boolean inverseOf(UserAction action) {
			return false;
		}

		@Override
		public boolean overwrites(UserAction action) {
			return false;
		}

		/**
		 * Memindah objek ke tempat yang seharusnya.
		 */
		public void execute() {
			int size = parent.objects.size();
			for (int i = 0; i < size; i++) {
				parent.transX[i] += ofx;
				parent.transY[i] += ofy;
			}
			parent.apply();
		}
	}

	/**
	 * Daftar parameter tranformasi objek pada sumbu x.
	 */
	private final float[] transX;

	/**
	 * Daftar parameter tranformasi objek pada sumbu y.
	 */
	private final float[] transY;

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
	 * ke parameter yang diberikan untuk menghemat memori. Akan otomatis
	 * menyimpanan parameter ofset tiap objek.
	 * @param objects daftar kumpulan objek kanvas yang dikenai aksi.
	 * @param inverse inverse dari aksi ini.
	 */
	private MoveMultiple(ArrayList<CanvasObject> objects, MoveMultiple inverse) {
		this.objects = objects;
		this.inverse = inverse;
		this.transX = new float[inverse.transX.length];
		this.transY = new float[inverse.transY.length];
		System.arraycopy(inverse.transX, 0, this.transX, 0, transX.length);
		System.arraycopy(inverse.transY, 0, this.transY, 0, transY.length);
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
		this.transX = new float[objects.size()];
		this.transY = new float[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			CanvasObject co = objects.get(i);
			transX[i] = co.offsetX();
			transY[i] = co.offsetY();
		}
		if (reversible)
			this.inverse = new MoveMultiple(this.objects, this);
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
	public MoveStepper anchorUp() {
		int dx = finalX - anchorX;
		int dy = finalY - anchorY;

		MoveStepper forward = new MoveStepper(this, dx, dy);
		MoveStepper backward = new MoveStepper(this, -dx, -dy);
		forward.inverse = backward;
		backward.inverse = forward;

		return forward;
	}

	/**
	 * Menjalankan aksi ini pada kumpulan objek yang terdaftar.
	 */
	public void apply() {
		int size = objects.size();
		for (int i = 0; i < size; i++)
			objects.get(i).offsetTo(transX[i], transY[i]);
	}

	public int getMoveActions(ArrayList<UserAction> buffer) {
		int size = objects.size();
		for (int i = 0; i < size; i++)
			buffer.add(new MoveAction(objects.get(i)).setParameter(transX[i],
					transY[i]));
		return size;
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		return this.inverse == action;
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
		return "MM";
	}
}