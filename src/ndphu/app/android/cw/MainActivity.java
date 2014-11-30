package ndphu.app.android.cw;

import ndphu.app.android.cw.fragment.BookDetailsDialogFragment;
import ndphu.app.android.cw.fragment.BookSearchResultFragment;
import ndphu.app.android.cw.fragment.BookSearchResultFragment.OnSearchItemSelected;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements OnNavigationItemSelected, OnSearchItemSelected {
	public static final String SOURCE_TRUYENTRANHTUAN = "TRUYENTRANHTUAN";
	public static final String SOURCE_MANGA24H = "MANGA24H";
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar mToolbar;
	private FragmentManager mFragmentManager;
	private BookSearchResultFragment mSearchResultFragment;
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;

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
		NavigationDrawerFragment mNavFragment = (NavigationDrawerFragment) mFragmentManager
				.findFragmentById(R.id.fragment_drawer);
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
		mSearchView.setQueryHint("Search on Manga24h");
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
		if (position < Source.SOURCES.size()) {
			// Replace with the search result fragment
			Source bookSource = Source.SOURCES.get(position);
			mSearchResultFragment = new BookSearchResultFragment();
			mSearchResultFragment.setBookSearchListener(this);
			mFragmentManager.beginTransaction().replace(R.id.content_frame, mSearchResultFragment).commit();
			mSearchResultFragment.setSource(bookSource.getId());
		}
		mDrawerLayout.closeDrawers();
	}

	public Toolbar getToolbar() {
		return mToolbar;

	}

	@Override
	public void onSearchItemSelected(SearchResult selectedItem) {
		if (selectedItem.bookLink != null && selectedItem.bookLink.trim().length() > 0 && !selectedItem.bookLink.trim().equals("0")) {
			BookDetailsDialogFragment detailFragment = new BookDetailsDialogFragment();
			detailFragment.setBookUrl(selectedItem.bookLink);
			detailFragment.show(mFragmentManager, "BOOK_DETAILS_FRAGMENT");
		}
	}
}
