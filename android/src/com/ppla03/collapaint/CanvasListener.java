package com.ppla03.collapaint;

import com.ppla03.collapaint.model.object.FreeObject;
import com.ppla03.collapaint.model.object.LineObject;

/**
 * Listener untuk event-event dari {@link CanvasView}
 * @author hamba v7
 * 
 */
public interface CanvasListener {
	/**
	 * Saat memasuki mode select
	 */
	int ENTER_MODE = 4;
	/**
	 * Saat keluar dari mode seleksi
	 */
	int EXIT_MODE = 8;
	/**
	 * Saat ada sebuah objek yang diseleksi
	 */
	int EDIT_OBJECT = 32;
	/**
	 * Menyeleksi banyak objek
	 */
	int EDIT_MULTIPLE = 64;

	/**
	 * Dipicu saat hidden mode berubah.
	 * @param hidden
	 */
	void onHideModeChange(boolean hidden);

	/**
	 * Dipicu saat ada operasi seleksi terjadi di kanvas. Jika {@code event} =
	 * {@link CanvasListener#EDIT_OBJECT}, maka param berisi kode objek (
	 * {@link CanvasView.ObjectType}). Jika {@code event} =
	 * {@link CanvasListener#EDIT_MULTIPLE}, maka param berisi jumlah objek yang
	 * terseleksi.
	 * @param event kode event, {@link CanvasListener#ENTER_MODE},
	 *            {@link CanvasListener#EDIT_OBJECT}, atau
	 *            {@link CanvasListener#EDIT_MULTIPLE}
	 * @param param parameter seleksi
	 */
	void onSelectionEvent(int event, int param);

	/**
	 * Dipicu saat ada perubahan status undo dan redo.
	 * @param undoable apakah bisa melakukan undo atau tidak.
	 * @param redoable apakah bisa melakukan redo atau tidak.
	 */
	void onURStatusChange(boolean undoable, boolean redoable);

	/**
	 * Dipicu saat kanvas meminta persetujuan atas perubahan yang terjadi.
	 */
	void onWaitForApproval();

	/**
	 * Saat user mulai menggambar {@link LineObject} atau {@link FreeObject}
	 */
	void onBeginDraw();
}
