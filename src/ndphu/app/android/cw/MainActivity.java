package ndphu.app.android.cw;

import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.fragment.favorite.FavoriteFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment.HomeFragmentListener;
import ndphu.app.android.cw.fragment.reading.VerticalReadingFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment.OnSearchItemSelected;
import ndphu.app.android.cw.fragment.settings.SettingsFragment;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import ndphu.app.android.cw.util.Utils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, OnNavigationItemSelected, OnSearchItemSelected,
		HomeFragmentListener, LoadBookListener, MenuItemCompat.OnActionExpandListener {
	protected static final String TAG = MainActivity.class.getSimpleName();
	public static final String PREF_APP_THEME = "pref_app_theme";
	// For intent
	public static final String EXTRA_BOOK_ID = "book_id";
	public static final String EXTRA_CHAPTER_INDEX = "chapter_index";

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	// private Toolbar mToolbar;
	private FragmentManager mFragmentManager;

	// For search fragment
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;

	// Home fragment
	private HomeFragment mHomeFragment;
	private ProgressDialog mProgressDialog;
	private SearchFragment searchFragment;

	// Favorite fragment
	private FavoriteFragment mFavoriteFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int appTheme = getSharedPreferences(PREF_APP_THEME, Context.MODE_APPEND).getInt(PREF_APP_THEME, R.style.AppBaseThemeLight);
		setTheme(appTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// mToolbar = (Toolbar) findViewById(R.id.toolbar);
		// setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
		// mToolbar, R.string.app_name, R.string.app_name);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mFragmentManager = getSupportFragmentManager();
		mFragmentManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {

			@Override
			public void onBackStackChanged() {
				if (mFragmentManager.getBackStackEntryCount() > 0) {
					String bsEntryName = mFragmentManager.getBackStackEntryAt(0).getName();
					if (!"reading".equals(bsEntryName)) {
						mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
					}
				}
			}
		});
		NavigationDrawerFragment mNavFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(R.id.fragment_drawer);
		mNavFragment.setNavigationItemSelected(this);
		// Initialize DAO instances
		// If there is not favorite book, set page to HOME, otherwise, set page
		// to Favorite book
		if (DaoUtils.getFavoriteBooks().size() == 0) {
			// No favorite, go to HOME
			mNavFragment.setSelection(0);
		} else {
			mNavFragment.setSelection(1);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra(EXTRA_BOOK_ID)) {
			final Book book = DaoUtils.getBookAndChapters(intent.getLongExtra(EXTRA_BOOK_ID, -1));
			if (book != null) {
				int currentChapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, book.getChapters().size() - 1);
				if (currentChapterIndex < 0) {
					currentChapterIndex = book.getChapters().size() - 1;
					book.setCurrentChapter(currentChapterIndex);
					DaoUtils.saveOrUpdate(book);
				}
				VerticalReadingFragment fragment = new VerticalReadingFragment();
				fragment.setBook(book);
				mFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("reading").commit();
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			}
		}
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
		mSearchView.setOnQueryTextListener(this);
		MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
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
			MenuItemCompat.collapseActionView(mSearchMenuItem);
		}
		supportInvalidateOptionsMenu();
		switch (position) {
		case 0:
			getSupportActionBar().setTitle("Home");
			if (mHomeFragment == null) {
				mHomeFragment = new HomeFragment();
				mHomeFragment.setHomeFragmentListener(this);
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mHomeFragment).commit();
			break;
		case 1:
			getSupportActionBar().setTitle("My Books");
			// My Books
			if (mFavoriteFragment == null) {
				mFavoriteFragment = new FavoriteFragment();
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFavoriteFragment).commit();
			break;
		case 2:
			getSupportActionBar().setTitle("Settings");
			// Settings
			SettingsFragment settingsFragment = new SettingsFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
			break;
		default:
			// Function not implements
			break;
		}
		mDrawerLayout.closeDrawers();
	}

	@Override
	public void onSearchItemSelected(SearchResult selectedItem) {
		if (selectedItem.bookUrl != null && selectedItem.bookUrl.trim().length() > 0 && !selectedItem.bookUrl.trim().equals("0")) {
			// showBookDetails(selectedItem);
			openBookFromSearch(selectedItem);
		}
	}

	public void openBookFromSearch(SearchResult selectedItem) {
		new LoadBookTask(selectedItem, this).execute();
	}

	public void startReadingActivity(Book book) {
		Intent intent = new Intent(this, ReadingActivity.class);
		intent.putExtra(ReadingActivity.EXTRA_BOOK_ID, book.getId());
		startActivity(intent);
	}

	@Override
	public void onHomePageItemSelected(HomePageItem item) {
		String hasedUrl = Utils.getMD5Hash(item.bookUrl);
		Log.d(TAG, "BookHased:" + hasedUrl);
		Book savedBook = DaoUtils.getBookByHasedUrl(hasedUrl);
		if (savedBook == null) {
			SearchResult result = new SearchResult(item.bookName, item.bookUrl, item.source);
			new LoadBookTask(result, this).execute();
		} else {
			startReadingActivity(savedBook);
		}
	}

	// Callbacks for Book loaded
	@Override
	public void onStartLoading(String url) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage("Please wait...");
		mProgressDialog.show();
	}

	@Override
	public void onComplete(Book book) {
		DaoUtils.saveOrUpdate(book);
		mProgressDialog.dismiss();
		startReadingActivity(book);
	}

	@Override
	public void onError(Exception ex) {
		mProgressDialog.dismiss();
		ex.printStackTrace();
		new AlertDialog.Builder(this).setTitle("Error").setMessage(ex.getMessage()).setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	// Search...
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		searchFragment = new SearchFragment();
		searchFragment.setBookSearchListener(this);
		mFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top)
				.replace(R.id.content_frame, searchFragment).addToBackStack(null).commit();
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		mFragmentManager.popBackStack();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		if (text.length() < 2) {
			return false;
		}
		searchFragment.executeSearch(text);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String text) {
		if (text.length() < 2) {
			return false;
		}
		searchFragment.executeSearch(text);
		return true;
	}
}
