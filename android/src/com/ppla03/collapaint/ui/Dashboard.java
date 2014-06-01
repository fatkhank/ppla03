package com.ppla03.collapaint.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//--share---
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import com.ppla03.collapaint.CanvasExporter;
import com.ppla03.collapaint.CanvasExporter.CanvasExportListener;
import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ManageParticipantListener;
import com.ppla03.collapaint.conn.OnKickUserListener;
import com.ppla03.collapaint.conn.ParticipantManager;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.UserModel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

class Dashboard implements OnClickListener, ManageParticipantListener,
		OnKickUserListener, OnTabChangeListener {

	View parent;
	WorkspaceActivity workspace;
	ParticipantManager manager;

	ProgressBar loader;
	TabHost host;

	// --- hide ---
	View closeBar;
	CheckBox hide;
	ImageButton close;

	// --- participant list ---
	ListView partiList;
	ProgressBar partiLoader;
	ImageButton partiReload;
	TextView partiFailed;
	ParticipantAdapter adapter;
	ImageButton invite;
	EditText email;

	// --- tab ---
	private static final String TAB_DOWNLOAD = "TD", TAB_SETTING = "TS",
			TAB_SHARE = "TH", TAB_REPORT = "TR";

	// --- download ---
	View downContainer;
	TextView downFormatLabel;
	Spinner downloadFormat;
	CheckBox downloadCropped;
	Button downloadButton;
	ArrayAdapter<String> formatAdapter;

	// --- share ---
	View shareContainer;
	LoginButton loginFb;
	Button shareFb;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");;

	// --- report ---
	View reportCont;
	Button reportSend;
	EditText reportInput;

	// --- setting ---
	View settCont;
	EditText settWidth, settHeight;
	Button settOK;

	InputFilter[] canvasSizeFilter = { new LengthFilter(4) };

	public Dashboard(Bundle savedInstanceState, WorkspaceActivity activity,
			View parent) {

		try {
			this.workspace = activity;
			this.parent = parent;

			closeBar = workspace.findViewById(R.id.w_right_dash_bar);
			closeBar.setVisibility(View.GONE);
			close = (ImageButton) workspace.findViewById(R.id.d_button_close);
			close.setOnClickListener(this);
			hide = (CheckBox) workspace.findViewById(R.id.d_button_hide);
			hide.setOnClickListener(this);
			hide.setChecked(false);
			hide.setText(R.string.d_nohide_text);
			loader = (ProgressBar) parent.findViewById(R.id.d_loader);
			loader.setVisibility(View.GONE);

			// ------ participant list ------
			adapter = new ParticipantAdapter(this);
			partiList = (ListView) parent.findViewById(R.id.d_parti_list);
			// partiList.setVisibility(View.GONE);
			partiList.setAdapter(adapter);
			partiLoader = (ProgressBar) parent
					.findViewById(R.id.d_participant_loader);
			partiReload = (ImageButton) parent
					.findViewById(R.id.d_parti_reload);
			partiReload.setVisibility(View.GONE);
			partiReload.setOnClickListener(this);
			partiFailed = (TextView) parent.findViewById(R.id.d_parti_failed);
			partiFailed.setVisibility(View.GONE);
			invite = (ImageButton) parent.findViewById(R.id.d_add_user);
			email = (EditText) parent.findViewById(R.id.d_insert_email);
			invite.setOnClickListener(this);

			// ------ TAB ------
			host = (TabHost) parent.findViewById(android.R.id.tabhost);
			host.setup();
			host.setOnTabChangedListener(this);

			// download
			TabSpec spec = host.newTabSpec(TAB_DOWNLOAD);
			spec.setIndicator(
					"",
					workspace.getResources().getDrawable(
							R.drawable.ic_action_download));
			spec.setContent(R.id.d_download_pane);
			host.addTab(spec);

			// tampilkan setting hanya untuk owner
			// setting
			spec = host.newTabSpec(TAB_SETTING);
			spec.setIndicator(
					"",
					workspace.getResources().getDrawable(
							R.drawable.ic_action_settings));
			spec.setContent(R.id.d_setting_pane);
			host.addTab(spec);

			// share
			spec = host.newTabSpec(TAB_SHARE);
			spec.setIndicator(
					"",
					workspace.getResources().getDrawable(
							R.drawable.ic_action_share));
			spec.setContent(R.id.d_share_pane);
			host.addTab(spec);

			// report
			spec = host.newTabSpec(TAB_REPORT);
			spec.setIndicator("",
					workspace.getResources().getDrawable(R.drawable.ic_bug));
			spec.setContent(R.id.d_report_pane);
			host.addTab(spec);

			// ------ share -------
			shareContainer = parent.findViewById(R.id.d_share_pane);
			uiHelper = new UiLifecycleHelper(workspace, statusCallback);
			uiHelper.onCreate(savedInstanceState);
			shareFb = (Button) parent.findViewById(R.id.d_share_fb);
			shareFb.setOnClickListener(this);
			loginFb = (LoginButton) parent.findViewById(R.id.d_fb_login_button);
			loginFb.setUserInfoChangedCallback(new UserInfoChangedCallback() {
				@Override
				public void onUserInfoFetched(GraphUser user) {
					if (user != null) {
						postImage();
						uiHelper.onDestroy();
					} else {}
				}
			});
			loginFb.setVisibility(View.GONE);

			// ------ report ------
			reportCont = parent.findViewById(R.id.d_report_pane);
			reportSend = (Button) parent.findViewById(R.id.d_report_send);
			reportSend.setOnClickListener(this);
			reportInput = (EditText) parent.findViewById(R.id.d_report_input);

			// ------ download ------
			downContainer = parent.findViewById(R.id.d_download_pane);
			downFormatLabel = (TextView) parent
					.findViewById(R.id.d_text_format);
			downloadFormat = (Spinner) parent
					.findViewById(R.id.d_download_format);
			formatAdapter = new ArrayAdapter<>(activity, R.layout.text_simple);
			formatAdapter.add("PNG");
			formatAdapter.add("JPG");
			downloadFormat.setSelection(0);
			downloadFormat.setAdapter(formatAdapter);
			downloadCropped = (CheckBox) parent
					.findViewById(R.id.d_checkbox_cropped);
			downloadButton = (Button) parent
					.findViewById(R.id.d_button_download);
			downloadButton.setOnClickListener(this);

			// ------ setting ------
			// sembuyikan pengaturan jika bukan owner
			settCont = parent.findViewById(R.id.d_setting_pane);
			settWidth = (EditText) parent.findViewById(R.id.d_width_input);
			settWidth.setFilters(canvasSizeFilter);
			settHeight = (EditText) parent.findViewById(R.id.d_height_input);
			settHeight.setFilters(canvasSizeFilter);
			settOK = (Button) parent.findViewById(R.id.d_button_resize);
			settOK.setOnClickListener(this);

			manager = ParticipantManager.getInstance().setListener(this);
		} catch (Exception ex) {
			android.util.Log.d("POS", "e:" + ex);
			for (StackTraceElement s : ex.getStackTrace()) {
				android.util.Log.d("POS", "ex:" + s);
			}
		}

	}

	@Override
	public void onClick(View v) {
		// main button
		if (v == close) {
			workspace.closeCanvas();

			// --- share
		} else if (v == shareFb) {
			loginFb.performClick();

			// --- hide
		} else if (v == hide) {
			workspace.canvas.setHideMode(hide.isChecked());

			// --- participant
		} else if (v == partiReload) {
			reloadList();
		} else if (v == invite) {
			String em = email.getText().toString();
			if (!em.isEmpty()) {
				manager.inviteUser(em, workspace.canvas.getModel());
				email.setVisibility(View.GONE);
				invite.setVisibility(View.GONE);
			}

			// --- download
		} else if (v == downloadButton) {
			downloadCanvas();

			// --- report
		} else if (v == reportSend) {
			reportBug();

			// --- setting

		} else if (v == settOK) {
			// ubah ukuran kanvas
			int width = Integer.parseInt(settWidth.getText().toString());
			if (width > CanvasModel.MAX_WIDTH)
				width = CanvasModel.MAX_WIDTH;
			else if (width < CanvasModel.MIN_WIDTH)
				width = CanvasModel.MIN_WIDTH;
			int height = Integer.parseInt(settHeight.getText().toString());
			if (height > CanvasModel.MAX_HEIGHT)
				height = CanvasModel.MAX_HEIGHT;
			else if (height < CanvasModel.MIN_HEIGHT)
				height = CanvasModel.MIN_HEIGHT;
			workspace.resizeCanvas(width, height, 0, 0);
		}
	}

	/**
	 * Menginformasikan bahwa mode hide berganti
	 * @param hidden
	 */
	void onHideModeChanged(boolean hidden) {
		hide.setChecked(hidden);
		if (hidden) {
			hide.setText(R.string.d_hide_text);
			CollaDialog.toast(workspace, R.string.d_hide_msg, Toast.LENGTH_SHORT);
		} else {
			hide.setText(R.string.d_nohide_text);
			CollaDialog.toast(workspace, R.string.d_nohide_msg,
					Toast.LENGTH_SHORT);
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		switch (tabId) {
		case TAB_DOWNLOAD:

			break;
		case TAB_SETTING:
			settWidth.setText(String.valueOf(workspace.canvas.getModel()
					.getWidth()));
			settHeight.setText(String.valueOf(workspace.canvas.getModel()
					.getHeight()));
			break;
		case TAB_SHARE:

			break;
		case TAB_REPORT:

			break;
		}
	}

	private void downloadCanvas() {
		CanvasModel model = workspace.canvas.getModel();
		CompressFormat format = downloadFormat.getSelectedItemPosition() == 0 ? CompressFormat.PNG
				: CompressFormat.JPEG;
		boolean transparent = format.equals(CompressFormat.PNG);
		boolean cropped = downloadCropped.isChecked();
		loader.setVisibility(View.VISIBLE);
		downloadButton.setVisibility(View.GONE);
		downloadFormat.setVisibility(View.GONE);
		downloadCropped.setVisibility(View.GONE);
		downFormatLabel.setVisibility(View.GONE);
		CanvasExporter.export(model, format, transparent, cropped,
				downloadListener);
	}

	private CanvasExportListener downloadListener = new CanvasExportListener() {

		@Override
		public void onFinishExport(int status) {
			loader.setVisibility(View.GONE);
			downloadFormat.setVisibility(View.VISIBLE);
			downloadCropped.setVisibility(View.VISIBLE);
			downFormatLabel.setVisibility(View.VISIBLE);
			downloadButton.setVisibility(View.VISIBLE);
			if (status == CanvasExporter.SUCCESS) {
				String path = CanvasExporter.getResultFile().getAbsolutePath();
				String text = "Downloaded to " + path;
				CollaDialog.toast(workspace, text, Toast.LENGTH_LONG);
				// Toast.makeText(workspace, text, Toast.LENGTH_SHORT).show();
				MediaScannerConnection.scanFile(workspace,
						new String[] { path }, null, null);
			} else if (status == CanvasExporter.FAILED) {
				CollaDialog.toast(workspace, R.string.d_download_failed,
						Toast.LENGTH_SHORT);
			} else if (status == CanvasExporter.DISK_UNAVAILABLE) {
				CollaDialog.toast(workspace, R.string.disk_unavailable,
						Toast.LENGTH_SHORT);
			}
		}
	};

	private void reloadList() {
		partiList.setVisibility(View.GONE);
		partiLoader.setVisibility(View.VISIBLE);
		partiReload.setVisibility(View.GONE);
		partiFailed.setVisibility(View.GONE);
		manager.getParticipants(workspace.canvas.getModel());
	}

	void init() {
		if (workspace.canvas.isInHideMode())
			hide.setText(R.string.d_hide_text);
		else
			hide.setText(R.string.d_nohide_text);
		reloadList();
		closeBar.setVisibility(View.VISIBLE);
	}

	void hide() {
		closeBar.setVisibility(View.GONE);
	}

	@Override
	public void onParticipantFetched(CanvasModel canvas,
			ArrayList<Participation> participants) {
		adapter.clear();
		adapter.addAll(participants);
		partiLoader.setVisibility(View.GONE);
		partiFailed.setVisibility(View.GONE);
		partiReload.setVisibility(View.VISIBLE);
		partiList.setVisibility(View.VISIBLE);
	}

	@Override
	public void onParticipationFetchedFailed(CanvasModel model, int status) {
		partiLoader.setVisibility(View.GONE);
		partiReload.setVisibility(View.VISIBLE);
		partiFailed.setVisibility(View.VISIBLE);
		partiList.setVisibility(View.GONE);
	}

	@Override
	public void onInviteUser(String accountId, CanvasModel model, int status) {
		email.setVisibility(View.VISIBLE);
		invite.setVisibility(View.VISIBLE);
		accountId += " ";
		if (status == ServerConnector.SUCCESS) {
			CollaDialog.toast(
					workspace,
					accountId
							+ workspace.getResources().getString(
									R.string.d_invite_sucess),
					Toast.LENGTH_SHORT);
			email.setText("");
			reloadList();
		} else if (status == ManageParticipantListener.ALREADY_INVITED) {
			CollaDialog.toast(
					workspace,
					accountId
							+ workspace.getResources().getString(
									R.string.d_invite_sucess),
					Toast.LENGTH_SHORT);
		} else if (status == ManageParticipantListener.ALREADY_JOINED) {
			CollaDialog.toast(
					workspace,
					accountId
							+ workspace.getResources().getString(
									R.string.d_invite_sucess),
					Toast.LENGTH_SHORT);

		} else if (status == ManageParticipantListener.NOT_REGISTERED) {
			CollaDialog.toast(
					workspace,
					accountId
							+ workspace.getResources().getString(
									R.string.d_invite_not_registered),
					Toast.LENGTH_SHORT);
		} else
			CollaDialog.toast(workspace, R.string.check_connection,
					Toast.LENGTH_SHORT);
	}

	@Override
	public void onKickUser(UserModel user, CanvasModel model, int status) {
		partiList.setVisibility(View.VISIBLE);
		partiReload.setVisibility(View.VISIBLE);
		partiLoader.setVisibility(View.GONE);
		if (status == ServerConnector.SUCCESS) {
			CollaDialog.toast(workspace, user.name + " "
					+ workspace.getResources().getString(R.string.d_no_longer),
					Toast.LENGTH_SHORT);
			reloadList();
		} else if (status == ServerConnector.CONNECTION_PROBLEM)
			CollaDialog.toast(workspace, R.string.check_connection,
					Toast.LENGTH_SHORT);
	}

	public void kick(Participation part) {
		manager.kickUser(part.user, part.canvas, this);
		partiList.setVisibility(View.GONE);
		partiReload.setVisibility(View.GONE);
		partiLoader.setVisibility(View.VISIBLE);
	}

	private void reportBug() {
		// get user input and set it to result
		// edit text
		String to = "darwinmdn12@gmail.com";
		String subject = "report bug 3:)";
		String message = reportInput.getText().toString();

		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
		// email.putExtra(Intent.EXTRA_CC, new
		// String[]{ to});
		// email.putExtra(Intent.EXTRA_BCC, new
		// String[]{to});
		email.putExtra(Intent.EXTRA_SUBJECT, subject);
		email.putExtra(Intent.EXTRA_TEXT, message);

		// need this to prompts email client only
		email.setType("message/rfc822");

		workspace.startActivity(Intent.createChooser(email,
				"Choose an Email client :"));
	}

	// ======================SHARE===========================================

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {
				Log.d("FacebookSampleActivity", "Facebook session opened");
			} else if (state.isClosed()) {
				Log.d("FacebookSampleActivity", "Facebook session closed");
			}
		}
	};

	public void postImage() {
		if (checkPermissions()) {
			loader.setVisibility(View.VISIBLE);
			CanvasExporter.export(workspace.canvas.getModel(),
					CompressFormat.PNG, false, false, postListener);
		} else {
			requestPermissions();
		}

	}

	private CanvasExportListener postListener = new CanvasExportListener() {

		@Override
		public void onFinishExport(int status) {
			loader.setVisibility(View.GONE);
			if (status == CanvasExporter.SUCCESS) {
				File image = CanvasExporter.getResultFile();

				Bitmap img = BitmapFactory.decodeFile(image.getPath());

				Request uploadRequest = Request.newUploadPhotoRequest(
						Session.getActiveSession(), img,
						new Request.Callback() {
							@Override
							public void onCompleted(Response response) {
								CollaDialog.toast(workspace,
										R.string.d_share_success,
										Toast.LENGTH_LONG);
							}
						});
				uploadRequest.executeAsync();
				if (Session.getActiveSession() != null) {
					Session.getActiveSession().closeAndClearTokenInformation();
				}

				Session.setActiveSession(null);
			} else if (status == CanvasExporter.DISK_UNAVAILABLE) {
				CollaDialog.toast(workspace, R.string.disk_unavailable,
						Toast.LENGTH_SHORT);
				return;
			} else {
				CollaDialog.toast(workspace, R.string.d_share_failed,
						Toast.LENGTH_SHORT);
			}

		}
	};

	public boolean checkPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			return s.getPermissions().contains("publish_actions");
		} else
			return false;
	}

	public void requestPermissions() {
		Session s = Session.getActiveSession();
		if (s != null)
			s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
					workspace, PERMISSIONS));
	}

	void onStart() {
		if (!workspace.canvas.getModel().owner.equals(CollaUserManager
				.getCurrentUser())) {
			(parent.findViewById(R.id.d_setting_pane)).setVisibility(View.GONE);
			View tab = host.getTabWidget().getChildTabViewAt(1);
			tab.setEnabled(false);
			tab.setBackgroundColor(workspace.getResources().getColor(
					R.color.dark));
		}

	}

	void onResume() {
		uiHelper.onResume();
	}

	void onPause() {
		uiHelper.onPause();
	}

	void onDestroy() {
		uiHelper.onDestroy();
	}

	void onActivityResult(int requestCode, int resultCode, Intent data) {
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	void onSaveInstanceState(Bundle savedState) {
		uiHelper.onSaveInstanceState(savedState);
	}
}
