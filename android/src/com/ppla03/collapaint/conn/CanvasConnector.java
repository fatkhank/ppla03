package com.ppla03.collapaint.conn;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;

import collapaint.code.ActionCode;
import collapaint.code.ActionJCode;
import collapaint.code.PortalJCode.Reply;
import collapaint.code.PortalJCode.Request;

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
	public static String PORTAL_URL = HOST + "portal";

	@Override
	protected void onHostAddressChange(String host) {
		COMMIT_URL = host + "action";
		PORTAL_URL = host + "portal";
	}

	private static CanvasConnector instance;
	private static SyncEventListener syncListener;
	private static OnCanvasOpenListener openListener;
	/**
	 * Menampung daftar aksi yang dikirim
	 */
	private ArrayList<AtomicAction> sentActions;
	/**
	 * Menampung daftar objek yang dikirim
	 */
	private ArrayList<CanvasObject> sentObjects;
	/**
	 * menampung daftar aksi yang diterima dari server.
	 */
	private ArrayList<AtomicAction> replyActions;
	/**
	 * Menampung daftar objek yang diterima dari server.
	 */
	private ArrayList<CanvasObject> replyObjects;
	/**
	 * Katalog objek.
	 */
	private HashMap<Integer, CanvasObject> objectMap;

	@SuppressLint("UseSparseArrays")
	private CanvasConnector() {
		sentActions = new ArrayList<AtomicAction>();
		replyActions = new ArrayList<AtomicAction>();
		sentObjects = new ArrayList<CanvasObject>();
		replyObjects = new ArrayList<CanvasObject>();
		objectMap = new HashMap<Integer, CanvasObject>();
	}

	public static CanvasConnector getInstance() {
		if (instance == null) {
			instance = new CanvasConnector();
		}
		return instance;
	}

	public CanvasConnector setSyncListener(SyncEventListener cs) {
		syncListener = cs;
		return this;
	}

	public void updateActions(int userId, int canvasID, int lastActNum,
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
				if (ua instanceof ResizeCanvas) {
					ResizeCanvas rc = (ResizeCanvas) ua;
					joAct.put(ActionJCode.ACTION_CODE, ActionCode.RESIZE_ACTION);
					joAct.put(ActionJCode.ACTION_PARAM, rc.getParameter());
					joAct.put(ActionJCode.CANVAS_WIDTH, rc.width);
					joAct.put(ActionJCode.CANVAS_HEIGHT, rc.height);
					joAct.put(ActionJCode.CANVAS_TOP, rc.top);
					joAct.put(ActionJCode.CANVAS_LEFT, rc.left);
				} else {
					CanvasObject co = null;
					if (ua instanceof DrawAction) {
						joAct.put(ActionJCode.ACTION_CODE,
								ActionCode.DRAW_ACTION);
						joAct.put(ActionJCode.ACTION_PARAM, "");
						co = ((DrawAction) ua).object;
					} else if (ua instanceof DeleteAction) {
						joAct.put(ActionJCode.ACTION_CODE,
								ActionCode.DELETE_ACTION);
						joAct.put(ActionJCode.ACTION_PARAM, "");
						co = ((DeleteAction) ua).object;
					} else if (ua instanceof TransformAction) {
						TransformAction ta = (TransformAction) ua;
						joAct.put(ActionJCode.ACTION_CODE,
								ActionCode.TRANSFORM_ACTION);
						joAct.put(ActionJCode.ACTION_PARAM, ta.getParameter());
						co = ta.object;
					} else if (ua instanceof GeomAction) {
						GeomAction ga = (GeomAction) ua;
						joAct.put(ActionJCode.ACTION_CODE,
								ActionCode.GEOM_ACTION);
						joAct.put(ActionJCode.ACTION_PARAM, ga.getParameter());
						co = ga.object;
					} else if (ua instanceof StyleAction) {
						StyleAction sa = (StyleAction) ua;
						joAct.put(ActionJCode.ACTION_CODE,
								ActionCode.STYLE_ACTION);
						joAct.put(ActionJCode.ACTION_PARAM, sa.getParameter());
						co = sa.object;
					} else
						break;
					if (co.getGlobalID() == -1) {
						// jika objek baru dan aksi draw -> masukkan di list
						if (ua instanceof DrawAction) {
							joAct.put(ActionJCode.ACTION_OBJ_LISTED,
									sentObjects.size());
							sentObjects.add(co);
						} else {
							// jika objek baru dan aksi bukan draw -> cari
							// idnya
							int lid = 0;
							for (; lid < sentObjects.size(); lid++) {
								if (sentObjects.get(lid).privateID == co.privateID) {
									joAct.put(ActionJCode.ACTION_OBJ_LISTED,
											lid);
									break;
								}
							}
							// jika objek tidak ditemukan, masukkan objeknya
							if (lid >= sentObjects.size()) {
								joAct.put(ActionJCode.ACTION_OBJ_LISTED,
										sentObjects.size());
								sentObjects.add(co);
							}
						}
					} else
						// jika objek sudah diketahui masukkan id globalnya
						joAct.put(ActionJCode.ACTION_OBJ_KNOWN,
								co.getGlobalID());
				}
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
				joObj.put(ActionJCode.OBJECT_GEOM,
						GeomAction.getParameterOf(co));
				joObj.put(ActionJCode.OBJECT_STYLE,
						StyleAction.getParameterOf(co));
				jarObj.put(joObj);
			}
			msg.put(ActionJCode.OBJECT_LIST, jarObj);

			// ------------ lan ---------
			msg.put(ActionJCode.LAST_ACTION_NUM, lastActNum);
			msg.put(ActionJCode.CANVAS_ID, canvasID);
			msg.put(ActionJCode.USER_ID, userId);

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
				// ada masalah dari server
				if (reply.has(ActionJCode.ERROR)) {
					int code = reply.getInt(ActionJCode.ERROR);
					if (code == ActionJCode.SERVER_ERROR)
						syncListener.onActionUpdateFailed(SERVER_PROBLEM);
					else if (code == ActionJCode.BAD_REQUEST)
						syncListener
								.onActionUpdateFailed(ActionJCode.BAD_REQUEST);
					return;
				}

				// jika paket lan tidak sesuai -> abaikan paket
				int oldLan = reply.getInt(ActionJCode.OLD_ACTION_NUM);
				if (!syncListener.accept(oldLan)) {
					syncListener.onPacketDropped(oldLan);
					return;
				}

				// ----------------------- parse objects ----------------------
				replyObjects.clear();
				JSONArray jarObj = reply.getJSONArray(ActionJCode.OBJECT_LIST);
				int length = jarObj.length();
				for (int i = 0; i < length; i++) {
					JSONObject joObj = jarObj.getJSONObject(i);
					int gid = joObj.getInt(ActionJCode.OBJECT_GLOBAL_ID);
					CanvasObject co = null;
					if (joObj.has(ActionJCode.OBJECT_LOCAL_ID)) {
						// jika objek adalah objek dari request -> pasangkan
						// global id dari server
						int oid = joObj.getInt(ActionJCode.OBJECT_LOCAL_ID);
						int j = 0;
						do {
							co = sentObjects.get(j++);
						} while (co.privateID != oid);
						co.setGlobaID(gid);
						objectMap.put(gid, co);
					} else {
						// jika objek merupakan objek baru dari server ->
						// abaikan jika data objek di server tidak ada
						if (!joObj.has(ActionJCode.OBJECT_MISSING)) {
							int code = joObj.getInt(ActionJCode.OBJECT_CODE);

							String shape = joObj
									.getString(ActionJCode.OBJECT_GEOM);
							String style = joObj
									.getString(ActionJCode.OBJECT_STYLE);
							String transform = joObj
									.getString(ActionJCode.OBJECT_TRANSFORM);
							co = createObject(code, shape, style, transform);
							if (co != null) {
								objectMap.put(gid, co);
								co.setGlobaID(gid);
							}
						}
					}
					replyObjects.add(co);
				}

				// ----------------- parse actions -----------------------
				replyActions.clear();
				JSONArray jarAct = reply.getJSONArray(ActionJCode.ACTION_LIST);
				length = jarAct.length();
				for (int i = 0; i < length; i++) {
					JSONObject joAct = jarAct.getJSONObject(i);
					if (joAct.has(ActionJCode.ACTION_SUBMITTED)) {
						// aksi adalah aksi yang dikirim -> langsung masukkan ke
						// replyActions
						int said = joAct.getInt(ActionJCode.ACTION_SUBMITTED);
						replyActions.add(sentActions.get(said));
					} else {
						// aksi dari server -> buat baru
						int code = joAct.getInt(ActionJCode.ACTION_CODE);
						String param = joAct
								.getString(ActionJCode.ACTION_PARAM);
						if (code == ActionCode.RESIZE_ACTION) {
							// aksi resize canvas -> buat baru
							replyActions.add(createAction(code, null, param));
						} else {
							// aksi lain -> cek dulu objeknya
							CanvasObject co = null;
							if (joAct.has(ActionJCode.ACTION_OBJ_LISTED)) {
								// jika objek tercantumkan, cari di replyObject
								co = replyObjects.get(joAct
										.getInt(ActionJCode.ACTION_OBJ_LISTED));
								android.util.Log.d("POS", "obj local:" + co);
							} else {
								// jika objek sudah diketahui, cari di objectMap
								// menggunakan id globalnya
								co = objectMap.get(joAct
										.getInt(ActionJCode.ACTION_OBJ_KNOWN));
							}

							// abaikan aksi jika tidak diketahui objeknya
							if (co != null)
								replyActions.add(createAction(code, co, param));
						}
					}

				}
				int lan = reply.getInt(ActionJCode.LAST_ACTION_NUM);

				syncListener.onActionUpdated(lan, replyActions);
			} catch (Exception e) {
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
		else if (code == ActionCode.RESIZE_ACTION)
			return new ResizeCanvas(param);
		return null;
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
		void onCanvasOpened(int status, int lastActNum);
	}

	private static CanvasModel canvas;

	public void openCanvas(int userId, CanvasModel canvasModel,
			OnCanvasOpenListener listener) {
		canvas = canvasModel;
		openListener = listener;
		JSONObject jo = new JSONObject();
		try {
			jo.put(Request.ACTION, Request.ACTION_OPEN);
			jo.put(Request.CANVAS_ID, canvasModel.getId());
			jo.put(Request.USER_ID, userId);
			new Client(PORTAL_URL, replyOpen).execute(jo);
		} catch (JSONException e) {
			openListener.onCanvasOpened(INTERNAL_PROBLEM, 0);
		}
	}

	private ReplyListener replyOpen = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				try {
					int lan = reply.getInt(Reply.LAST_ACTION_NUM);

					// baca ukuran kanvas
					int width = reply.getInt(Reply.CANVAS_WIDTH);
					int height = reply.getInt(Reply.CANVAS_HEIGHT);
					int left = reply.getInt(Reply.CANVAS_LEFT);
					int top = reply.getInt(Reply.CANVAS_TOP);
					canvas.setDimension(width, height, top, left);

					// baca objek2 yang ada
					objectMap.clear();
					canvas.objects.clear();
					JSONArray objects = reply.getJSONArray(Reply.OBJECT_LIST);
					for (int i = 0; i < objects.length(); i++) {
						JSONObject cob = objects.getJSONObject(i);
						int code = cob.getInt(Reply.OBJECT_CODE);
						String geom = cob.getString(Reply.OBJECT_GEOM);
						String style = cob.getString(Reply.OBJECT_STYLE);
						String transform = cob
								.getString(Reply.OBJECT_TRANSFORM);
						CanvasObject co = createObject(code, geom, style,
								transform);

						// masukkan idnya
						int id = cob.getInt(Reply.OBJECT_ID);
						co.setGlobaID(id);
						// cek objek sudah didelete atau belum
						boolean exist = cob.getBoolean(Reply.OBJECT_EXIST);
						if (exist)
							canvas.objects.add(co);
						objectMap.put(id, co);
					}

					openListener.onCanvasOpened(SUCCESS, lan);
				} catch (JSONException e) {
					openListener.onCanvasOpened(UNKNOWN_REPLY, 0);
				}

			} else
				openListener.onCanvasOpened(status, 0);
		}
	};

	public void closeCanvas(CanvasModel canvas, UserModel user) {
		JSONObject request = new JSONObject();
		try {
			request.put(Request.CANVAS_ID, canvas.getId());
			request.put(Request.USER_ID, user.collaID);
			request.put(Request.ACTION, Request.ACTION_CLOSE);

			new Client(PORTAL_URL, replisClose).execute(request);
		} catch (JSONException e) {
			replisClose.process(INTERNAL_PROBLEM, null);
		}
	}

	private ReplyListener replisClose = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			syncListener.onCanvasClosed(status);
		}
	};

}
