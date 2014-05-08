package com.ppla03.collapaint;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.conn.CanvasConnector.OnCanvasOpenListener;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.conn.SyncEventListener;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;

/**
 * Mengurusi proses pemuatan dan sinkronisasi kanvas.
 * @author hamba v7
 * 
 */
public class CanvasSynchronizer implements SyncEventListener,
		DialogInterface.OnClickListener, OnCanvasOpenListener {
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
		// TODO close listener
		void onCanvasClosed(CanvasModel model, int status);
	}

	private CanvasLoadListener loadListener;
	private CanvasModel currentModel;

	private AlertDialog hideModeDialog;
	private CanvasView view;
	private CanvasConnector connector;
	private int lastActNum;

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

	private int sync_time = 5000;

	private int mode;
	private final int IDLE = 1;
	private final int SYNCING = 2;
	private final int FORCED = 4;
	private final int LOADING = 7;
	private final int STOP = 16;

	private CanvasSynchronizer() {
		actionBuffer = new ArrayList<>();
		playbackList = new ArrayList<>();
		sentList = new ArrayList<>();
		handler = new Handler();
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
		if (currentModel == null) {
			// TODO dummy canvas
			UserModel owner = new UserModel();
			owner.collaID = 1;
			owner.accountID = "accountofdebugger";
			owner.name = "debugger";
			currentModel = new CanvasModel(owner, "test canvas", 800, 400);
			currentModel.setid(1);
			listener.onCanvasLoaded(currentModel, ServerConnector.SUCCESS);
		} else {
			this.loadListener = listener;
			lastActNum = 0;
			actionBuffer.clear();
			playbackList.clear();
			sentList.clear();
			mode = LOADING;
			updater.run();
			connector.openCanvas(CollaUserManager.getCurrentUser().collaID,
					currentModel, this);
		}
	}

	public void closeCanvas(CanvasCloseListener listener) {
		// TODO close canvas

	}

	public void setCanvasView(CanvasView canvas) {
		// TODO onset set view
		AlertDialog.Builder builder = new AlertDialog.Builder(
				canvas.getContext());
		builder.setMessage("There is a connection problem. Change to Hide Mode?");
		hideModeDialog = builder.setPositiveButton("YES", this)
				.setNegativeButton("NO", this).setCancelable(false).create();
		this.view = canvas;
		canvas.open(currentModel);
		canvas.execute(actionBuffer);
		actionBuffer.clear();
		if (!canvas.isInHideMode()) {
			// TODO debug sync
			Log.d("POS", "----- synchronizer started ------");
			handler.postDelayed(updater, sync_time);
		}
	}

	/**
	 * Memberhentikan proses sinkronisasi
	 */
	public void stop() {
		mode |= STOP;
	}

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
			connector.updateActions(currentModel.getId(), lastActNum, sentList);
		}
	};

	/**
	 * Menambahkan aksi ke penampung aksi
	 * @param action
	 */
	public void addToBuffer(UserAction action) {
		if (actionBuffer.isEmpty())
			actionBuffer.add(action);
		else {
			int last = actionBuffer.size() - 1;
			UserAction act = actionBuffer.get(last);
			if (action.inverseOf(act))
				actionBuffer.remove(last);
			else if (action.overwrites(act))
				actionBuffer.set(last, action);
			else
				actionBuffer.add(action);
		}
	}

	@Override
	public void onActionUpdated(int newId, ArrayList<AtomicAction> actions) {
		if (mode == LOADING) {
			Log.d("POS", "canvasLoaded");
			mode = IDLE;
			actionBuffer.addAll(actions);
			loadListener.onCanvasLoaded(currentModel, ServerConnector.SUCCESS);
		} else {
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
	}

	@Override
	public void onActionUpdateFailed(int status) {
		if ((mode & LOADING) == LOADING) {
			mode &= ~LOADING;
			loadListener.onCanvasLoaded(currentModel, status);
		} else if (status == ServerConnector.CONNECTION_PROBLEM
				|| status == ServerConnector.SERVER_PROBLEM) {
			if (!view.isInHideMode())
				hideModeDialog.show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			view.setHideMode(true);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			handler.postDelayed(updater, sync_time);
		}
	}

	@Override
	public void onCanvasClosed(CanvasModel model, int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCanvasOpened(int status, int lan) {
		if (status == ServerConnector.SUCCESS)
			this.lastActNum = lan;
		loadListener.onCanvasLoaded(currentModel, status);
	}
}
