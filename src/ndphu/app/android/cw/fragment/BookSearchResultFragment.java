package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.SearchResultAdapter;
import ndphu.app.android.cw.adapter.SearchResultAdapter.DisplayMode;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.SearchBookTask;
import ndphu.app.android.cw.task.SearchBookTask.SearchBookTaskListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BookSearchResultFragment extends Fragment implements SearchBookTaskListener, OnQueryTextListener, OnItemClickListener {

	private int mSourceId;
	private ListView mListView;
	private SearchResultAdapter mAdapter;
	private SearchView mSearchView;
	private WeakReference<OnSearchItemSelected> mSearchItemSelectedListener;

	public void setSource(int id) {
		this.mSourceId = id;
	}
	
	public static interface OnSearchItemSelected {
		public void onSearchItemSelected(SearchResult selectedItem);
	}
	
	public BookSearchResultFragment(OnSearchItemSelected listener) {
		mSearchItemSelectedListener = new WeakReference<OnSearchItemSelected>(listener);
	}

	public void executeSearch(String searchString) {
		SearchBookTask task = new SearchBookTask(searchString, mSourceId);
		task.setSearchBookTaskListener(this);
		task.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_book_search_listview, container, false);
		mListView = (ListView) view.findViewById(R.id.fragment_book_search_listview);
		mAdapter = new SearchResultAdapter(getActivity(), 0);
		mAdapter.setDisplayMode(DisplayMode.LIST);
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
		mSearchView = (SearchView)((MainActivity)getActivity()).getMenu().findItem(R.id.action_search).getActionView();
		mSearchView.setOnQueryTextListener(this);
	}
	

	@Override
	public void onStart(String searchString) {
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
	}
}
