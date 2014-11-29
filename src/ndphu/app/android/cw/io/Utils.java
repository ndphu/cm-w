package ndphu.app.android.cw.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Base64;

public class Utils {
	public static List<File> listAllFiles(String[] string) {
		assert string != null;

		List<File> files = new ArrayList<File>();
		for (String dir : string) {
			File diretory = new File(dir);
			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isFile()) {
						if (pathname.getName().endsWith(".zip")) {
							return true;
						}
					}
					return false;
				}
			};
			File[] listFiles = diretory.listFiles(fileFilter);
			Comparator<File> fileNameComparator = new Comparator<File>() {

				@Override
				public int compare(File lhs, File rhs) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			};

			for (File file : listFiles) {
				files.add(file);
			}

			Collections.sort(files, fileNameComparator);
		}
		return files;
	}

	public static byte[] getRawDataFromURL(String url) {
		try {
			return IOUtils.toByteArray(new URL(processRawUrl(url)).openStream());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String processRawUrl(String url) {
		return url.replace(" ", "%20");
	}

	public static Bitmap getBitmapFromUrl(String url, int width, int height) {
		try {
			return BitmapFactory.decodeStream((InputStream) new URL(processRawUrl(url)).getContent());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		/*byte[] rawDataFromURL = getRawDataFromURL(url);
		if (rawDataFromURL == null) {
			try {
				Bitmap fromStream = BitmapFactory.decodeStream((InputStream) new URL(processRawUrl(url)).getContent());
				return fromStream;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return decodeBitmap(rawDataFromURL, width, width);
		}*/
	}

	public static Bitmap decodeBitmap(byte[] data, int width, int height) {
		long maxBitmapSize = (long) (width * height * 4 * 1.5);
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
		float ratio = (float) width / bitmap.getWidth();
		int destHeight = (int) (ratio * bitmap.getHeight());
		return Bitmap.createScaledBitmap(bitmap, width, destHeight, true);
	}

	public static String encodeToBase64(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);
	}

	public static Bitmap decodeFromBase64(String data) {
		if (!data.equalsIgnoreCase("")) {
			byte[] b = Base64.decode(data, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}
		return null;
	}
	
	public static final String md5(final String s) {
	    final String MD5 = "MD5";
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance(MD5);
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        for (byte aMessageDigest : messageDigest) {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

}
