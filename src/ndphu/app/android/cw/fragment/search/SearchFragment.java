package ndphu.app.android.cw.fragment.search;

import java.lang.ref.WeakReference;
import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.SearchResultAdapter;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.SearchBook;
import ndphu.app.android.cw.task.SearchBook.SearchBookTaskListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SearchFragment extends Fragment implements SearchBookTaskListener, OnItemClickListener {

	private static final String TAG = "SearchFragment";
	private ListView mListView;
	private SearchResultAdapter mAdapter;
	private WeakReference<OnSearchItemSelected> mSearchItemSelectedListener;

	public void executeSearch(String searchString) {
		Log.i(TAG, "Execute search: " + searchString);
		SearchBook task = new SearchBook(searchString);
		task.setSearchBookTaskListener(this);
		task.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "On Create View");
		View view = inflater.inflate(R.layout.fragment_book_search_listview, container, false);
		mListView = (ListView) view.findViewById(R.id.fragment_book_search_listview);
		if (mAdapter == null) {
			mAdapter = new SearchResultAdapter(getActivity(), 0);
		}
		mListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.i(TAG, "On View Created");
		super.onViewCreated(view, savedInstanceState);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onStartSearching(String searchString) {
		// mSearchView.setEnabled(false);
	}

	@Override
	public void onComplete(List<SearchResult> result) {
		Log.i(TAG, "result set size: " + result.size());
		mListView.setVisibility(View.VISIBLE);
		mAdapter.clear();
		mAdapter.addAll(result);
	}

	@Override
	public void onError(Exception ex) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SearchResult selectedItem = mAdapter.getItem(position);
		if (mSearchItemSelectedListener.get() != null) {
			mSearchItemSelectedListener.get().onSearchItemSelected(selectedItem);
		}
		((MainActivity) getActivity()).getMenu().findItem(R.id.action_search).collapseActionView();
	}

	public void setBookSearchListener(OnSearchItemSelected listener) {
		mSearchItemSelectedListener = new WeakReference<OnSearchItemSelected>(listener);
	}

	public static interface OnSearchItemSelected {
		public void onSearchItemSelected(SearchResult selectedItem);
	}
}
