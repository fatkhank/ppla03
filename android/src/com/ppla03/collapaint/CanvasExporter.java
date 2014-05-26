package com.ppla03.collapaint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.object.CanvasObject;

/**
 * Digunakan untuk mengkonversi berkas kanvas ke format tertentu.
 * @author hamba v7
 * 
 */
public class CanvasExporter {
	/**
	 * Listener untuk proses ekspor kanvas
	 * @author hamba v7
	 * 
	 */
	public static interface CanvasExportListener {
		/**
		 * Dipicu saat proses ekspor selesai
		 * @param status {@link CanvasExporter#SUCCESS},
		 *            {@link CanvasExporter#FAILED} atau
		 *            {@link CanvasExporter#DISK_UNAVAILABLE}
		 */
		void onFinishExport(int status);
	}

	private static CanvasExportListener listener;
	private static CanvasModel exportModel;
	private static CompressFormat format;
	private static boolean cropped;
	private static boolean transparent;

	/**
	 * File yang baru saa diekspor
	 */
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
	 * Penamaan file mengikuti nama kanvas jpg memiliki ekstensi .jpg, dan file
	 * bertipe png adalah .png. Jika parameter {@code filename} tidak mempunyai
	 * ekstensi, akan otomatis ditambahkan yang sesuai.<br/>
	 * Jika terdapat nama yang sama, maka akan ditambahkan angka sebelum
	 * ekstensi.
	 * 
	 * @param canvas model kanvas yang akan diekspor
	 * @param format format file, hanya yang {@link CompressFormat#PNG} atau
	 *            {@link CompressFormat#JPEG}
	 * @param transparent background transparan atau tidak
	 * @param cropped dipotong atau tidak. Jika false, maka keseluruhan kanvas
	 *            akan diekspor. Jika true, maka hanya bagian yang ada objeknya
	 *            yang akan diekspor.
	 * 
	 */
	public static void export(CanvasModel canvas, CompressFormat format,
			boolean transparent, boolean cropped, CanvasExportListener listener) {
		exportModel = canvas;
		CanvasExporter.listener = listener;
		CanvasExporter.format = format;
		CanvasExporter.transparent = transparent;
		CanvasExporter.cropped = cropped;
		new Exporter().execute();
	}

	/**
	 * Exporter kanvas
	 * @author hamba v7
	 * 
	 */
	private static class Exporter extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			String storageState = Environment.getExternalStorageState();

			// jika tempat penyimpanan tidak ada
			if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
				return DISK_UNAVAILABLE;
			}

			// buat folder kalau belum ada
			File dir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			dir.mkdirs();

			// atur nama file agar selalu berakhiran ekstensi yang sesuai
			String extension = format.equals(CompressFormat.PNG) ? ".png"
					: ".jpg";
			String filename = exportModel.name;
			if (filename.endsWith(extension))
				filename = filename.substring(0,
						filename.length() - extension.length());

			// buat file
			recent = new File(dir, filename + extension);
			// jika file sudah ada -> concat nama dengan angka
			int i = 1;
			while (recent.exists())
				recent = new File(dir, filename + i++ + extension);

			// ekspor kanvas
			RectF bound = new RectF();
			Bitmap oriBitmap = draw(exportModel, transparent, bound);

			// hitung gambar akhir
			Bitmap finalBitmap;
			if (cropped)
				finalBitmap = Bitmap.createBitmap(oriBitmap, (int) bound.left,
						(int) bound.top, (int) bound.width(),
						(int) bound.height());
			else
				finalBitmap = oriBitmap;

			// tulis ke file
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(recent);
				finalBitmap.compress(format, 100, out);
			} catch (Exception e) {
				return FAILED;
			} finally {
				if (out != null)
					try {
						out.close();
					} catch (IOException e) {}
			}
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			listener.onFinishExport(result);
		}
	};

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
			out.close();
		} catch (IOException ex) {}
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
			Bitmap b = BitmapFactory.decodeStream(in);
			in.close();
			return b;
		} catch (IOException e) {}
		return null;
	}
}
