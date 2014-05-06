package com.ppla03.collapaint.model.action;

import com.ppla03.collapaint.model.CanvasModel;

/**
 * Mengubah ukuran kanvas
 * @author hamba v7
 *
 */
public class ResizeCanvas extends AtomicAction {
	private final int width, height, left, top;
	private final CanvasModel model; 
	
	public ResizeCanvas(CanvasModel model, int width, int height, int top, int left, boolean reversible) {
		this.model = model;
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
		if(reversible){
			this.inverse = new ResizeCanvas(model, model.getWidth(), model.getHeight(), model.getTop(), model.getLeft(), false);
			this.inverse.inverse = this;
			
		}
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
