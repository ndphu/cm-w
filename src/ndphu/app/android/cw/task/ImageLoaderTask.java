package ndphu.app.android.cw.task;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.io.Utils;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;

public class ImageLoaderTask extends AsyncTask<Object, Void, Object[]> {

	private static final String TAG = ImageLoaderTask.class.getSimpleName();

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "Loading image");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] doInBackground(Object... params) {
		String filePath = (String) params[0];

		WeakReference<TouchImageView> imageView = (WeakReference<TouchImageView>) params[2];
		// Fix out of memory error
		Display display = ((WindowManager) imageView.get().getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screeHeight = size.y;
		Bitmap result = null;
		try {
			result = BitmapFactory.decodeFile(filePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e(TAG, "Regular bitmap decoding process failed for image at " + filePath + ". Retry with owned decoding process.");
			try {
				result = Utils.decodeBitmap(IOUtils.toByteArray(new FileInputStream(filePath)), screenWidth, screeHeight);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Owned decoding process failed.");
			}
		}
		return new Object[] { result, imageView };
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onPostExecute(Object[] result) {
		Bitmap bm = (Bitmap) result[0];
		WeakReference<TouchImageView> iv = (WeakReference<TouchImageView>) result[1];
		if (iv != null && iv.get() != null) {
			iv.get().setImageBitmap(bm);
			updateScaleTypeFromOrientation(iv.get());
		}
	}

	private void updateScaleTypeFromOrientation(final TouchImageView img) {
		if (img == null || img.getContext() == null) {
			return;
		}
		Configuration configuration = img.getContext().getResources().getConfiguration();
		boolean isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (isLandscape) {
			img.setScaleType(ScaleType.CENTER_CROP);
			img.setScrollPosition(0f, 0f);
		} else {
			img.setScaleType(ScaleType.FIT_CENTER);
		}
	}
}