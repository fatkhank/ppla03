package com.ppla03.collapaint.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public abstract class ServerConnector {
	public static String HOST = "http://192.168.43.64:8080/collapaint/";
//	public static String HOST = "http://10.5.134.128:8080/collapaint/";

	protected abstract void onHostAddressChange(String host);

	/**
	 * Operasi berhasil dan mendapatkan jawaban dari server dalam bentuk yang
	 * diinginkan.
	 */
	public static final int SUCCESS = 1;

	/**
	 * Terjadi masalah pada modul koneksi ke server.
	 */
	public static final int INTERNAL_PROBLEM = 32768;

	/**
	 * Terjadi masalah koneksi.
	 */
	public static final int CONNECTION_PROBLEM = 4096;

	/**
	 * Terjadi masalah pada server.
	 */
	public static final int SERVER_PROBLEM = 8192;

	/**
	 * Jawaban dari server tidak dapat diterjemahkan atau formatnya tidak sesuai
	 * dengan format yang diinginkan.
	 */
	public static final int UNKNOWN_REPLY = 16384;

	/**
	 * Waktu tunggu jawaban dari server.
	 */
	private static final int TIMEOUT = 10000;

	/**
	 * Listener saat jawaban dari server diterima.
	 * 
	 * @author hamba v7
	 * 
	 */
	protected interface ReplyListener {
		/**
		 * Jawaban telah diterima
		 * 
		 * @param status
		 *            status jawaban
		 * @param reply
		 *            jawaban dari server, atau null jika {@code status} !=
		 *            {@link ServerConnector#SUCCESS}.
		 * @see {@link ServerConnector#SUCCESS},
		 *      {@link ServerConnector#INTERNAL_PROBLEM},
		 *      {@link ServerConnector#SERVER_PROBLEM},
		 *      {@link ServerConnector#UNKNOWN_REPLY},
		 *      {@link ServerConnector#CONNECTION_PROBLEM}
		 */
		void process(int status, JSONObject reply);
	}

	protected final class Client extends AsyncTask<JSONObject, Void, Integer> {
		private final ReplyListener listener;
		private final String url;
		private JSONObject reply;

		public Client(String url, ReplyListener listener) {
			this.listener = listener;
			this.url = url;
		}

		@Override
		protected Integer doInBackground(JSONObject... msgs) {
			try {
				HttpClient client = new DefaultHttpClient();

				HttpPost post = new HttpPost(url);
				post.setHeader("Content-type", "application/json");
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);

				// TODO debug server connection
				android.util.Log.d("POS", "send:" + msgs[0].toString() + ":"
						+ url);

				post.setParams(params);
				post.setEntity(new StringEntity(msgs[0].toString()));
				HttpResponse res = client.execute(post);

				InputStream is = res.getEntity().getContent();

				int status = res.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
					return SERVER_PROBLEM;
				if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
					return CONNECTION_PROBLEM;

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String st = null;
				while ((st = br.readLine()) != null)
					sb.append(st);

				reply = new JSONObject(sb.toString());

				// TODO debug server connection
				android.util.Log.d("POS", "rep:" + reply.toString());

				return SUCCESS;
			} catch (UnsupportedEncodingException ue) {
				return INTERNAL_PROBLEM;
			} catch (ClientProtocolException e) {
				return CONNECTION_PROBLEM;
			} catch (IOException e) {
				return CONNECTION_PROBLEM;
			} catch (JSONException e) {
				return UNKNOWN_REPLY;
			} catch (Exception e) {
				return INTERNAL_PROBLEM;
			}
		}

		@Override
		protected void onPostExecute(Integer status) {
			listener.process(status, reply);
		}

	}
}
