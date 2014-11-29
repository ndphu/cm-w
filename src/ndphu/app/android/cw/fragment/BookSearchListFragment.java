package ndphu.app.android.cw.fragment;

import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.SearchResultAdapter;
import ndphu.app.android.cw.adapter.SearchResultAdapter.DisplayMode;
import ndphu.app.android.cw.asynctask.SearchBookTask;
import ndphu.app.android.cw.asynctask.SearchBookTask.SearchBookTaskListener;
import ndphu.app.android.cw.model.SearchResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BookSearchListFragment extends Fragment implements SearchBookTaskListener, OnQueryTextListener {

	private int mSourceId;
	private ListView mListView;
	private SearchResultAdapter mAdapter;

	public void setSource(int id) {
		this.mSourceId = id;
	}

	public void executeSearch(String searchString) {
		SearchBookTask task = new SearchBookTask(searchString, mSourceId);
		task.setSearchBookTaskListener(this);
		task.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_book_search_listview, container, false);
		mListView = (ListView) view.findViewById(R.id.fragment_book_search_listview);
		mAdapter = new SearchResultAdapter(getActivity(), 0);
		mAdapter.setDisplayMode(DisplayMode.LIST);
		mListView.setAdapter(mAdapter);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onStart(String searchString) {
	}

	@Override
	public void onComplete(List<SearchResult> result) {
		mAdapter.clear();
		mAdapter.addAll(result);
	}

	@Override
	public void onError(Exception ex) {
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
}
