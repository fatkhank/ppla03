package com.ppla03.collapaint;

import java.util.ArrayList;

import android.os.Handler;

import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.conn.CanvasConnector.OnCanvasOpenListener;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.conn.SyncEventListener;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.ui.CollaDialog;

/**
 * Mengurusi proses pemuatan dan sinkronisasi kanvas.
 * @author hamba v7
 * 
 */
public class CanvasSynchronizer implements SyncEventListener,
		CollaDialog.OnClickListener, OnCanvasOpenListener {

	private static final int DEFAULT_CANVAS_WIDTH = 800,
			DEFAULT_CANVAS_HEIGHT = 400, DEFAULT_CANVAS_ID = 1;
	private static final String DEFAULT_CANVAS_NAME = "Untitled";

	/**
	 * Listener proses memuat kanvas.
	 * @author hamba v7
	 * 
	 */
	public static interface CanvasLoadListener {
		/**
		 * Dipicu saat proses memuat kanvas selesai
		 * @param model kanvas yang dimuat
		 * @param status status, bisa berisi {@link ServerConnector#SUCCESS},
		 *            {@link ServerConnector#INTERNAL_PROBLEM},
		 *            {@link ServerConnector#SERVER_PROBLEM},
		 *            {@link ServerConnector#UNKNOWN_REPLY}, atau
		 *            {@link ServerConnector#INTERNAL_PROBLEM}
		 */
		void onCanvasLoaded(CanvasModel model, int status);
	}

	/**
	 * Listener proses menutup kanvas.
	 * @author hamba v7
	 * 
	 */
	public static interface CanvasCloseListener {
		/**
		 * Dipicu saat proses penutupan kanvas mendapat jawaban dari server.
		 * @param status
		 */
		void onCanvasClosed(int status);
	}

	private CanvasLoadListener loadListener;
	private CanvasCloseListener closeListener;
	private static CanvasModel currentModel;

	private CanvasView view;
	private CanvasConnector connector;
	private static int lastActNum;

	private static CanvasSynchronizer instance;
	/**
	 * Menyimpan lastactnum terakhir.
	 */
	private int checkPoint;

	/**
	 * Menampung aksi yang akan dikirim ke server
	 */
	private final ArrayList<UserAction> actionBuffer;
	/**
	 * Menampung aksi yang akan dieksekusi oleh kanvas.
	 */
	private final ArrayList<UserAction> playbackList;
	/**
	 * Daftar aksi yang sedang dikirim ke server
	 */
	private final ArrayList<AtomicAction> sentList;

	private Handler handler;

	/**
	 * Interval waktu untuk sinkronisasi
	 */
	private int sync_time = 500;

	private int mode;
	private final int IDLE = 1;
	private final int SYNCING = 2;
	private final int FORCED = 4;
	// private final int LOADING = 7;
	private final int STOP = 16;

	private CanvasSynchronizer() {
		actionBuffer = new ArrayList<>();
		playbackList = new ArrayList<>();
		sentList = new ArrayList<>();
		mode = IDLE;
	}

	public static CanvasSynchronizer getInstance() {
		if (instance == null)
			instance = new CanvasSynchronizer();
		return instance;
	}

	/**
	 * Mengatur model kanvas yang sedang disinkronisasi
	 * @param model
	 * @return
	 */
	public CanvasSynchronizer setCanvas(CanvasModel model) {
		currentModel = model;
		return this;
	}

	/**
	 * Memulai proses muat kanvas
	 * @param listener
	 */
	public void loadCanvas(CanvasLoadListener listener) {
		if (connector == null)
			connector = CanvasConnector.getInstance().setSyncListener(this);
		// buat handler baru
		handler = new Handler();
		if (currentModel == null) {
			// buat dummy kanvas kalau tidak ada
			UserModel owner = CollaUserManager.getCurrentUser();
			currentModel = new CanvasModel(owner, DEFAULT_CANVAS_NAME,
					DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
			currentModel.setid(DEFAULT_CANVAS_ID);
			listener.onCanvasLoaded(currentModel, ServerConnector.SUCCESS);
		} else {
			this.loadListener = listener;
			lastActNum = 0;
			actionBuffer.clear();
			playbackList.clear();
			sentList.clear();
			connector.openCanvas(CollaUserManager.getCurrentUser().collaID,
					currentModel, this);
		}
	}

	public void closeCanvas(CanvasCloseListener listener) {
		this.closeListener = listener;
		mode |= STOP;
		connector.closeCanvas(currentModel, CollaUserManager.getCurrentUser());
	}

	public boolean setCanvasView(CanvasView canvas) {
		if (currentModel == null)
			return false;
		this.view = canvas;
		canvas.open(currentModel);
		canvas.execute(actionBuffer);
		actionBuffer.clear();
		return true;
	}

	public void start() {
		android.util.Log.d("POS", "----- synchronizer started ------");
		mode = IDLE;
		if (handler == null)
			handler = new Handler();
		handler.postDelayed(updater, sync_time);
	}

	/**
	 * Memberhentikan proses sinkronisasi
	 */
	public void stop() {
		android.util.Log.d("POS", "----- synchronizer stopped ------");
		mode |= STOP;
	}

	/**
	 * Memerintahkan untuk melakukan sinkronasasi sekarang juga.
	 */
	public void forceUpdate() {
		mode |= FORCED;
		if ((mode & SYNCING) != SYNCING)
			updater.run();
	}

	/**
	 * Menandai checkpoin
	 */
	public void markCheckpoint() {
		checkPoint = lastActNum;
	}

	/**
	 * Mengembalikan aksi yang dilakukan pengguna
	 */
	public void revert() {
		lastActNum = checkPoint;
		forceUpdate();
	}

	private final Runnable updater = new Runnable() {

		@Override
		public void run() {
			// add buffer to sentList
			int size = actionBuffer.size();
			for (int i = 0; i < size; i++)
				actionBuffer.get(i).insertInAtomic(sentList);
			actionBuffer.clear();
			mode |= SYNCING;
			connector.updateActions(CollaUserManager.getCurrentUser().collaID,
					currentModel.getId(), lastActNum, sentList);
		}
	};

	/**
	 * Menambahkan aksi ke penampung aksi
	 * @param action
	 */
	public void addToBuffer(UserAction action) {
		if (action == null)
			return;
		// cek efek terhadap aksi terdahulu
		int i = actionBuffer.size() - 1;
		// masukkan aksi di belakang
		actionBuffer.add(action);
		while (i >= 0) {
			UserAction act = actionBuffer.get(i);
			if (action.inverseOf(act)) {
				// jika kedua aksi inverse -> hilangkan keduanya
				actionBuffer.remove(actionBuffer.size() - 1);
				actionBuffer.remove(i);
				break;
			} else if (action.overwrites(act)) {
				// jika aksi overwrite -> hilangkan yg lama
				actionBuffer.remove(i);
			}
			i--;
		}
	}

	@Override
	public void onActionUpdated(int newId, ArrayList<AtomicAction> actions) {
		playbackList.clear();

		// undo aksi yang dilakukan selama proses update
		int c = actionBuffer.size() - 1;
		for (int i = c; i >= 0; i--)
			playbackList.add(actionBuffer.get(i).getInverse());
		// undo aksi yang sedang diupdate
		c = sentList.size() - 1;
		for (int i = c; i >= 0; i--)
			playbackList.add(sentList.get(i).getInverse());
		// jalankan aksi dari server
		playbackList.addAll(actions);
		// redo aksi yang dilakukan selama proses update
		playbackList.addAll(actionBuffer);
		view.execute(playbackList);
		lastActNum = newId;
		sentList.clear();

		if ((mode & FORCED) == FORCED) {
			mode &= ~FORCED;
			updater.run();
		} else if ((mode & STOP) != STOP) {
			mode = IDLE;
			handler.postDelayed(updater, sync_time);
		}
	}

	@Override
	public void onActionUpdateFailed(int status) {
		if (status == ServerConnector.CONNECTION_PROBLEM
				|| status == ServerConnector.SERVER_PROBLEM) {
			if (!view.isInHideMode())
				CollaDialog.alert(view.getContext(), R.string.w_change2hide,
						"Yes", "No", this);
		}
	}

	@Override
	public void onClick(int which) {
		if (which == CollaDialog.YES) {
			view.setHideMode(true);
		} else if (which == CollaDialog.NO) {
			handler.postDelayed(updater, sync_time);
		}
	}

	@Override
	public void onCanvasOpened(int status, int lan) {
		if (status == ServerConnector.SUCCESS) {
			lastActNum = lan;
		}
		loadListener.onCanvasLoaded(currentModel, status);
	}

	@Override
	public void onCanvasClosed(int status) {
		closeListener.onCanvasClosed(status);
	}

	@Override
	public boolean accept(int oldLan) {
		return oldLan == lastActNum;
	}

	@Override
	public void onPacketDropped(int lan) {
		if ((mode & SYNCING) != SYNCING)
			updater.run();
	}

}
