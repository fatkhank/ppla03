package com.ppla03.collapaint.model.object;

import java.io.IOException;
import java.util.ArrayList;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Menangani jenis huruf.
 * @author hamba v7
 * 
 */
public class FontManager {
	public static final int MIN_FONT_SIZE = 10;
	public static final int MAX_FONT_SIZE = 100;

	public static class Font {
		public final String name;
		public final Typeface typeface;

		public Font(String name, Typeface typeface) {
			this.name = name;
			this.typeface = typeface;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final ArrayList<Font> fonts = new ArrayList<>();
	static {
		fonts.add(new Font("Sans Serif", Typeface.SANS_SERIF));
		fonts.add(new Font("Monospace", Typeface.MONOSPACE));
	};

	/**
	 * Memerintahkan untuk memuat daftar huruf dengan membaca daftar berkas
	 * huruf dari {@link AssetManager}.
	 * @param am sumber.
	 * @throws IOException jika ada permasalahan dalam pembacaan berkas.
	 */
	public static boolean readAsset(AssetManager am) {
		try {
			String[] names = am.list("fonts");
			fonts.ensureCapacity(names.length);
			fonts.clear();
			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				Typeface tp = Typeface.createFromAsset(am, "fonts/" + name);
				name = name.substring(0, name.length() - 4).replace('_', ' ');
				fonts.add(new Font(name, tp));

			}
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	/**
	 * Mendapatkan tipe huruf dengan id tertentu.
	 * @param fontNumber id tipe huruf.
	 * @return tipe huruf.
	 */
	public static Typeface getFont(int fontNumber) {
		return fonts.get(fontNumber).typeface;
	}

	public static ArrayList<Font> getFontList() {
		return fonts;
	}
}
