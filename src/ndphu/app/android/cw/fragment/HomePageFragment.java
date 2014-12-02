package ndphu.app.android.cw.fragment;

import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.HomePageItemAdapter;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.SearchResult.Source;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class HomePageFragment extends Fragment implements OnItemClickListener {

	private GridView mGridView;
	private HomePageItemAdapter mGridAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_homepage, container, false);
		mGridView = (GridView) view.findViewById(R.id.fragment_homepage_gridview);
		mGridAdapter = new HomePageItemAdapter(getActivity(), 0);
		mGridView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mGridView.setAdapter(mGridAdapter);
		new LoadHomePage().execute();
	}

	private class LoadHomePage extends AsyncTask<Void, Void, List<HomePageItem>> {

		@Override
		protected List<HomePageItem> doInBackground(Void... arg0) {
			return new Manga24hProcessor().getHomePages();
		}

		@Override
		protected void onPostExecute(List<HomePageItem> result) {
			super.onPostExecute(result);
			mGridAdapter.clear();
			mGridAdapter.addAll(result);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomePageItem item = mGridAdapter.getItem(position);
		SearchResult result = new SearchResult(item.mBookName, item.mBookUrl, Source.MANGA24H);
		((MainActivity) getActivity()).showBookDetails(result);
	}
}
