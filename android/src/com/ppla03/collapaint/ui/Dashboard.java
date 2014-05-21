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
import com.ppla03.collapaint.CollaUserManager;
import com.ppla03.collapaint.R;
import com.ppla03.collapaint.conn.ManageParticipantListener;
import com.ppla03.collapaint.conn.OnKickUserListener;
import com.ppla03.collapaint.conn.ParticipantManager;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.Participation.Role;
import com.ppla03.collapaint.model.UserModel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

class Dashboard implements OnClickListener, ManageParticipantListener,
		OnKickUserListener {
	View parent;
	WorkspaceActivity workspace;
	ParticipantManager manager;

	ImageButton close;

	// --- hide ---
	CheckBox hide;

	// --- participant list ---
	ListView partiList;
	ProgressBar partiLoader;
	ImageButton partiReload;
	TextView partiFailed;
	ParticipantAdapter adapter;
	ImageButton invite;
	EditText email;

	// --- download ---
	CheckBox downHeader;
	View downContainer;
	Spinner downloadFormat;
	CheckBox downloadCropped;
	Button downloadButton;
	ArrayAdapter<String> formatAdapter;

	// --- share ---
	CheckBox shareHeader;
	View shareContainer;
	LoginButton loginFb;
	ImageButton shareFb;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");;

	// --- report ---
	CheckBox reportHeader;
	View reportCont;
	Button reportSend;
	EditText reportInput;

	// --- setting ---
	View settCont;
	CheckBox settHeader;
	EditText settWidth, settHeight;
	Button settOK;

	public Dashboard(Bundle savedInstanceState, WorkspaceActivity activity,
			View parent) {

		try {
			this.workspace = activity;
			this.parent = parent;

			close = (ImageButton) parent.findViewById(R.id.d_button_close);
			close.setOnClickListener(this);
			hide = (CheckBox) parent.findViewById(R.id.d_button_hide);
			hide.setOnClickListener(this);
			hide.setText(R.string.d_nohide_text);

			// ------ participant list ------
			adapter = new ParticipantAdapter(this);
			partiList = (ListView) parent.findViewById(R.id.d_parti_list);
//			partiList.setVisibility(View.GONE);
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

			// ------ share -------
			shareHeader = (CheckBox) parent.findViewById(R.id.d_share_header);
			shareHeader.setOnClickListener(this);
			shareContainer = parent.findViewById(R.id.d_share_pane);
			shareContainer.setVisibility(View.GONE);
			uiHelper = new UiLifecycleHelper(workspace, statusCallback);
			uiHelper.onCreate(savedInstanceState);
			shareFb = (ImageButton) parent.findViewById(R.id.d_share_fb);
			shareFb.setOnClickListener(this);
			loginFb = (LoginButton) parent.findViewById(R.id.fb_login_button);
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
			reportHeader = (CheckBox) parent.findViewById(R.id.d_report_header);
			reportHeader.setOnClickListener(this);
			reportCont = parent.findViewById(R.id.d_report_pane);
			reportSend = (Button) parent.findViewById(R.id.d_report_send);
			reportSend.setOnClickListener(this);
			reportInput = (EditText) parent.findViewById(R.id.d_report_input);

			// ------ download ------
			downHeader = (CheckBox) parent.findViewById(R.id.d_download_header);
			downHeader.setOnClickListener(this);
			downContainer = parent.findViewById(R.id.d_download_pane);
			downContainer.setVisibility(View.VISIBLE);
			downloadFormat = (Spinner) parent
					.findViewById(R.id.d_download_format);
			formatAdapter = new ArrayAdapter<>(activity,
					android.R.layout.simple_list_item_1);
			formatAdapter.add("PNG");
			formatAdapter.add("JPG");
			downloadFormat.setSelection(0);
			downloadFormat.setAdapter(formatAdapter);
			downloadCropped = (CheckBox) parent
					.findViewById(R.id.d_checkbox_cropped);
			downloadButton = (Button) parent
					.findViewById(R.id.d_button_download);
			downloadButton.setOnClickListener(this);

			settHeader = (CheckBox) parent.findViewById(R.id.d_setting_header);
			settHeader.setOnClickListener(this);

			// sembuyikan pengaturan jika bukan owner
			if (!workspace.canvas.getModel().owner.equals(CollaUserManager
					.getCurrentUser())) {
				settHeader.setVisibility(View.GONE);
			}
			settCont = parent.findViewById(R.id.d_setting_pane);
			settCont.setVisibility(View.GONE);
			settWidth = (EditText) parent.findViewById(R.id.d_width_input);
			settHeight = (EditText) parent.findViewById(R.id.d_height_input);
			settOK = (Button) parent.findViewById(R.id.d_button_resize);
			settOK.setOnClickListener(this);

			manager = ParticipantManager.getInstance().setListener(this);

			setCurrentTab(downHeader, downContainer);
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
		} else if (v == shareHeader) {
			setCurrentTab(shareHeader, shareContainer);
		} else if (v == shareFb) {
			loginFb.performClick();

			// --- hide
		} else if (v == hide) {
			if (hide.isChecked()) {
				hide.setText(R.string.d_hide_text);
				Toast.makeText(workspace, R.string.d_hide_msg,
						Toast.LENGTH_SHORT).show();
			} else {
				hide.setText(R.string.d_nohide_text);
				Toast.makeText(workspace, R.string.d_nohide_msg,
						Toast.LENGTH_SHORT).show();
			}

			workspace.canvas.setHideMode(!workspace.canvas.isInHideMode());

			// --- participant
		} else if (v == partiReload) {
			reloadList();
		} else if (v == invite) {
			String em = email.getText().toString();
			if (!em.isEmpty()) {
				manager.inviteUser(em, workspace.canvas.getModel());
			}

			// --- download
		} else if (v == downloadButton) {
			downloadCanvas();
		} else if (v == downHeader) {
			setCurrentTab(downHeader, downContainer);

			// --- report
		} else if (v == reportHeader) {
			setCurrentTab(reportHeader, reportCont);
		} else if (v == reportSend) {
			reportBug();

			// --- setting
		} else if (v == settHeader) {
			setCurrentTab(settHeader, settCont);
			settWidth.setText(String.valueOf(workspace.canvas.getModel()
					.getWidth()));
			settHeight.setText(String.valueOf(workspace.canvas.getModel()
					.getHeight()));
		} else if (v == settOK) {
			// ubah ukuran kanvas
			int width = Integer.parseInt(settWidth.getText().toString());
			int height = Integer.parseInt(settHeight.getText().toString());
			workspace.canvas.resizeCanvas(width, height, 0, 0);
		}
	}

	/**
	 * Menampilkan sebuah tab, dan menutup tab lain.
	 * @param header
	 * @param content
	 */
	void setCurrentTab(CheckBox header, View content) {
		settHeader.setChecked(false);
		downHeader.setChecked(false);
		shareHeader.setChecked(false);
		reportHeader.setChecked(false);
		settCont.setVisibility(View.GONE);
		downContainer.setVisibility(View.GONE);
		shareContainer.setVisibility(View.GONE);
		reportCont.setVisibility(View.GONE);

		header.setChecked(true);
		content.setVisibility(View.VISIBLE);

	}

	private void downloadCanvas() {
		CanvasModel model = workspace.canvas.getModel();
		CompressFormat format = downloadFormat.getSelectedItemPosition() == 0 ? CompressFormat.PNG
				: CompressFormat.JPEG;
		boolean transparent = format.equals(CompressFormat.PNG);
		boolean cropped = downloadCropped.isChecked();
		int status = CanvasExporter.export(model, format, transparent, cropped);
		String res = "";
		if (status == CanvasExporter.SUCCESS) {
			String path = CanvasExporter.getResultFile().getAbsolutePath();
			res = "Downloaded to " + path;
			Toast.makeText(workspace, res, Toast.LENGTH_SHORT).show();
			MediaScannerConnection.scanFile(workspace, new String[] { path },
					null, null);
		} else if (status == CanvasExporter.FAILED) {
			Toast.makeText(workspace, R.string.d_download_failed,
					Toast.LENGTH_SHORT).show();
		} else if (status == CanvasExporter.DISK_UNAVAILABLE) {
			Toast.makeText(workspace, R.string.disk_unavailable,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void reloadList() {
		partiList.setVisibility(View.GONE);
		partiLoader.setVisibility(View.VISIBLE);
		partiReload.setVisibility(View.GONE);
		partiFailed.setVisibility(View.GONE);
		manager.getParticipants(workspace.canvas.getModel());
	}

	public void init() {
		if (workspace.canvas.isInHideMode())
			hide.setText(R.string.d_hide_text);
		else
			hide.setText(R.string.d_nohide_text);

		// TODO debug adapter
		adapter.clear();
		UserModel user1 = new UserModel(1, "", "user pertama");
		UserModel user2 = new UserModel(1, "", "user kedua");
		UserModel user3 = new UserModel(1, "", "user ketiga");
		UserModel user4 = new UserModel(1, "", "user keempat");

		CanvasModel canvas = new CanvasModel(user1, "dummy canvas", 1000, 1200);

		adapter.add(new Participation(user1, canvas, Role.OWNER));
		adapter.add(new Participation(user2, canvas, Role.MEMBER));
		adapter.add(new Participation(user4, canvas, Role.MEMBER));
		adapter.add(new Participation(user3, canvas, Role.INVITATION));
		// reloadList();
	}


	@Override
	public void onParticipantFetched(CanvasModel canvas,
			ArrayList<Participation> participants) {
		adapter.clear();
		adapter.addAll(participants);
		partiLoader.setVisibility(View.GONE);
		partiReload.setVisibility(View.GONE);
		partiFailed.setVisibility(View.GONE);
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
		if (status == ServerConnector.SUCCESS) {
			email.setText("");
			Toast.makeText(workspace, "User is invited", Toast.LENGTH_SHORT)
					.show();
			reloadList();
		} else if (status == ManageParticipantListener.ALREADY_INVITED) {
			Toast.makeText(workspace, "User has been invited",
					Toast.LENGTH_SHORT).show();
		} else if (status == ManageParticipantListener.ALREADY_JOINED) {
			Toast.makeText(workspace, "The user has joined this canvas",
					Toast.LENGTH_SHORT).show();

			// TODO user yang diundang tidak terdaftar
			// } else if(status == ManageParticipantListener.){

		} else
			Toast.makeText(workspace, R.string.check_connection,
					Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onKickUser(UserModel user, CanvasModel model, int status) {
		if (status == ServerConnector.SUCCESS) {
			Toast.makeText(workspace, "User get kick :'(", Toast.LENGTH_SHORT)
					.show();
			reloadList();
		} else
			Toast.makeText(workspace, "Error", Toast.LENGTH_SHORT).show();
	}

	public void kick(Participation part) {
		manager.kickUser(part.user, part.canvas, this);
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
			CanvasExporter.export(workspace.canvas.getModel(),
					CompressFormat.PNG, false, false);
			File image = CanvasExporter.getResultFile();

			Bitmap img = BitmapFactory.decodeFile(image.getPath());

			Request uploadRequest = Request.newUploadPhotoRequest(
					Session.getActiveSession(), img, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							Toast.makeText(workspace,
									"Photo uploaded successfully",
									Toast.LENGTH_LONG).show();
						}
					});
			uploadRequest.executeAsync();
			if (Session.getActiveSession() != null) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}

			Session.setActiveSession(null);
		} else {
			requestPermissions();
		}

	}

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
