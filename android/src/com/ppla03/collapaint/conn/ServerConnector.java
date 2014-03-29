package com.ppla03.collapaint.conn;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class ServerConnector {
	protected interface ReplyListener {
		void process(JSONObject reply);
	}

	protected final class Client extends
			AsyncTask<JSONObject, Void, JSONObject> {
		private final ReplyListener listener;
		private final String url;

		public Client(String url, ReplyListener listener) {
			this.listener = listener;
			this.url = url;
		}

		@Override
		protected JSONObject doInBackground(JSONObject... params) {
			// TODO Auto-generated method stub
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(url);
				post.setHeader("Content-type", "application/json");

				post.setEntity(new StringEntity(params[0].toString()));
				HttpResponse res = client.execute(post);

				InputStream is = res.getEntity().getContent();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String st = null;
				while ((st = br.readLine()) != null)
					sb.append(st);

				return new JSONObject(sb.toString());
			} catch (Exception e) {
				JSONObject result = new JSONObject();
				try {
					result.put("error", e.getMessage());
				} catch (JSONException e1) {
				}
				return result;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			listener.process(result);
		}

	}
}
