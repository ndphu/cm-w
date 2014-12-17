package ndphu.app.android.cw.task;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.io.processor.BlogTruyenProcessor;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.SearchResult;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBookTask extends AsyncTask<Void, Void, Object> {

	private static final String TAG = LoadBookTask.class.getSimpleName();
	private WeakReference<LoadBookListener> mListener;
	private SearchResult mSearchResult;

	public LoadBookTask(SearchResult searchResult, LoadBookListener listener) {
		mSearchResult = searchResult;
		initListener(listener);
	}

	private void initListener(LoadBookListener listener) {
		mListener = new WeakReference<LoadBookTask.LoadBookListener>(listener);
	}

	public static interface LoadBookListener {
		public void onStartLoading(String url);

		public void onComplete(Book book);

		public void onError(Exception ex);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener.get() != null) {
			if (mSearchResult != null) {
				mListener.get().onStartLoading(mSearchResult.bookName);
			} else {
				mListener.get().onStartLoading("Uknown");
			}
		}
	}

	@Override
	protected Object doInBackground(Void... params) {
		try {
			BookProcessor processor = null;
			switch (this.mSearchResult.bookSource) {
			case BLOGTRUYEN:
				processor = new BlogTruyenProcessor();
				break;
			case MANGA24H:
				processor = new Manga24hProcessor();
				break;
			case IZMANGA:
				processor = new IZMangaProcessor();
				break;
			default:
				break;
			}
			return processor.loadBook(this.mSearchResult.bookUrl, false);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ex;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		Log.d(TAG, "postexecute loadbooktask");
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
