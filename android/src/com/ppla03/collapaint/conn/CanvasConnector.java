package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.*;
import com.ppla03.collapaint.model.object.*;

public class CanvasConnector extends ServerConnector {
	public static final class ActionCode {
		public static char DRAW_ACTION = 'w';
		public static char DELETE_ACTION = 'd';
		public static char RESHAPE_ACTION = 'g';
		public static char STYLE_ACTION = 's';
	}

	public static final class ObjectCode {
		public static char RECT = 'r';
		public static char OVAL = 'o';
		public static char LINES = 'l';
		public static char POLYGON = 'p';
		public static char PATH = 'f';
		public static char TEXT = 't';
		public static char IMAGE = 'i';
	}

	private static CanvasConnector instance;
	private static CanvasSynchronizer syncer;
	public static final String COMMIT_URL = "http://192.168.43.64:8080/colla/commit";
	private ArrayList<UserAction> actionBuffer;
	private ArrayList<CanvasObject> objectPool;

	private CanvasConnector() {
		actionBuffer = new ArrayList<>();
		objectPool = new ArrayList<>();
	}

	public static CanvasConnector getInstance() {
		if (instance == null) {
			instance = new CanvasConnector();
		}
		return instance;
	}

	public CanvasConnector setSynchronizer(CanvasSynchronizer cs) {
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

	public void updateActions(int lastActNum, ArrayList<UserAction> actions) {
		// encode action to json
		JSONObject msg = new JSONObject();
		try {
			Log.d("POS", "conn:"+actions.size());
			// --------- action ------------
			objectPool.clear();
			JSONArray acts = new JSONArray();
			int size = actions.size();
			for (int i = 0; i < size; i++) {
				UserAction ua = actions.get(i);
				JSONObject jo = new JSONObject();
				CanvasObject co = null;
				if (ua instanceof DrawAction) {
					DrawAction da = (DrawAction) ua;
					jo.put("code", "" + ActionCode.DRAW_ACTION);
					co = da.object;
				} else if (ua instanceof DeleteAction) {
					jo.put("code", "" + ActionCode.DELETE_ACTION);
					co = ((DeleteAction) ua).object;
				} else if (ua instanceof ReshapeAction) {
					ReshapeAction ra = (ReshapeAction) ua;
					jo.put("code", "" + ActionCode.RESHAPE_ACTION);
					jo.put("param", ra.getparameter());
					co = ra.object;
				} else if (ua instanceof StyleAction) {
					StyleAction sa = (StyleAction) ua;
					jo.put("code", "" + ActionCode.STYLE_ACTION);
					jo.put("param", sa.getParameter());
					co = sa.object;
				}
				if (co.getGlobalID() == -1) {
					if (ua instanceof DrawAction)
						objectPool.add(co);
					jo.put("id", objectPool.size() - 1);
				} else {
					jo.put("gid", co.getGlobalID());
				}
				acts.put(jo);
			}
			msg.put("action", acts);

			// ------------ object -------------
			JSONArray objs = new JSONArray();
			size = objectPool.size();
			for (int i = 0; i < size; i++) {
				JSONObject jo = new JSONObject();
				CanvasObject co = objectPool.get(i);
				if (co instanceof RectObject)
					jo.put("code", ObjectCode.RECT);
				else if (co instanceof OvalObject)
					jo.put("code", ObjectCode.OVAL);
				else if (co instanceof LinesObject)
					jo.put("code", ObjectCode.LINES);
				else if (co instanceof PolygonObject)
					jo.put("code", ObjectCode.POLYGON);
				else if (co instanceof PathObject)
					jo.put("code", ObjectCode.PATH);
				else if (co instanceof TextObject)
					jo.put("code", ObjectCode.TEXT);
				else if (co instanceof ImageObject){
					jo.put("code", ObjectCode.IMAGE);
					// TODO upload image
				}
				jo.put("id", co.id);
				jo.put("style", co.getStyleParameter());
				jo.put("shape", co.getShapeParameter());
				objs.put(jo);
			}
			msg.put("object", objs);

			// ------------ lan ---------
			msg.put("lan", lastActNum);

			Log.d("POS", msg.toString());
			// new Client(COMMIT_URL, updateListener);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final ServerConnector.ReplyListener updateListener = new ReplyListener() {

		@Override
		public void process(JSONObject reply) {
			// TODO Auto-generated method stub
			try {
				int lan = reply.getInt("lan");
				JSONArray actions = reply.getJSONArray("act");
				int size = actions.length();
				for (int i = 0; i < size; i++) {
					JSONObject act = actions.getJSONObject(i);

					JSONObject cojo = act.getJSONObject("obj");

					UserAction ua;
					char code = act.getString("code").charAt(0);
					if (code == ActionCode.DRAW_ACTION) {

						// ua = new DrawAction(object);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void downloadImage(ImageObject image) {
		// TODO
	}

	public void uploadImage(Bitmap bitmap, String id) {
		// TODO
	}

}
