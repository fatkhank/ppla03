package com.ppla03.collapaint.conn;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification.Style;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.Transformation;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

public class CanvasConnector extends ServerConnector {
	static final class JCode {
		static final String LAST_ACTION_NUM = "lan";
		static final String CANVAS_ID = "cid";
		static final String ACTION_LIST = "act";
		static final String ACTION_OBJ_LISTED = "id";
		static final String ACTION_OBJ_KNOWN = "gid";
		static final String ACTION_SUBMITTED = "sid";
		static final String ACTION_CODE = "cd";
		static final String ACTION_PARAM = "par";
		static final String OBJECT_LIST = "obj";
		static final String OBJECT_ID = "id";
		static final String OBJECT_GLOBAL_ID = "gid";
		static final String OBJECT_CODE = "cd";
		static final String OBJECT_SHAPE = "sh";
		static final String OBJECT_STYLE = "sty";
		static final String OBJECT_TRANSFORM = "txf";
	}

	public static final class ActionCode {
		public static char DRAW_ACTION = 'A';
		public static char DELETE_ACTION = 'B';
		public static char RESHAPE_ACTION = 'C';
		public static char STYLE_ACTION = 'D';
		public static char TRANSFORM_ACTION = 'E';
	}

	public static final class ObjectCode {
		public static char RECT = 'R';
		public static char OVAL = 'O';
		public static char LINES = 'L';
		public static char POLYGON = 'P';
		public static char PATH = 'F';
		public static char TEXT = 'T';
		public static char IMAGE = 'I';
	}

	public static final String COMMIT_URL = HOST + "action";

	private static CanvasConnector instance;
	private static SyncEventListener syncer;
	private ArrayList<UserAction> sentActions;
	private ArrayList<CanvasObject> sentObjects;
	private ArrayList<UserAction> replyActions;
	private ArrayList<CanvasObject> replyObjects;
	private HashMap<Integer, CanvasObject> objectMap;

	private CanvasConnector() {
		sentActions = new ArrayList<>();
		replyActions = new ArrayList<>();
		sentObjects = new ArrayList<>();
		replyObjects = new ArrayList<>();
		objectMap = new HashMap<>();
	}

	public static CanvasConnector getInstance() {
		if (instance == null) {
			instance = new CanvasConnector();
		}
		return instance;
	}

	public CanvasConnector setUpdateSynchronizer(SyncEventListener cs) {
		syncer = cs;
		return this;
	}

	public void getParticipants(CanvasModel canvas) {
		// TODO
	}

	public void inviteUser(String username) {
		// TODO
	}

	public void kickUser(UserModel user) {
		// TODO
	}

	public void closeCanvas() {
		// TODO
	}

