package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.SearchResult;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

public class Manga24hSearchResultFragment extends Fragment {
	private ImageView mBookCover;
	private ListView mChapterList;
	private BookProcessor mProcessor;
	private ProgressDialog mProgressDialog;
	private Book mBook;

	private WeakReference<SearchResult> mBookItem;

	public Manga24hSearchResultFragment(SearchResult item) {
		mBookItem = new WeakReference<SearchResult>(item);
	}

	private AsyncTask<Void, Void, Book> mLoadingTask = new AsyncTask<Void, Void, Book>() {

		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setTitle("Loading");
			mProgressDialog.setMessage(mBookItem.get().bookName);
			mProgressDialog.show();
		};

		@Override
		protected Book doInBackground(Void... params) {
			try {
				return mProcessor.loadBook(mBookItem.get().bookSource, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Book result) {
			mProgressDialog.dismiss();
			if (result == null) {

			} else {
				renderBookContent();
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_manga24h, container, false);
		mBookCover = (ImageView) view.findViewById(R.id.fragment_manga24h_book_cover);
		mChapterList = (ListView) view.findViewById(R.id.fragment_manga24h_listview_chapter);
		mProcessor = new Manga24hProcessor();
		return view;
	}

	protected void renderBookContent() {
		Picasso.with(getActivity()).load(Uri.parse(mBook.getCover())).into(mBookCover);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((MainActivity) getActivity()).getToolbar().setTitle(mBookItem.get().bookName);
		mLoadingTask.execute();
	}

}
