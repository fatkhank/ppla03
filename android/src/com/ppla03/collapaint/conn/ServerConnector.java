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

public class ServerConnector {
	public static final String HOST = "http://192.168.43.64:8080/collapaint/";

	/**
	 * Operasi berhasil dan mendapatkan jawaban dari server dalam bentuk yang
	 * diinginkan.
	 */
	public static final int SUCCESS = 1;

	/**
	 * Terjadi masalah pada modul koneksi ke server.
	 */
	public static final int INTERNAL_PROBLEM = 2;

	/**
	 * Terjadi masalah koneksi.
	 */
	public static final int CONNECTION_PROBLEM = 3;

	/**
	 * Terjadi masalah pada server.
	 */
	public static final int SERVER_PROBLEM = 4;

	/**
	 * Jawaban dari server tidak dapat diterjemahkan
	 */
	public static final int UNKNOWN_REPLY = 5;
	
	private static final int TIMEOUT = 10000;

	protected interface ReplyListener {
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
				return SUCCESS;
			} catch (UnsupportedEncodingException ue) {
				return INTERNAL_PROBLEM;
			} catch (ClientProtocolException e) {
				return CONNECTION_PROBLEM;
			} catch (IOException e) {
				return CONNECTION_PROBLEM;
			} catch (JSONException e) {
				return UNKNOWN_REPLY;
			}catch (Exception e) {
				return INTERNAL_PROBLEM;
			}
		}

		@Override
		protected void onPostExecute(Integer status) {
			listener.process(status, reply);
		}

	}
}
