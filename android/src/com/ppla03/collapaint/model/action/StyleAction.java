package com.ppla03.collapaint.model.action;

import android.graphics.Color;
import android.graphics.Paint;

import com.ppla03.collapaint.model.object.CanvasObject;
import com.ppla03.collapaint.model.object.StrokeStyle;

public class StyleAction extends UserAction {
	public final CanvasObject object;
	private int fillColor;
	private int strokeColor;
	private int strokeWidth;
	private int strokeStyle;

	public StyleAction(CanvasObject object) {
		// TODO Auto-generated constructor stub
		this.object = object;
	}

	public void setStyle(int fillColor, int strokeColor, int strokeWidth,
			int strokeStyle) {
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.strokeWidth = strokeWidth;
		this.strokeStyle = strokeStyle;
	}

	public String getParameter() {
		// TODO
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(fillColor));
		sb.append(Integer.toHexString(strokeColor));
		// String res =
		return sb.toString();
	}
	
	public void setParameter(String param){
		
	}
	
	public static String getParameterOf(CanvasObject object){
		//TODO style parameter
		return null;
	}
	
	public static void applyStyle(String param, CanvasObject object){
		//TODO apply style to object
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
