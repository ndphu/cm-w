package ndphu.app.android.cw.runable;

import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class DownloadFileRunnable implements Runnable {

	public static interface DownloadFileListener {
		public void onCompleted(String url, String destination, long fileSize);

		public void onFailed(String url, String destination, Exception ex);
	}

	private static final String TAG = DownloadFileRunnable.class.getSimpleName();
	private String mUrl = null;
	private String mDestination = null;
	// private WeakReference<DownloadFileListener> mListener;
	private DownloadFileListener mListener;

	public DownloadFileRunnable(String url, String destination) {
		setUrl(url);
		setDestination(destination);
	}

	// public void setDownloadFileListener(DownloadFileListener listener) {
	// mListener = new WeakReference<DownloadFileListener>(listener);
	// }
	public void setDownloadFileListener(DownloadFileListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		Log.d(TAG, "Download file at " + getUrl() + " and saved as " + getDestination());
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(getUrl());
		try {
			HttpResponse response = client.execute(get);
			int downloadedSize = IOUtils
					.copy(response.getEntity().getContent(), new FileOutputStream(getDestination()));
			// if (mListener != null && mListener.get() != null) {
			// mListener.get().onCompleted(getUrl(), getDestination(),
			// downloadedSize);
			// }
			if (mListener != null) {
				mListener.onCompleted(getUrl(), getDestination(), downloadedSize);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// if (mListener != null && mListener.get() != null) {
			// mListener.get().onFailed(getUrl(), getDestination(), ex);
			// }
			if (mListener != null) {
				mListener.onFailed(getUrl(), getDestination(), ex);
			}
		}
	}

	public String getUrl() {
		mUrl = mUrl.replace(" ", "%20");
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getDestination() {
		return mDestination;
	}

	public void setDestination(String mDestination) {
		this.mDestination = mDestination;
	}

}
