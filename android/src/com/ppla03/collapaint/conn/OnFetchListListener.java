package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

/**
 * Listener untuk pemanggilan daftar kanvas oleh seorang pengguna.
 * @author hamba v7
 * 
 */
public interface OnFetchListListener {

	/**
	 * Daftar kanvas berhasil dipanggil.
	 * @param asker pengguna yang meminta
	 * 
	 * @param status hasil pemainggilan daftar kanvas, dapat berisi
	 *            {@link ServerConnector#SUCCESS},
	 *            {@link ServerConnector#INTERNAL_PROBLEM},
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#UNKNOWN_REPLY}, atau
	 *            {@link ServerConnector#SERVER_PROBLEM}
	 * 
	 * @param owned daftar kanvas yang dimiliki oleh {@code asker}
	 * 
	 * @param oldList daftar kanvas yang {@code asker} ikut sebagai partisipan
	 *            dan sudah pernah dibuka
	 * 
	 * @param invited daftar kanvas yang {@code asker} diundang sebagai
	 *            pertisipan, namun belum pernah dibuka
	 */
	void onListFethed(UserModel asker, int status,
			ArrayList<CanvasModel> owned, ArrayList<CanvasModel> oldList,
			ArrayList<CanvasModel> invited);
}
