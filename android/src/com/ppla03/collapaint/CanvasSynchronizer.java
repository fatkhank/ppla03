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
	private AlertDialog hideModeDialog;
	private CanvasView canvas;
	private CanvasConnector connector;
	private int lastActNum;

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
	private boolean syncing;
	private boolean forced;
	private boolean loading;

	public CanvasSynchronizer(CanvasView canvas, Context context) {
		this.canvas = canvas;
		connector = CanvasConnector.getInstance().setSyncListener(this);
		actionBuffer = new ArrayList<>();
		playbackList = new ArrayList<>();
		sentList = new ArrayList<>();
		handler = new Handler();
		loading = false;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("There is a connection problem. Change to Hide Mode?");
		builder.setPositiveButton("YES", this);
		builder.setNegativeButton("BACK", this);
		hideModeDialog = builder.create();
	}

	public void loadCanvas(CanvasModel model) {
		lastActNum = 0;
		actionBuffer.clear();
		playbackList.clear();
		sentList.clear();
		syncing = false;
		forced = false;
		loading = true;
		updater.run();
	}

	public void start() {
		Log.d("POS", "----- synchronizer started ------");
		handler.postDelayed(updater, sync_time);
	}

	public void stop() {
		// TODO
	}

	public void forceUpdate() {
		forced = true;
		if (!syncing)
			updater.run();
	}

	public void test() {
		// TODO test
		updater.run();
	}

	private final Runnable updater = new Runnable() {

		@Override
		public void run() {
			Log.d("POS", "CSYNC:update (" + actionBuffer.size() + ")");
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
					MoveMultiple tm = (MoveMultiple) act;
					objs = tm.objects;
					int len = objs.size();
					for (int j = 0; j < len; j++)
						sentList.add(new MoveAction(objs.get(j))
								.setParameter(tm.getParameter()));
				} else
					sentList.add(act);
			}
			actionBuffer.clear();
			syncing = true;
			connector.updateActions(canvas.getModel().getId(), lastActNum,
					sentList);
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
		Log.d("POS", "csync:" + newId);

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
		syncing = false;
		canvas.execute(playbackList);
		lastActNum = newId;
		sentList.clear();

		if (loading) {
			loading = false;
			canvas.onCanvasLoaded(ServerConnector.SUCCESS);
		} else if (forced) {
			forced = false;
			updater.run();
		} else
			handler.postDelayed(updater, sync_time);

		Log.d("POS", "csync finished:" + lastActNum);
	}

	@Override
	public void onActionUpdatedFailed(int status) {
		// TODO Auto-generated method stub
		if (loading) {
			loading = false;
			// TODO failed loading
		} else if (status == ServerConnector.CONNECTION_PROBLEM
				|| status == ServerConnector.SERVER_PROBLEM) {
			hideModeDialog.show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			canvas.setHideMode(true);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			// TODO back to canvas list
		}
	}
}
