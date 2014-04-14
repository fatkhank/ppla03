package com.ppla03.collapaint.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.ppla03.collapaint.R;

public class BrowserActivity extends Activity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener{	
	private Button mSignOutButton;
	private Button mCreateButton;
	private TextView textView;
	private GoogleApiClient mGoogleApiClient;
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_list);
	    // Get the message from the intent
	    Intent intent = getIntent();
	    String message = intent.getStringExtra(AuthenticationActivity.EXTRA_MESSAGE);
	    
	    mSignOutButton = (Button) findViewById(R.id.button1);
	    mSignOutButton.setOnClickListener(this);
	    mCreateButton= (Button) findViewById(R.id.buttonCreate);
	    mCreateButton.setOnClickListener(this);
	    
	    // Create the text view
	    textView = (TextView) findViewById(R.id.textView1);
	    textView.setText("Hello, "+message);
	    mGoogleApiClient = buildGoogleApiClient();
	}
	
	  private GoogleApiClient buildGoogleApiClient() {
		    // When we build the GoogleApiClient we specify where connected and
		    // connection failed callbacks should be returned, which Google APIs our
		    // app uses and which OAuth 2.0 scopes our app requests.
		    return new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(Plus.API, null)
		        .addScope(Plus.SCOPE_PLUS_LOGIN)
		        .build();
		  }
	
	public void onClick(View v) {
	      switch (v.getId()) {
	          case R.id.button1:
	        	  AuthenticationActivity.TERM=true;
	        	  finish();
	            break;
	          case R.id.buttonCreate:
	        	    Intent intent = new Intent(this, LoaderActivity.class);
	        	    startActivity(intent);
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
}
