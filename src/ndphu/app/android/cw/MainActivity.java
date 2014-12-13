package ndphu.app.android.cw;

import java.util.List;

import ndphu.app.android.cw.dao.BookDao;
import ndphu.app.android.cw.dao.ChapterDao;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.fragment.favorite.FavoriteFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment.HomeFragmentListener;
import ndphu.app.android.cw.fragment.search.SearchFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment.OnSearchItemSelected;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import ndphu.app.android.cw.util.Utils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

	// Dao
	BookDao mBookDao;
	ChapterDao mChapterDao;

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
		mBookDao = new BookDao(this);
		mChapterDao = new ChapterDao(this);
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
			if (mHomeFragment == null) {
				mHomeFragment = new HomeFragment();
				mHomeFragment.setHomeFragmentListener(this);
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mHomeFragment).commit();
			break;
		case 1:
			// My Books
			if (mFavoriteFragment == null) {
				mFavoriteFragment = new FavoriteFragment();
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFavoriteFragment).commit();
			break;
		case 2:
			// Settings
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
		String hasedUrl = Utils.getMD5Hash(item.mBookUrl);
		Log.d(TAG, "BookHased:" + hasedUrl);
		List<Book> books = mBookDao.readAllWhere(Book.COL_HASED_URL, hasedUrl);
		if (books.size() == 0) {
			SearchResult result = new SearchResult(item.mBookName, item.mBookUrl, item.mSource);
			new LoadBookTask(result, this).execute();
		} else {
			Book savedBook = books.get(0);
			loadChaptersList(savedBook);
			startReadingActivity(savedBook);
		}
	}

	// Dao
	private Book loadChaptersList(Book book) {
		book.getChapters().addAll(mChapterDao.readAllWhere(Chapter.COL_BOOK_ID, book.getId() + ""));
		return book;
	}

	private void saveBookToDB(Book book) {
		long newBookId = mBookDao.create(book);
		book.setId(newBookId);
		Log.d(TAG, "New book id: " + newBookId);
		for (Chapter chapter : book.getChapters()) {
			chapter.setBookId(newBookId);
			mChapterDao.create(chapter);
		}
	}

	// Callbacks for Book loaded
	@Override
	public void onStart(String url) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setTitle("Loading");
		mProgressDialog.setMessage("Please wait...");
		mProgressDialog.show();
	}

	@Override
	public void onComplete(Book book) {
		saveBookToDB(book);
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
