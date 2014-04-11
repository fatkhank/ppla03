package com.ppla03.collapaint.conn;

import org.json.JSONException;
import org.json.JSONObject;

import com.ppla03.collapaint.model.UserModel;

/**
 * Mengurusi manajemen akun
 * @author hamba v7
 * 
 */
public class UserConnector extends ServerConnector {
	private static String CREATE_URL = HOST + "newuser";

	/**
	 * Listener saat ada jawaban server terkait pendaftaran pengguna baru.
	 * @author hamba v7
	 * 
	 */
	public static interface OnUserCreateListener {
		/**
		 * Username sudah digunakan.
		 */
		int DUPLICATE_USERNAME = 3;

		/**
		 * Dipicu saat ada jawaban dari server terkait pendaftar akun.
		 * @param user pengguna yang didaftarkan
		 * @param status hasil <br/>
		 *            {@link ServerConnector#SUCCESS} berarti akun berhasil
		 *            dibuat. <br/>
		 *            Bisa juga berisi {@link #DUPLICATE_USERNAME},
		 *            {@link ServerConnector#CONNECTION_PROBLEM},
		 *            {@link ServerConnector#UNKNOWN_REPLY},
		 *            {@link ServerConnector#SERVER_PROBLEM}, atau
		 *            {@link ServerConnector#INTERNAL_PROBLEM}
		 */
		void onCreated(UserModel user, int status);
	}

	static class UserJCode {

		static final String NAME = "name";
		static final String ID = "id";
		static final String ERROR = "error";
		static final int DUPLICATE_NAME = 3;
	}

	private static UserConnector instance;
	private OnUserCreateListener listener;

	private UserModel proposedAccount;

	private UserConnector() {}

	public static UserConnector getInstance() {
		if (instance == null)
			instance = new UserConnector();
		return instance;
	}

	/**
	 * Mendaftarkan akun ke server.
	 * @param name
	 */
	public void register(String name) {
		proposedAccount = new UserModel();
		proposedAccount.username = name.toLowerCase();
		JSONObject request = new JSONObject();
		try {
			request.put(UserJCode.NAME, name);
			new Client(CREATE_URL, createReply).execute(request);
		} catch (JSONException e) {
			createReply.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ReplyListener createReply = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				if (reply.has(UserJCode.ERROR))
					listener.onCreated(proposedAccount,
							OnUserCreateListener.DUPLICATE_USERNAME);
				else {
					try {
						if (reply.get(UserJCode.NAME).equals(
								proposedAccount.username)) {
							proposedAccount.id = reply.getInt(UserJCode.ID);
							listener.onCreated(proposedAccount, SUCCESS);
						}
					} catch (JSONException e) {
						listener.onCreated(proposedAccount, UNKNOWN_REPLY);
					}
				}
			} else
				listener.onCreated(proposedAccount, status);
		}
	};
}
