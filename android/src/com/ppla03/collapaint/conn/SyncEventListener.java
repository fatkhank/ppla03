package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.action.UserAction;

/**
 * Listener event dari {@link CanvasConnector} yang berkaitan dengan kegiatan
 * sinkronisasi kanvas.
 * @author hamba v7
 * 
 */
public interface SyncEventListener {

	/**
	 * Dipicu saat terjadi permasalahan pada saat update aksi dijalankan.
	 * @param status
	 */
	void onActionUpdatedFailed(int status);

	/**
	 * Dipicu saat update aksi telah dijalankan.
	 * @param lastActionNumber
	 * @param replyActions
	 */
	void onActionUpdated(int lastActionNumber,
			ArrayList<UserAction> replyActions);

}
