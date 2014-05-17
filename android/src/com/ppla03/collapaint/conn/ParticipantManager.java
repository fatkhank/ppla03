package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import collapaint.code.ParticipantJCode;
import collapaint.code.ParticipantJCode.Reply;
import collapaint.code.ParticipantJCode.Request;

import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.Participation.Action;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.Participation.Role;

public class ParticipantManager extends ServerConnector {
	private static ParticipantManager instance;
	private static ManageParticipantListener listener;
	private InvitationResponseListener responseListener;
	private static String PARTICIPANT_SERVLET_URL = HOST + "participant";

	@Override
	protected void onHostAddressChange(String host) {
		PARTICIPANT_SERVLET_URL = host + "participant";
	}

	public ParticipantManager setListener(ManageParticipantListener mpl) {
		listener = mpl;
		return this;
	}

	public ParticipantManager setResponseListener(
			InvitationResponseListener listener) {
		this.responseListener = listener;
		return this;
	}

	public static ParticipantManager getInstance() {
		if (instance == null)
			instance = new ParticipantManager();
		return instance;
	}

	/**
	 * Kanvas yang sedang diambil daftar partisipannya
	 */
	private CanvasModel askPartModel;

	/**
	 * Mengambil daftar partisipan dar suatu kanvas.
	 * @param canvas
	 */
	public void getParticipants(CanvasModel canvas) {
		JSONObject request = new JSONObject();
		// TODO debug participant manager
		android.util.Log.d("POS", "part manager get par");
		try {
			// masukan aksi, id user dan kanvas
			askPartModel = canvas;
			request.put(Request.ACTION, Request.Action.LIST);
			request.put(Request.CANVAS_ID, canvas.getId());
			int uid = CollaUserManager.getCurrentUser().collaID;
			request.put(Request.USER_ID, uid);
			new Client(PARTICIPANT_SERVLET_URL, replisMember).execute(request);
			android.util.Log.d("POS", "executed");
		} catch (JSONException e) {
			android.util.Log.d("POS", "exception:" + e);
			replisMember.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ArrayList<Participation> participants = new ArrayList<>();

	private final ReplyListener replisMember = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				try {
					if (reply.has(ParticipantJCode.ERROR)) {
						// kalau ada error
						participants.clear();
						listener.onParticipationFetchedFailed(askPartModel,
								ServerConnector.SERVER_PROBLEM);
					} else {
						JSONArray pars = reply
								.getJSONArray(Reply.PARTICIPANT_LIST);
						participants.clear();
						for (int i = 0; i < pars.length(); i++) {
							JSONObject jo = pars.getJSONObject(i);

							// ambil data user
							int userId = jo.getInt(Reply.USER_ID);
							String name = jo.getString(Reply.USER_NAME);
							if (name.isEmpty())
								name = "Anonym";
							UserModel user = new UserModel(userId, "", name);

							// ambil data partisipasi
							Participation p = new Participation(user,
									askPartModel);
							// role
							int role = jo.getInt(Reply.PARTICIPANT_STATUS);
							if (role == Reply.ParticipantStatus.OWNER)
								p.setRole(Role.OWNER);
							else if (role == Reply.ParticipantStatus.MEMBER)
								p.setRole(Role.MEMBER);
							else
								p.setRole(Role.INVITATION);

							// status
							int action = jo.getInt(Reply.PARTICIPANT_ACTION);
							if (action == Reply.PARTICIPANT_ACTION_OPEN)
								p.setAction(Action.OPEN);
							else
								p.setAction(Action.CLOSE);
							// masukkan ke daftar
							participants.add(p);
						}

						listener.onParticipantFetched(askPartModel,
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

	private CanvasModel inviteCanvas;

	/**
	 * mengundang seorang user ke sebuah kanvas
	 * @param email email dari user yg diundang
	 * @param canvas
	 */
	public void inviteUser(String email, CanvasModel canvas) {
		JSONObject request = new JSONObject();
		try {
			this.inviteCanvas = canvas;
			request.put(Request.ACTION, Request.Action.INTIVE);
			request.put(Request.CANVAS_ID, canvas.getId());
			int inviterId = CollaUserManager.getCurrentUser().collaID;
			request.put(Request.USER_ID, inviterId);
			request.put(Request.USER_EMAIL, email);

			new Client(PARTICIPANT_SERVLET_URL, replisInvite).execute(request);
		} catch (JSONException e) {
			replisInvite.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ReplyListener replisInvite = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				// cek ada error atau tidak
				if (reply.has(ParticipantJCode.ERROR)) {
					listener.onInviteUser("", inviteCanvas, SERVER_PROBLEM);
					return;
				}
				try {
					String email = reply.getString(Reply.USER_EMAIL);
					String result = reply.getString(Reply.INVITE_STATUS);
					if (result.equals(Reply.InviteStatus.ALREADY_JOINED)) {
						listener.onInviteUser(email, inviteCanvas,
								ManageParticipantListener.ALREADY_JOINED);
					} else if (result
							.equals(Reply.InviteStatus.ALREADY_INVITED)) {
						listener.onInviteUser(email, inviteCanvas,
								ManageParticipantListener.ALREADY_INVITED);
					} else
						listener.onInviteUser(email, inviteCanvas, SUCCESS);
				} catch (JSONException e) {
					listener.onInviteUser("", inviteCanvas, UNKNOWN_REPLY);
				}

			} else
				listener.onInviteUser("", inviteCanvas, status);
		}
	};

	private UserModel victim;
	private CanvasModel kickCanvas;

	public void kickUser(UserModel user, CanvasModel canvas) {
		JSONObject request = new JSONObject();
		try {
			this.victim = user;
			this.kickCanvas = canvas;
			request.put(Request.ACTION, Request.Action.KICK);
			request.put(Request.CANVAS_ID, canvas.getId());
			// id yang akan dikick
			request.put(Request.USER_ID, user.collaID);
			// id yang mengekick
			int kickerId = CollaUserManager.getCurrentUser().collaID;
			request.put(Request.KICKER_ID, kickerId);

			new Client(PARTICIPANT_SERVLET_URL, replisKick).execute(request);
		} catch (JSONException e) {
			replisKick.process(INTERNAL_PROBLEM, null);
		}
	}

	private ReplyListener replisKick = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status != SUCCESS) {
				listener.onKickUser(victim, kickCanvas, status);
				return;
			}
			if (reply.has(ParticipantJCode.ERROR)) {
				listener.onKickUser(victim, kickCanvas, SERVER_PROBLEM);
				return;
			}
			try {
				String result = reply.getString(Reply.KICK_STATUS);
				if (result.equals(Reply.KickStatus.NOT_A_MEMBER))
					listener.onKickUser(victim, kickCanvas,
							ManageParticipantListener.NOT_A_MEMBER);
				else
					listener.onKickUser(victim, kickCanvas, SUCCESS);
			} catch (JSONException e) {
				listener.onKickUser(victim, kickCanvas, UNKNOWN_REPLY);
			}
		}
	};

	private ArrayList<Participation> invitationList = new ArrayList<>();

	/**
	 * Response terhadap undangan
	 * @author hamba v7
	 * 
	 */
	public enum InviteResponse {
		/**
		 * Diterima
		 */
		ACCEPT,
		/**
		 * Ditolak
		 */
		DECLINE
	}

	public void responseInvitation(CanvasModel model, UserModel user,
			InviteResponse response) {
		JSONObject request = new JSONObject();
		try {
			Participation invitation = new Participation(user, model);
			invitationList.add(invitation);
			request.put(Request.ACTION, Request.Action.RESPONSE);
			request.put(Request.USER_ID, invitation.user.collaID);
			// terjemahkan response
			request.put(Request.CANVAS_ID, invitation.canvas.getId());
			if (response == InviteResponse.ACCEPT)
				request.put(Request.RESPONSE, Request.Response.ACCEPT);
			else
				request.put(Request.RESPONSE, Request.Response.DECLINE);
			// kirim request
			new Client(PARTICIPANT_SERVLET_URL, replisResponseInvite);
		} catch (JSONException e) {
			replisResponseInvite.process(INTERNAL_PROBLEM, null);
		}
	}

	private ReplyListener replisResponseInvite = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status != SUCCESS) {
				responseListener.onResponse(null, status);
			} else {
				try {
					int canvasId = reply.getInt(Reply.CANVAS_ID);
					int userId = reply.getInt(Reply.USER_ID);
					// cari di daftar undangan yang cocok
					Participation inv = null;
					int i = 0;
					for (i = 0; i < invitationList.size(); i++) {
						Participation p = invitationList.get(i);
						if (p.canvas.getId() == canvasId
								&& p.user.collaID == userId) {
							inv = p;
							break;
						}
					}
					if (i < invitationList.size())
						invitationList.remove(i);

					if (reply.has(ParticipantJCode.ERROR)) {
						responseListener.onResponse(inv, SERVER_PROBLEM);
					} else {
						String result = reply.getString(Reply.RESPONSE_STATUS);
						if (result.equals(Reply.ResponseStatus.SUCCESS))
							responseListener.onResponse(inv, SUCCESS);
						else
							responseListener.onResponse(inv,
									InvitationResponseListener.ALREADY_JOINED);
					}
				} catch (JSONException ex) {
					responseListener.onResponse(null, UNKNOWN_REPLY);
				}
			}
		}
	};

}
