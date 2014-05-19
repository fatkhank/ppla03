package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.AtomicAction;

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
	void onActionUpdateFailed(int status);

	/**
	 * Dipicu saat update aksi telah dijalankan.
	 * @param lastActionNumber
	 * @param replyActions
	 */
	void onActionUpdated(int lastActionNumber,
			ArrayList<AtomicAction> replyActions);

	/**
	 * Dipicu saat proses penutupan kanvas berhasil
	 * @param status
	 */
	void onCanvasClosed(int status);

	/**
	 * Apakah menerima paket sinkronisasi dengan suatu lastActionNumber
	 * tertentu.
	 * @param oldLan lan yang tadi dikirimkan dari paket
	 * @return
	 */
	boolean accept(int oldLan);
}
