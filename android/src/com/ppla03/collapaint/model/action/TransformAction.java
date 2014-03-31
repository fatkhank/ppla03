package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

public class TransformAction extends UserAction {
	private int oriX, oriY;
	private int destX, destY;
	public final CanvasObject object;
	
	public TransformAction(CanvasObject object) {
		this.object = object;
		oriX = object.getXOffset();
		oriY = object.getYOffset();
	}
	
	public String getParameter(){
		//TODO
		return null;
	}
	
	public TransformAction setParameter(String param){
		//TODO
		return this;
	}
	
	public static String getParameterOf(CanvasObject object){
		//TODO get transform parameter
		return "";
	}
	
	public static void apply(String param, CanvasObject object){
		//TODO apply transform to object
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
