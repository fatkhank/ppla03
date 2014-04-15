package com.ppla03.collapaint;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.conn.SyncEventListener;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

public class CanvasSynchronizer implements SyncEventListener,
		DialogInterface.OnClickListener {
	public static interface CanvasLoadListener {
		void onCanvasLoaded(CanvasModel model, int status);
	}

	private CanvasLoadListener listener;
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
	private final ArrayList<UserAction> sentList;

	private Handler handler;

	private int sync_time = 5000;

	private int mode;
	private final int IDLE = 1;
	private final int SYNCING = 2;
	private final int FORCED = 4;
	private final int LOADING = 7;
	private final int STOP = 16;

	private CanvasSynchronizer() {
		actionBuffer = new ArrayList<UserAction>();
		playbackList = new ArrayList<UserAction>();
		sentList = new ArrayList<UserAction>();
		handler = new Handler();
		mode = IDLE;
	}

	public static CanvasSynchronizer getInstance() {
		if (instance == null)
			instance = new CanvasSynchronizer();
		return instance;
	}

	public CanvasSynchronizer setCanvas(CanvasModel model) {
		currentModel = model;
		return this;
	}

	public void loadCanvas(CanvasLoadListener listener) {
		Log.d("POS", "loadCanvas");
		if (connector == null)
			connector = CanvasConnector.getInstance().setSyncListener(this);
		this.listener = listener;
		lastActNum = 0;
		actionBuffer.clear();
		playbackList.clear();
		sentList.clear();
		// TODO debug load canvas
		// mode = LOADING;
		// updater.run();
		listener.onCanvasLoaded(currentModel, 1);
	}

	public void setCanvasView(CanvasView canvas) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				canvas.getContext());
		builder.setMessage("There is a connection problem. Change to Hide Mode?");
		builder.setPositiveButton("YES", this);
		builder.setNegativeButton("NO", this);
		hideModeDialog = builder.create();
		this.view = canvas;
		canvas.open(currentModel);
		canvas.execute(actionBuffer);
		actionBuffer.clear();
		Log.d("POS", "----- synchronizer started ------");
		handler.postDelayed(updater, sync_time);
	}

	public void stop() {
		mode |= STOP;
	}

	public void forceUpdate() {
		mode |= FORCED;
		if ((mode & SYNCING) != SYNCING)
			updater.run();
	}

	public void markCheckpoint() {
		checkPoint = lastActNum;
	}

	public void revert() {
		lastActNum = checkPoint;
		forceUpdate();
	}

	private final Runnable updater = new Runnable() {

		@Override
		public void run() {
			// add buffer to sentList
			int size = actionBuffer.size();
			for (int i = 0; i < size; i++) {
				UserAction act = actionBuffer.get(i);
				// ubah aksi berobjek jamak ke aksi berobjek tunggal
				ArrayList<CanvasObject> objs = null;
				if (act instanceof DrawMultiple) {
					objs = ((DrawMultiple) act).objects;
					int len = objs.size();
					for (int j = 0; j < len; j++)
						sentList.add(new DrawAction(objs.get(j)));
				} else if (act instanceof DeleteMultiple) {
					objs = ((DeleteMultiple) act).objects;
					int len = objs.size();
					for (int j = 0; j < len; j++)
						sentList.add(new DeleteAction(objs.get(j)));
				} else if (act instanceof MoveMultiple) {
					((MoveMultiple) act).getMoveActions(sentList);
				} else
					sentList.add(act);
			}
			actionBuffer.clear();
			mode |= SYNCING;
			connector.updateActions(currentModel.getId(), lastActNum, sentList);
		}
	};

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
	public void onActionUpdated(int newId, ArrayList<UserAction> actions) {
		if (mode == LOADING) {
			Log.d("POS", "canvasLoaded");
			mode = IDLE;
			actionBuffer.addAll(actions);
			listener.onCanvasLoaded(currentModel, ServerConnector.SUCCESS);
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
	public void onActionUpdatedFailed(int status) {
		if ((mode & LOADING) == LOADING) {
			mode &= ~LOADING;
			listener.onCanvasLoaded(currentModel, status);
		} else if (status == ServerConnector.CONNECTION_PROBLEM
				|| status == ServerConnector.SERVER_PROBLEM) {
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
}
