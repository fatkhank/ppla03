package com.ppla03.collapaint.conn;

import com.ppla03.collapaint.model.Participation;

public interface InvitationResponseListener {
	/**
	 * User memang sudah bergabung
	 */
	int ALREADY_JOINED = 4;

	/**
	 * Dipicu saat ada jawaban dari server terkait response user terhadap
	 * undangan.
	 * @param invitation undangan yang diresponse
	 * @param status status. Berhasil jika berisi
	 *            {@link ServerConnector#SUCCESS} atau {@link #ALREADY_JOINED}
	 *            jika ternyata user memeang telah bergabung.<br/>
	 *            Gagal bila berisi {@link ServerConnector#INTERNAL_PROBLEM},
	 *            {@link ServerConnector#CONNECTION_PROBLEM},
	 *            {@link ServerConnector#SERVER_PROBLEM}, atau
	 *            {@link ServerConnector#UNKNOWN_REPLY}.
	 * 
	 */
	void onResponse(Participation invitation, int status);
}
