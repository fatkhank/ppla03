package com.ppla03.collapaint.model.object;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class FontManager {
	private static Typeface[] fonts;
	private static String[] names;

	public static void readAsset(AssetManager am) throws IOException {
		names = am.list("fonts");
		fonts = new Typeface[names.length];
		for (int i = 0; i < names.length; i++) {
			String st = names[i];
			fonts[i] = Typeface.createFromAsset(am, "fonts/" + st);
			names[i] = st.substring(0, st.length() - 4).replace('_', ' ');
		}
	}

	public static Typeface getFont(int fontNumber) {
		return fonts[fontNumber];
	}

	public static String getName(int fontnumber) {
		return names[fontnumber];
	}

	public static int size() {
		return fonts.length;
	}
}
