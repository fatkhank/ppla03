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
import com.ppla03.collapaint.conn.ParticipantManager;
import com.ppla03.collapaint.conn.ServerConnector;
import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.Participation;
import com.ppla03.collapaint.model.UserModel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

public class Dashboard implements OnClickListener, ManageParticipantListener {
	View parent;
	WorkspaceActivity workspace;
	ParticipantManager manager;

	ImageButton close;
	ImageButton hide;

	// --- participant list ---
	ListView partiList;
	ProgressBar partiLoader;
	Button partiReload;
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
	private String TAG="Share";
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");;
	
	// --- report ---
	CheckBox reportHeader;

	// --- setting ---
	View settCont;
	CheckBox settHeader;
	EditText settWidth, settHeight;
	Button settOK;

	public Dashboard(Bundle savedInstanceState, WorkspaceActivity activity, View parent) {
		this.workspace = activity;
		this.parent = parent;

		close = (ImageButton) parent.findViewById(R.id.d_button_close);
		close.setOnClickListener(this);
		hide = (ImageButton) parent.findViewById(R.id.d_button_hide);
		hide.setOnClickListener(this);

		// ------ participant list ------
		adapter = new ParticipantAdapter(this);
		partiList = (ListView) parent.findViewById(R.id.d_parti_list);
		partiList.setVisibility(View.GONE);
		partiList.setAdapter(adapter);
		partiLoader = (ProgressBar) parent
				.findViewById(R.id.d_participant_loader);
		partiReload = (Button) parent.findViewById(R.id.d_parti_reload);
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
        shareFb= (ImageButton) parent.findViewById(R.id.d_share_fb);
        shareFb.setOnClickListener(this);
        shareFb.setVisibility(View.GONE);
        loginFb = (LoginButton) parent.findViewById(R.id.fb_login_button); 
        loginFb.setUserInfoChangedCallback(new UserInfoChangedCallback() { 
        @Override
                public void onUserInfoFetched(GraphUser user) { 
                    if (user != null) { 
                        postImage(); 
                        uiHelper.onDestroy(); 
                    } else { 
                    } 
                } 
            }); 
        loginFb.setVisibility(View.GONE); 
        
		
		// ------ report ------
		reportHeader = (CheckBox) parent.findViewById(R.id.d_report_header);
		reportHeader.setOnClickListener(this);
		
		// ------ download ------
		downHeader = (CheckBox) parent.findViewById(R.id.d_download_header);
		downHeader.setOnClickListener(this);
		downContainer = parent.findViewById(R.id.d_download_pane);
		downContainer.setVisibility(View.VISIBLE);
		downloadFormat = (Spinner) parent.findViewById(R.id.d_download_format);
		formatAdapter = new ArrayAdapter<>(activity,
				android.R.layout.simple_list_item_1);
		formatAdapter.add("PNG");
		formatAdapter.add("JPG");
		downloadFormat.setSelection(0);
		downloadFormat.setAdapter(formatAdapter);
		downloadCropped = (CheckBox) parent
				.findViewById(R.id.d_checkbox_cropped);
		downloadButton = (Button) parent.findViewById(R.id.d_button_download);
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
	}

	@Override
	public void onClick(View v) {
		// main button
		if (v == close) {
			workspace.closeCanvas();
		} else if (v == shareHeader) {
			settHeader.setChecked(false);
			downHeader.setChecked(false);
			settCont.setVisibility(View.GONE);
			downContainer.setVisibility(View.GONE);
			shareHeader.setChecked(true);
			shareContainer.setVisibility(View.VISIBLE);
			shareFb.setVisibility(View.VISIBLE);
			reportHeader.setChecked(false);
		} else if (v == shareFb){
			loginFb.performClick();
		}
		else if (v == hide) {
			workspace.canvas.setHideMode(!workspace.canvas.isInHideMode());

			// participant
		} else if (v == partiReload) {
			reloadList();

		} else if (v == invite) {
			if (email.getText() == null) {} else {
				manager.inviteUser(email.getText().toString(),
						workspace.canvas.getModel());
			}
		}
		// download
		else if (v == downloadButton) {
			downloadCanvas();
		} else if (v == downHeader) {
			settHeader.setChecked(false);
			settCont.setVisibility(View.GONE);
			downHeader.setChecked(true);
			downContainer.setVisibility(View.VISIBLE);
			shareHeader.setChecked(true);
			shareContainer.setVisibility(View.GONE);
			shareFb.setVisibility(View.GONE);
			reportHeader.setChecked(false);
		} else if(v== reportHeader){
			settHeader.setChecked(false);
			downHeader.setChecked(false);
			settCont.setVisibility(View.GONE);
			downContainer.setVisibility(View.GONE);
			shareHeader.setChecked(false);
			shareContainer.setVisibility(View.GONE);
			shareFb.setVisibility(View.GONE);
			reportHeader.setChecked(true);
			reportWork();
		}// setting
			else if (v == settHeader) {
			downHeader.setChecked(false);
			downContainer.setVisibility(View.GONE);
			settHeader.setChecked(true);
			settCont.setVisibility(View.VISIBLE);
			settWidth.setText(String.valueOf(workspace.canvas.getModel()
					.getWidth()));
			settHeight.setText(String.valueOf(workspace.canvas.getModel()
					.getHeight()));
			shareHeader.setChecked(false);
			shareContainer.setVisibility(View.GONE);
			shareFb.setVisibility(View.GONE);
			reportHeader.setChecked(false);
		} else if (v == settOK) {
			// ubah ukuran kanvas
			int width = Integer.parseInt(settWidth.getText().toString());
			int height = Integer.parseInt(settHeight.getText().toString());
			workspace.canvas.resizeCanvas(width, height, 0, 0);
		}
	}

