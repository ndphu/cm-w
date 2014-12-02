package ndphu.app.android.cw;

import ndphu.app.android.cw.fragment.BookDetailsDialogFragment;
import ndphu.app.android.cw.fragment.HomePageFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.fragment.SearchFragment;
import ndphu.app.android.cw.fragment.SearchFragment.OnSearchItemSelected;
import ndphu.app.android.cw.model.SearchResult;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends ActionBarActivity implements OnNavigationItemSelected, OnSearchItemSelected {
	public static final String SOURCE_TRUYENTRANHTUAN = "TRUYENTRANHTUAN";
	public static final String SOURCE_MANGA24H = "MANGA24H";
	protected static final String TAG = MainActivity.class.getSimpleName();
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar mToolbar;
	private FragmentManager mFragmentManager;

	// For search fragment
	private SearchFragment mSearchResultFragment;
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;
	private View mFragmentResultContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mFragmentManager = getSupportFragmentManager();
		NavigationDrawerFragment mNavFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(R.id.fragment_drawer);
		mNavFragment.setNavigationItemSelected(this);
		onItemSelected(0);

		// Search
		mFragmentResultContainer = MainActivity.this.findViewById(R.id.fragment_search_result);
		mSearchResultFragment = new SearchFragment();
		mSearchResultFragment.setBookSearchListener(this);
		mFragmentManager.beginTransaction().replace(R.id.fragment_search_result, mSearchResultFragment).commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		HomePageFragment home = new HomePageFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, home).commit();
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
		customizeSearchMenuItem();
		customizeSearchView();
		return true;
	}

	public Menu getMenu() {
		return this.mMenu;
	}

	private void customizeSearchMenuItem() {
		MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, new MenuItemCompat.OnActionExpandListener() {



			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				mFragmentResultContainer.setVisibility(View.VISIBLE);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				mFragmentResultContainer.setVisibility(View.GONE);
				return true;
			}
		});
	}

	private void customizeSearchView() {
		mSearchView.setQueryHint("Enter the title of your book");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(int position) {
		mDrawerLayout.closeDrawers();
	}

	public Toolbar getToolbar() {
		return mToolbar;

	}

	@Override
	public void onSearchItemSelected(SearchResult selectedItem) {
		if (selectedItem.bookUrl != null && selectedItem.bookUrl.trim().length() > 0 && !selectedItem.bookUrl.trim().equals("0")) {
			showBookDetails(selectedItem);
		}
	}

	public void showBookDetails(SearchResult target) {
		BookDetailsDialogFragment detailFragment = new BookDetailsDialogFragment();
		detailFragment.setTarget(target);
		detailFragment.show(mFragmentManager, "BOOK_DETAILS_FRAGMENT");
	}
}
