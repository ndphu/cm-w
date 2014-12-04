package ndphu.app.android.cw.fragment.home;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.HomePageItemAdapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class HomeFragment extends Fragment implements OnItemSelectedListener, OnItemClickListener {

	// private ViewPager mViewPager = null;
	//
	// private ScreenSlidePagerAdapter mPagerAdapter;

	private Spinner mSpinner;
	private MenuItem mDropDownItem;
	private ActionBar mActionBar;
	private GridView mGridView;
	private HomePageItemAdapter mGridAdapter;
	private HomeLoader mHomeLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mSpinner = (Spinner) inflater.inflate(R.layout.actionbar_spinner, container, false);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.categories, android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setOnItemSelectedListener(this);
		mSpinner.setAdapter(mSpinnerAdapter);
		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(mSpinner);
		View view = inflater.inflate(R.layout.fragment_homepage, container, false);
		mGridView = (GridView) view.findViewById(R.id.fragment_homepage_gridview);
		mGridAdapter = new HomePageItemAdapter(getActivity(), 0);
		mGridView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		mDropDownItem = menu.findItem(R.id.action_select_category);
		mDropDownItem.setVisible(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_select_category:
			mSpinner.performClick();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		mDropDownItem.setVisible(false);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(false);
		super.onDestroy();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		TextView selectedTextView = (TextView) mSpinner.getSelectedView();
		selectedTextView.setTextSize(20);
		selectedTextView.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
		mGridView.setAdapter(mGridAdapter);
		mHomeLoader = new HotLoader();
		new LoadDataTask().execute();
	}

	private class LoadDataTask extends AsyncTask<Void, Void, List<HomePageItem>> {

		@Override
		protected List<HomePageItem> doInBackground(Void... params) {
			List<HomePageItem> result = new ArrayList<HomePageItem>();
			if (mHomeLoader != null) {
				result = mHomeLoader.getHomePageData();
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<HomePageItem> result) {
			super.onPostExecute(result);
			mGridAdapter.clear();
			mGridAdapter.addAll(result);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomePageItem item = mGridAdapter.getItem(position);
		SearchResult result = new SearchResult(item.mBookName, item.mBookUrl, Source.MANGA24H);
		((MainActivity) getActivity()).showBookDetails(result);
	}

	// private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
	// public ScreenSlidePagerAdapter(FragmentManager fm) {
	// super(fm);
	// }
	//
	// @Override
	// public Fragment getItem(int position) {
	// HomeDetailsFragment details = new HomeDetailsFragment();
	// details.setHomeLoader(new HotLoader());
	// return details;
	// }
	//
	// @Override
	// public int getCount() {
	// return HOME_PAGE_TITLES.length;
	// }
	//
	// @Override
	// public CharSequence getPageTitle(int position) {
	// return HOME_PAGE_TITLES[position];
	// }
	// }

}
