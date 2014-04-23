package com.ppla03.collapaint.model;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Merepresentasikan model dari sebuah kanvas.
 * @author hamba v7
 * 
 */
public class CanvasModel {
	private int globalId;

	public final UserModel owner;

	/**
	 * Nama kanvas.
	 */
	public final String name;

	/**
	 * Lebar kanvas
	 */
	public int width;

	/**
	 * Tinggi kanvas.
	 */
	public int height;

	/**
	 * Daftar objek yang ada di kanvas, terurut dari yang berada paling belakang
	 * di kanvas.
	 */
	public final ArrayList<CanvasObject> objects;

	/**
	 * Membuat objek kanvas dengan parameter yang diberikan
	 * @param owner
	 * @param name
	 * @param width
	 * @param height
	 */
	public CanvasModel(UserModel owner, String name, int width, int height) {
		this.owner = owner;
		this.name = (name == null || name.isEmpty()) ? "untitled" : name;
		this.width = width;
		this.height = height;
		globalId = -1;
		objects = new ArrayList<CanvasObject>();
	}

	public int getId() {
		return globalId;
	}

	public void setid(int id) {
		this.globalId = id;
	}

	@Override
	public String toString() {
		return name + " by " + owner.nickname;
	}
}
