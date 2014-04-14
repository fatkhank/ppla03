package com.ppla03.collapaint.model.object;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Menangani jenis huruf.
 * @author hamba v7
 * 
 */
public class FontManager {
	private static Typeface[] fonts;
	private static String[] names;
	static {
		fonts = new Typeface[] { Typeface.SANS_SERIF, Typeface.MONOSPACE };
		names = new String[] { "Serif", "Mono" };
	}

	/**
	 * Memerintahkan untuk memuat daftar huruf dengan membaca daftar berkas
	 * huruf dari {@link AssetManager}.
	 * @param am sumber.
	 * @throws IOException jika ada permasalahan dalam pembacaan berkas.
	 */
	public static void readAsset(AssetManager am) throws IOException {
		names = am.list("fonts");
		fonts = new Typeface[names.length];
		for (int i = 0; i < names.length; i++) {
			String st = names[i];
			fonts[i] = Typeface.createFromAsset(am, "fonts/" + st);
			names[i] = st.substring(0, st.length() - 4).replace('_', ' ');
		}
	}

	/**
	 * Mendapatkan tipe huruf dengan id tertentu.
	 * @param fontNumber id tipe huruf.
	 * @return tipe huruf.
	 */
	public static Typeface getFont(int fontNumber) {
		return fonts[fontNumber];
	}

	/**
	 * Mendapatkan nama tipe huruf dengan id tertentu.
	 * @param fontnumber id tipe huruf.
	 * @return nama tipe huruf.
	 */
	public static String getName(int fontnumber) {
		return names[fontnumber];
	}

	/**
	 * Mengambil banyaknya daftar pilihan tipe huruf yang tersedia.
	 * @return banyak daftar pilihan tipe huruf.
	 */
	public static int size() {
		return fonts.length;
	}
}
