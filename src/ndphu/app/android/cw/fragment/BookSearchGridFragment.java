package ndphu.app.android.cw.fragment;

import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.SearchResultAdapter;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.SearchBookTask;
import ndphu.app.android.cw.task.SearchBookTask.SearchBookTaskListener;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class BookSearchGridFragment extends Fragment implements SearchBookTaskListener {

	private int mSourceId;
	private GridView mGridView;
	private SearchResultAdapter mGridAdapter;
	private ProgressDialog mProgressDialog;

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
		View view = inflater.inflate(R.layout.fragment_book_search, container, false);
		mGridView = (GridView) view.findViewById(R.id.fragment_book_search_gridview);
		mGridAdapter = new SearchResultAdapter(getActivity(), 0);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mGridView.setAdapter(mGridAdapter);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onStart(String searchString) {
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setTitle("Loading");
		mProgressDialog.setMessage("Search by \"" + searchString + "\"");
		mProgressDialog.show();
	}

	@Override
	public void onComplete(List<SearchResult> result) {
		mGridAdapter.clear();
		mGridAdapter.addAll(result);
		mProgressDialog.dismiss();
	}

	@Override
	public void onError(Exception ex) {
		mProgressDialog.dismiss();
	}
}
