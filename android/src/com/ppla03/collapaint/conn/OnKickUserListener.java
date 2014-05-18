package com.ppla03.collapaint.conn;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

/**
 * Listener saat proses kick user mendapat jawaban dari server.
 * @author hamba v7
 *
 */
public interface OnKickUserListener {
	/**
	 * Dipicu saat operasi membuang partisipan selesai.
	 * @param user partisipan yang dibuang
	 * @param model kanvas yang dimaksud
	 * @param status hasil operasi. Berhasil bila berisi
	 *            {@link ServerConnector#SUCCESS}, atau {@link #NOT_A_MEMBER}
	 *            jika yang dikick tidak terdaftar sebagai member;<br/>
	 *            Gagal bila berisi {@link ServerConnector#INTERNAL_PROBLEM},
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#SERVER_PROBLEM}, atau
	 *            {@link ServerConnector#UNKNOWN_REPLY}.
	 */
	void onKickUser(UserModel user, CanvasModel model, int status);
}