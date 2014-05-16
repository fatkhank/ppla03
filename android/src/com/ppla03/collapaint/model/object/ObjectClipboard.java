package com.ppla03.collapaint.model.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Menyimpan daftar objek kanvas, sehingga dapat disalin ke kanvas lain.
 * @author hamba v7
 * 
 */
public class ObjectClipboard {
	/**
	 * Daftar objek yang disalin
	 */
	private static final ArrayList<CanvasObject> copiedObjects = new ArrayList<CanvasObject>();

	/**
	 * Menyimpan objek-objek kanvas untuk disalin. Perubahan pada objek yang
	 * telah disalin tidak akan mempengaruhi objek yang disimpan.
	 * @param objects daftar objek yang ingin disalin.
	 */
	public static void put(List<CanvasObject> objects) {
		copiedObjects.clear();
		for (int i = 0; i < objects.size(); i++)
			copiedObjects.add(objects.get(i).cloneObject());
	}

	/**
	 * Mengecek apakah ada objek dalam simpanan.
	 * @return
	 */
	public static boolean hasObject() {
		return !copiedObjects.isEmpty();
	}

	/**
	 * Mengambil daftar objek-objek kanvas dalam simpanan. Modifikasi yang
	 * dilakukan terhadap daftar objek yang dihasilkan dapat mempengarui isi
	 * objek.
	 * @return
	 */
	public static ArrayList<CanvasObject> retrieve() {
		return copiedObjects;
	}
}
