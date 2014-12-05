package ndphu.app.android.cw.task;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.io.processor.BlogTruyenProcessor;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.SearchResult;
import android.os.AsyncTask;

public class SearchBook extends AsyncTask<Void, Void, List<SearchResult>> {

	private String mSearchString;
	private WeakReference<SearchBookTaskListener> mListener;
	private List<BookProcessor> mBookProcessors;

	public interface SearchBookTaskListener {
		void onStartSearching(String searchString);

		void onComplete(List<SearchResult> result);

		void onError(Exception ex);
	}

	public void setSearchBookTaskListener(SearchBookTaskListener listener) {
		mListener = new WeakReference<SearchBookTaskListener>(listener);
	}

	public SearchBook(String searchString) {
		this.mSearchString = searchString;
		mBookProcessors = new ArrayList<BookProcessor>();
		mBookProcessors.add(new IZMangaProcessor());
		mBookProcessors.add(new Manga24hProcessor());
		mBookProcessors.add(new BlogTruyenProcessor());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null && mListener.get() != null) {
			mListener.get().onStartSearching(this.mSearchString);
		}
	}

	@Override
	protected List<SearchResult> doInBackground(Void... params) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		for (BookProcessor processor : mBookProcessors) {
			List<SearchResult> bookResult = processor.search(this.mSearchString);
			result.addAll(bookResult);
		}
		return result;
	}

	@Override
	protected void onPostExecute(List<SearchResult> result) {
		super.onPostExecute(result);
		if (mListener != null && mListener.get() != null) {
			mListener.get().onComplete(result);
		}
	}
}
