package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import collapaint.code.CanvasJCode;
import collapaint.code.CanvasJCode.Reply;
import collapaint.code.CanvasJCode.Request;
import collapaint.code.ParticipantJCode;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

/**
 * Menangani manajemen penggunaan kanvas oleh user.
 * @author hamba v7
 * 
 */
public class BrowserConnector extends ServerConnector {
	private static String CANVAS_URL = HOST + "canvas";

	protected void onHostAddressChange(String host) {
		CANVAS_URL = host + "action";
	}

	private static BrowserConnector instance;
	private CanvasCreationListener createListener;
	private OnFetchListListener listFetchListener;
	private CanvasModel proposedModel;

	private BrowserConnector() {}

	/**
	 * Mengambil instansi {@link BrowserConnector} yang sedang aktif.
	 * @return
	 */
	public static BrowserConnector getInstance() {
		if (instance == null)
			instance = new BrowserConnector();
		return instance;
	}

	/**
	 * Mengatur listener event pembuatan kanvas.
	 * @param listener
	 * @return this.
	 */
	public BrowserConnector setCreateListener(CanvasCreationListener listener) {
		this.createListener = listener;
		return this;
	}

	public BrowserConnector setListFetchListener(OnFetchListListener listener) {
		this.listFetchListener = listener;
		return this;
	}

	/**
	 * Membuat sebuah kanvas. Jika proses telah dibuat, akan memanggil
	 * {@link CanvasCreationListener#onCreated(CanvasModel, int)} dari listener
	 * yang didaftarkan.
	 * @param owner pemilik kanvas.
	 * @param name nama kanvas.
	 * @param width lebar kanvas.
	 * @param height tinggi kanvas.
	 */
	public void createCanvas(UserModel owner, String name, int width, int height) {
		JSONObject msg = new JSONObject();
		try {
			proposedModel = new CanvasModel(owner, name, width, height);
			msg.put(Request.USER_ID, owner.collaID);
			msg.put(Request.CANVAS_NAME, name);
			msg.put(Request.CANVAS_WIDTH, width);
			msg.put(Request.CANVAS_HEIGHT, height);
			msg.put(Request.ACTION, Request.Action.CREATE);

			new Client(CANVAS_URL, createReply).execute(msg);
		} catch (JSONException e) {
			createReply.process(INTERNAL_PROBLEM, null);
		}
	}

	/**
	 * Menangani reply pembuatan kanvas.
	 */
	private final ReplyListener createReply = new ReplyListener() {
		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				try {
					if (reply.has(CanvasJCode.ERROR)) {
						String error = reply.getString(CanvasJCode.ERROR);
						if (error.equals(CanvasJCode.Error.DUPLICATE_NAME))
							createListener.onCreated(proposedModel,
									CanvasCreationListener.DUPLICATE_NAME);
						else if (error.equals(CanvasJCode.Error.NOT_AUTHORIZED))
							createListener.onCreated(proposedModel,
									CanvasCreationListener.NOT_AUTHORIZED);
					} else {
						proposedModel.setid(reply.getInt(Reply.CANVAS_ID));
						createListener.onCreated(proposedModel, SUCCESS);
					}
				} catch (JSONException e) {
					createListener.onCreated(proposedModel, UNKNOWN_REPLY);
				}
			} else
				createListener.onCreated(proposedModel, status);
		}
	};

	private UserModel asker;

	/**
	 * Mendapatkan daftar kanvas di mana pengguna sebagai pemilik atau
	 * partisipan.
	 * @param user pengguna yang dimaksud.
	 */
	public void getCanvasList(UserModel user) {
		JSONObject request = new JSONObject();
		try {
			asker = user;
			request.put(Request.USER_ID, user.collaID);
			request.put(Request.ACTION, Request.Action.LIST);
			new Client(CANVAS_URL, listReply).execute(request);
		} catch (JSONException e) {
			listReply.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ArrayList<CanvasModel> ownList = new ArrayList<CanvasModel>();
	private final ArrayList<CanvasModel> oldList = new ArrayList<CanvasModel>();
	private final ArrayList<CanvasModel> newList = new ArrayList<CanvasModel>();

	private final ReplyListener listReply = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			// TODO json browser
			if (status == SUCCESS) {
				try {
					// -------- process owned -----
					JSONArray owns = reply.getJSONArray(Reply.OWNED_LIST);
					ownList.clear();
					for (int i = 0; i < owns.length(); i++) {
						JSONObject canvas = owns.getJSONObject(i);
						String name = canvas.getString(Reply.CANVAS_NAME);
						int width = canvas.getInt(Reply.CANVAS_WIDTH);
						int height = canvas.getInt(Reply.CANVAS_HEIGHT);

						CanvasModel model = new CanvasModel(asker, name, width,
								height);

						model.setid(canvas.getInt(Reply.CANVAS_ID));
						ownList.add(model);
					}

					// -------- process old list -----
					JSONArray olds = reply.getJSONArray(Reply.OLD_LIST);
					oldList.clear();
					for (int i = 0; i < olds.length(); i++) {
						JSONObject canvas = olds.getJSONObject(i);
						UserModel owner = new UserModel();
						owner.collaID = canvas.getInt(Reply.OWNER_ID);
						owner.name = canvas.getString(Reply.OWNER_NAME);
						String name = canvas.getString(Reply.CANVAS_NAME);
						int width = canvas.getInt(Reply.CANVAS_WIDTH);
						int height = canvas.getInt(Reply.CANVAS_HEIGHT);
						CanvasModel model = new CanvasModel(owner, name, width,
								height);
						model.setid(canvas.getInt(Reply.CANVAS_ID));
						oldList.add(model);
					}
					// -------- process invited -----
					JSONArray news = reply.getJSONArray(Reply.INVITATION_LIST);
					newList.clear();
					for (int i = 0; i < olds.length(); i++) {
						JSONObject canvas = news.getJSONObject(i);
						UserModel owner = new UserModel();
						owner.collaID = canvas.getInt(Reply.OWNER_ID);
						owner.name = canvas.getString(Reply.OWNER_NAME);
						String name = canvas.getString(Reply.CANVAS_NAME);
						int width = canvas.getInt(Reply.CANVAS_WIDTH);
						int height = canvas.getInt(Reply.CANVAS_HEIGHT);
						CanvasModel model = new CanvasModel(owner, name, width,
								height);
						model.setid(canvas.getInt(Reply.CANVAS_ID));
						newList.add(model);
					}

					listFetchListener.onListFetched(asker, SUCCESS, ownList,
							oldList, newList);
				} catch (JSONException e) {
					listFetchListener.onListFetched(asker, UNKNOWN_REPLY,
							oldList, oldList, newList);
				}
			} else
				listFetchListener.onListFetched(asker, status, ownList, oldList,
						newList);
		}
	};

	/**
	 * Menghapus suatu kanvas
	 * @param model
	 */
	public void deleteCanvas(UserModel user, CanvasModel model) {
		// TODO delete canvas

	}
}
