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
import android.widget.ImageButton;
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
public class ColorPane implements OnSeekBarChangeListener,
		OnEditorActionListener, android.view.View.OnClickListener {
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
			Color.BLACK, PorterDuff.Mode.SRC_IN);

	private View parent;

	private SeekBar rSlider, gSlider, bSlider, aSlider;
	private EditText rInput, gInput, bInput, aInput;
	private Button preview;
	private ImageButton approve, cancel;
	private ColorView colorView;

	static final int PALLETE = 0, RGB = 1;
	static final String PALLETE_STRING = "Palletes", RGB_STRING = "RGB";
	static final String PALLETE_TAG = "pallete", RGB_TAG = "rgb";
	private int mode = RGB;

	private ColorChangeListener listener;
	private int red, green, blue, alpha;
	private int selectedColor;

	public ColorPane(Activity activity, View view, ColorChangeListener listener) {
		try {
			this.listener = listener;

			this.parent = view;

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

			approve = (ImageButton) view.findViewById(R.id.w_color_ok);
			cancel = (ImageButton) view.findViewById(R.id.w_color_cancel);
			approve.setOnClickListener(this);
			cancel.setOnClickListener(this);

			parent.setVisibility(View.GONE);
			setColor(Color.BLACK);

			// ---setup pallete ---
			// colorView = (ColorView) view.findViewById(R.id.cd_tab_pallete);
		} catch (Exception ex) {
			StackTraceElement[] ste = ex.getStackTrace();
			for (StackTraceElement s : ste) {
				android.util.Log.d("POS", s.toString());
			}
		}
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
		parent.setVisibility(View.VISIBLE);
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
	public void onClick(View v) {
		if (mode == PALLETE) {
			listener.onColorChanged(ColorView.currentColor);
		} else {
			selectedColor = Color.argb(alpha, red, green, blue);
			listener.onColorChanged(selectedColor);
		}
		parent.setVisibility(View.GONE);
	}
}
