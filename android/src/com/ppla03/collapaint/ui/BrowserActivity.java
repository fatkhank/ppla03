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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.ppla03.collapaint.conn.OnKickUserListener;
import com.ppla03.collapaint.conn.ParticipantManager;
import com.ppla03.collapaint.conn.ParticipantManager.InviteResponse;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

public class BrowserActivity extends Activity implements OnClickListener,
		OnItemClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		CanvasCreationListener, OnFetchListListener, OnKickUserListener,
		AnimatorListener, AnimatorUpdateListener {
	private Button mSignOutButton;
	private TextView username;
	private GoogleApiClient mGoogleApiClient;

	// --- create ---
	private View createView;
	private CheckBox showCreate;
	private Button createButton;
	private ProgressBar createProggress;
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

			mSignOutButton = (Button) findViewById(R.id.b_signout);
			mSignOutButton.setOnClickListener(this);

			// Create the text view
			username = (TextView) findViewById(R.id.b_user_name);
			username.setText("Hello, " + CollaUserManager.getCurrentUser().name);
			mGoogleApiClient = buildGoogleApiClient();

			// --- setup ---
			BrowserConnector.getInstance().setCreateListener(this);

			// --- create canvas ---
			showCreate = (CheckBox) findViewById(R.id.b_create_show);
			showCreate.setOnClickListener(this);
			showCreate.setChecked(false);

			createView = findViewById(R.id.b_create_view);
			createView.setVisibility(View.GONE);

			createProggress = (ProgressBar) findViewById(R.id.b_create_progress);
			createProggress.setVisibility(View.GONE);

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
			createButton.setOnClickListener(this);

			animCreate = new ValueAnimator();
			animCreate.setDuration(500);
			animCreate.addUpdateListener(this);
			animCreate.addListener(this);

			reloader = new Handler();

			// ---- canvas list ---
			reloadButton = (ImageButton) findViewById(R.id.b_list_reload);
			reloadButton.setOnClickListener(this);
			reloadProgress = (ProgressBar) findViewById(R.id.b_list_loader_progress);
			listInfo = (TextView) findViewById(R.id.b_list_info);

			canvasAdapter = new CanvasListAdapter(this);
			canvasHeader = (TextView) findViewById(R.id.b_canvas_header);
			canvasList = (ListView) findViewById(R.id.b_canvas_list);
			canvasList.setAdapter(canvasAdapter);
			canvasList.setOnItemClickListener(this);

			// --- invitation list ---
			inviteHeader = (TextView) findViewById(R.id.b_invitation_header);
			inviteAdapter = new InvitationAdapter(this);
			inviteList = (ListView) findViewById(R.id.b_invitation_list);
			inviteList.setAdapter(inviteAdapter);
			inviteList.setOnItemClickListener(this);

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
		reloadButton.setVisibility(View.GONE);
		listInfo.setText("Get canvas list");
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	public void onClick(View v) {
		if (v == mSignOutButton) {
			AuthenticationActivity.TERM = true;
			Intent intent = new Intent(this, AuthenticationActivity.class);
			startActivity(intent);
			finish();
		} else if (v == showCreate) {
			if (showCreate.isChecked()) {
				// show
				if (createView.getVisibility() == View.GONE)
					createView.setX(-createView.getWidth());
				animCreate.setFloatValues(createView.getX(), 0);
			} else {
				// hide
				animCreate.setFloatValues(createView.getX(),
						-createView.getWidth());
			}
			animCreate.start();
		} else if (v == reloadButton) {
			loadCanvasList();
		} else if (v == createButton) {
			String canvasName = nameInput.getText().toString();
			int width = Integer.parseInt(widthInput.getText().toString());
			int height = Integer.parseInt(heightInput.getText().toString());
			BrowserConnector.getInstance().createCanvas(
					CollaUserManager.getCurrentUser(), canvasName, width,
					height);
		}
	}

	@Override
	public void onConnected(Bundle arg0) {}

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
			finish();
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == canvasList) {
			CanvasSynchronizer.getInstance().setCanvas(
					canvasAdapter.getItem(position));
			Intent intent = new Intent(this, LoaderActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onListFetched(UserModel asker, int status,
			ArrayList<CanvasModel> owned, ArrayList<CanvasModel> oldList,
			ArrayList<CanvasModel> invited) {

		reloadButton.setVisibility(View.VISIBLE);
		reloadProgress.setVisibility(View.GONE);

		if (status == ServerConnector.SUCCESS) {
			if (!invited.isEmpty()) {
				inviteAdapter.clear();
				inviteAdapter.addAll(invited);
				inviteHeader.setVisibility(View.VISIBLE);
				inviteList.setVisibility(View.VISIBLE);
			}

			canvasAdapter.clear();
			canvasAdapter.addAll(owned);
			canvasAdapter.addAll(oldList);
			canvasHeader.setVisibility(View.VISIBLE);
			canvasList.setVisibility(View.VISIBLE);

			listInfo.setVisibility(View.GONE);

			if (canvasAdapter.isEmpty())
				listInfo.setText("You have no canvas.");

		} else {

			listInfo.setText(getResources().getString(R.string.bcl_failed));
			String msg;
			if (status == ServerConnector.CONNECTION_PROBLEM) {
				msg = "Connection problem.";
			} else
				msg = "System error.";
			listInfo.setText(msg);
			// Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDeleted(CanvasModel model, int status) {
		loadCanvasList();
	}

	/**
	 * Meresponse terhadap suatu undangan.
	 * @param model
	 * @param response
	 */
	void responseInvitation(CanvasModel model, InviteResponse response) {
		// TODO response invitation
		LayoutInflater li = this.getLayoutInflater();
		View promptsView;
		final InviteResponse hola = response;
		final CanvasModel model2 = model;

		promptsView = li.inflate(R.layout.dialog_sure, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(promptsView);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						final ParticipantManager hoho = new ParticipantManager();
						hoho.responseInvitation(model2,
								CollaUserManager.getCurrentUser(), hola);
						loadCanvasList();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	/**
	 * Menghapus kanvas yang dimiliki seorang user
	 * @param model kanvas milik user yang mau dihapus
	 */
	void deleteCanvas(CanvasModel model) {
		BrowserConnector.getInstance().deleteCanvas(
				CollaUserManager.getCurrentUser(), model);
	}

	/**
	 * Menghapus partisiasi user pada suatu kanvas. (Me-kick dirinya sendiri).
	 * @param model kanvas yang dimaksud
	 */
	void removeParticipation(CanvasModel model) {
		ParticipantManager.getInstance().kickUser(
				CollaUserManager.getCurrentUser(), model, this);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		Float value = (Float) animation.getAnimatedValue();
		createView.setX(value);
	}

	@Override
	public void onAnimationStart(Animator animation) {
		if (showCreate.isChecked()) {
			createView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		if (showCreate.isChecked()) {
			nameInput.setText(DEFAULT_NAME);
		} else
			createView.setVisibility(View.GONE);
	}

	@Override
	public void onAnimationCancel(Animator animation) {}

	@Override
	public void onAnimationRepeat(Animator animation) {}

	@Override
	public void onKickUser(UserModel user, CanvasModel model, int status) {
		loadCanvasList();
	}

}