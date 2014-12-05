package ndphu.app.android.cw;

import java.io.File;
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
import ndphu.app.android.cw.io.processor.BlogTruyenProcessor;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.model.Source;
import ndphu.app.android.cw.runable.DownloadFileRunnable;
import ndphu.app.android.cw.runable.DownloadFileRunnable.DownloadFileListener;
import ndphu.app.android.cw.util.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ReadingActivity extends Activity implements LoadingProgressIndicatorListener, RejectedExecutionHandler, ChapterNavigationBarListener {
	protected static final String TAG = ReadingActivity.class.getSimpleName();
	public static String EXTRA_CHAPTER_ARRAY = "chapter_array";
	public static String EXTRA_CHAPTER_URL = "chapter_url";
	public static String EXTRA_SOURCE = "chapter_source";

	// GUI elements
	private ExtendedViewPager mViewPager;
	private LoadingProgressIndicator mLoadingIndicator;
	private ChapterNavigationBar mChapterNavigationBar;

	// Activity data
	private String mChapterUrl;
	private List<Page> mPages;
	private List<CharSequence> mChapterUrlList;
	private int mCurrentChapterIndex = -1;
	private Source mChapterSource;

	// Caching params
	private final Object mDiskCacheLock = new Object();
	private boolean mCacheInitialized = false;
	private File mCacheDir;
	private Map<String, String> mCachedMap;

	// Thread executor params
	private int mCorePoolSize = 8;
	private int mMaximumPoolSize = 24;
	private long mKeepAlive = 10000;
	private ThreadPoolExecutor mExecutor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_reading);

		// Init thread pool
		mExecutor = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAlive, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), this);

		// Read from intent
		mChapterUrl = getIntent().getStringExtra(EXTRA_CHAPTER_URL);
		mChapterUrlList = getIntent().getCharSequenceArrayListExtra(EXTRA_CHAPTER_ARRAY);
		mChapterSource = Source.valueOf(getIntent().getStringExtra(EXTRA_SOURCE));
		mCurrentChapterIndex = mChapterUrlList.indexOf(mChapterUrl);

		// Init layout
		// Init view pager
		mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
		mLoadingIndicator = (LoadingProgressIndicator) findViewById(R.id.activity_reading_progress_bar);
		mLoadingIndicator.setLoadingProgressIndicatorListener(this);
		mLoadingIndicator.setAlpha(0.8f);

		// Init chapter navigation
		mChapterNavigationBar = (ChapterNavigationBar) findViewById(R.id.activity_reading_chapter_navigation_bar);
		mChapterNavigationBar.setVisibility(View.GONE);
		mChapterNavigationBar.setChapterNavigationBarListener(this);

		refresh();
	}

	private void refresh() {
		// Init cache dir
		mCacheDir = new File(getExternalCacheDir().getAbsolutePath() + "/" + Utils.getMD5Hash(mChapterUrl));
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
			switch (mChapterSource) {
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
				return processor.getPageList(mChapterUrl);
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
		};

	};

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			// if (position == mPages.size() - 1) {
			// mChapterNavigationBar.setVisibility(View.VISIBLE);
			// } else {
			// mChapterNavigationBar.setVisibility(View.GONE);
			// }
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
			// updateViewAtPosition(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if ((position == mPages.size() - 2 && positionOffset > 0)) {
				mChapterNavigationBar.setVisibility(View.VISIBLE);
				mChapterNavigationBar.setAlpha(positionOffset);
			} else if (position == mPages.size() - 1) {
				mChapterNavigationBar.setVisibility(View.VISIBLE);
				mChapterNavigationBar.setAlpha(1);
			} else if (position == 0) {
				mChapterNavigationBar.setVisibility(View.VISIBLE);
				mChapterNavigationBar.setAlpha(1 - positionOffset);
			} else {
				mChapterNavigationBar.setVisibility(View.GONE);
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	private void updateViewAtPosition(int position) {
		Page page = mPages.get(position);
		loadImageToView(page.getHashedUrl());
	}

	public boolean isLandScape() {
		Configuration configuration = getResources().getConfiguration();
		return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	private PagerAdapter mPageAdapter = new PagerAdapter() {

		@Override
		public int getCount() {
			return mPages.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final TouchImageView img = new TouchImageView(container.getContext());
			img.setImageResource(R.drawable.ic_launcher);
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

	private void updateScaleTypeFromOrientation(final TouchImageView img) {
		if (isLandScape()) {
			img.setScaleType(ScaleType.CENTER_CROP);
			img.setScrollPosition(0f, 0f);
		} else {
			img.setScaleType(ScaleType.FIT_CENTER);
		}
	}

	private class ImageLoader extends AsyncTask<Object, Void, Object[]> {

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "Loading image");
		};

		@SuppressWarnings("unchecked")
		@Override
		protected Object[] doInBackground(Object... params) {
			String filePath = (String) params[0];

			WeakReference<TouchImageView> imageView = (WeakReference<TouchImageView>) params[2];
			Bitmap result = BitmapFactory.decodeFile(filePath);
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
		};

	}

	private void loadImageToView(String viewTag) {
		if (getBitmapFileFromCache(viewTag) == null) {
			return;
		}
		TouchImageView viewToBeUpdated = (TouchImageView) mViewPager.findViewWithTag(viewTag);
		if (viewToBeUpdated != null) {
			new ImageLoader().execute(getBitmapFileFromCache(viewTag), viewTag, new WeakReference<TouchImageView>(viewToBeUpdated));
		}
	};

	private class PageLoaderTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected void onPreExecute() {
			mLoadingIndicator.setVisibility(View.VISIBLE);
			mLoadingIndicator.setProgressBarProgress(0);
			mLoadingIndicator.setProgressBarMax(mPages.size());
		};

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
								Log.e(TAG, "Download completed: " + url);
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
		};
	};

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

	@Override
	protected void onDestroy() {
		mExecutor.shutdownNow();
		super.onDestroy();
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// Sure the runnable is DownloadFileRunnable
		DownloadFileRunnable dfr = (DownloadFileRunnable) r;
		Log.e(TAG, "Reject execution: " + dfr.getUrl());

	}

	@Override
	public void onNextClick() {
		mCurrentChapterIndex = mCurrentChapterIndex - 1;
		if (mCurrentChapterIndex >= 0) {
			mChapterUrl = (String) mChapterUrlList.get(mCurrentChapterIndex);
			refresh();
		} else {
			mCurrentChapterIndex = mCurrentChapterIndex + 1;
		}
	}

	@Override
	public void onPrevClick() {
		mCurrentChapterIndex = mCurrentChapterIndex + 1;
		if (mCurrentChapterIndex < mChapterUrlList.size()) {
			mChapterUrl = (String) mChapterUrlList.get(mCurrentChapterIndex);
			refresh();
		} else {
			mCurrentChapterIndex = mCurrentChapterIndex - 1;
		}
	}

}
