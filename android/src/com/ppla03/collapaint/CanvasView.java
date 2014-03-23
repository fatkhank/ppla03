package com.ppla03.collapaint;

import java.util.ArrayList;

import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.CanvasObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {

	private CanvasObject protoObject;
	private ArrayList<CanvasObject> selectedObjects;
	private int mode;
	private int fillColor;
	private int strokeColor;
	private int strokeWidth;
	private int strokeStyle;
	private boolean hidden_mode;

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

	public void setMode(int mode) {
		// TODO
	}

	public void approveAction() {
		// TODO
	}

	public void cancelAction() {
		// TODO
	}

	public void setColor(int color) {
		// TODO
	}

	public void setStrokeWidth(int width) {
		// TODO
	}

	public void setStrokeStyle(int style) {
		// TODO
	}

	public void insertImage(Bitmap bitmap) {
		// TODO
	}

	public boolean selectArea(Rect area) {
		// TODO
		return false;
	}

	public void moveSelectedObject() {
		// TODO
	}

	public void copySelectedObjects() {
		// TODO
	}

	public void deleteSelectedObjects() {
		// TODO
	}

	public void cancelSelect() {
		// TODO
	}

	public void insertObjects(ArrayList<CanvasObject> objects) {
		// TODO
	}

	public boolean isUndoable() {
		// TODO
		return false;
	}

	public void undo() {
		// TODO
	}

	public boolean isRedoable() {
		// TODO
		return false;
	}

	public void redo() {
		// TODO
	}

	public void setHideMode(boolean hidden) {
		// TODO
	}

	public void execute(ArrayList<UserAction> actions) {
		// TODO
	}

	public void closeCanvas() {
		// TODO
	}

	public void onUpdateComplete(int status) {
		// TODO
	}

	public void onCanvasClosed(int status) {
		// TODO
	}

}
