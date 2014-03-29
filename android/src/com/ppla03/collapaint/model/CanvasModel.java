package com.ppla03.collapaint.model;

import java.util.ArrayList;
import java.util.Stack;

import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.CanvasObject;

public class CanvasModel {
	private int id;
	private String name;
	private UserModel owner;
	public final int width;
	public final int height;
	public final ArrayList<CanvasObject> objects;

	public CanvasModel(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
		objects = new ArrayList<CanvasObject>();
	}
}
