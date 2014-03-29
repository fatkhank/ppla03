package com.ppla03.collapaint;

import java.util.ArrayList;
import java.util.List;

import com.ppla03.collapaint.model.object.CanvasObject;

public class ObjectClipboard {
	private static final ArrayList<CanvasObject> copiedObjects = new ArrayList<>();

	public static void put(List<CanvasObject> objects) {
		copiedObjects.clear();
		copiedObjects.addAll(objects);
	}

	public static boolean hasObject() {
		return !copiedObjects.isEmpty();
	}

	public static ArrayList<CanvasObject> retrieve() {
		return copiedObjects;
	}
}