	public void updateActions(int canvasID, int lastActNum,
			ArrayList<UserAction> actions) {
		// encode action to json
		JSONObject msg = new JSONObject();
		try {
			// ------------------------ process actions ------------------------
			sentObjects.clear();
			sentActions.clear();
			JSONArray jarAct = new JSONArray();
			int size = actions.size();
			for (int i = 0; i < size; i++) {
				UserAction ua = actions.get(i);
				JSONObject joAct = new JSONObject();
				CanvasObject co = null;
				if (ua instanceof DrawAction) {
					joAct.put(JCode.ACTION_CODE, "" + ActionCode.DRAW_ACTION);
					co = ((DrawAction) ua).object;
				} else if (ua instanceof DeleteAction) {
					joAct.put(JCode.ACTION_CODE, "" + ActionCode.DELETE_ACTION);
					co = ((DeleteAction) ua).object;
				} else if (ua instanceof ReshapeAction) {
					ReshapeAction ra = (ReshapeAction) ua;
					joAct.put(JCode.ACTION_CODE, "" + ActionCode.RESHAPE_ACTION);
					joAct.put(JCode.ACTION_PARAM, ra.getParameter());
					co = ra.object;
				} else if (ua instanceof StyleAction) {
					StyleAction sa = (StyleAction) ua;
					joAct.put(JCode.ACTION_CODE, "" + ActionCode.STYLE_ACTION);
					joAct.put(JCode.ACTION_PARAM, sa.getParameter());
					co = sa.object;
				} else if (ua instanceof MoveAction) {
					MoveAction ta = (MoveAction) ua;
					joAct.put(JCode.ACTION_CODE, ""
							+ ActionCode.TRANSFORM_ACTION);
					joAct.put(JCode.ACTION_PARAM, ta.getParameter());
					co = ta.object;
				}
				if (co.getGlobalID() == -1) {
					// if object is new and the action is draw, put in object
					// list
					if (ua instanceof DrawAction) {
						joAct.put(JCode.ACTION_OBJ_LISTED, sentObjects.size());
						sentObjects.add(co);
					} else
						// if object is new but the action is not draw, find in
						// object list
						for (int j = 0; j < sentObjects.size(); j++) {
							if (sentObjects.get(j).privateID == co.privateID) {
								joAct.put(JCode.ACTION_OBJ_LISTED, j);
								break;
							}
						}
				} else
					// if object is known, just put object's global id
					joAct.put(JCode.ACTION_OBJ_KNOWN, co.getGlobalID());
				jarAct.put(joAct);
				sentActions.add(ua);
			}
			msg.put(JCode.ACTION_LIST, jarAct);

			// ------------------------ process objects ------------------------
			JSONArray jarObj = new JSONArray();
			size = sentObjects.size();
			for (int i = 0; i < size; i++) {
				JSONObject joObj = new JSONObject();
				CanvasObject co = sentObjects.get(i);
				if (co instanceof RectObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.RECT);
				else if (co instanceof OvalObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.OVAL);
				else if (co instanceof LineObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.LINES);
				else if (co instanceof PolygonObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.POLYGON);
				else if (co instanceof FreeObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.PATH);
				else if (co instanceof TextObject)
					joObj.put(JCode.OBJECT_CODE, ObjectCode.TEXT);
				else if (co instanceof ImageObject) {
					joObj.put(JCode.OBJECT_CODE, ObjectCode.IMAGE);
					// TODO upload image
				}
				joObj.put(JCode.OBJECT_ID, co.privateID);
				joObj.put(JCode.OBJECT_TRANSFORM,
						MoveAction.getParameterOf(co));
				joObj.put(JCode.OBJECT_STYLE, StyleAction.getParameterOf(co));
				joObj.put(JCode.OBJECT_SHAPE, ReshapeAction.getParameterOf(co));
				jarObj.put(joObj);
			}
			msg.put(JCode.OBJECT_LIST, jarObj);

			// ------------ lan ---------
			msg.put(JCode.LAST_ACTION_NUM, lastActNum);
			msg.put(JCode.CANVAS_ID, canvasID);

			new Client(COMMIT_URL, updateListener);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final ServerConnector.ReplyListener updateListener = new ReplyListener() {

		@Override
		public void process(JSONObject reply) {
			try {
				// parse objects
				replyObjects.clear();
				JSONArray jarObj = reply.getJSONArray(JCode.OBJECT_LIST);
				int length = jarObj.length();
				for (int i = 0; i < length; i++) {
					JSONObject joObj = jarObj.getJSONObject(i);
					int gid = joObj.getInt(JCode.OBJECT_GLOBAL_ID);
					CanvasObject co;
					if (joObj.has(JCode.OBJECT_ID)) {
						// if object is in sent objects, assign global id from
						// reply
						int oid = joObj.getInt(JCode.OBJECT_ID);
						int j = 0;
						do {
							co = sentObjects.get(j++);
						} while (co.privateID != oid);
						co.setGlobaID(gid);
					} else {
						// if object is new create object from reply
						char code = joObj.getString(JCode.OBJECT_CODE)
								.charAt(0);
						String shape = joObj.getString(JCode.OBJECT_SHAPE);
						String style = joObj.getString(JCode.OBJECT_STYLE);
						String transform = joObj
								.getString(JCode.OBJECT_TRANSFORM);
						co = createObject(code, shape, style, transform);
						objectMap.put(gid, co);
					}
					replyObjects.add(co);
				}

				// parse actions
				replyActions.clear();
				JSONArray jarAct = reply.getJSONArray(JCode.ACTION_LIST);
				length = jarAct.length();
				for (int i = 0; i < length; i++) {
					JSONObject joAct = jarAct.getJSONObject(i);
					if (joAct.has(JCode.ACTION_SUBMITTED)) {
						// if action is in sentActions, copy to replyActions
						int said = joAct.getInt(JCode.ACTION_SUBMITTED);
						replyActions.add(sentActions.get(said));
					} else {
						// if action is new, create action from reply
						char code = joAct.getString(JCode.ACTION_CODE)
								.charAt(0);
						String param = joAct.getString(JCode.ACTION_PARAM);
						CanvasObject co = null;
						if (joAct.has(JCode.ACTION_OBJ_LISTED))
							// if action object is listed, find in replyObject
							co = replyObjects.get(joAct
									.getInt(JCode.ACTION_OBJ_LISTED));
						else
							// if action object is already known, find in
							// objectMap using global id
							co = objectMap.get(joAct
									.getInt(JCode.ACTION_OBJ_KNOWN));
						replyActions.add(createAction(code, co, param));
					}

				}
				int lan = reply.getInt(JCode.LAST_ACTION_NUM);
				syncer.onActionUpdated(lan, replyActions);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	CanvasObject createObject(char code, String shape, String style,
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
		else if (code == ObjectCode.IMAGE)
			co = new ImageObject();
		ReshapeAction.apply(shape, co);
		MoveAction.applyTransform(transform, co);
		StyleAction.applyStyle(style, co);
		return co;
	}

	UserAction createAction(char code, CanvasObject object, String param) {
		if (code == ActionCode.DRAW_ACTION) {
			return new DrawAction(object);
		} else if (code == ActionCode.DELETE_ACTION) {
			return new DeleteAction(object);
		} else if (code == ActionCode.RESHAPE_ACTION) {
			ReshapeAction ra = new ReshapeAction(object, false);
			ra.setParameter(param);
			return ra;
		} else if (code == ActionCode.STYLE_ACTION) {
			StyleAction sa = new StyleAction(object, false);
			sa.setParameter(param);
			return sa;
		} else if (code == ActionCode.TRANSFORM_ACTION)
			return new MoveAction(object).setParameter(param);
		return null;
	}

	public void downloadImage(ImageObject image) {
		// TODO download image
	}

	public void uploadImage(Bitmap bitmap, String id) {
		// TODO upload image implementation
	}

}
