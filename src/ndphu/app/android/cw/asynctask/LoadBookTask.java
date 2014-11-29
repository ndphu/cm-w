package ndphu.app.android.cw.asynctask;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Book;
import android.os.AsyncTask;

public class LoadBookTask extends AsyncTask<Void, Void, Object> {

	private WeakReference<LoadBookListener> mListener;
	private String mBookUrl;

	public LoadBookTask(String bookUrl, LoadBookListener listener) {
		mBookUrl = bookUrl;
		mListener = new WeakReference<LoadBookTask.LoadBookListener>(listener);
	}

	public static interface LoadBookListener {
		public void onStart(String url);

		public void onComplete(Book book);

		public void onError(Exception ex);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener.get() != null) {
			mListener.get().onStart(mBookUrl);
		}
	}
	

	@Override
	protected Object doInBackground(Void... params) {
		try {
			return new Manga24hProcessor().loadBook(this.mBookUrl, false);
		} catch (Exception ex) {
			return ex;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if (mListener.get() != null) {
			if (result == null) {
				mListener.get().onError(new Exception("Unknow"));
			}
			if (result instanceof Book) {
				mListener.get().onComplete((Book) result);
			} else {
				mListener.get().onError((Exception) result);
			}
		}
	}

}
