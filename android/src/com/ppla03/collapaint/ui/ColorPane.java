package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.R;

import android.app.Activity;
import android.graphics.Color;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TextView;

/**
 * Dialog untuk memilih warna.
 * @author hamba v7
 * 
 */
class ColorPane implements OnSeekBarChangeListener, OnEditorActionListener,
		OnClickListener {
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

		/**
		 * Dipicu saat dialog ditutup.
		 * @param color
		 * @param approve perubahan warna disetujui atau dibatalkan
		 */
		void onColorDialogClosed(int color, boolean approve);
	}

	private View parent;

	private SeekBar rSlider, gSlider, bSlider, aSlider;
	private EditText rInput, gInput, bInput, aInput;
	private Button preview;
	private ImageButton approve, cancel;

	static final int PALLETE = 0, RGB = 1;
	static final String PALLETE_STRING = "Palletes", RGB_STRING = "RGB";
	static final String PALLETE_TAG = "pallete", RGB_TAG = "rgb";
	private int mode = RGB;

	private ColorChangeListener listener;
	private int red, green, blue, alpha;
	private int originalColor;

	private static final InputFilter[] colorFilter = { new InputFilter.LengthFilter(
			3) };

	public ColorPane(Activity activity, View view, ColorChangeListener listener) {
		try {
			this.listener = listener;

			this.parent = view;

			// --- setup rgb chooser ---
			rSlider = (SeekBar) view.findViewById(R.id.cp_r_slider);
			gSlider = (SeekBar) view.findViewById(R.id.cp_g_slider);
			bSlider = (SeekBar) view.findViewById(R.id.cp_b_slider);
			aSlider = (SeekBar) view.findViewById(R.id.cp_a_slider);

			rSlider.setOnSeekBarChangeListener(this);
			gSlider.setOnSeekBarChangeListener(this);
			bSlider.setOnSeekBarChangeListener(this);
			aSlider.setOnSeekBarChangeListener(this);

			rInput = (EditText) view.findViewById(R.id.cp_r_input);
			gInput = (EditText) view.findViewById(R.id.cp_g_input);
			bInput = (EditText) view.findViewById(R.id.cp_b_input);
			aInput = (EditText) view.findViewById(R.id.cp_a_input);

			rInput.setOnEditorActionListener(this);
			gInput.setOnEditorActionListener(this);
			bInput.setOnEditorActionListener(this);
			aInput.setOnEditorActionListener(this);

			rInput.setFilters(colorFilter);
			gInput.setFilters(colorFilter);
			bInput.setFilters(colorFilter);
			aInput.setFilters(colorFilter);

			preview = (Button) view.findViewById(R.id.cp_rgb_preview);

			approve = (ImageButton) view.findViewById(R.id.cp_ok);
			cancel = (ImageButton) view.findViewById(R.id.cp_cancel);
			approve.setOnClickListener(this);
			cancel.setOnClickListener(this);

			parent.setVisibility(View.GONE);
			setColor(Color.BLACK, true);

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
	 * @param save disimpan sebagai warna asli bukan
	 */
	public void setColor(int color, boolean save) {
		if (save)
			originalColor = color;

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
			int clr = Color.argb(alpha, red, green, blue);
			preview.setBackgroundColor(clr);
			listener.onColorChanged(clr);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		int value = Integer.parseInt(v.getText().toString());
		if (value > 255) {
			v.setText(String.valueOf(255));
			value = 255;
		}
		if (v == rInput) {
			red = value;
			rSlider.setProgress(red);
		} else if (v == gInput) {
			green = value;
			gSlider.setProgress(green);
		} else if (v == bInput) {
			blue = value;
			bSlider.setProgress(blue);
		} else if (v == aInput) {
			alpha = value;
			aSlider.setProgress(alpha);
		}
		preview.setBackgroundColor(Color.argb(alpha, red, green, blue));
		return true;
	}

	@Override
	public void onClick(View v) {
		boolean app = (v == approve);
		if (app)
			originalColor = Color.argb(alpha, red, green, blue);
		if (mode == PALLETE) {
			// TODO color view
			listener.onColorDialogClosed(ColorView.currentColor, app);
		} else {
			listener.onColorDialogClosed(originalColor, app);
		}
	}

}
