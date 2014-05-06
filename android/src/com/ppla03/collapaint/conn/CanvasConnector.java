package com.ppla03.collapaint.conn;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.util.Log;

import collapaint.code.ActionCode;
import collapaint.code.ActionJCode;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

public class CanvasConnector extends ServerConnector {
	public static final class ObjectCode {
		public static final int RECT = 1;
		public static final int OVAL = 2;
		public static final int LINES = 3;
		public static final int POLYGON = 4;
		public static final int PATH = 5;
		public static final int TEXT = 6;
	}

	public static String COMMIT_URL = HOST + "action";

	private static CanvasConnector instance;
	private static SyncEventListener syncListener;
	private static OnCanvasOpenListener openListener;
	private static OnCanvasCloseListener closeListener;
	private ArrayList<AtomicAction> sentActions;
	private ArrayList<CanvasObject> sentObjects;
	private ArrayList<AtomicAction> replyActions;
	private ArrayList<CanvasObject> replyObjects;
	private HashMap<Integer, CanvasObject> objectMap;

	@SuppressLint("UseSparseArrays")
	private CanvasConnector() {
		sentActions = new ArrayList<>();
		replyActions = new ArrayList<>();
		sentObjects = new ArrayList<>();
		replyObjects = new ArrayList<>();
		objectMap = new HashMap<Integer, CanvasObject>();
	}

	public static CanvasConnector getInstance() {
		if (instance == null) {
			instance = new CanvasConnector();
		}
		return instance;
	}

	/**
	 * Listener untuk proses membuka suatu kanvas.
	 * @author hamba v7
	 * 
	 */
	public static interface OnCanvasOpenListener {
		/**
		 * Canvas sudah terbuka.
		 * @param model model kanvas
		 * @param status jika status == {@link ServerConnector#SUCCESS}, maka
		 *            {@code model} berisi data kanvas terbaru.
		 */
		void onCanvasOpened(CanvasModel model, int status);
	}

	public static interface OnCanvasCloseListener {
		void onCanvasClosed(CanvasModel model, int status);
	}

	public CanvasConnector setSyncListener(SyncEventListener cs) {
		syncListener = cs;
		return this;
	}

