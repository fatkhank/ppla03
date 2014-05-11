package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.UserModel;

/**
 * Antarmuka aksi yang berkaitan dengan pengelolaan partisipan
 * @author hamba v7
 * 
 */
public interface ManageParticipantListener {
	/**
	 * Undangan yang sama sudah dibuat.
	 */
	public static final int ALREADY_INVITED = 4;
	/**
	 * User sudah bergabung ke kanvas.
	 */
	public static final int ALREADY_JOINED = 6;

	/**
	 * User yang dikick tidak tergabung dalam canvas yang bersangkutan.
	 */
	public static final int NOT_A_MEMBER = 9;

	/**
	 * Dipicu saat operasi pengambilan daftar partisipan telah berhasil.
	 * @param canvas kanvas yang dimaksud
	 * @param owner pemilik kanvas
	 * @param participants daftar partisipan selain pemilik kanvas
	 */
	void onParticipantFetched(CanvasModel canvas,
			ArrayList<Participation> participants);

	/**
	 * Dipicu saat operasi pengambilan daftar partisipan gagal.
	 * @param model kanvas yang dimaksud
	 * @param status status kesalahan
	 */
	void onParticipationFetchedFailed(CanvasModel model, int status);

	/**
	 * Dipicu saat operasi menambahkan partisipan selesai.
	 * @param accountId id akun yang diundang
	 * @param model kanvas yang dimaksud
	 * @param status hasil operasi. Berhasil bila berisi
	 *            {@link ServerConnector#SUCCESS}, {@link #ALREADY_INVITED},
	 *            atau {@link #ALREADY_JOINED} <br/>
	 *            Gagal bila berisi {@link ServerConnector#INTERNAL_PROBLEM},
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#SERVER_PROBLEM}, atau
	 *            {@link ServerConnector#UNKNOWN_REPLY}.
	 */
	void onInviteUser(String accountId, CanvasModel model, int status);

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
