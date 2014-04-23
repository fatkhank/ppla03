package com.ppla03.collapaint.model;

import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Merepresentasikan model dari sebuah kanvas.
 * @author hamba v7
 * 
 */
public class CanvasModel {
	/**
	 * id global kanvas
	 */
	private int globalID;

	/**
	 * Pembuat kanvas.
	 */
	public final UserModel owner;

	/**
	 * Nama kanvas.
	 */
	public final String name;

	/**
	 * Lebar kanvas
	 */
	private int width;

	/**
	 * Tinggi kanvas
	 */
	private int height;

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
		globalID = -1;
		objects = new ArrayList<CanvasObject>();
	}

	/**
	 * Mengambil id kanvas.
	 * @return
	 */
	public int getId() {
		return globalID;
	}

	/**
	 * Mengatur id kanvas
	 * @param id
	 */
	public void setid(int id) {
		this.globalID = id;
	}

	/**
	 * Mengatur ukuran kanvas.
	 * @param width
	 * @param height
	 */
	public void setDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Mengambil lebar kanvas.
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Mengambil tinggi kanvas.
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return name + " by " + owner.name;
	}
}
