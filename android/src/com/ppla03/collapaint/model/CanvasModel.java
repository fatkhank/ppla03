package com.ppla03.collapaint.model;

import java.sql.Date;
import java.util.ArrayList;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Merepresentasikan model dari sebuah kanvas.
 * @author hamba v7
 * 
 */
public class CanvasModel{
	/**
	 * Lebar minimal kanvas
	 */
	public static final int MIN_WIDTH = 32;
	/**
	 * Tinggi minimal kanvas
	 */
	public static final int MIN_HEIGHT = 32;
	/**
	 * Lebar maksimal kanvas
	 */
	public static final int MAX_WIDTH = 8192;
	/**
	 * Tinggi maksimal kanvas
	 */
	public static final int MAX_HEIGHT = 8192;

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
	 * Koordinat y titik pojok kiri atas kanvas.
	 */
	private int top;

	/**
	 * Koordinat x titik pojok kiri atas kanvas.
	 */
	private int left;

	/**
	 * Lebar kanvas
	 */
	private int width;

	/**
	 * Tinggi kanvas
	 */
	private int height;

	public Date createDate;

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
	 * @param top
	 * @param left
	 */
	public void setDimension(int width, int height, int top, int left) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
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

	/**
	 * Mengambil koordinat y pojok kiri atas kanvas.
	 * @return
	 */
	public int getTop() {
		return top;
	}

	/**
	 * Mengambil koordinat x pojok kiri atas kanvas.
	 * @return
	 */
	public int getLeft() {
		return left;
	}

	@Override
	public String toString() {
		return name + " by " + owner.name;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof CanvasModel)
				&& (((CanvasModel) o).globalID == this.globalID);
	}
}
