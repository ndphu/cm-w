package ndphu.app.android.cw.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class Utils {

	private static final String TAG = Utils.class.getSimpleName();

	public static Bitmap decodeBitmap(byte[] data, int screenWidth, int screenHeight) {
		Log.i(TAG,"Input size: " + data.length);
		long maxBitmapSize = (long) (screenWidth * screenHeight * 4 * 1.5);
		Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inPreferQualityOverSpeed = true;
		Bitmap bitmap = null;
		while (bitmap == null) {
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			if (bitmap.getByteCount() < maxBitmapSize) {
				break;
			} else {
				bitmap = null;
				options.inSampleSize++;
			}
		}
		float ratio = (float) screenWidth / bitmap.getWidth();
		int destHeight = (int) (ratio * bitmap.getHeight());
		return Bitmap.createScaledBitmap(bitmap, screenWidth, destHeight, true);
	}

	public static String getMD5Hash(String input) {
		StringBuffer hexString = new StringBuffer();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (md == null) {
			return null;
		}
		byte[] hash = null;
		try {
			hash = md.digest(input.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (hash == null) {
			return null;
		}

		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return hexString.toString();
	}

	public static int countString(String input, String pattern) {
		int lastIndex = 0;
		int count = 0;

		while (lastIndex != -1) {

			lastIndex = input.indexOf(pattern, lastIndex);

			if (lastIndex != -1) {
				count++;
				lastIndex += pattern.length();
			}
		}
		return count;
	}
}
