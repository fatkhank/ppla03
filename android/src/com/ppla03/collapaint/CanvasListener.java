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
	 * Dipicu saat hidden mode berubah.
	 * @param hidden
	 */
	void onHideModeChange(boolean hidden);

	/**
	 * Dipicu saat ada operasi seleksi terjadi di kanvas.
	 * @param success kalau ada seleksi yang terjadi di kanvas.
	 * @param selected jumlah objek yang diseleksi
	 */
	void onSelectionEvent(boolean success, int selected);

	/**
	 * Dipicu saat ada perubahan status undo dan redo.
	 * @param undoable apakah bisa melakukan undo atau tidak.
	 * @param redoable apakah bisa melakukan redo atau tidak.
	 */
	void onURStatusChange(boolean undoable, boolean redoable);

	void onWaitForApproval();

	/**
	 * Saat user mulai menggambar {@link LineObject} atau {@link FreeObject}
	 */
	void onBeginDraw();
}
