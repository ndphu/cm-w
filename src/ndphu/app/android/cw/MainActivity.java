package ndphu.app.android.cw;

import ndphu.app.android.cw.fragment.BookDetailsDialogFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.fragment.home.HomeFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment.OnSearchItemSelected;
import ndphu.app.android.cw.model.SearchResult;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements OnNavigationItemSelected, OnSearchItemSelected {
	protected static final String TAG = MainActivity.class.getSimpleName();
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	// private Toolbar mToolbar;
	private FragmentManager mFragmentManager;

	// For search fragment
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// mToolbar = (Toolbar) findViewById(R.id.toolbar);
		// setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Category");
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mFragmentManager = getSupportFragmentManager();
		NavigationDrawerFragment mNavFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(R.id.fragment_drawer);
		mNavFragment.setNavigationItemSelected(this);
		onItemSelected(0);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu = menu;
		mSearchMenuItem = mMenu.findItem(R.id.action_search);
		mSearchView = (SearchView) mSearchMenuItem.getActionView();
		customizeSearchView();
		return true;
	}

	public Menu getMenu() {
		return this.mMenu;
	}

	private void customizeSearchView() {
		mSearchView.setQueryHint("Enter the title of your book");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_search: {
			return true;
		}
		case android.R.id.home: {
			if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
				mDrawerLayout.closeDrawers();
			} else {
				mDrawerLayout.openDrawer(GravityCompat.START);
			}
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(int position) {
		if (mSearchMenuItem != null) {
			mSearchMenuItem.setVisible(false);
		}
		switch (position) {
		case 0:
			// Home
			HomeFragment home = new HomeFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, home).commit();
			break;
		case 1:
			// My Books
			break;
		case 2:
			SearchFragment searchFragment = new SearchFragment();
			searchFragment.setBookSearchListener(this);
			mFragmentManager.beginTransaction().replace(R.id.content_frame, searchFragment).commit();
			break;
		default:
			// Function not implements
			break;
		}
		mDrawerLayout.closeDrawers();
	}

	//
	// public Toolbar getToolbar() {
	// return mToolbar;
	//
	// }

	@Override
	public void onSearchItemSelected(SearchResult selectedItem) {
		if (selectedItem.bookUrl != null && selectedItem.bookUrl.trim().length() > 0 && !selectedItem.bookUrl.trim().equals("0")) {
			showBookDetails(selectedItem);
		}
	}

	public void showBookDetails(SearchResult target) {
		BookDetailsDialogFragment detailFragment = new BookDetailsDialogFragment();
		detailFragment.setTarget(target);
		//detailFragment.show(mFragmentManager, "BOOK_DETAILS_FRAGMENT");
		mFragmentManager.beginTransaction().replace(R.id.content_frame, detailFragment).addToBackStack(null).commit();
	}
}
