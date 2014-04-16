package com.ppla03.collapaint.conn;

import com.ppla03.collapaint.model.CanvasModel;

/**
 * Listener hasil pembuatan sebuah kanvas.
 * @author hamba v7
 * 
 */
public interface OnCanvasCreateListener {
	/**
	 * Kanvas gagal dibuat karena owner pernah membuat kanvas dengan nama yang
	 * sama sudah pernah dibuat.
	 */
	int DUPLICATE_NAME = 32;

	/**
	 * Kanvas gagal dibuat karena owner tidak terdaftar.
	 */
	int USER_UNKNOWN = 128;

	/**
	 * Dipicu saat pembuatan sebuah kanvas mendapat jawaban dari server.
	 * @param newCanvas kanvas yang sedang dibuat.
	 * @param status hasil pembuatan.<br/>
	 *            {@link ServerConnector#SUCCESS} berarti kanvas berhasil
	 *            dibuat.<br/>
	 *            {@link OnCanvasCreateListener#DUPLICATE_NAME} berarti kanvas
	 *            dengan nama yang sama sudah pernah dibuat. <br/>
	 *            {@link OnCanvasCreateListener#USER_UNKNOWN} berarti owner
	 *            belum terdaftar. <br/>
	 *            Jika ada error lain, status akan berisi
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#SERVER_PROBLEM},
	 *            {@link ServerConnector#UNKNOWN_REPLY}, atau
	 *            {@link ServerConnector#INTERNAL_PROBLEM}
	 */
	void onCreated(CanvasModel newCanvas, int status);
}