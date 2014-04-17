package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.UserManager;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

public class ParticipantManager extends ServerConnector {
	private static ParticipantManager instance;
	private static ManageParticipantListener listener;
	public static String MEMBER_URL = HOST + "member";

	public ParticipantManager setListener(ManageParticipantListener mpl) {
		listener = mpl;
		return this;
	}

	public static ParticipantManager getInstance() {
		if (instance == null)
			instance = new ParticipantManager();
		return instance;
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
		static final int CANVAS_UNKNOWN = 3;
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
					if (reply.has(ParJCode.ERROR)) {
						participants.clear();
						listener.onParticipantFetched(askPartModel,
								askPartModel.owner, participants);
					} else {
						int userId = reply.getInt(ParJCode.OWNER_ID);
						String name = reply.getString(ParJCode.OWNER_NAME);
						UserModel owner = new UserModel(userId, "", name);

						JSONArray pars = reply.getJSONArray(ParJCode.USER_LIST);
						participants.clear();
						for (int i = 0; i < pars.length(); i++) {
							JSONObject jo = pars.getJSONObject(i);
							userId = jo.getInt(ParJCode.USER_ID);
							if (userId == CollaUserManager.getCurrentUser().collaID)
								name = "You";
							else {
								name = jo.getString(ParJCode.USER_NAME);
								if (name.isEmpty())
									name = "Anonym";
							}
							participants.add(new UserModel(userId, "", name));
						}

						listener.onParticipantFetched(askPartModel, owner,
								participants);
					}
				} catch (Exception e) {
					listener.onParticipationFetchedFailed(askPartModel,
							ServerConnector.UNKNOWN_REPLY);
				}
			} else
				listener.onParticipationFetchedFailed(askPartModel, status);
		}
	};

	public void inviteUser(String username) {
		// TODO
	}

	public void kickUser(UserModel user) {
		// TODO
	}

}
