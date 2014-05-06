package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

/**
 * Menangani manajemen penggunaan kanvas oleh user.
 * @author hamba v7
 * 
 */
public class BrowserConnector extends ServerConnector {
	private static String CREATE_URL = HOST + "create";
	private static String LIST_URL = HOST + "list";

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

	static class CreateJCode {

		// --- request ---
		static final String OWNER_ID = "oid";
		static final String CANVAS_NAME = "name";
		static final String CANVAS_WIDTH = "width";
		static final String CANVAS_HEIGHT = "height";
		// --- reply ---
		static final String CANVAS_ID = "id";
		static final String RESULT_ERROR = "error";
		static final int DUPLICATE_NAME = 2;
		static final int USER_UNKNOWN = 8;
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
			//TODO create canvas top left =0

			msg.put(CreateJCode.OWNER_ID, owner.collaID);
			msg.put(CreateJCode.CANVAS_NAME, name);
			msg.put(CreateJCode.CANVAS_WIDTH, width);
			msg.put(CreateJCode.CANVAS_HEIGHT, height);

			new Client(CREATE_URL, createReply).execute(msg);
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
					if (reply.has(CreateJCode.RESULT_ERROR)) {
						int error = reply.getInt(CreateJCode.RESULT_ERROR);
						if (error == CreateJCode.DUPLICATE_NAME)
							createListener.onCreated(proposedModel,
									CanvasCreationListener.DUPLICATE_NAME);
						else if (error == CreateJCode.USER_UNKNOWN)
							createListener.onCreated(proposedModel,
									CanvasCreationListener.USER_UNKNOWN);
					} else {
						proposedModel
								.setid(reply.getInt(CreateJCode.CANVAS_ID));
						createListener.onCreated(proposedModel, SUCCESS);
					}
				} catch (JSONException e) {
					createListener.onCreated(proposedModel, UNKNOWN_REPLY);
				}
			} else
				createListener.onCreated(proposedModel, status);
		}
	};

	static class ListJCode {

		// --- request ---
		static final String USER_ID = "uid";
		// --- reply ---
		static final String CANVAS_OWNED = "own";
		static final String CANVAS_OLD = "old";
		static final String CANVAS_NEW = "new";
		static final String CANVAS_ID = "i";
		static final String NAME = "n";
		static final String WIDTH = "w";
		static final String HEIGHT = "h";
		static final String OWNER_ID = "o";
		static final String OWNER_NAME = "on";
		static final String ERROR = "error";
	}

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
			request.put(ListJCode.USER_ID, user.collaID);
			new Client(LIST_URL, listReply).execute(request);
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
					JSONArray owns = reply.getJSONArray(ListJCode.CANVAS_OWNED);
					ownList.clear();
					for (int i = 0; i < owns.length(); i++) {
						JSONObject canvas = owns.getJSONObject(i);
						String name = canvas.getString(ListJCode.NAME);
						int width = canvas.getInt(ListJCode.WIDTH);
						int height = canvas.getInt(ListJCode.HEIGHT);
						CanvasModel model = new CanvasModel(asker, name, width,
								height);
						model.setid(canvas.getInt(ListJCode.CANVAS_ID));
						ownList.add(model);
					}
					// -------- process old list -----
					JSONArray olds = reply.getJSONArray(ListJCode.CANVAS_OLD);
					oldList.clear();
					for (int i = 0; i < olds.length(); i++) {
						JSONObject canvas = olds.getJSONObject(i);
						UserModel owner = new UserModel();
						owner.collaID = canvas.getInt(ListJCode.OWNER_ID);
						String name = canvas.getString(ListJCode.NAME);
						int width = canvas.getInt(ListJCode.WIDTH);
						int height = canvas.getInt(ListJCode.HEIGHT);
						CanvasModel model = new CanvasModel(owner, name, width,
								height);
						model.setid(canvas.getInt(ListJCode.CANVAS_ID));
						oldList.add(model);
					}
					// -------- process invited -----
					JSONArray news = reply.getJSONArray(ListJCode.CANVAS_NEW);
					newList.clear();
					for (int i = 0; i < olds.length(); i++) {
						JSONObject canvas = news.getJSONObject(i);
						UserModel owner = new UserModel();
						owner.collaID = canvas.getInt(ListJCode.OWNER_ID);
						owner.name = canvas.getString(ListJCode.OWNER_NAME);
						String name = canvas.getString(ListJCode.NAME);
						int width = canvas.getInt(ListJCode.WIDTH);
						int height = canvas.getInt(ListJCode.HEIGHT);
						CanvasModel model = new CanvasModel(owner, name, width,
								height);
						model.setid(canvas.getInt(ListJCode.CANVAS_ID));
						newList.add(model);
					}

					listFetchListener.onListFethed(asker, SUCCESS, ownList,
							oldList, newList);
				} catch (JSONException e) {
					listFetchListener.onListFethed(asker, UNKNOWN_REPLY,
							oldList, oldList, newList);
				}
			} else
				listFetchListener.onListFethed(asker, status, ownList, oldList,
						newList);
		}
	};
	
	/**
	 * Menghapus suatu kanvas
	 * @param model
	 */
	public void deleteCanvas(UserModel user, CanvasModel model){
		//TODO delete canvas
		
	}
}
