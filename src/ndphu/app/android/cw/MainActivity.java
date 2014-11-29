package ndphu.app.android.cw;

import ndphu.app.android.cw.fragment.BookSearchListFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.model.Source;
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

	public class MainActivity extends ActionBarActivity implements OnNavigationItemSelected {
	public static final String SOURCE_TRUYENTRANHTUAN = "TRUYENTRANHTUAN";
	public static final String SOURCE_MANGA24H = "MANGA24H";
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar mToolbar;
	private FragmentManager mFragmentManager;
	private BookSearchListFragment mBookSearchFragment;
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;
	private String mCurrentSearchString;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu = menu;
		mSearchMenuItem = mMenu.findItem(R.id.action_settings);
		mSearchView = (SearchView) mSearchMenuItem.getActionView();
		customizeSearchView();
		return true;
	}

	private void customizeSearchView() {
		mSearchView.setOnQueryTextListener(mBookSearchFragment);
		mSearchView.setQueryHint("Search on Manga24h");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(int position) {
		if (position < Source.SOURCES.size()) {
			// Replace with the search result fragment
			mBookSearchFragment = new BookSearchListFragment();
			mFragmentManager.beginTransaction().replace(R.id.content_frame, mBookSearchFragment).commit();
			Source bookSource = Source.SOURCES.get(position);
			mBookSearchFragment.setSource(bookSource.getId());
		}
	}



	public Toolbar getToolbar() {
		return mToolbar;

	}
}
