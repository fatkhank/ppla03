package com.ppla03.collapaint.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ImageChooser implements OnItemClickListener, OnClickListener {
	public static interface ImageChooserListener {
		void onImageChoosed(File file);
	}

	private ImageChooserListener listener;
	private Dialog dialog;
	private ListView fileView;
	private Button back;
	private File[] fileList;
	private File selectedFile;
	private File currentDir;
	private ArrayAdapter<String> adapter;
	private static final ArrayList<String> fileNames = new ArrayList<String>();

	public ImageChooser(Activity activity, ImageChooserListener listener) {
		this.listener = listener;

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);
		fileView = new ListView(activity);
		fileView.setOnItemClickListener(this);
		layout.addView(fileView);
		back = new Button(activity);
		back.setText("Back");
		back.setOnClickListener(this);
		layout.addView(back);
		setDirectory(null);
		builder.setView(layout);
		dialog = builder.create();

		adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_list_item_1);
	}

	private static final FileFilter filter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().endsWith(".png")
					|| file.getName().endsWith(".jpg") || file.getName()
					.endsWith(".jpeg")) && file.canRead();
		}
	};

	private static final Comparator<File> fileComparator = new Comparator<File>() {

		@Override
		public int compare(File f1, File f2) {
			int result = 0;
			if (f1.isDirectory())
				result--;
			if (f2.isDirectory())
				result++;
			if (result == 0)
				return f1.getName().toLowerCase(Locale.ENGLISH)
						.compareTo(f2.getName().toLowerCase(Locale.ENGLISH));
			return result;
		}
	};

	private void setDirectory(File file) {
		if (file == null)
			file = Environment.getExternalStorageDirectory();
		currentDir = file;
		if (file == Environment.getExternalStorageDirectory())
			back.setVisibility(View.GONE);
		else
			back.setVisibility(View.VISIBLE);
		fileList = file.listFiles(filter);
		Arrays.sort(fileList, fileComparator);

		fileNames.clear();
		for (int i = 0; i < fileList.length; i++)
			fileNames.add(fileList[i].getName());

		adapter.clear();
		adapter.addAll(fileNames);
		fileView.setAdapter(adapter);
	}

	public void show() {
		setDirectory(null);
		dialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int choice,
			long arg3) {
		File file = fileList[choice];
		if (file.isDirectory())
			setDirectory(file);
		else {
			selectedFile = file;
			dialog.hide();
			listener.onImageChoosed(selectedFile);
		}
	}

	@Override
	public void onClick(View button) {
		setDirectory(currentDir.getParentFile());
	}

}
