package com.ppla03.collapaint.model.action;

/**
 * Aksi-aksi yang bisa dilakukan pengguna berkaitan dengan aksi di kanvas.
 * @author hamba v7
 * 
 */
public abstract class UserAction {

	protected UserAction inverse;

	/**
	 * Mendapatkan aksi yang dapat mengembalikan perubahan oleh aksi ini.
	 * @return inverse.
	 */
	public abstract UserAction getInverse();

	/**
	 * Mengecek apakah aksi ini dapat mengembalikan perubahan oleh suatu aksi.
	 * @param action aksi lain.
	 * @return jika true, berarti jika aksi ini dilakukan setelah aksi lain
	 *         dilakukan, efek yang terjadi adalah seperti kedua aksi tidak
	 *         pernah dilakukan pada objek.
	 */
	public abstract boolean inverseOf(UserAction action);

	/**
	 * Mengecek apakah aksi ini menimpa aksi yang lain.
	 * @param action aksi lain.
	 * @return jika true, efek yang ditimbulkan jika kedua aksi dijalankan
	 *         adalah seperti menjalankan aksi ini tanpa menjalankan aksi yang
	 *         lain.
	 */
	public abstract boolean overwrites(UserAction action);

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
