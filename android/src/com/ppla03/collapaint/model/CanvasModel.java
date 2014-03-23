package com.ppla03.collapaint.model;

import java.util.ArrayList;
import java.util.Stack;

import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.CanvasObject;

public class CanvasModel {
	private int id;
	private String name;
	private UserModel owner;
	private int width;
	private int height;
	private ArrayList<CanvasObject> object;
	private Stack<UserAction> userActions;
	private int lastActionNumber;

	public int getWidth() {
		// TODO
		return 0;
	}

	public int getHeight() {
		// TODO
		return 0;
	}

	public void addAction(UserAction action) {
		// TODO
	}

	public UserAction popAction() {
		// TODO
		return null;
	}

	public boolean stackEmpty() {
		// TODO
		return true;
	}

	public ArrayList<CanvasObject> getObjects() {
		// TODO
		return null;
	}
}
