package ndphu.app.android.cw.task;

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
	private SearchBookTaskListener mListener;
	private List<BookProcessor> mBookProcessors;

	public interface SearchBookTaskListener {
		void onStartSearching(String searchString);

		void onComplete(List<SearchResult> result);

		void onError(Exception ex);
	}

	public void setSearchBookTaskListener(SearchBookTaskListener listener) {
		mListener = listener;
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
		if (isCancelled()) {
			return;
		}
		if (mListener != null) {
			mListener.onStartSearching(this.mSearchString);
		}
	}

	@Override
	protected List<SearchResult> doInBackground(Void... params) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		for (BookProcessor processor : mBookProcessors) {
			if (isCancelled()) {
				return null;
			}
			List<SearchResult> bookResult = processor.search(this.mSearchString);
			result.addAll(bookResult);
		}
		return result;
	}

	@Override
	protected void onPostExecute(List<SearchResult> result) {
		super.onPostExecute(result);
		if (isCancelled()) {
			return;
		}
		if (mListener != null) {
			mListener.onComplete(result);
		}
	}

	public void cancelSearch() {
		this.cancel(true);

	}

}
