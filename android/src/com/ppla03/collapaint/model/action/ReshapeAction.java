package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import android.graphics.Point;

import com.ppla03.collapaint.model.object.CanvasObject;

public class ReshapeAction extends UserAction {
	public final CanvasObject object;
	public final ArrayList<Point> param;

	public ReshapeAction(CanvasObject object) {
		// TODO Auto-generated constructor stub
		this.object = object;
		param = new ArrayList<>();
	}

	public String getParameter() {
		// TODO
		return "";
	}

	public void setParameter(String param) {
		// TODO
	}
	
	public static String getParameterOf(CanvasObject object){
		//TODO get reshape parameter
		return "";
	}
	
	public static void apply(String param, CanvasObject object){
		//TODO apply shape action
	}

	@Override
	public UserAction getInverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inverseOf(UserAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean overwrites(UserAction action) {
		// TODO Auto-generated method stub
		return false;
	}

}