	public void updateActions(int canvasID, int lastActNum,
			ArrayList<AtomicAction> actions) {
		// encode action to json
		JSONObject msg = new JSONObject();
		try {
			// ------------------------ process actions ------------------------
			sentObjects.clear();
			sentActions.clear();
			JSONArray jarAct = new JSONArray();
			int size = actions.size();
			for (int i = 0; i < size; i++) {
				AtomicAction ua = actions.get(i);
				JSONObject joAct = new JSONObject();
				CanvasObject co = null;
				if (ua instanceof DrawAction) {
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.DRAW_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, "");
					co = ((DrawAction) ua).object;
				} else if (ua instanceof DeleteAction) {
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.DELETE_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, "");
					co = ((DeleteAction) ua).object;
				} else if (ua instanceof TransformAction) {
					TransformAction ta = (TransformAction) ua;
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.TRANSFORM_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, ta.getParameter());
					co = ta.object;
				} else if (ua instanceof GeomAction) {
					GeomAction ga = (GeomAction) ua;
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.GEOM_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, ga.getParameter());
					co = ga.object;
				} else if (ua instanceof StyleAction) {
					StyleAction sa = (StyleAction) ua;
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.STYLE_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, sa.getParameter());
					co = sa.object;
				}

				if (co.getGlobalID() == -1) {
					// if object is new and the action is draw, put in object
					// list
					if (ua instanceof DrawAction) {
						joAct.put(ActionJCode.ACTION_OBJ_LISTED, sentObjects.size());
						sentObjects.add(co);
					} else
						// if object is new but the action is not draw, find in
						// object list
						for (int j = 0; j < sentObjects.size(); j++) {
							if (sentObjects.get(j).privateID == co.privateID) {
								joAct.put(ActionJCode.ACTION_OBJ_LISTED, j);
								break;
							}
						}
				} else
					// if object is known, just put object's global id
					joAct.put(ActionJCode.ACTION_OBJ_KNOWN, co.getGlobalID());
				jarAct.put(joAct);
				sentActions.add(ua);
			}
			msg.put(ActionJCode.ACTION_LIST, jarAct);

			// ------------------------ process objects ------------------------
			JSONArray jarObj = new JSONArray();
			size = sentObjects.size();
			for (int i = 0; i < size; i++) {
				JSONObject joObj = new JSONObject();
				CanvasObject co = sentObjects.get(i);
				if (co instanceof RectObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.RECT);
				else if (co instanceof OvalObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.OVAL);
				else if (co instanceof LineObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.LINES);
				else if (co instanceof PolygonObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.POLYGON);
				else if (co instanceof FreeObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.PATH);
				else if (co instanceof TextObject)
					joObj.put(ActionJCode.OBJECT_CODE, ObjectCode.TEXT);
				joObj.put(ActionJCode.OBJECT_LOCAL_ID, co.privateID);
				joObj.put(ActionJCode.OBJECT_TRANSFORM,
						TransformAction.getParameterOf(co));
				joObj.put(ActionJCode.OBJECT_GEOM, GeomAction.getParameterOf(co));
				joObj.put(ActionJCode.OBJECT_STYLE, StyleAction.getParameterOf(co));
				jarObj.put(joObj);
			}
			msg.put(ActionJCode.OBJECT_LIST, jarObj);

			// ------------ lan ---------
			msg.put(ActionJCode.LAST_ACTION_NUM, lastActNum);
			msg.put(ActionJCode.CANVAS_ID, canvasID);

			// TODO debug ccon
			Log.d("POS", "send:" + msg.toString());

			new Client(COMMIT_URL, replisUpdate).execute(msg);
		} catch (JSONException ex) {
			replisUpdate.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ServerConnector.ReplyListener replisUpdate = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			try {
				if (status != SUCCESS) {
					syncListener.onActionUpdateFailed(status);
					return;
				}
				if (reply.has(ActionJCode.ERROR)) {
					int code = reply.getInt(ActionJCode.ERROR);
					if (code == ActionJCode.SERVER_ERROR)
						syncListener.onActionUpdateFailed(SERVER_PROBLEM);
					else if (code == ActionJCode.BAD_REQUEST)
						syncListener.onActionUpdateFailed(INTERNAL_PROBLEM);
				}
				// TODO debug ccon2
				Log.d("POS", "rep:" + reply.toString());

				// parse objects
				replyObjects.clear();
				JSONArray jarObj = reply.getJSONArray(ActionJCode.OBJECT_LIST);
				int length = jarObj.length();
				for (int i = 0; i < length; i++) {
					JSONObject joObj = jarObj.getJSONObject(i);
					int gid = joObj.getInt(ActionJCode.OBJECT_GLOBAL_ID);
					CanvasObject co;
					if (joObj.has(ActionJCode.OBJECT_LOCAL_ID)) {
						// if object is in sent objects, assign global id from
						// reply
						int oid = joObj.getInt(ActionJCode.OBJECT_LOCAL_ID);
						int j = 0;
						do {
							co = sentObjects.get(j++);
						} while (co.privateID != oid);
						co.setGlobaID(gid);
					} else {
						// if object is new create object from reply
						int code = joObj.getInt(ActionJCode.OBJECT_CODE);
						String shape = joObj.getString(ActionJCode.OBJECT_GEOM);
						String style = joObj.getString(ActionJCode.OBJECT_STYLE);
						String transform = joObj
								.getString(ActionJCode.OBJECT_TRANSFORM);
						co = createObject(code, shape, style, transform);
						objectMap.put(gid, co);
						co.setGlobaID(gid);
					}
					replyObjects.add(co);
				}

				// parse actions
				replyActions.clear();
				JSONArray jarAct = reply.getJSONArray(ActionJCode.ACTION_LIST);
				length = jarAct.length();
				for (int i = 0; i < length; i++) {
					JSONObject joAct = jarAct.getJSONObject(i);
					if (joAct.has(ActionJCode.ACTION_SUBMITTED)) {
						// if action is in sentActions, copy to replyActions
						int said = joAct.getInt(ActionJCode.ACTION_SUBMITTED);
						replyActions.add(sentActions.get(said));
					} else {
						// if action is new, create action from reply
						int code = joAct.getInt(ActionJCode.ACTION_CODE);
						String param = joAct.getString(ActionJCode.ACTION_PARAM);
						CanvasObject co = null;
						if (joAct.has(ActionJCode.ACTION_OBJ_LISTED))
							// if action object is listed, find in replyObject
							co = replyObjects.get(joAct
									.getInt(ActionJCode.ACTION_OBJ_LISTED));
						else
							// if action object is already known, find in
							// objectMap using global id
							co = objectMap.get(joAct
									.getInt(ActionJCode.ACTION_OBJ_KNOWN));
						if (co != null)
							replyActions.add(createAction(code, co, param));
					}

				}
				int lan = reply.getInt(ActionJCode.LAST_ACTION_NUM);

				syncListener.onActionUpdated(lan, replyActions);
			} catch (Exception e) {
				StackTraceElement[] ste = e.getStackTrace();
				Log.d("POS", "internal error");
				for (int i = 0; i < ste.length; i++) {
					Log.d("POS", i + ":" + ste[i]);
				}

				syncListener.onActionUpdateFailed(INTERNAL_PROBLEM);
			}
		}
	};

	/**
	 * Menyusun objek kanvas berdasarkan informasi dari server.
	 * @param code kode aksi. {@link ObjectCode}
	 * @param geom paameter geometri objek
	 * @param style
	 * @param transform
	 * @return
	 */
	CanvasObject createObject(int code, String geom, String style,
			String transform) {
		CanvasObject co = null;
		if (code == ObjectCode.RECT)
			co = new RectObject();
		else if (code == ObjectCode.OVAL)
			co = new OvalObject();
		else if (code == ObjectCode.LINES)
			co = new LineObject();
		else if (code == ObjectCode.POLYGON)
			co = new PolygonObject();
		else if (code == ObjectCode.PATH)
			co = new FreeObject();
		else if (code == ObjectCode.TEXT)
			co = new TextObject();
		GeomAction.apply(geom, co);
		TransformAction.applyTransform(transform, co);
		StyleAction.applyStyle(style, co);
		return co;
	}

	/**
	 * Menyusun aksi berdasarkan informasi dari server.
	 * @param code kode aksi {@link ActionCode}
	 * @param object objek yang diubah
	 * @param param parameter aksi
	 * @return aksi yang terbentuk
	 */
	private AtomicAction createAction(int code, CanvasObject object,
			String param) {
		if (code == ActionCode.DRAW_ACTION)
			return new DrawAction(object);
		else if (code == ActionCode.DELETE_ACTION)
			return new DeleteAction(object);
		else if (code == ActionCode.TRANSFORM_ACTION)
			return new TransformAction(object, false).setParameter(param);
		else if (code == ActionCode.GEOM_ACTION)
			return new GeomAction(object, param);
		else if (code == ActionCode.STYLE_ACTION)
			return new StyleAction(object, false).setParameter(param);
		return null;
	}

	public void openCanvas(CanvasModel canvas, OnCanvasOpenListener listener) {
		// TODO open canvas
		this.openListener = listener;
	}

	public void closeCanvas(CanvasModel canvas, UserModel user,
			OnCanvasCloseListener listener) {
		// TODO close int connector
		this.closeListener = listener;
	}
}
