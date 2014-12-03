package ndphu.app.android.cw.fragment;

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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SearchFragment extends Fragment implements SearchBookTaskListener, OnQueryTextListener,
		OnItemClickListener {

	private ListView mListView;
	private SearchResultAdapter mAdapter;
	private SearchView mSearchView;
	private WeakReference<OnSearchItemSelected> mSearchItemSelectedListener;

	public static interface OnSearchItemSelected {
		public void onSearchItemSelected(SearchResult selectedItem);
	}

	public void executeSearch(String searchString) {
		SearchBook task = new SearchBook(searchString);
		task.setSearchBookTaskListener(this);
		task.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		MainActivity activty = ((MainActivity) getActivity());
		activty.getToolbar().setTitle("Search");
		View view = inflater.inflate(R.layout.fragment_book_search_listview, container, false);
		mListView = (ListView) view.findViewById(R.id.fragment_book_search_listview);
		mAdapter = new SearchResultAdapter(getActivity(), 0);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem searchMenuItem = ((MainActivity) getActivity()).getMenu().findItem(R.id.action_search);
		mSearchView = (SearchView) searchMenuItem.getActionView();
		mSearchView.setOnQueryTextListener(this);
		searchMenuItem.setVisible(true);
		searchMenuItem.expandActionView();
	}

	@Override
	public void onStartSearching(String searchString) {
		mSearchView.setEnabled(false);
	}

	@Override
	public void onComplete(List<SearchResult> result) {
		mSearchView.setEnabled(true);
		mAdapter.clear();
		mAdapter.addAll(result);
	}

	@Override
	public void onError(Exception ex) {
		mSearchView.setEnabled(true);
	}

	@Override
	public boolean onQueryTextChange(String text) {
		if (text.length() < 2) {
			return false;
		}
		executeSearch(text);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		if (text.length() == 0) {
			return false;
		}
		executeSearch(text);
		return true;
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
}
