package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import android.util.Base64;

import com.ppla03.collapaint.model.object.CanvasObject;

public class MoveMultiple extends UserAction {
	private static final int OFFSET_X = 0, OFFSET_Y = 1;
	private final int[] trans = new int[2];
	public final ArrayList<CanvasObject> objects;

	private static int anchorX, anchorY, finalX, finalY;

	private MoveMultiple(ArrayList<CanvasObject> objects, MoveMultiple inverse) {
		this.objects = objects;
		this.inverse = inverse;
	}

	public MoveMultiple(ArrayList<CanvasObject> objects, boolean reversible) {
		this.objects = new ArrayList<>(objects);
		if (reversible) {
			MoveMultiple tm = new MoveMultiple(this.objects, this);
			tm.trans[OFFSET_X] = -this.trans[OFFSET_X];
			tm.trans[OFFSET_Y] = -this.trans[OFFSET_Y];
			this.inverse = tm;
		}
	}

	public String getParameter() {
		return MoveAction.encode(trans[OFFSET_X], trans[OFFSET_Y]);
	}

	public MoveMultiple anchorDown(int x, int y) {
		anchorX = x;
		anchorY = y;
		return this;
	}

	public MoveMultiple moveTo(int x, int y) {
		finalX = x;
		finalY = y;
		return this;
	}

	public MoveMultiple anchorUp() {
		int dx = finalX - anchorX;
		int dy = finalY - anchorY;
		trans[OFFSET_X] += dx;
		trans[OFFSET_Y] += dy;
		MoveMultiple mi = (MoveMultiple) inverse;
		mi.trans[OFFSET_X] = -trans[OFFSET_X];
		mi.trans[OFFSET_Y] = -trans[OFFSET_Y];
		MoveMultiple mm = new MoveMultiple(objects, null);
		mm.trans[OFFSET_X] = dx;
		mm.trans[OFFSET_Y] = dy;
		MoveMultiple mmi = new MoveMultiple(objects, mm);
		mmi.trans[OFFSET_X] = -dx;
		mmi.trans[OFFSET_Y] = -dy;
		mm.inverse = mmi;
		return mm;
	}

	public void apply() {
		int dx = trans[OFFSET_X];
		int dy = trans[OFFSET_Y];
		int size = objects.size();
		for (int i = 0; i < size; i++)
			objects.get(i).setOffset(dx, dy);
	}

	@Override
	public UserAction getInverse() {
		return inverse;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		if (action == null || !(action instanceof MoveMultiple))
			return false;
		MoveMultiple tm = (MoveMultiple) action;
		if ((tm.trans[OFFSET_X] != -trans[OFFSET_X])
				|| (tm.trans[OFFSET_Y] != -trans[OFFSET_Y]))
			return false;
		return tm.objects.equals(objects);
	}

	@Override
	public boolean overwrites(UserAction action) {
		if (action != null) {
			if (action instanceof MoveMultiple) {
				MoveMultiple tm = (MoveMultiple) action;
				return tm.objects.equals(objects);
			} else if (action instanceof MoveAction) {
				MoveAction ma = (MoveAction) action;
				return objects.contains(ma.object);
			}
		}
		return false;
	}
}
