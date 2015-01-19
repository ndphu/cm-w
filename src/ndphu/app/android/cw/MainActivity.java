package ndphu.app.android.cw;

import java.util.List;

import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment;
import ndphu.app.android.cw.fragment.NavigationDrawerFragment.OnNavigationItemSelected;
import ndphu.app.android.cw.fragment.favorite.FavoriteFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment;
import ndphu.app.android.cw.fragment.home.HomeFragment.HomeFragmentListener;
import ndphu.app.android.cw.fragment.reading.ReadingFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment;
import ndphu.app.android.cw.fragment.search.SearchFragment.OnSearchItemSelected;
import ndphu.app.android.cw.fragment.settings.SettingsFragment;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import ndphu.app.android.cw.task.SearchBook;
import ndphu.app.android.cw.task.SearchBook.SearchBookTaskListener;
import ndphu.app.android.cw.util.Utils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener,
		OnNavigationItemSelected, OnSearchItemSelected, HomeFragmentListener, LoadBookListener,
		MenuItemCompat.OnActionExpandListener {
	protected static final String TAG = MainActivity.class.getSimpleName();
	public static final String PREF_APP_SETTINGS = "pref_app_settings";
	public static final String PREF_SWIPE_MODE = "pref_app_settings_swipe_mode";
	public static final String PREF_PRELOAD_NEXT_CHAPTER = "pref_app_settings_preload_next_chapter";
	public static final String PREF_ENABLE_VOLUMN_KEY = "pref_app_settings_enable_volumn_key";
	public static final String PREF_ENABLE_CACHE_CHAPTERS = "pref_app_settings_enable_cache_chapters";
	public static final String PREF_ENABLE_CACHE_PAGES = "pref_app_settings_enable_cache_pages";

	// For intent
	public static final String EXTRA_BOOK_ID = "book_id";
	public static final String EXTRA_CHAPTER_INDEX = "chapter_index";
	public static final String EXTRA_READING_STATE = "extra_reading_state";

	public static final String FRAGMENT_READING_TAG = "reading";
	public static final int SWIPE_MODE_VERTICAL = 0;
	public static final int SWIPE_MODE_HORIZONTAL = 1;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	// private Toolbar mToolbar;
	private FragmentManager mFragmentManager;

	// For search fragment
	private Menu mMenu;
	private MenuItem mSearchMenuItem;
	private SearchView mSearchView;
	private PopupMenu searchPopupMenu;

	// Home fragment
	private HomeFragment mHomeFragment;
	private ProgressDialog mProgressDialog;
	private SearchFragment searchFragment;

	// Favorite fragment
	private FavoriteFragment mFavoriteFragment;
	private long mReadingBookId = -1;
	private SearchBook mSearchBookTask;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
				Log.i(TAG, "bs counter=" + mFragmentManager.getBackStackEntryCount());
				if (mFragmentManager.getBackStackEntryCount() > 0) {
					mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				} else {
					mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				}
			}
		});
		final NavigationDrawerFragment mNavFragment = (NavigationDrawerFragment) mFragmentManager
				.findFragmentById(R.id.fragment_drawer);
		mNavFragment.setNavigationItemSelected(this);
		new AsyncTask<Void, Void, Integer>() {
			protected void onPreExecute() {
				getSupportActionBar().hide();
			};

			@Override
			protected Integer doInBackground(Void... params) {
				// Initialize DAO instances
				DaoUtils.initialize(MainActivity.this);
				// If there is not favorite book, set page to HOME, otherwise,
				// set page
				// to Favorite book
				int initializePage = DaoUtils.getFavoriteBooks().size() == 0 ? 0 : 1;
				return initializePage;
			}

			protected void onPostExecute(Integer result) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				final View splashScreen = findViewById(R.id.activity_main_splash_screen);
				Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						splashScreen.setVisibility(View.GONE);
						getSupportActionBar().show();
					}
				});
				splashScreen.setAnimation(anim);
				mNavFragment.setSelection(result);
				if (savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_READING_STATE, false)) {
					// application was killed by low memory on device, we need
					// to restore the previous book
					Log.i(TAG, "restoring reading state");
					// force remove all reading fragments
					mFragmentManager.popBackStack(FRAGMENT_READING_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
					Long bookId = savedInstanceState.getLong(EXTRA_BOOK_ID, 0);
					final Book book = DaoUtils.getBookAndChapters(bookId);
					if (book != null) {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage("Do you want to continue from where you read?")
								.setPositiveButton("YES", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										Integer currentChapter = book.getCurrentChapter();
										if (currentChapter == null || currentChapter < 0) {
											currentChapter = book.getChapters().size() - 1;
										}
										openBook(book, currentChapter);
									}
								}).setNegativeButton("NO", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).create().show();
					}
				}
			};

		}.execute();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra(EXTRA_BOOK_ID)) {
			mReadingBookId = intent.getLongExtra(EXTRA_BOOK_ID, -1);
			final Book book = DaoUtils.getBookAndChapters(mReadingBookId);
			int currentChapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, book.getChapters().size() - 1);
			openBook(book, currentChapterIndex);
		}
	}

	/**
	 * Open book by create a ReadingFragment to show book content
	 * 
	 * @param book
	 * @param currentChapterIndex
	 */
	private void openBook(Book book, int currentChapterIndex) {
		if (book != null) {
			if (currentChapterIndex < 0) {
				currentChapterIndex = book.getChapters().size() - 1;
				book.setCurrentChapter(currentChapterIndex);
				DaoUtils.saveOrUpdate(book);
			}
			ReadingFragment fragment = new ReadingFragment();
			fragment.setBook(book);
			mFragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
							R.anim.slide_out_right).replace(R.id.content_frame, fragment)
					.addToBackStack(FRAGMENT_READING_TAG).commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.w(TAG, "Saving current state...");
		int entryCount = mFragmentManager.getBackStackEntryCount();
		Log.i(TAG, "Saving state reading.");
		if (entryCount > 0) {
			outState.putBoolean(MainActivity.EXTRA_READING_STATE, true);
			if (mReadingBookId > 0) {
				outState.putLong(EXTRA_BOOK_ID, mReadingBookId);
			}
		} else {
			outState.putBoolean(MainActivity.EXTRA_READING_STATE, false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
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
		if (selectedItem.bookUrl != null && selectedItem.bookUrl.trim().length() > 0
				&& !selectedItem.bookUrl.trim().equals("0")) {
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

	private void openBook(Book book) {
		// startReadingActivity(context, book);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(ReadingActivity.EXTRA_BOOK_ID, book.getId());
		intent.putExtra(ReadingActivity.EXTRA_CHAPTER_INDEX, book.getCurrentChapter());
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
			// startReadingActivity(savedBook);
			openBook(savedBook);
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
		// startReadingActivity(book);
		openBook(book);
	}

	@Override
	public void onError(Exception ex) {
		mProgressDialog.dismiss();
		ex.printStackTrace();
		new AlertDialog.Builder(this).setTitle("Error").setMessage(ex.getMessage())
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	// Search...
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		// searchFragment = new SearchFragment();
		// searchFragment.setBookSearchListener(this);
		// mFragmentManager
		// .beginTransaction()
		// .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom,
		// R.anim.slide_in_bottom,
		// R.anim.slide_out_top).replace(R.id.content_frame,
		// searchFragment).addToBackStack(null).commit();
		mSearchView = (SearchView) item.getActionView();
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		try {
			mFragmentManager.popBackStack();
		} catch (Exception ex) {

		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String text) {
		if (mSearchBookTask != null) {
			mSearchBookTask.cancelSearch();
		}
		if (searchPopupMenu != null) {
			try {
				searchPopupMenu.dismiss();
			} catch (Exception ex) {
				// ignore
			}
		}
		if (text.length() < 3) {
			return false;
		} else {
			mSearchBookTask = new SearchBook(text);
			mSearchBookTask.setSearchBookTaskListener(new SearchBookTaskListener() {

				@Override
				public void onStartSearching(String searchString) {

				}

				@Override
				public void onError(Exception ex) {
					ex.printStackTrace();
				}

				@Override
				public void onComplete(final List<SearchResult> result) {
					searchPopupMenu = new PopupMenu(MainActivity.this, mSearchView);
					for (SearchResult searchResult : result) {
						TextView tv = new TextView(MainActivity.this);
						tv.setText(Html.fromHtml(searchResult.bookName));
						searchPopupMenu.getMenu().add(tv.getText());
					}
					searchPopupMenu.show();
					searchPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem item) {
							int order = item.getOrder();
							SearchResult selectedResult = result.get(order);
							MainActivity.this.getMenu().findItem(R.id.action_search).collapseActionView();
							openBookFromSearch(selectedResult);
							return false;
						}
					});
				}
			});
			mSearchBookTask.execute();
		}
		return true;
	}
}
