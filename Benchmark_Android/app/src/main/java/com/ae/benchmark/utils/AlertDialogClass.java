package com.ae.benchmark.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ae.benchmark.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AlertDialogClass {

	Context mctx;
	NetWorkCheck nw;
	private SharedPreferences sharedPref;

	public AlertDialogClass(Context mctx) {
		this.mctx = mctx;
		nw = new NetWorkCheck(mctx);
		sharedPref = mctx.getSharedPreferences("MYPREF", Context.MODE_PRIVATE);
	}
	public static byte[] getFileDataFromDrawable(Context context, int id) {
		Drawable drawable = ContextCompat.getDrawable(context, id);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * Turn drawable into byte array.
	 *
	 * @param drawable data
	 * @return byte array
	 */
	public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}
	public static byte[] convertBytesToFile(String file) throws FileNotFoundException {

//		File f = new File(file);

		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		try {
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum);
			}
		} catch (IOException ex) {
		}
		byte[] bytes = bos.toByteArray();
		return bytes;
	}

	public void sHowDialog(String error) {

		final Dialog dialog = new Dialog(mctx,android.R.style.Theme_Holo_Dialog_NoActionBar);
		// Include dialog.xml file
		dialog.setContentView(R.layout.custom_dialogs_layout);
		// Set dialog title
//		dialog.setTitle("Alert");
		TextView text_header = (TextView) dialog.findViewById(R.id.tv_header);
		text_header.setText("Alert");

		// set values for custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.textDialog);
		text.setText(error);
		dialog.show();

		Button declineButton = (Button) dialog.findViewById(R.id.btn_confirm);
		declineButton.setText("OK");
		// if decline button is clicked, close the custom dialog
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close dialog
				dialog.dismiss();
			}
		});



	}
	public static ArrayList<int[]> getSpans(String body, char prefix) {
		ArrayList<int[]> spans = new ArrayList<int[]>();

		Pattern pattern = Pattern.compile(prefix + "\\w+");
		Matcher matcher = pattern.matcher(body);

		// Check all occurrences
		while (matcher.find()) {
			int[] currentSpan = new int[2];
			currentSpan[0] = matcher.start();
			currentSpan[1] = matcher.end();
			spans.add(currentSpan);
		}

		return  spans;
	}
	public void sHowDialog(String header, String error) {

		final Dialog dialog = new Dialog(mctx,android.R.style.Theme_Holo_Dialog_NoActionBar);
		// Include dialog.xml file
		dialog.setContentView(R.layout.custom_dialogs_layout);
		// Set dialog title
//		dialog.setTitle("Alert");
		TextView text_header = (TextView) dialog.findViewById(R.id.tv_header);
		text_header.setText(header);

		// set values for custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.textDialog);
		text.setText(error);
		dialog.show();

		Button declineButton = (Button) dialog.findViewById(R.id.btn_confirm);
		declineButton.setText("OK");
		// if decline button is clicked, close the custom dialog
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close dialog
				dialog.dismiss();
			}
		});



	}
	public void sHowInternetDialog() {

//		AlertDialog.Builder ab = new AlertDialog.Builder(mctx);
//
//		ab.setTitle("Net Connection Alert");
//		ab.setMessage("Opps! Please connect to the internet.");
//		ab.setPositiveButton("OK",
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						dialog.cancel();
//
//					}
//				});
//
//		AlertDialog ad = ab.create();
//		ad.show();
		final Dialog dialog = new Dialog(mctx,android.R.style.Theme_Holo_Dialog_NoActionBar);
		// Include dialog.xml file
		dialog.setContentView(R.layout.custom_dialogs_layout);
		// Set dialog title
		TextView text_header = (TextView) dialog.findViewById(R.id.tv_header);
		text_header.setText("Net Connection Alert");
		// set values for custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.textDialog);
		text.setText("Opps! Please connect to the internet.");
		dialog.show();

		Button declineButton = (Button) dialog.findViewById(R.id.btn_confirm);
		declineButton.setText("OK");
		// if decline button is clicked, close the custom dialog
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close dialog
				dialog.dismiss();
			}
		});
	}
	public String getBase64OfString(String str) {
		byte[] data;

		String base64 = "";

		try {

			data = str.getBytes("UTF-8");

			base64 = Base64.encodeToString(data, Base64.DEFAULT);

			Log.i("Base 64 ", base64);

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();

		}
		return base64;
	}

	public String getCurrentDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		return dateFormat.format(date);

	}


	public static String changeDateFormate(String datechange){
		String temp = "";
		SimpleDateFormat form = new SimpleDateFormat("dd-mm-yyyy");
		Date date = null;
		try
		{
			date = form.parse(datechange);
		}
		catch (ParseException e)
		{

			e.printStackTrace();
		}
		SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd");
		temp = postFormater.format(date);

		return temp;
	}

	public static String changeTimeFormate(String datechange){
		String temp = "";
		SimpleDateFormat form = new SimpleDateFormat("hh:mm");
		Date date = null;
		try
		{
			date = form.parse(datechange);
		}
		catch (ParseException e)
		{

			e.printStackTrace();
		}
		SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm");
		temp = postFormater.format(date);

		return temp;
	}
	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API19(Context context, Uri uri){
		String filePath = "";
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return filePath;
	}


	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Audio.Media.DATA };
		String result = null;

		CursorLoader cursorLoader = new CursorLoader(
				context,
				contentUri, proj, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		if(cursor != null){
			int column_index =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);
		}
		return result;
	}

	public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
		String[] proj = { MediaStore.Audio.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index
				= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static List ComaSeperatedStringToList(String str){
		List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
		return items;
	}

	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API19VIDEO(Context context, Uri uri){
		String filePath = "";
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}


	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API11to18VIDEO(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Video.Media.DATA };
		String result = null;

		CursorLoader cursorLoader = new CursorLoader(
				context,
				contentUri, proj, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		if(cursor != null){
			int column_index =
					cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);
		}
		return result;
	}

	public static String getRealPathFromURI_BelowAPI11VIDEO(Context context, Uri contentUri){
		String[] proj = { MediaStore.Video.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index
				= cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}



//	public static GregorianCalendar GetGeorgianCAlanderFRomSimpleLocalDATE(String date){
//		SimpleDateFormat postFormater = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss a");
//		Date date1 = null;
//		try
//		{
//			date1 = postFormater.parse(date);
//		}
//		catch (ParseException e)
//		{
//
//			e.printStackTrace();
//		}
//		GregorianCalendar cal = new GregorianCalendar();
//			cal.setTime(date1);
//
//
//		return cal;
//	}

	public static GregorianCalendar GetGeorgianCAlanderFRomSimpleDATE(String date){
		SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date1 = null;
		try
		{
			date1 = postFormater.parse(date);
		}
		catch (ParseException e)
		{

			e.printStackTrace();
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date1);


		return cal;
	}

}
