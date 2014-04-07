package com.ppla03.collapaint;

import com.ppla03.collapaint.model.object.FontManager;
import com.ppla03.collapaint.ui.TesterActivity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class LoaderActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			FontManager.readAsset(getAssets());
			Intent intent = new Intent(this, TesterActivity.class);
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, "Cannot load font assets", Toast.LENGTH_SHORT).show();
		}
	}

}
