package com.ppla03.collapaint;

public interface CanvasListener {
	void onCanvasSelection(boolean success);
	
	void onURStatusChange(boolean undoable, boolean redoable);
}
