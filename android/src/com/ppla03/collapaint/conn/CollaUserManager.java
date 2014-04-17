package com.ppla03.collapaint.conn;

import org.json.JSONException;
import org.json.JSONObject;

import com.ppla03.collapaint.model.UserModel;

/**
 * Mengurusi manajemen akun
 * @author hamba v7
 * 
 */
public class CollaUserManager extends ServerConnector {

	private static String CREATE_URL = HOST + "newuser";

	/**
	 * Listener saat ada jawaban server terkait pendaftaran pengguna baru.
	 * @author hamba v7
	 * 
	 */
	public static interface OnUserCheckListener {

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
		void onChecked(UserModel user, int status);
	}

	static class UserJCode {

		static final String COLLA_ID = "id";
		static final String ACCOUNT_ID = "acid";
		static final String NAME = "name";
		// --- reply ---
		static final String ERROR = "error";
		static final String STATUS = "status";
		static final int NEW = 3;
		static final int EXIST = 9;
	}

	private static CollaUserManager instance;
	private OnUserCheckListener listener;

	private UserModel checkUser;
	private UserModel currentUser;

	private CollaUserManager() {}

	public static CollaUserManager getInstance() {
		if (instance == null)
			instance = new CollaUserManager();
		return instance;
	}

	/**
	 * Mendapatkan user yang sedang aktif sekarang
	 */
	public static UserModel getCurrentUser() {
		if (getInstance().currentUser == null) {
			//TODO change id
			instance.currentUser = new UserModel(3, "anonymous@collapaint.com",
					"Mr. Anonymous");
		}
		return instance.currentUser;
	}

	/**
	 * Mendaftarkan akun ke server.
	 * @param name
	 */
	public void check(String accountID, String nickname) {
		checkUser = new UserModel();
		checkUser.accountID = accountID;
		checkUser.nickname = nickname;

		JSONObject request = new JSONObject();
		try {
			request.put(UserJCode.NAME, nickname);
			request.put(UserJCode.ACCOUNT_ID, accountID);
			new Client(CREATE_URL, replisCheck).execute(request);
		} catch (JSONException e) {
			replisCheck.process(INTERNAL_PROBLEM, null);
		}
	}

	private final ReplyListener replisCheck = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				if (reply.has(UserJCode.ERROR))
					listener.onChecked(checkUser, SERVER_PROBLEM);
				else {
					try {
						checkUser.collaID = reply.getInt(UserJCode.COLLA_ID);
						currentUser = checkUser;
						listener.onChecked(checkUser, SUCCESS);
					} catch (JSONException e) {
						listener.onChecked(checkUser, UNKNOWN_REPLY);
					}
				}
			} else
				listener.onChecked(checkUser, status);
		}
	};
}
