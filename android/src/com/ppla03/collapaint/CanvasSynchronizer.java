package com.ppla03.collapaint;

import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

import com.ppla03.collapaint.conn.CanvasConnector;
import com.ppla03.collapaint.model.action.CopyAction;
import com.ppla03.collapaint.model.action.DeleteAction;
import com.ppla03.collapaint.model.action.DeleteMultiple;
import com.ppla03.collapaint.model.action.DrawAction;
import com.ppla03.collapaint.model.action.DrawMultiple;
import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.CanvasObject;

public class CanvasSynchronizer {
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

	public CanvasSynchronizer(CanvasView canvas) {
		this.canvas = canvas;
		connector = CanvasConnector.getInstance();
		connector.setSynchronizer(this);
		actionBuffer = new ArrayList<>();
		playbackList = new ArrayList<>();
		sentList = new ArrayList<>();
		handler = new Handler();
	}

	public void start() {
		Log.d("POS", "----- synchronizer started ------");
		handler.postDelayed(update, sync_time);
	}

	public void stop() {
		// TODO
	}

	public void forceUpdate() {
		// TODO
	}

	private final Runnable update = new Runnable() {

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
				} else if (act instanceof CopyAction) {
					objs = ((DeleteMultiple) act).objects;
					int len = objs.size();
					for (int j = 0; j < len; j++)
						sentList.add(new DrawAction(objs.get(j)));
				} else
					sentList.add(act);
			}
			actionBuffer.clear();
			syncing = true;
			connector.updateActions(lastActNum, sentList);
		}
	};

	public void test() {
		// TODO
		update.run();
	}

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

	public void onActionUpdated(int newId, ArrayList<UserAction> actions) {
		playbackList.clear();
		int c = actionBuffer.size() - 1;
		for (int i = c; i >= 0; i++)
			playbackList.add(actionBuffer.get(i).getInverse());
		c = sentList.size() - 1;
		for (int i = c; i >= 0; i++)
			playbackList.add(sentList.get(i).getInverse());
		c = actions.size();
		playbackList.addAll(actions);
		playbackList.addAll(actionBuffer);
		syncing = false;
		canvas.execute(playbackList);
		lastActNum = newId;
		handler.postDelayed(update, sync_time);
		sentList.clear();
	}
}
