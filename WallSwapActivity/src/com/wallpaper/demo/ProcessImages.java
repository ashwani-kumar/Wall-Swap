package com.wallpaper.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessImages {

	public String processImage(String paramString, int paramInt) {
		String str4 = copyImage(paramString);
		// if image is in wallswap directory then return
		if(str4.contains(File.separator+"WallSwap"+File.separator))
			return str4;
		// Get the source image's dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(paramString, options);
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;

		// Only scale if the source is big enough. This code is just trying to
		// fit a image into a certain width.
		int desiredWidth = paramInt;
		if (desiredWidth > srcWidth)
			desiredWidth = srcWidth;

		// Calculate the correct inSampleSize/scale value. This helps reduce
		// memory use. It should be a power of 2
		// from:
		// http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
		int inSampleSize = 1;
		while (srcWidth / 2 > desiredWidth) {
			srcWidth /= 2;
			srcHeight /= 2;
			inSampleSize *= 2;
		}

		float desiredScale = (float) desiredWidth / srcWidth;

		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = inSampleSize;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap sampledSrcBitmap = BitmapFactory
				.decodeFile(paramString, options);

		// Resize
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0,
				sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(),
				matrix, true);
		sampledSrcBitmap = null;

		// Save
		FileOutputStream out;
		try {
			if (str4 != "") {
				out = new FileOutputStream(str4);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
				scaledBitmap = null;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str4;

	}

	private String copyImage(String source) {
		if (null == source) {
			return "";
		}
		String imageName = source.substring(source.lastIndexOf(File.separator),
				source.length());
		String wallswapDirPath = Environment.getExternalStorageDirectory()
				+ File.separator + "WallSwap";
		File wallSwapDir = new File(wallswapDirPath);
		if(!wallSwapDir.exists())
			wallSwapDir.mkdir();
		String target = wallswapDirPath + imageName;

		try {
			File sourceFile = new File(source);
			File targetFile = new File(target);
			if (sourceFile.exists() == false) {
				Log.i("WallSwap", "Cannot find " + source);
				return target;
			}
			if (targetFile.exists() == false) {
				if (targetFile.getParentFile().exists() == false) {
					try {
						wallSwapDir.mkdirs();
					} catch (SecurityException se) {
						Log.e("WallSwap", se.toString());
					}
				}

				try {
					targetFile.createNewFile();
				} catch (Exception e) {
					Log.e("WallSwap", e.toString());
				}
			}

			InputStream inputStream = new FileInputStream(sourceFile);
			OutputStream outputStream = new FileOutputStream(targetFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}
			inputStream.close();
			outputStream.close();
		} catch (FileNotFoundException ex) {
			Log.e("WallSwap", ex.toString());
		} catch (IOException e) {
			Log.e("WallSwap", e.toString());
		}
		return target;
	}

}