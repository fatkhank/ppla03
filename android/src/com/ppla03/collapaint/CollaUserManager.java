package com.ppla03.collapaint;

import org.json.JSONException;
import org.json.JSONObject;

import collapaint.code.UserJCode;
import collapaint.code.UserJCode.Reply;
import collapaint.code.UserJCode.Request;

import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.UserModel;

/**
 * Mengurusi manajemen akun.
 * @author hamba v7
 * 
 */
public class CollaUserManager extends ServerConnector {
	private static final int USER_ID_IF_NULL = 1;
	private static final String USER_EMAIL_IF_NULL = "someone@gmail.com";
	private static final String USER_NAME_IF_NULL = "Mr. Anonymous";

	private static String CREATE_URL = HOST + "user";

	protected void onHostAddressChange(String host) {
		CREATE_URL = host + "user";
	}

	/**
	 * Listener saat ada jawaban server terkait pengecekan suatu akun.
	 * @author hamba v7
	 * 
	 */
	public static interface OnUserCheckListener {

		/**
		 * Dipicu saat ada jawaban dari server terkait pengecekan akun. Jika
		 * sukses, berarti akun berhasil terdaftar di server.
		 * @param status status.<br/>
		 *            Succes bila bernilai {@link ServerConnector#SUCCESS}.<br/>
		 *            Gagal bila bernilai
		 *            {@link ServerConnector#CONNECTION_PROBLEM},
		 *            {@link ServerConnector#INTERNAL_PROBLEM},
		 *            {@link ServerConnector#SERVER_PROBLEM}, atau,
		 *            {@link ServerConnector#UNKNOWN_REPLY}
		 */
		public void onAccountChecked(int status);
	}

	private static final CollaUserManager instance = new CollaUserManager();
	private static OnUserCheckListener listener;

	private static UserModel checkUser;
	private static UserModel currentUser = new UserModel(USER_ID_IF_NULL,
			USER_EMAIL_IF_NULL, USER_NAME_IF_NULL);

	private CollaUserManager() {}

	/**
	 * Mendapatkan user yang sedang aktif sekarang.
	 */
	public static UserModel getCurrentUser() {
		return currentUser;
	}

	/**
	 * Mengecek status akun ke server.
	 */
	public static void check(String accountID, String nickname,
			OnUserCheckListener list) {
		listener = list;
		checkUser = new UserModel();
		checkUser.accountID = accountID;
		checkUser.name = nickname;

		JSONObject request = new JSONObject();
		try {
			request.put(Request.USER_NAME, nickname);
			request.put(Request.USER_EMAIL, accountID);
			request.put(Request.ACTION, Request.ACTION_CHECK);

			instance.check(request);
		} catch (JSONException e) {
			replisCheck.process(INTERNAL_PROBLEM, null);
		}
	}

	private void check(JSONObject request) {
		new Client(CREATE_URL, replisCheck).execute(request);
	}

	private static final ReplyListener replisCheck = new ReplyListener() {

		@Override
		public void process(int status, JSONObject reply) {
			if (status == SUCCESS) {
				if (reply.has(UserJCode.ERROR))
					listener.onAccountChecked(SERVER_PROBLEM);
				else {
					try {
						checkUser.collaID = reply.getInt(Reply.USER_ID);
						currentUser = checkUser;
						listener.onAccountChecked(SUCCESS);
					} catch (JSONException e) {
						listener.onAccountChecked(UNKNOWN_REPLY);
					}
				}
			} else
				listener.onAccountChecked(status);
		}
	};

	/**
	 * Melogout pengguna sekarang
	 */
	public static void logout() {

	}
}