	private void reportWork() {
		// TODO
		createDialog("report");
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
			MediaScannerConnection.scanFile(workspace, new String[] { path },
					null, null);
		} else if (status == CanvasExporter.FAILED) {
			res = "Download failed";
		} else if (status == CanvasExporter.DISK_UNAVAILABLE) {
			res = "Disk is unavailable";
		}
		Toast.makeText(workspace, res, Toast.LENGTH_SHORT).show();
	}

	private void reloadList() {
		partiList.setVisibility(View.GONE);
		partiLoader.setVisibility(View.VISIBLE);
		partiReload.setVisibility(View.GONE);
		partiFailed.setVisibility(View.GONE);
		manager.getParticipants(workspace.canvas.getModel());
	}

	public void show() {
		parent.setVisibility(View.VISIBLE);
		reloadList();
	}

	public void hide() {
		parent.setVisibility(View.GONE);
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
		// TODO invite message
		if (status == ServerConnector.SUCCESS) {
			Toast.makeText(workspace, "User is invited", Toast.LENGTH_SHORT)
					.show();
			reloadList();
		} else if (status == ManageParticipantListener.ALREADY_INVITED) {
			Toast.makeText(workspace, "User has been invited",
					Toast.LENGTH_SHORT).show();
		} else if (status == ManageParticipantListener.ALREADY_JOINED) {
			Toast.makeText(workspace, "The user has joined this canvas",
					Toast.LENGTH_SHORT).show();
		} else
			Toast.makeText(workspace,
					"Error,please check your connection problem",
					Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onKickUser(UserModel user, CanvasModel model, int status) {
		// TODO kick
		if (status == ServerConnector.SUCCESS) {
			Toast.makeText(workspace, "User get kick :'(", Toast.LENGTH_SHORT)
					.show();
			reloadList();
		} else
			Toast.makeText(workspace, "Error", Toast.LENGTH_SHORT).show();
	}

	public void kick(Participation part) {
		manager.kickUser(part.user, part.canvas);
	}

	private void createDialog(String what) {
		LayoutInflater li = workspace.getLayoutInflater();
		View promptsView;

		promptsView = li.inflate(R.layout.dialog_report, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				workspace);

		alertDialogBuilder.setView(promptsView);

		// Report Here
		if (what.equals("report")) {
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// get user input and set it to result
									// edit text
									String to = "darwinmdn12@gmail.com";
									String subject = "report bug 3:)";
									String message = userInput.getText()
											.toString();

									Intent email = new Intent(
											Intent.ACTION_SEND);
									email.putExtra(Intent.EXTRA_EMAIL,
											new String[] { to });
									// email.putExtra(Intent.EXTRA_CC, new
									// String[]{ to});
									// email.putExtra(Intent.EXTRA_BCC, new
									// String[]{to});
									email.putExtra(Intent.EXTRA_SUBJECT,
											subject);
									email.putExtra(Intent.EXTRA_TEXT, message);

									// need this to prompts email client only
									email.setType("message/rfc822");

									workspace.startActivity(Intent
											.createChooser(email,
													"Choose an Email client :"));
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
		}

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	//======================SHARE=========================================== 
    
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
            CanvasExporter export=new CanvasExporter(); 
            export.export(workspace.canvas.getModel(), CompressFormat.PNG, false, false); 
            File image= export.getResultFile(); 
              
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
            requestPermissions();} 
          
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
