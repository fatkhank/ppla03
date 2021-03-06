package com.ppla03.collapaint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;

public class FontManager {

	public static final int MIN_FONT_SIZE = 10;
	public static final int MAX_FONT_SIZE = 216;

	private static final int UNDERLINE = 4;

	private static final String FONT_FOLDER = "collafonts";

	public static class Font {
		public final String name;
		public Typeface normal;
		public Typeface bold;
		public Typeface italic;
		public Typeface bold_italic;

		public Font(String name) {
			this.name = name;
		}

		public boolean hasBold() {
			return bold != null;
		}

		public boolean hasItalic() {
			return italic != null;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final ArrayList<Font> fonts = new ArrayList<Font>();
	static {
		Font f = new Font("");
		f.normal = Typeface.SANS_SERIF;
		f.bold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		f.italic = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
		f.bold_italic = Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC);
		fonts.add(f);
	}

	private static final HashMap<String, Font> mapper = new HashMap<String, Font>();

	/**
	 * Memerintahkan untuk memuat daftar huruf dengan membaca daftar berkas
	 * huruf dari {@link AssetManager}.
	 * @param am sumber.
	 * @throws IOException jika ada permasalahan dalam pembacaan berkas.
	 */
	public static boolean readFontAsset(AssetManager am) {
		try {
			String[] fileNames = am.list(FONT_FOLDER);
			mapper.clear();
			fonts.ensureCapacity(fileNames.length / 4);
			fonts.clear();
			for (int i = 0; i < fileNames.length; i++) {
				// nama file komplit
				String fileName = fileNames[i];
				Typeface tp = Typeface.createFromAsset(am, FONT_FOLDER + '/'
						+ fileName);
				// hilangkan ekstensi, menghasilkan nama font+code
				String fontName = fileName.substring(0, fileName.length() - 4)
						.replace('_', ' ');
				// nama font
				String family = fontName.substring(0, fontName.length() - 2);
				Font f = mapper.get(family);
				if (f == null) {
					f = new Font(family);
					mapper.put(family, f);
					fonts.add(f);
				}
				// pisahkan berdasarkan kodenya
				char code = fontName.charAt(fontName.length() - 1);
				switch (code) {
				case 'b':
					f.bold = tp;
					break;
				case 'i':
					f.italic = tp;
					break;
				case 'd':
					f.bold_italic = tp;
					break;
				default:
					f.normal = tp;
					break;
				}
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Menghasilkan kode font dari properti yang diberikan
	 * @param font jenis font
	 * @param bold tebal atau tidak
	 * @param italic miring atau tidak
	 * @param underline bergaris bawah atau tidak
	 * @return
	 */
	public static int getFontCode(int font, boolean bold, boolean italic,
			boolean underline) {
		font <<= 3;
		if (bold)
			font += Typeface.BOLD;
		if (italic)
			font += Typeface.ITALIC;
		if (underline)
			font += UNDERLINE;
		return font;
	}

	/**
	 * Mengambil font pada id tertentu
	 * @param id id
	 * @return
	 */
	public static Font getFont(int id) {
		return fonts.get(id);
	}

	/**
	 * Mengaplikasikan suatu dekorasi font ke paint
	 * @param fontCode
	 * @param paint
	 */
	public static void apply(int fontCode, Paint paint) {
		int id = fontCode >> 3;
		paint.setUnderlineText((fontCode & UNDERLINE) == UNDERLINE);
		Font font = fonts.get(id);
		int code = fontCode & 3;
		if (code == Typeface.BOLD)
			paint.setTypeface(font.bold);
		else if (code == Typeface.ITALIC)
			paint.setTypeface(font.italic);
		else if (code == Typeface.BOLD_ITALIC)
			paint.setTypeface(font.bold_italic);
		else
			paint.setTypeface(font.normal);
	}

	private static ArrayAdapter<Font> adapter;

	public static ArrayAdapter<Font> getAdapter(Context context) {
		if (adapter == null || !adapter.getContext().equals(context)) {
			adapter = new ArrayAdapter<Font>(context, R.layout.text_simple);
			adapter.clear();
			adapter.addAll(fonts);
		}
		return adapter;
	}

	/**
	 * Mengetahui apakah kode font berikut tebal atau tidak.
	 * @param fontCode
	 * @return
	 */
	public static boolean isBold(int fontCode) {
		return (fontCode & Typeface.BOLD) == Typeface.BOLD;
	}

	/**
	 * Mengetahui apakah kode font berikut miring atau tidak.
	 * @param fontCode
	 * @return
	 */
	public static boolean isItalic(int fontCode) {
		return (fontCode & Typeface.ITALIC) == Typeface.ITALIC;
	}

	/**
	 * Mengetahui apakah kode font berikut bergaris bawah atau tidak.
	 * @param fontCode
	 * @return
	 */
	public static boolean isUnderline(int fontCode) {
		return (fontCode & UNDERLINE) == UNDERLINE;
	}

	/**
	 * Mengambil indeks jenis huruf
	 * @param fontCode
	 * @return
	 */
	public static int fontId(int fontCode) {
		return fontCode >> 3;
	}

}
