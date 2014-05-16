package com.ppla03.collapaint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.RectF;
import android.os.Environment;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Digunakan untuk mengkonversi berkas kanvas ke format tertentu.
 * @author hamba v7
 * 
 */
public class CanvasExporter {

	private static File recent;

	/**
	 * Kanvas berhasil diekspor.
	 */
	public static final int SUCCESS = 1;

	/**
	 * Gagal membuat file.
	 */
	public static final int FAILED = 0;

	/**
	 * Penyimpanan eksternal tidak tersedia.
	 */
	public static final int DISK_UNAVAILABLE = 3;

	/**
	 * Ukuran preview kanvas
	 */
	private static final int PREVIEW_SIZE = 64;

	/**
	 * Mengubah format kanvas ke format gambar JPG atau PNG. Gambar bertipe PNG
	 * akan memiliki background yang transparan, sedangkan JPG akan memiliki
	 * background sesuai warna kanvas. <br/>
	 * Direktori ekspor adalah direktori media/pictures .<br/>
	 * File berada Jika file belum ada, maka akan dibuat, jika sudah ada, maka
	 * akan ditimpa tanpa pemberitahuan.<br/>
	 * Penamaan file mengikuti nama kanvas jpg memiliki ekstensi .jpg, dan file
	 * bertipe png adalah .png. jika parameter {@code filename} tidak mempunyai
	 * ekstensi, akan otomatis ditambahkan yang sesuai.<br/>
	 * Jika terdapat nama yang sama, maka akan ditambahkan angka sebelum
	 * ekstensi.
	 * 
	 * @param canvas model kanvas yang akan diekspor
	 * @param format format file, hanya yang {@link CompressFormat#PNG} atau
	 *            {@link CompressFormat#JPEG}
	 * @param transparent background transparan atau tidak
	 * @param cropped dipotong atau tidak. Jika true, maka keseluruhan kanvas
	 *            akan diekspor. Jika false, maka hanya bagian yang ada objeknya
	 *            yang akan diekspor.
	 * 
	 * @return status {@link #SUCCESS}, {@link #FAILED}, atau
	 *         {@link #DISK_UNAVAILABLE}.
	 */
	public static int export(CanvasModel canvas, CompressFormat format,
			boolean transparent, boolean cropped) {
		String filename = canvas.name;
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File dir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			dir.mkdirs();
			String extension = format.equals(CompressFormat.PNG) ? ".png"
					: ".jpg";
			if (filename.endsWith(extension))
				filename = filename.substring(0,
						filename.length() - extension.length());
			recent = new File(dir, filename + extension);
			int i = 1;
			while (recent.exists())
				recent = new File(dir, filename + i++ + extension);
			boolean result = export(canvas, recent, format, transparent,
					cropped);
			if (result)
				return SUCCESS;
			else
				return FAILED;
		} else
			return DISK_UNAVAILABLE;
	}

	/**
	 * Mendapatkan file hasil ekspor kanvas yang barusaja dibuat.
	 * @return
	 */
	public static File getResultFile() {
		return recent;
	}

	/**
	 * Jarak tepi gambar ke objek kanvas paling tepi.
	 */
	public static final int CROP_PADDING = 10;

	private static boolean export(CanvasModel model, File file,
			CompressFormat format, boolean transparent, boolean cropped) {
		RectF bound = new RectF();
		Bitmap oriBitmap = draw(model, transparent, bound);

		// hitung gambar akhir
		Bitmap finalBitmap;
		if (cropped)
			finalBitmap = Bitmap.createBitmap(oriBitmap, (int) bound.left,
					(int) bound.top, (int) bound.width(), (int) bound.height());
		else
			finalBitmap = oriBitmap;

		// tulis ke file
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			finalBitmap.compress(format, 100, out);
			out.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Mendapatkan bitmap dari sebuah kanvas.
	 * @param model
	 * @param transparent
	 * @param bound
	 * @return
	 */
	private static Bitmap draw(CanvasModel model, boolean transparent,
			RectF bound) {
		Bitmap bitmap = Bitmap.createBitmap(model.getWidth(),
				model.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		// gambar dan hitung bound
		if (transparent)
			canvas.drawColor(Color.TRANSPARENT,
					android.graphics.PorterDuff.Mode.CLEAR);
		else
			canvas.drawColor(Color.WHITE);

		bound.set(model.getWidth(), model.getHeight(), 0, 0);
		RectF r = new RectF();
		for (int i = 0; i < model.objects.size(); i++) {
			CanvasObject sp = model.objects.get(i);
			sp.draw(canvas);
			sp.getWorldBounds(r);
			bound.union(r);
		}

		bound.left -= CROP_PADDING;
		bound.top -= CROP_PADDING;
		bound.right += CROP_PADDING;
		bound.bottom += CROP_PADDING;

		return bitmap;
	}

	/**
	 * Menyimpan preview dari sebuah kanvas ke penyimpanan lokal.
	 * @param model
	 * @param context
	 */
	public static void savePreview(CanvasModel model, Context context) {
		RectF bound = new RectF();
		Bitmap preview = draw(model, false, bound);

		// kecilkan gambar
		preview = Bitmap.createScaledBitmap(preview, PREVIEW_SIZE,
				PREVIEW_SIZE, false);

		try {
			FileOutputStream out = context.openFileOutput("c" + model.getId()
					+ ".png", Context.MODE_PRIVATE);
			preview.compress(CompressFormat.PNG, 90, out);
		} catch (FileNotFoundException e) {}
	}

	/**
	 * Mengambil gambaran dari sebuah kanvas yang tersmpan d penympanan lokal.
	 * @param model
	 * @param context
	 * @return null jika kanvas tersebut belum mempunya preview.
	 */
	public static Bitmap getPreview(CanvasModel model, Context context) {
		try {
			FileInputStream in = context.openFileInput("c" + model.getId()
					+ ".png");
			return BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {}
		return null;
	}
}
