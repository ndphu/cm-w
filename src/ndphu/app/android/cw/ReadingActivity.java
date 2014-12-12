package ndphu.app.android.cw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ndphu.app.android.cw.customview.ChapterNavigationBar;
import ndphu.app.android.cw.customview.ChapterNavigationBar.ChapterNavigationBarListener;
import ndphu.app.android.cw.customview.ExtendedViewPager;
import ndphu.app.android.cw.customview.LoadingProgressIndicator;
import ndphu.app.android.cw.customview.LoadingProgressIndicator.LoadingProgressIndicatorListener;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.fragment.BookDetailsFragment;
import ndphu.app.android.cw.io.processor.BlogTruyenProcessor;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.runable.DownloadFileRunnable;
import ndphu.app.android.cw.runable.DownloadFileRunnable.DownloadFileListener;
import ndphu.app.android.cw.util.Utils;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

public class ReadingActivity extends ActionBarActivity implements LoadingProgressIndicatorListener, RejectedExecutionHandler, DrawerListener,
		BookDetailsFragment.OnChapterSelectedListener, ChapterNavigationBarListener {
	public static final String TAG = ReadingActivity.class.getSimpleName();
	public static final String EXTRA_BOOK_JSON = "book_in_json";
	public static final String EXTRA_CHAPTER_INDEX = "chapter_index";

	public static final String PREF_CURRENT_CHAPTER = "pref_current_chapter";
	// Caching params
	private final Object mDiskCacheLock = new Object();
	protected ChapterNavigationBar mChapterNavigation;
	// GUI elements
	private ExtendedViewPager mViewPager;
	private LoadingProgressIndicator mLoadingIndicator;
	private BookDetailsFragment mBookDetailsFragment;
	// Activity data
	private Book mBook;
	private List<Page> mPages;
	private Chapter mCurrentChapter;
	private int mCurrentChapterIndex;
	private boolean mCacheInitialized = false;
	private File mCacheDir;
	private Map<String, String> mCachedMap;
	// Thread executor params
	private int mCorePoolSize = 8;
	private int mMaximumPoolSize = 24;
	private long mKeepAlive = 10000;
	private ThreadPoolExecutor mExecutor;
	private DrawerLayout mDrawerLayout;
	// Navigation state
	private boolean mHasNext;
	private boolean mHasPrev;
	private PagerAdapter mPageAdapter = new PagerAdapter() {

		@Override
		public int getCount() {
			// Add the lastpage for navigation
			return mPages.size() + 1;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			if (position < mPages.size()) {
				final TouchImageView img = new TouchImageView(container.getContext());
				img.setImageResource(R.drawable.ic_placeholder_loading);
				img.setScaleType(ScaleType.CENTER);
				updateScaleTypeFromOrientation(img);
				String hasedUrl = mPages.get(position).getHashedUrl();
				String cachedFile = getBitmapFileFromCache(hasedUrl);
				if (cachedFile == null) {
					Log.w(TAG, "Cached is not ready for url " + hasedUrl);
				} else {
					Log.d(TAG, "Cache hit: " + cachedFile);
					new ImageLoader().execute(cachedFile, hasedUrl, new WeakReference<TouchImageView>(img));
				}
				container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				img.setTag(hasedUrl);
				return img;
			} else {
				if (mChapterNavigation == null) {
					mChapterNavigation = (ChapterNavigationBar) getLayoutInflater().inflate(R.layout.cv_chapter_navigation, container, false);
					mChapterNavigation.setChapterNavigationBarListener(ReadingActivity.this);
				}
				mChapterNavigation.showPrev(mHasPrev);
				mChapterNavigation.showNext(mHasNext);
				container.addView(mChapterNavigation, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				return mChapterNavigation;
			}

		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	};
	private SharedPreferences mSharedPref;
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (position < mCurrentChapter.getPages().size()) {
				Toast.makeText(ReadingActivity.this, (position + 1) + "/" + mPages.size(), Toast.LENGTH_SHORT).show();
				Page page = mPages.get(position);
				String viewTag = page.getHashedUrl();
				TouchImageView tiv = (TouchImageView) mViewPager.findViewWithTag(viewTag);
				if (tiv != null) {
					if (tiv.getScaleType() == ScaleType.FIT_CENTER) {
						// this view still not updated for new orientation, so we
						// need to update manually
						updateScaleTypeFromOrientation(tiv);
					}
				}
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};
	private int mScreenWidth;
	private int mScreenHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_reading);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScreenWidth = size.x;
		mScreenHeight = size.y;
		// hide actionbar
		getSupportActionBar().hide();

		// Init thread pool
		mExecutor = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAlive, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), this);

		// Init drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerLayout.setDrawerListener(this);

		// Init layout
		// Init view pager
		mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
		mLoadingIndicator = (LoadingProgressIndicator) findViewById(R.id.activity_reading_progress_bar);
		mLoadingIndicator.setLoadingProgressIndicatorListener(this);
		mLoadingIndicator.setAlpha(0.8f);

		mBookDetailsFragment = (BookDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
		mBookDetailsFragment.setOnChapterSelectedListener(this);
		readFromIntent(getIntent());
	}

	private void readFromIntent(Intent intent) {
		// Read from intent
		// mChapterUrl = intent.getStringExtra(EXTRA_CHAPTER_URL);
		// mChapterUrlList = intent.getCharSequenceArrayListExtra(EXTRA_CHAPTER_ARRAY);
		// mChapterSource = Source.valueOf(intent.getStringExtra(EXTRA_SOURCE));
		// mCurrentChapterIndex = mChapterUrlList.indexOf(mChapterUrl);
		Gson gson = new Gson();
		mBook = gson.fromJson(intent.getStringExtra(EXTRA_BOOK_JSON), Book.class);
		mBookDetailsFragment.setBook(mBook);
		if (mBook.getChapters().size() == 0) {
			new AlertDialog.Builder(this).setTitle("Info").setMessage("This book has no chapter")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).show();
		} else {
			// Default is the first chapter
			final int currentChapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, mBook.getChapters().size() - 1);
			mSharedPref = getSharedPreferences(Utils.getMD5Hash(mBook.getBookUrl()), Context.MODE_PRIVATE);
			if (mSharedPref.contains(PREF_CURRENT_CHAPTER)) {
				final int savedChapterIndex = mSharedPref.getInt(PREF_CURRENT_CHAPTER, currentChapterIndex);
				if (mBookDetailsFragment.isValidChapterIndex(savedChapterIndex) && currentChapterIndex != savedChapterIndex) {
					new AlertDialog.Builder(this).setTitle("Info").setMessage("Do you wish to resume from where you read?")
							.setPositiveButton("YES", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									mBookDetailsFragment.setSetCurrentChapterIndex(savedChapterIndex);
								}
							}).setNegativeButton("NO", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									mBookDetailsFragment.setSetCurrentChapterIndex(currentChapterIndex);
								}
							}).show();
				} else {
					mBookDetailsFragment.setSetCurrentChapterIndex(currentChapterIndex);
				}
			} else {
				mBookDetailsFragment.setSetCurrentChapterIndex(currentChapterIndex);
			}
		}
		mDrawerLayout.openDrawer(GravityCompat.START);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		readFromIntent(intent);
	}

	private void refresh() {
		// Init cache dir
		mCacheDir = new File(getExternalCacheDir().getAbsolutePath() + "/" + Utils.getMD5Hash(mCurrentChapter.getChapterUrl()));
		new InitCacheTask().execute(mCacheDir);
		// Execute task
		new LoadChapterDataTask().execute();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Update current
		updateViewAtPosition(mViewPager.getCurrentItem());
	}

	private void updateViewAtPosition(int position) {
		Page page = mPages.get(position);
		loadImageToView(page.getHashedUrl());
	}

	public boolean isLandScape() {
		Configuration configuration = getResources().getConfiguration();
		return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	private void updateScaleTypeFromOrientation(final TouchImageView img) {
		if (isLandScape()) {
			img.setScaleType(ScaleType.CENTER_CROP);
			img.setScrollPosition(0f, 0f);
		} else {
			img.setScaleType(ScaleType.FIT_CENTER);
		}
	}

	private void loadImageToView(String viewTag) {
		if (getBitmapFileFromCache(viewTag) == null) {
			return;
		}
		TouchImageView viewToBeUpdated = (TouchImageView) mViewPager.findViewWithTag(viewTag);
		if (viewToBeUpdated != null) {
			new ImageLoader().execute(getBitmapFileFromCache(viewTag), viewTag, new WeakReference<TouchImageView>(viewToBeUpdated));
		}
	}

	@Override
	public void onCancelProgress() {
		mLoadingIndicator.setVisibility(View.GONE);
	}

	public void addBitmapFileToCache(String key, String url) {
		// Also add to disk cache
		synchronized (mDiskCacheLock) {
			if (mCachedMap != null && mCachedMap.get(key) == null) {
				mCachedMap.put(key, url);
			}
		}
	}

	public String getBitmapFileFromCache(String key) {
		synchronized (mDiskCacheLock) {
			// Wait while disk cache is started from background thread
			while (!mCacheInitialized) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mCachedMap != null) {
				return mCachedMap.get(key);
			}
		}
		return null;
	}

	;

	@Override
	protected void onDestroy() {
		mExecutor.shutdownNow();
		super.onDestroy();
	}

	;

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// Sure the runnable is DownloadFileRunnable
		DownloadFileRunnable dfr = (DownloadFileRunnable) r;
		Log.e(TAG, "Reject execution: " + dfr.getUrl());

	}

	@Override
	public void onNextClick() {
		int newChapIdx = mCurrentChapterIndex - 1;
		goToChapter(newChapIdx);
	}

	@Override
	public void onPrevClick() {
		int newChapIdx = mCurrentChapterIndex + 1;
		goToChapter(newChapIdx);
	}

	private void goToChapter(int chapterIndex) {
		if (mBookDetailsFragment.isValidChapterIndex(chapterIndex)) {
			mBookDetailsFragment.setSetCurrentChapterIndex(chapterIndex);
		}
	}

	@Override
	public void onDrawerClosed(View arg0) {

	}

	@Override
	public void onDrawerOpened(View arg0) {

	}

	@Override
	public void onDrawerSlide(View arg0, float arg1) {

	}

	@Override
	public void onDrawerStateChanged(int arg0) {

	}

	@Override
	public void onChapterSelected(int chapterIndex) {
		mSharedPref.edit().putInt(PREF_CURRENT_CHAPTER, chapterIndex).commit();
		mCurrentChapterIndex = chapterIndex;
		mCurrentChapter = mBook.getChapters().get(chapterIndex);
		mHasNext = mBookDetailsFragment.isValidChapterIndex(chapterIndex - 1);
		mHasPrev = mBookDetailsFragment.isValidChapterIndex(chapterIndex + 1);
		refresh();
		mDrawerLayout.closeDrawers();
	}

	class InitCacheTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				cacheDir.mkdir();
				mDiskCacheLock.notifyAll();
				mCacheInitialized = true;
				mCachedMap = new HashMap<String, String>();
				Log.d(TAG, "Cache dir path: " + mCacheDir.getAbsolutePath());
			}
			return null;
		}
	}

	private class LoadChapterDataTask extends AsyncTask<Void, Void, Object> {

		WeakReference<ProgressDialog> mProgressDialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ProgressDialog pd = new ProgressDialog(new WeakReference<Activity>(ReadingActivity.this).get());
			mProgressDialog = new WeakReference<ProgressDialog>(pd);
			pd.setTitle("Loading");
			pd.setMessage("Preparing book data. It may take one minute or two.");
			pd.show();
		}

		@Override
		protected Object doInBackground(Void... params) {
			BookProcessor processor = null;
			switch (mCurrentChapter.getChapterSource()) {
			case MANGA24H:
				processor = new Manga24hProcessor();
				break;
			case BLOGTRUYEN:
				processor = new BlogTruyenProcessor();
				break;
			case IZMANGA:
				processor = new IZMangaProcessor();
			default:
				break;
			}
			try {
				return processor.getPageList(mCurrentChapter.getChapterUrl());
			} catch (IOException e) {
				e.printStackTrace();
				return e;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDialog != null && mProgressDialog.get() != null) {
				mProgressDialog.get().dismiss();
			}
			if (result != null) {
				if (result instanceof Exception) {
					new AlertDialog.Builder(ReadingActivity.this).setTitle("Error").setMessage(((Exception) result).getMessage()).create().show();
				} else if (result instanceof List) {
					mPages = (List<Page>) result;
					mViewPager.setAdapter(mPageAdapter);
					mViewPager.setOnPageChangeListener(mPageChangeListener);
					new PageLoaderTask().execute();
				}
			}
		}

		;

	}

	private class ImageLoader extends AsyncTask<Object, Void, Object[]> {

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "Loading image");
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Object[] doInBackground(Object... params) {
			String filePath = (String) params[0];

			WeakReference<TouchImageView> imageView = (WeakReference<TouchImageView>) params[2];
			// Fix out of memory error
			Bitmap result = null;
			try {
				result = BitmapFactory.decodeFile(filePath);
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(TAG, "Regular bitmap decoding process failed for image at " + filePath + ". Retry with owned decoding process.");
				try {
					result = Utils.decodeBitmap(IOUtils.toByteArray(new FileInputStream(filePath)), mScreenWidth, mScreenHeight);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Owned decoding process failed.");
				}
			}
			return new Object[] { result, imageView };
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void onPostExecute(Object[] result) {
			Bitmap bm = (Bitmap) result[0];
			WeakReference<TouchImageView> iv = (WeakReference<TouchImageView>) result[1];
			if (iv != null && iv.get() != null) {
				iv.get().setImageBitmap(bm);
				updateScaleTypeFromOrientation(iv.get());
			}
		}

		;

	}

	private class PageLoaderTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected void onPreExecute() {
			mLoadingIndicator.setVisibility(View.VISIBLE);
			mLoadingIndicator.setProgressBarProgress(0);
			mLoadingIndicator.setProgressBarMax(mPages.size());
		}

		;

		@Override
		protected void onProgressUpdate(Object... values) {
			Integer loadedCount = (Integer) values[0];
			mLoadingIndicator.setProgressBarProgress(loadedCount);
			if (loadedCount >= mPages.size()) {
				mLoadingIndicator.setVisibility(View.GONE);
			}
			mPageAdapter.notifyDataSetChanged();
			String viewTag = (String) values[1];
			loadImageToView(viewTag);
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (Page p : mPages) {
				String pageUrl = p.getLink();
				Log.d(TAG, "Loading page: " + pageUrl);
				final String hasedUrl = p.getHashedUrl();
				Log.d(TAG, "MD5 hash of the URL: " + hasedUrl);
				try {
					if (isCancelled()) {
						break;
					}
					File cacheFile = new File(mCacheDir.getAbsolutePath() + "/" + hasedUrl);
					if (cacheFile.isFile()) {
						// Found cached file. Do not need to download any more
						Log.d(TAG, "Found cached file at " + cacheFile.getAbsolutePath());
						addBitmapFileToCache(hasedUrl, cacheFile.getAbsolutePath());
						publishProgress(new Object[] { mCachedMap.size(), hasedUrl });
					} else {
						DownloadFileRunnable dfr = new DownloadFileRunnable(pageUrl, cacheFile.getAbsolutePath());
						dfr.setDownloadFileListener(new DownloadFileListener() {

							@Override
							public void onFailed(String url, String destination, Exception ex) {
								Log.e(TAG, "Download failed for url: " + url);
								ex.printStackTrace();
							}

							@Override
							public void onCompleted(String url, String destination, long fileSize) {
								Log.d(TAG, "Download completed: " + url);
								addBitmapFileToCache(hasedUrl, destination);
								publishProgress(new Object[] { mCachedMap.size(), hasedUrl });
							}
						});
						mExecutor.execute(dfr);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			synchronized (mDiskCacheLock) {
				if (mCachedMap.entrySet().size() >= mPages.size()) {
					mLoadingIndicator.setVisibility(View.GONE);
				}
			}
		}
	}

}
