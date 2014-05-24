package com.ppla03.collapaint.ui;

import com.ppla03.collapaint.R;

import android.R.anim;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageButton;

public class CheckImage extends ImageButton implements Checkable {
	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
	private boolean mChecked;
	public boolean done;

	public CheckImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int state[] = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(state, CHECKED_STATE_SET);
		}
		return state;
	}

	@Override
	public void setChecked(boolean checked) {
		if (this.mChecked != checked) {
			this.mChecked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

}
