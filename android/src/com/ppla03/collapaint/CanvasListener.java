package com.ppla03.collapaint;

import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;

/**
 * Listener untuk event-event dari {@link CanvasView}
 * @author hamba v7
 * 
 */
public interface CanvasListener {

	/**
	 * Dipicu saat proses memuat kanvas selesai.
	 * @param model Model kanvas yang dimuat
	 * @param status berhasil atau tidak. <br/>
	 *            {@link ServerConnector#SUCCESS} berarti berhasil.<br/>
	 *            Status berikut berarti gagal:
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#SERVER_PROBLEM},
	 *            {@link ServerConnector#INTERNAL_PROBLEM}.
	 */
	void onCanvasModelLoaded(CanvasModel model, int status);

	/**
	 * Dipicu saat hidden mode berubah.
	 * @param hidden
	 */
	void onHideModeChange(boolean hidden);

	/**
	 * Dipicu saat ada operasi seleksi terjadi di kanvas.
	 * @param success kalau ada seleksi yang terjadi di kanvas.
	 */
	void onSelectionEvent(boolean success);

	/**
	 * Dipicu saat ada perubahan status undo dan redo.
	 * @param undoable apakah bisa melakukan undo atau tidak.
	 * @param redoable apakah bisa melakukan redo atau tidak.
	 */
	void onURStatusChange(boolean undoable, boolean redoable);

	
	void onWaitForApproval();
}
