package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TextView;

/**
 * Dialog untuk memilih warna.
 * @author hamba v7
 * 
 */
public class ColorDialog implements OnClickListener, OnSeekBarChangeListener,
		OnEditorActionListener, OnTabChangeListener {
	/**
	 * Listener saat ada warna yang dipilih.
	 * @author hamba v7
	 * 
	 */
	public static interface ColorChangeListener {
		/**
		 * Dipicu saat ada warna yang dipilih.
		 * @param color
		 */
		void onColorChanged(int color);
	}

	private static final ColorFilter redFilter = new PorterDuffColorFilter(
			Color.RED, PorterDuff.Mode.SRC_IN);
	private static final ColorFilter greenFilter = new PorterDuffColorFilter(
			Color.GREEN, PorterDuff.Mode.SRC_IN);
	private static final ColorFilter blueFilter = new PorterDuffColorFilter(
			Color.BLUE, PorterDuff.Mode.SRC_IN);
	private static final ColorFilter alphaFilter = new PorterDuffColorFilter(
			Color.WHITE, PorterDuff.Mode.SRC_IN);

	private AlertDialog dialog;
	private TabHost tab;
	private SeekBar rSlider, gSlider, bSlider, aSlider;
	private EditText rInput, gInput, bInput, aInput;
	private Button preview;
	private ColorView colorView;

	static final int PALLETE = 0, RGB = 1;
	static final String PALLETE_STRING = "Palletes", RGB_STRING = "RGB";
	static final String PALLETE_TAG = "pallete", RGB_TAG = "rgb";
	private int mode = PALLETE;

	private ColorChangeListener listener;
	private int red, green, blue, alpha;
	private int selectedColor;

	public ColorDialog(Activity activity, ColorChangeListener listener) {
		this.listener = listener;

		// inflate layout
		View view = activity.getLayoutInflater().inflate(R.layout.dialog_color,
				null);

		// create builder
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(view);

		builder.setTitle("Choose color");

		builder.setPositiveButton("OK", this);

		dialog = builder.create();

		// --- tab setup ---
		tab = (TabHost) view.findViewById(R.id.cd_tabhost);
		tab.setup();

		TabSpec tab1 = tab.newTabSpec(PALLETE_TAG);
		tab1.setIndicator(PALLETE_STRING);
		tab1.setContent(R.id.cd_tab_pallete);
		tab.addTab(tab1);

		TabSpec tab2 = tab.newTabSpec(RGB_TAG);
		tab2.setIndicator(RGB_STRING);
		tab2.setContent(R.id.cd_tab_rgb);
		tab.addTab(tab2);

		tab.setOnTabChangedListener(this);

		// --- setup rgb chooser ---
		rSlider = (SeekBar) view.findViewById(R.id.cd_r_slider);
		gSlider = (SeekBar) view.findViewById(R.id.cd_g_slider);
		bSlider = (SeekBar) view.findViewById(R.id.cd_b_slider);
		aSlider = (SeekBar) view.findViewById(R.id.cd_a_slider);

		rSlider.setOnSeekBarChangeListener(this);
		gSlider.setOnSeekBarChangeListener(this);
		bSlider.setOnSeekBarChangeListener(this);
		aSlider.setOnSeekBarChangeListener(this);

		rSlider.getProgressDrawable().setColorFilter(redFilter);
		gSlider.getProgressDrawable().setColorFilter(greenFilter);
		bSlider.getProgressDrawable().setColorFilter(blueFilter);
		aSlider.getProgressDrawable().setColorFilter(alphaFilter);

		// TODO require api 16
		// rSlider.getThumb().setColorFilter(redFilter);
		// gSlider.getThumb().setColorFilter(greenFilter);
		// bSlider.getThumb().setColorFilter(blueFilter);
		// aSlider.getThumb().setColorFilter(alphaFilter);

		rInput = (EditText) view.findViewById(R.id.cd_r_input);
		gInput = (EditText) view.findViewById(R.id.cd_g_input);
		bInput = (EditText) view.findViewById(R.id.cd_b_input);
		aInput = (EditText) view.findViewById(R.id.cd_a_input);

		rInput.setOnEditorActionListener(this);
		gInput.setOnEditorActionListener(this);
		bInput.setOnEditorActionListener(this);
		aInput.setOnEditorActionListener(this);

		preview = (Button) view.findViewById(R.id.cd_rgb_preview);
		setColor(Color.BLACK);

		// ---setup pallete ---
		colorView = (ColorView) view.findViewById(R.id.cd_tab_pallete);
	}

	/**
	 * Mengganti warna default dialog.
	 * @param color
	 */
	public void setColor(int color) {
		selectedColor = color;

		red = Color.red(color);
		green = Color.green(color);
		blue = Color.blue(color);
		alpha = Color.alpha(color);

		rSlider.setProgress(red);
		gSlider.setProgress(green);
		bSlider.setProgress(blue);
		aSlider.setProgress(alpha);

		rInput.setText(String.valueOf(red));
		gInput.setText(String.valueOf(green));
		bInput.setText(String.valueOf(blue));
		aInput.setText(String.valueOf(alpha));

		preview.setBackgroundColor(color);
	}

	/**
	 * Menampilkan dialog untuk memilih warna.
	 */
	public void show() {
		setColor(selectedColor);
		dialog.show();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if (mode == PALLETE) {
			listener.onColorChanged(ColorView.currentColor);
		} else {
			selectedColor = Color.argb(alpha, red, green, blue);
			listener.onColorChanged(selectedColor);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			if (seekBar == rSlider) {
				red = progress;
				rInput.setText(String.valueOf(red));
			} else if (seekBar == gSlider) {
				green = progress;
				gInput.setText(String.valueOf(green));
			} else if (seekBar == bSlider) {
				blue = progress;
				bInput.setText(String.valueOf(blue));
			} else if (seekBar == aSlider) {
				alpha = progress;
				aInput.setText(String.valueOf(alpha));
			}
			preview.setBackgroundColor(Color.argb(alpha, red, green, blue));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		int value = Integer.parseInt(v.getText().toString());
		if (v == rInput) {
			if (value > 255)
				v.setText(String.valueOf(red));
			else {
				red = value;
				rSlider.setProgress(red);
			}
		} else if (v == gInput) {
			if (value > 255)
				v.setText(String.valueOf(blue));
			else {
				green = value;
				gSlider.setProgress(green);
			}
		} else if (v == bInput) {
			if (value > 255)
				v.setText(String.valueOf(blue));
			else {
				blue = value;
				bSlider.setProgress(blue);
			}
		} else if (v == aInput) {
			if (value > 255)
				v.setText(String.valueOf(alpha));
			else {
				alpha = value;
				aSlider.setProgress(alpha);
			}
		}
		preview.setBackgroundColor(Color.argb(alpha, red, green, blue));
		return true;
	}

	@Override
	public void onTabChanged(String tabId) {
		if (tabId.equals(PALLETE_TAG))
			mode = PALLETE;
		else
			mode = RGB;
	}
}
