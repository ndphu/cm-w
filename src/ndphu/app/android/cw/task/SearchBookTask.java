package ndphu.app.android.cw.task;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;
import android.os.AsyncTask;

public class SearchBookTask extends AsyncTask<Void, Void, List<SearchResult>> {

	private String mSearchString;
	private WeakReference<SearchBookTaskListener> mListener;
	private BookProcessor mBookProcessor;

	public interface SearchBookTaskListener {
		void onStart(String searchString);

		void onComplete(List<SearchResult> result);

		void onError(Exception ex);
	}

	public void setSearchBookTaskListener(SearchBookTaskListener listener) {
		mListener = new WeakReference<SearchBookTaskListener>(listener);
	}

	public SearchBookTask(String searchString, int sourceType) {
		this.mSearchString = searchString;
		switch (sourceType) {
		case Source.MANGA24H:
			mBookProcessor = new Manga24hProcessor();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null && mListener.get() != null) {
			mListener.get().onStart(this.mSearchString);
		}
	}

	@Override
	protected List<SearchResult> doInBackground(Void... params) {
		List<Book> bookResult = mBookProcessor.search(this.mSearchString);
		List<SearchResult> result = new ArrayList<SearchResult>();
		for (Book book : bookResult) {
			result.add(new SearchResult(book.getName(), book.getBookDesc(), book.getBookUrl(), book.getCover(), Source.SOURCE_MANGA24H));
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
