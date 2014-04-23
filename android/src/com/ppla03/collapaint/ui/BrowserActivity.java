package com.ppla03.collapaint.ui;

import java.util.ArrayList;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.ppla03.collapaint.conn.OnCanvasCreateListener;
import com.ppla03.collapaint.conn.OnFetchListListener;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

public class BrowserActivity extends Activity implements View.OnClickListener,
		OnItemClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		OnCanvasCreateListener, DialogInterface.OnClickListener,
		OnFetchListListener {
	private Button mSignOutButton;
	private Button mCreateButton;
	private TextView username;
	private GoogleApiClient mGoogleApiClient;

	// --- create dialog ---
	private AlertDialog createDialog;
	private EditText nameInput, widthInput, heightInput;
	private Button loaderCancel;
	private RelativeLayout loaderCover;

	// --- canvas list ---
	private ListView canvasList;
	private TextView listText, listTitleCanvas, listTitleOwner;
	private ProgressBar listProgress;
	private Button listReload;
	private CanvasListAdapter canvasAdapter;

	static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;
	static final String DEFAULT_NAME = "New canvas";
	Handler reloader;

	static final InputFilter[] canvasDimFilter = new InputFilter[] { new InputFilter.LengthFilter(
			4) };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Get the message from the intent
		Intent intent = getIntent();
		String message = intent
				.getStringExtra(AuthenticationActivity.EXTRA_MESSAGE);

		mSignOutButton = (Button) findViewById(R.id.button1);
		mSignOutButton.setOnClickListener(this);
		mCreateButton = (Button) findViewById(R.id.b_create);
		mCreateButton.setOnClickListener(this);

		// Create the text view
		username = (TextView) findViewById(R.id.textView1);
		username.setText("Hello, " + message);
		mGoogleApiClient = buildGoogleApiClient();

		// --- setup ---
		BrowserConnector.getInstance().setCreateListener(this);
		android.util.Log.d("POS", "instance");

		// --- dialog create canvas ---
		AlertDialog.Builder createDB = new AlertDialog.Builder(this);
		View createView = getLayoutInflater().inflate(
				R.layout.dialog_create_canvas, null);
		createDB.setTitle("Create new canvas");
		nameInput = (EditText) createView.findViewById(R.id.bcd_name_input);
		widthInput = (EditText) createView.findViewById(R.id.bcd_width_input);
		heightInput = (EditText) createView.findViewById(R.id.bcd_height_input);
		widthInput.setFilters(canvasDimFilter);
		heightInput.setFilters(canvasDimFilter);
		widthInput.setText(String.valueOf(DEFAULT_WIDTH));
		heightInput.setText(String.valueOf(DEFAULT_HEIGHT));
		createDB.setView(createView);
		createDB.setPositiveButton("Create", this);
		createDB.setNegativeButton("Cancel", this);

		createDialog = createDB.create();
		loaderCover = (RelativeLayout) findViewById(R.id.b_loader_cover);
		loaderCover.setVisibility(View.GONE);
		loaderCancel = (Button) findViewById(R.id.b_loader_cancel);
		loaderCancel.setOnClickListener(this);
		loaderCancel.setVisibility(View.GONE);
		reloader = new Handler();

		// ---- canvas list ---
		canvasList = (ListView) findViewById(R.id.b_list_view);
		canvasAdapter = new CanvasListAdapter(this);
		canvasList.setAdapter(canvasAdapter);
		canvasList.setVisibility(View.INVISIBLE);
		canvasList.setOnItemClickListener(this);
		listReload = (Button) findViewById(R.id.b_list_reload);
		listReload.setVisibility(View.GONE);
		listReload.setOnClickListener(this);
		listText = (TextView) findViewById(R.id.b_list_text);
		listTitleCanvas = (TextView) findViewById(R.id.b_title_canvas);
		listTitleOwner = (TextView) findViewById(R.id.b_title_owner);
		listProgress = (ProgressBar) findViewById(R.id.b_list_progress);

		loadCanvasList();
	}

	private GoogleApiClient buildGoogleApiClient() {
		// When we build the GoogleApiClient we specify where connected and
		// connection failed callbacks should be returned, which Google APIs our
		// app uses and which OAuth 2.0 scopes our app requests.
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	void loadCanvasList() {
		BrowserConnector.getInstance().setListFetchListener(this)
				.getCanvasList(CollaUserManager.getCurrentUser());
		listReload.setVisibility(View.GONE);
		listTitleCanvas.setVisibility(View.GONE);
		listTitleOwner.setVisibility(View.GONE);
		listText.setText(getResources().getString(R.string.bcl_loading));
		listProgress.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onBackPressed() {
		//TODO close
		finish();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			//TODO warning signout
			AuthenticationActivity.TERM = true;
			finish();
			break;
		case R.id.b_create:
			nameInput.setText(DEFAULT_NAME);
			nameInput.selectAll();
			createDialog.show();
			break;
		case R.id.b_loader_cancel:
			loaderCover.setVisibility(View.GONE);
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
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreated(CanvasModel newCanvas, int status) {
		if (loaderCover.getVisibility() != View.VISIBLE)
			return;
		loaderCover.setVisibility(View.GONE);
		if (status == ServerConnector.SUCCESS) {
			CanvasSynchronizer.getInstance().setCanvas(newCanvas);
			Intent intent = new Intent(this, LoaderActivity.class);
			startActivity(intent);
		} else {
			String msg;
			if (status == OnCanvasCreateListener.DUPLICATE_NAME) {
				createDialog.show();
				msg = "Canvas with same name is already exist. Try different name.";
			} else if (status == OnCanvasCreateListener.USER_UNKNOWN) {
				msg = "User is unregistered.";
			} else if (status == ServerConnector.CONNECTION_PROBLEM) {
				msg = "Connection problem.";
			} else
				msg = "System error.";
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == createDialog) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				String name = nameInput.getText().toString();
				int width = Integer.parseInt(widthInput.getText().toString());
				int height = Integer.parseInt(heightInput.getText().toString());

				String msg = null;
				if (width <= 0)
					msg = "Canvas width cannot be zero";
				else if (height <= 0)
					msg = "Canvas height cannot be zero";
				else if (name.isEmpty())
					msg = "Canvas name cannot be empty";

				if (msg != null) {
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
					reloader.postDelayed(showCreateDialog, 500);
				} else {
					BrowserConnector.getInstance().createCanvas(
							CollaUserManager.getCurrentUser(), name, width,
							height);
					loaderCover.setVisibility(View.VISIBLE);
				}
			}
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
		}
	}

	final Runnable showCreateDialog = new Runnable() {

		@Override
		public void run() {
			createDialog.show();
		}
	};

	@Override
	public void onListFethed(UserModel asker, int status,
			ArrayList<CanvasModel> owned, ArrayList<CanvasModel> oldList,
			ArrayList<CanvasModel> invited) {
		if (status == ServerConnector.SUCCESS) {
			canvasAdapter.clear();
			canvasAdapter.addAll(invited);
			canvasAdapter.addAll(owned);
			canvasAdapter.addAll(oldList);
			listProgress.setVisibility(View.GONE);
			if (canvasAdapter.isEmpty())
				listText.setText("You have no canvas.");
			else {
				listText.setVisibility(View.GONE);
				listProgress.setVisibility(View.INVISIBLE);
				canvasList.setVisibility(View.VISIBLE);
				listTitleCanvas.setVisibility(View.VISIBLE);
				listTitleOwner.setVisibility(View.VISIBLE);
			}
		} else {
			listProgress.setVisibility(View.GONE);
			listReload.setVisibility(View.VISIBLE);
			listText.setText(getResources().getString(R.string.bcl_failed));
			String msg;
			if (status == ServerConnector.CONNECTION_PROBLEM) {
				msg = "Connection problem.";
			} else
				msg = "System error.";
			Log.d("POS", "fetch:" + status);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	class CanvasListAdapter extends BaseAdapter {
		private BrowserActivity activity;
		private ArrayList<CanvasModel> models;

		private class ViewHolder {
			TextView canvasName;
			TextView userName;
		}

		public CanvasListAdapter(BrowserActivity context) {
			this.activity = context;
			models = new ArrayList<>();
		}

		public void addAll(ArrayList<CanvasModel> list) {
			models.addAll(list);
		}

		public void clear() {
			models.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = activity.getLayoutInflater().inflate(
						R.layout.list_canvas, null);
				ViewHolder holder = new ViewHolder();
				holder.canvasName = (TextView) view
						.findViewById(R.id.lc_canvas);
				holder.userName = (TextView) view.findViewById(R.id.lc_user);
				view.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			CanvasModel model = models.get(position);
			holder.canvasName.setText(model.name);
			if (model.owner.equals(CollaUserManager.getCurrentUser()))
				holder.userName.setText("You");
			else
				holder.userName.setText(model.owner.name);
			return view;
		}

		@Override
		public int getCount() {
			return models.size();
		}

		@Override
		public CanvasModel getItem(int position) {
			return models.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}
}
