package com.ppla03.collapaint;

/**
 * Listener untuk event-event dari {@link CanvasView}
 * @author hamba v7
 * 
 */
public interface CanvasListener {

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
}
