package com.ppla03.collapaint.conn;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

public class CanvasConnector extends ServerConnector {
	static final class JCode {
		static final String LAST_ACTION_NUM = "lan";
		static final String CANVAS_ID = "cid";
		static final String ACTION_LIST = "act";
		static final String ACTION_OBJ_LISTED = "ol";
		static final String ACTION_OBJ_KNOWN = "ok";
		static final String ACTION_SUBMITTED = "as";
		static final String ACTION_CODE = "cd";
		static final String ACTION_PARAM = "par";
		static final String OBJECT_LIST = "obj";
		static final String OBJECT_ID = "id";
		static final String OBJECT_GLOBAL_ID = "gid";
		static final String OBJECT_CODE = "cd";
		static final String OBJECT_SHAPE = "sh";
		static final String OBJECT_STYLE = "st";
		static final String OBJECT_TRANSFORM = "tx";
		static final String ERROR = "error";
		static final int SERVER_ERROR = 5;
		static final int BAD_REQUEST = 9;
	}

	public static final class ActionCode {
		public static final int DRAW_ACTION = 1;
		public static final int DELETE_ACTION = 2;
		public static final int RESHAPE_ACTION = 3;
		public static final int STYLE_ACTION = 4;
		public static final int TRANSFORM_ACTION = 5;
	}

	public static final class ObjectCode {
		public static final int RECT = 1;
		public static final int OVAL = 2;
		public static final int LINES = 3;
		public static final int POLYGON = 4;
		public static final int PATH = 5;
		public static final int TEXT = 6;
		public static final int IMAGE = 7;
	}

	public static String COMMIT_URL = HOST + "action";
	public static String MEMBER_URL = HOST + "member";

	private static CanvasConnector instance;
	private static SyncEventListener syncListener;
	private static ManageParticipantListener partListener;
	private ArrayList<UserAction> sentActions;
	private ArrayList<CanvasObject> sentObjects;
	private ArrayList<UserAction> replyActions;
	private ArrayList<CanvasObject> replyObjects;
	private HashMap<Integer, CanvasObject> objectMap;

	private CanvasConnector() {
		sentActions = new ArrayList<UserAction>();
		replyActions = new ArrayList<UserAction>();
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

	public CanvasConnector setManageParticipantListener(
			ManageParticipantListener mpl) {
		partListener = mpl;
		return this;
	}

	static class ParJCode {
		// request
		static final String CANVAS_ID = "cid";
		// reply
		static final String USER_ID = "id";
		static final String USER_NAME = "name";
		static final String USER_LIST = "pars";
		static final String OWNER_ID = "oid";
		static final String OWNER_NAME = "oname";
		static final String ERROR = "Error";
	}

	private CanvasModel askPartModel;

	public void getParticipants(CanvasModel canvas) {
		android.util.Log.d("POS", "getpars");
		JSONObject request = new JSONObject();
		try {
			request.put(ParJCode.CANVAS_ID, canvas.getId());
			new Client(MEMBER_URL, replisMember).execute(request);
		} catch (JSONException e) {
			replisMember.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ArrayList<UserModel> participants = new ArrayList<>();

	private final ReplyListener replisMember = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				try {
					int userId = reply.getInt(ParJCode.OWNER_ID);
					String userName = reply.getString(ParJCode.OWNER_NAME);
					UserModel owner = new UserModel(userId, userName);

					JSONArray pars = reply.getJSONArray(ParJCode.USER_LIST);
					participants.clear();
					for (int i = 0; i < pars.length(); i++) {
						JSONObject jo = pars.getJSONObject(i);
						userId = jo.getInt(ParJCode.USER_ID);
						userName = jo.getString(ParJCode.USER_NAME);
						participants.add(new UserModel(userId, userName));
					}

					partListener.onParticipantFetched(askPartModel, owner,
							participants);
				} catch (JSONException je) {

				}
			} else
				partListener.onParticipationFetchedFailed(askPartModel, status);
		}
	};

	public void inviteUser(String username) {
		// TODO
	}

	public void kickUser(UserModel user) {
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
					joAct.put(JCode.ACTION_CODE, ActionCode.DRAW_ACTION);
					joAct.put(JCode.ACTION_PARAM, "");
					co = ((DrawAction) ua).object;
				} else if (ua instanceof DeleteAction) {
					joAct.put(JCode.ACTION_CODE, ActionCode.DELETE_ACTION);
					joAct.put(JCode.ACTION_PARAM, "");
					co = ((DeleteAction) ua).object;
				} else if (ua instanceof ReshapeAction) {
					ReshapeAction ra = (ReshapeAction) ua;
					joAct.put(JCode.ACTION_CODE, ActionCode.RESHAPE_ACTION);
					joAct.put(JCode.ACTION_PARAM, ra.getParameter());
					co = ra.object;
				} else if (ua instanceof StyleAction) {
					StyleAction sa = (StyleAction) ua;
					joAct.put(JCode.ACTION_CODE, ActionCode.STYLE_ACTION);
					joAct.put(JCode.ACTION_PARAM, sa.getParameter());
					co = sa.object;
				} else if (ua instanceof MoveAction) {
					MoveAction ta = (MoveAction) ua;
					joAct.put(JCode.ACTION_CODE, ActionCode.TRANSFORM_ACTION);
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
				joObj.put(JCode.OBJECT_TRANSFORM, MoveAction.getParameterOf(co));
				joObj.put(JCode.OBJECT_STYLE, StyleAction.getParameterOf(co));
				joObj.put(JCode.OBJECT_SHAPE, ReshapeAction.getParameterOf(co));
				jarObj.put(joObj);
			}
			msg.put(JCode.OBJECT_LIST, jarObj);

			// ------------ lan ---------
			msg.put(JCode.LAST_ACTION_NUM, lastActNum);
			msg.put(JCode.CANVAS_ID, canvasID);

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
					syncListener.onActionUpdatedFailed(status);
					return;
				}
				if (reply.has(JCode.ERROR)) {
					int code = reply.getInt(JCode.ERROR);
					if (code == JCode.SERVER_ERROR)
						syncListener.onActionUpdatedFailed(SERVER_PROBLEM);
					else if (code == JCode.BAD_REQUEST)
						syncListener.onActionUpdatedFailed(INTERNAL_PROBLEM);
				}
				// TODO debug
				Log.d("POS", "rep:" + reply.toString());

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
						int code = joObj.getInt(JCode.OBJECT_CODE);
						String shape = joObj.getString(JCode.OBJECT_SHAPE);
						String style = joObj.getString(JCode.OBJECT_STYLE);
						String transform = joObj
								.getString(JCode.OBJECT_TRANSFORM);
						co = createObject(code, shape, style, transform);
						objectMap.put(gid, co);
						co.setGlobaID(gid);
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
						int code = joAct.getInt(JCode.ACTION_CODE);
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
						if (co != null)
							replyActions.add(createAction(code, co, param));
					}

				}
				int lan = reply.getInt(JCode.LAST_ACTION_NUM);

				syncListener.onActionUpdated(lan, replyActions);
			} catch (Exception e) {
				StackTraceElement[] ste = e.getStackTrace();
				Log.d("POS", "internal error");
				for (int i = 0; i < ste.length; i++) {
					Log.d("POS", i + ":" + ste[i]);
				}

				syncListener.onActionUpdatedFailed(INTERNAL_PROBLEM);
			}
		}
	};

	CanvasObject createObject(int code, String shape, String style,
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

	UserAction createAction(int code, CanvasObject object, String param) {
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
