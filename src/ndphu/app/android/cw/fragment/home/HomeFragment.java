package ndphu.app.android.cw.fragment.home;

import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.HomePageItemAdapter;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class HomeFragment extends Fragment implements OnItemSelectedListener, OnItemClickListener, OnScrollListener {

	private static final String TAG = HomeFragment.class.getSimpleName();
	// private ViewPager mViewPager = null;
	//
	// private ScreenSlidePagerAdapter mPagerAdapter;

	private Spinner mSpinner;
	private GridView mGridView;
	private HomePageItemAdapter mGridAdapter;
	private String[] mCategories;
	private Category mCurrentCategory;
	public int mPage = 0;
	private SpinnerAdapter mSpinnerAdapter;
	private int mCurrentSpinnerPosition = 0;
	private ProgressDialog mProgressDialog;
	public boolean mIsLoading = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCurrentCategory = Category.NEW;
		mCategories = new String[Category.values().length];
		Category[] categories = Category.values();
		for (int i = 0; i < categories.length; ++i) {
			mCategories[i] = categories[i].getDisplayName();
		}
		setHasOptionsMenu(true);
		mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mCategories);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_homepage, container, false);
		// Spinner
		Log.i(TAG, "Init Spinner");
		mSpinner = (Spinner) view.findViewById(R.id.fragment_homepage_spinner_category);
		mSpinner.setAdapter(mSpinnerAdapter);
		mSpinner.setSelection(mCurrentSpinnerPosition);
		mSpinner.post(new Runnable() {
			@Override
			public void run() {
				mSpinner.setOnItemSelectedListener(HomeFragment.this);
				if (mGridAdapter.getCount() == 0) {
					new LoadDataTask().execute();
				}
			}
		});
		// Grid view
		Log.i(TAG, "Init Gridview");
		mGridView = (GridView) view.findViewById(R.id.fragment_homepage_gridview);
		if (mGridAdapter == null) {
			mGridAdapter = new HomePageItemAdapter(getActivity(), 0);
		}
		mGridView.setAdapter(mGridAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnScrollListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mCurrentCategory = Category.values()[position];
		if (position != mCurrentSpinnerPosition) {
			mCurrentSpinnerPosition = position;
			mGridAdapter.clear();
			mPage = 0;
		}
		new LoadDataTask().execute();
	}

	private class LoadDataTask extends AsyncTask<Void, Void, List<HomePageItem>> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "Start loading");
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setTitle("Loading");
			mProgressDialog.setMessage("Preparing " + mCurrentCategory.getDisplayName() + "...");
			mProgressDialog.show();
		}

		@Override
		protected List<HomePageItem> doInBackground(Void... params) {
			return new IZMangaHomeLoader().getByCategory(mCurrentCategory, mPage);
		}

		@Override
		protected void onPostExecute(List<HomePageItem> result) {
			super.onPostExecute(result);
			mPage++;
			mIsLoading = false;
			mGridAdapter.addAll(result);
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomePageItem item = mGridAdapter.getItem(position);
		SearchResult result = new SearchResult(item.mBookName, item.mBookUrl, item.mSource);
		((MainActivity) getActivity()).showBookDetails(result);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
			if (!mIsLoading) {
				Log.i(TAG, "Loading when scolling to end");
				mIsLoading = true;
				new LoadDataTask().execute();
			}
		}
	}
}
