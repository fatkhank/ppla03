package com.ppla03.collapaint.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Merepresentasikan model dari sebuah kanvas.
 * @author hamba v7
 * 
 */
public class CanvasModel implements Parcelable {
	private int globalId;

	public final UserModel owner;

	/**
	 * Nama kanvas.
	 */
	public final String name;

	/**
	 * Lebar kanvas
	 */
	public final int width;

	/**
	 * Tinggi kanvas.
	 */
	public final int height;

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
		objects = new ArrayList<CanvasObject>();
	}

	public int getId() {
		return globalId;
	}

	public void setid(int id) {
		this.globalId = id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}
}
