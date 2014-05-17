package com.ppla03.collapaint.ui;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputFilter;
import android.text.LoginFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.ppla03.collapaint.CanvasSynchronizer;
import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.BrowserConnector;
import com.ppla03.collapaint.conn.CanvasCreationListener;
import com.ppla03.collapaint.conn.OnFetchListListener;
import com.ppla03.collapaint.conn.ParticipantManager;
import com.ppla03.collapaint.conn.ParticipantManager.InviteResponse;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

public class BrowserActivity extends Activity implements OnClickListener,
		OnItemClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		CanvasCreationListener, OnFetchListListener, AnimatorListener,
		AnimatorUpdateListener {
	private Button mSignOutButton;
	private Button mCreateButton;
	private TextView username;
	private GoogleApiClient mGoogleApiClient;

	// --- create ---
	private View createView;
	private Button showCreate, createButton;
	private EditText nameInput, widthInput, heightInput;
	private ValueAnimator animCreate;

	// --- canvas list ---
	private ImageButton reloadButton;
	private ProgressBar reloadProgress;
	private TextView listInfo;

	private ListView canvasList;
	private TextView canvasHeader;
	private CanvasListAdapter canvasAdapter;

	// --- invitation list ---
	private ListView inviteList;
	private TextView inviteHeader;
	private InvitationAdapter inviteAdapter;

	static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;
	static final String DEFAULT_NAME = "New canvas";
	Handler reloader;

	static final InputFilter[] canvasDimFilter = new InputFilter[] { new InputFilter.LengthFilter(
			4) };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_list);

			// Get the message from the intent
			Intent intent = getIntent();
			String message = intent
					.getStringExtra(AuthenticationActivity.EXTRA_MESSAGE);

			mSignOutButton = (Button) findViewById(R.id.d_parti_reload);
			mSignOutButton.setOnClickListener(this);
			mCreateButton = (Button) findViewById(R.id.b_create_show);
			mCreateButton.setOnClickListener(this);

			// Create the text view
			username = (TextView) findViewById(R.id.w_stroke_label);
			username.setText("Hello, " + message);
			mGoogleApiClient = buildGoogleApiClient();

			// --- setup ---
			BrowserConnector.getInstance().setCreateListener(this);

			// --- create canvas ---
			createView = findViewById(R.id.b_create_pane);
			createView.setVisibility(View.GONE);

			nameInput = (EditText) createView.findViewById(R.id.b_create_name);

			widthInput = (EditText) createView
					.findViewById(R.id.b_create_width);
			widthInput.setFilters(canvasDimFilter);
			widthInput.setText(String.valueOf(DEFAULT_WIDTH));

			heightInput = (EditText) createView
					.findViewById(R.id.b_create_height);
			heightInput.setFilters(canvasDimFilter);
			heightInput.setText(String.valueOf(DEFAULT_HEIGHT));

			createButton = (Button) findViewById(R.id.b_create);

			animCreate = new ValueAnimator();

			reloader = new Handler();

			// ---- canvas list ---
			reloadButton = (ImageButton) findViewById(R.id.b_list_reload);
			reloadButton.setOnClickListener(this);
			listInfo = (TextView) findViewById(R.id.b_list_info);

			canvasAdapter = new CanvasListAdapter(this);
			canvasHeader = (TextView) findViewById(R.id.b_canvas_header);
			canvasList = (ListView) findViewById(R.id.b_canvas_list);
			canvasList.setAdapter(canvasAdapter);
			canvasList.setOnItemClickListener(this);

			// --- invitation list ---
			inviteHeader = (TextView) findViewById(R.id.b_invitation_header);
			inviteList = (ListView) findViewById(R.id.b_invitation_list);
			inviteAdapter = new InvitationAdapter(this);

			loadCanvasList();
		} catch (Exception ex) {
			android.util.Log.d("POS", "e:" + ex);
			for (StackTraceElement s : ex.getStackTrace()) {
				android.util.Log.d("POS", "ex:" + s);
			}
		}

	}

	private GoogleApiClient buildGoogleApiClient() {
		// When we build the GoogleApiClient we specify where connected and
		// connection failed callbacks should be returned, which Google APIs our
		// app uses and which OAuth 2.0 scopes our app requests.
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	/**
	 * Mengambil daftar kanvas.
	 */
	void loadCanvasList() {
		BrowserConnector.getInstance().setListFetchListener(this)
				.getCanvasList(CollaUserManager.getCurrentUser());
		canvasHeader.setVisibility(View.GONE);
		canvasList.setVisibility(View.GONE);
		inviteHeader.setVisibility(View.GONE);
		inviteList.setVisibility(View.GONE);
		reloadProgress.setVisibility(View.VISIBLE);
		listInfo.setText("Get canvas list");
	}

	@Override
	public void onBackPressed() {
		// TODO close
		finish();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.d_parti_reload:
			// TODO warning signout
			AuthenticationActivity.TERM = true;
			finish();
			break;
		case R.id.b_create_show:
			if (createView.getVisibility() == View.VISIBLE) {
				// hide
				createView.setVisibility(View.GONE);
			} else {
				// show
				createView.setVisibility(View.VISIBLE);
				nameInput.setText(DEFAULT_NAME);
			}
			break;
		case R.id.b_list_reload:
			loadCanvasList();
			break;
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int arg0) {}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {}

	@Override
	public void onCreated(CanvasModel newCanvas, int status) {
		if (status == ServerConnector.SUCCESS) {
			CanvasSynchronizer.getInstance().setCanvas(newCanvas);
			Intent intent = new Intent(this, LoaderActivity.class);
			startActivity(intent);
		} else {
			String msg;
			if (status == CanvasCreationListener.DUPLICATE_NAME) {
				// TODO show create dialog
				msg = "Canvas with same name is already exist. Try different name.";
			} else if (status == CanvasCreationListener.NOT_AUTHORIZED) {
				msg = "User is unregistered.";
			} else if (status == ServerConnector.CONNECTION_PROBLEM) {
				msg = "Connection problem.";
			} else
				msg = "System error.";
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// if (dialog == createDialog) {
	// if (which == DialogInterface.BUTTON_POSITIVE) {
	// String name = nameInput.getText().toString();
	// int width = Integer.parseInt(widthInput.getText().toString());
	// int height = Integer.parseInt(heightInput.getText().toString());
	//
	// String msg = null;
	// if (width <= 0)
	// msg = "Canvas width cannot be zero";
	// else if (height <= 0)
	// msg = "Canvas height cannot be zero";
	// else if (name.isEmpty())
	// msg = "Canvas name cannot be empty";
	//
	// if (msg != null) {
	// Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	// reloader.postDelayed(showCreateDialog, 500);
	// } else {
	// BrowserConnector.getInstance().createCanvas(
	// CollaUserManager.getCurrentUser(), name, width,
	// height);
	// loaderCover.setVisibility(View.VISIBLE);
	// }
	// }
	// }
	// }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == canvasList) {
			CanvasSynchronizer.getInstance().setCanvas(
					canvasAdapter.getItem(position));
			Intent intent = new Intent(this, LoaderActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onListFethed(UserModel asker, int status,
			ArrayList<CanvasModel> owned, ArrayList<CanvasModel> oldList,
			ArrayList<CanvasModel> invited) {
		if (status == ServerConnector.SUCCESS) {
			inviteAdapter.clear();
			inviteAdapter.addAll(invited);
			inviteHeader.setVisibility(View.VISIBLE);
			inviteList.setVisibility(View.VISIBLE);

			canvasAdapter.clear();
			canvasAdapter.addAll(owned);
			canvasAdapter.addAll(oldList);
			canvasHeader.setVisibility(View.VISIBLE);
			canvasList.setVisibility(View.VISIBLE);

			reloadProgress.setVisibility(View.GONE);
			listInfo.setVisibility(View.GONE);

			// listProgress.setVisibility(View.GONE);
			// if (canvasAdapter.isEmpty())
			// listText.setText("You have no canvas.");
			// else {
			// listText.setVisibility(View.GONE);
			// listProgress.setVisibility(View.INVISIBLE);
			// canvasList.setVisibility(View.VISIBLE);
			// }
		} else {
			// listProgress.setVisibility(View.GONE);
			// listReload.setVisibility(View.VISIBLE);
			// listText.setText(getResources().getString(R.string.bcl_failed));
			String msg;
			if (status == ServerConnector.CONNECTION_PROBLEM) {
				msg = "Connection problem.";
			} else
				msg = "System error.";
			Log.d("POS", "fetch:" + status);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDeleted(CanvasModel model, int status) {
		// TODO Auto-generated method stub

	}

	/**
	 * Meresponse terhadap suatu undangan.
	 * @param model
	 * @param response
	 */
	void responseInvitation(CanvasModel model, InviteResponse response) {
		// TODO response invitation
	}

	/**
	 * Menghapus kanvas yang dimiliki seorang user
	 * @param model kanvas milik user yang mau dihapus
	 */
	void deleteCanvas(CanvasModel model) {
		// TODO delete canvas
	}

	/**
	 * Menghapus partisiasi user pada suatu kanvas. (Me-kick dirinya sendiri).
	 * @param model kanvas yang dimaksud
	 */
	void removeParticipation(CanvasModel model) {
		// TODO remove participation
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub

	}
}
