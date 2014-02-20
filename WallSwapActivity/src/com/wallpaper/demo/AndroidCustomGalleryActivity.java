package com.wallpaper.demo;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class AndroidCustomGalleryActivity extends Activity {
	private String[] arrPath;
	private int count;
	private ImageAdapter imageAdapter;
	private Cursor imagecursor;
	private GridView imagegrid;
	private ProcessImages mProcessImages;
	private int screenWidth;
	private Bitmap[] thumbnails;
	private boolean[] thumbnailsselection;

	public int determineScreenSize() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	public void startMediaScanner() {
		sendBroadcast(new Intent(
				"android.intent.action.MEDIA_MOUNTED",
				Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			setContentView(R.layout.nosd);
		} else {
			setContentView(R.layout.gallery);
			screenWidth = determineScreenSize();
			mProcessImages = new ProcessImages();
			imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
			PrepareGallery mPrepareGallery = new PrepareGallery(this);
			mPrepareGallery.execute();
			final Button selectBtn = (Button) findViewById(R.id.selectBtn);
			selectBtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					LongOperation mLongOperation = new LongOperation(
							AndroidCustomGalleryActivity.this);
					mLongOperation.execute("");
				}
			});
		}
	}

	private boolean deleteImage(String string) {
		// TODO Auto-generated method stub
		boolean isDeleted;
		File image = new File(string);
		if (image.exists()) {
			long startTime = System.currentTimeMillis();
			Log.i("Started Deleting at:", "" + startTime);
			isDeleted = image.delete();
			Log.i("Deleted in:", "" + (System.currentTimeMillis() - startTime));
		} else
			isDeleted = false;
		return isDeleted;
	}

	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ImageAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.galleryitem, null);
				holder.imageview = (ImageView) convertView
						.findViewById(R.id.thumbImage);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.itemCheckBox);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.checkbox.setId(position);
			holder.imageview.setId(position);

			holder.checkbox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					CheckBox cb = (CheckBox) v;
					int id = cb.getId();
					Log.i("ID", "" + id);
					if (thumbnailsselection[id]) {
						cb.setChecked(false);
						thumbnailsselection[id] = false;
					} else {
						cb.setChecked(true);
						thumbnailsselection[id] = true;
					}
				}
			});
			holder.imageview.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					int id = v.getId();
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + arrPath[id]),
							"image/*");
					startActivity(intent);
				}
			});
			holder.imageview.setImageBitmap(thumbnails[position]);
			holder.checkbox.setChecked(thumbnailsselection[position]);
			holder.id = position;
			if (arrPath[position].contains(File.separator + "WallSwap"
					+ File.separator)) {
				Log.i("image", arrPath[position]);
				holder.checkbox.setChecked(true);
				thumbnailsselection[position] = true;
			}
			return convertView;
		}
	}

	class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
		int id;
	}

	public void addImageToContentProvider(String imageName) {
		imageName = imageName.substring(imageName.lastIndexOf("/"),
				imageName.length());
		try {
			String str = android.provider.MediaStore.Images.Media.insertImage(
					getContentResolver(), imageName, "WallSwapPics",
					"WallSwapPics");
			Log.i("ANDROIDWALLSWAP", str);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class LongOperation extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;
		int cnt = 0;
		private Activity activity;
		private Context context;

		public LongOperation(Activity arg2) {
			this.activity = arg2;
			this.context = arg2;
			this.dialog = new ProgressDialog(this.context);
		}

		protected String doInBackground(String... param) {
			final int len = thumbnailsselection.length;
			String selectImages = "";
			for (int i = 0; i < len; i++) {
				if (thumbnailsselection[i]) {
					cnt++;
					String processedPath = mProcessImages.processImage(
							arrPath[i], screenWidth);
					addImageToContentProvider(processedPath);
					selectImages = selectImages + processedPath + ",";
				}
			}
			DeleteOperation deleteOperation = new DeleteOperation();
			deleteOperation.execute();

			return selectImages;
		}

		protected void onPostExecute(String paramString) {
			if (cnt != 0) {
				Toast.makeText(AndroidCustomGalleryActivity.this,
						cnt + " images selected.", Toast.LENGTH_SHORT).show();
//				Intent returnIntent = new Intent();
//				returnIntent.putExtra("imagePath", paramString);
//				setResult(RESULT_OK, returnIntent);
				if (this.dialog.isShowing())
					this.dialog.dismiss();
				finish();
			} else {
				Toast.makeText(AndroidCustomGalleryActivity.this,
						"No image selected.", Toast.LENGTH_SHORT).show();
				if (this.dialog.isShowing())
					this.dialog.dismiss();
				finish();
			}

		}

		protected void onPreExecute() {
			this.dialog.setMessage("Transforming images...");
			this.dialog.show();
		}

		protected void onProgressUpdate(Void... param) {
		}
	}

	private class DeleteOperation extends AsyncTask<Void, Void, String> {

		protected void onPostExecute(String paramString) {

		}

		protected void onPreExecute() {

		}

		protected void onProgressUpdate(Void... param) {
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			for (int i = 0; i < thumbnailsselection.length; i++)
				if (arrPath[i].contains(File.separator + "WallSwap"
						+ File.separator)) {
					Log.i("image", arrPath[i]);
					if (thumbnailsselection[i] == false) {
						deleteImage(arrPath[i]);
					}

				}
			return "";
		}
	}

	private class PrepareGallery extends AsyncTask<Void, Void, Cursor> {
		private Activity activity;
		private Context context;
		private ProgressDialog dialog;

		public PrepareGallery(Activity arg2) {
			this.activity = arg2;
			this.context = arg2;
			this.dialog = new ProgressDialog(this.context);
		}

		protected Cursor doInBackground(Void... param) {
			final String[] columns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID };
			final String orderBy = MediaStore.Images.Media._ID;
			Cursor cursor = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					null, null, orderBy);
			return cursor;
		}

		protected void onPostExecute(Cursor paramCursor) {
			imagecursor = paramCursor;
			int image_column_index = imagecursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			count = imagecursor.getCount();
			thumbnails = new Bitmap[count];
			arrPath = new String[count];
			thumbnailsselection = new boolean[count];
			for (int i = 0; i < count; i++) {
				imagecursor.moveToPosition(i);
				int id = imagecursor.getInt(image_column_index);
				int dataColumnIndex = imagecursor
						.getColumnIndex(MediaStore.Images.Media.DATA);
				thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
						getApplicationContext().getContentResolver(), id,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				arrPath[i] = imagecursor.getString(dataColumnIndex);
			}
			imageAdapter = new ImageAdapter();
			imagegrid.setAdapter(imageAdapter);
			imagecursor.close();

			if (this.dialog.isShowing())
				this.dialog.dismiss();
		}

		protected void onPreExecute() {
			this.dialog.setMessage("Preparing Gallery");
			this.dialog.show();
		}

		protected void onProgressUpdate(Void... param) {
		}
	}

}