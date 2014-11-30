package ndphu.app.android.cw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ndphu.app.android.cw.customview.ExtendedViewPager;
import ndphu.app.android.cw.customview.LoadingProgressIndicator;
import ndphu.app.android.cw.customview.LoadingProgressIndicator.LoadingProgressIndicatorListener;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.util.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

public class ReadingActivity extends Activity implements LoadingProgressIndicatorListener {
	protected static final String TAG = ReadingActivity.class.getSimpleName();

	public static String EXTRA_CHAPTER_URL = "chapter_url";

	private ExtendedViewPager mViewPager;
	private String mChapterUrl;
	private List<Page> mListPage;
	private LoadingProgressIndicator mLoadingProgressIndicator;
	private final Object mDiskCacheLock = new Object();
	private boolean mCacheInitialized = false;
	private File mCacheDir;
	private Map<String, String> mCachedMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_reading);
		mChapterUrl = getIntent().getStringExtra(EXTRA_CHAPTER_URL);
		mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
		mLoadingProgressIndicator = (LoadingProgressIndicator) findViewById(R.id.activity_reading_progress_bar);
		mLoadingProgressIndicator.setLoadingProgressIndicatorListener(this);
		mLoadChapterDataTask.execute();
		mCacheDir = new File(getExternalCacheDir().getAbsolutePath() + "/" + Utils.getMD5Hash(mChapterUrl));
		new InitCacheTask().execute(mCacheDir);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Ingored
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
				Log.i(TAG, "Cache dir path: " + mCacheDir.getAbsolutePath());
			}
			return null;
		}
	}

	private AsyncTask<Void, Void, Object> mLoadChapterDataTask = new AsyncTask<Void, Void, Object>() {

		@Override
		protected Object doInBackground(Void... params) {
			Manga24hProcessor processor = new Manga24hProcessor();
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
			if (result != null) {
				if (result instanceof Exception) {
					new AlertDialog.Builder(ReadingActivity.this).setTitle("Error")
							.setMessage(((Exception) result).getMessage()).create().show();
				} else if (result instanceof List) {
					mListPage = (List<Page>) result;
					mViewPager.setAdapter(mPageAdapter);
					mViewPager.setOnPageChangeListener(mPageChangeListener);
					mPagesLoader.execute();
				}
			}
		};

	};

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			Page page = mListPage.get(position);
			loadImageToView(page.getHashedUrl());
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	private PagerAdapter mPageAdapter = new PagerAdapter() {

		@Override
		public int getCount() {
			return mListPage.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			TouchImageView img = new TouchImageView(container.getContext());
			String hasedUrl = mListPage.get(position).getHashedUrl();
			Log.i(TAG, "Load page with Hased URL: " + hasedUrl);
			String cachedFile = getBitmapFileFromCache(hasedUrl);
			if (cachedFile == null) {
				Log.w(TAG, "Cached is not ready for url " + hasedUrl);
				Picasso.with(ReadingActivity.this).load(R.drawable.ic_launcher).into(img);
			} else {
				Log.i(TAG, "Cache hit: " + cachedFile);
				Picasso.with(ReadingActivity.this).load(new File(cachedFile)).into(img);
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

	private void loadImageToView(String viewTag) {
		if (getBitmapFileFromCache(viewTag) == null) {
			return;
		}
		TouchImageView viewToBeUpdated = (TouchImageView) mViewPager.findViewWithTag(viewTag);
		if (viewToBeUpdated != null) {
			Picasso.with(ReadingActivity.this).load(new File(getBitmapFileFromCache(viewTag))).into(viewToBeUpdated);
		}
	};

	private AsyncTask<Void, Object, Void> mPagesLoader = new AsyncTask<Void, Object, Void>() {

		protected void onPreExecute() {
			mLoadingProgressIndicator.setVisibility(View.VISIBLE);
			mLoadingProgressIndicator.setProgressBarProgress(0);
			mLoadingProgressIndicator.setProgressBarMax(mListPage.size());
		};

		protected void onProgressUpdate(Object... values) {
			mLoadingProgressIndicator.setProgressBarProgress((Integer) values[0]);
			mPageAdapter.notifyDataSetChanged();
			String viewTag = (String) values[1];
			loadImageToView(viewTag);
		}

		@Override
		protected Void doInBackground(Void... params) {
			int count = 1;
			for (Page p : mListPage) {
				String pageUrl = p.getLink();
				Log.i(TAG, "Loading page: " + pageUrl);
				String hasedUrl = p.getHashedUrl();
				Log.i(TAG, "MD5 hash of the URL: " + hasedUrl);
				try {
					if (isCancelled()) {
						break;
					}
					File cacheFile = new File(mCacheDir.getAbsolutePath() + "/" + hasedUrl);
					if (cacheFile.isFile()) {
						// Found cached file. Do not need to download any more
						Log.i(TAG, "Found cached file at " + cacheFile.getAbsolutePath());
					} else {
						DefaultHttpClient client = new DefaultHttpClient();
						HttpGet get = new HttpGet(pageUrl);
						HttpResponse response = client.execute(get);
						int downloadedSize = IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(cacheFile));
						Log.i(TAG, "Downloaded size: " + downloadedSize);
					}
					
					String cacheFilePath = cacheFile.getAbsolutePath();
					addBitmapFileToCache(hasedUrl, cacheFilePath);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				count++;
				publishProgress(new Object[] { Integer.valueOf(count), hasedUrl });
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			mLoadingProgressIndicator.setVisibility(View.GONE);
		};
	};

	@Override
	public void onCancelProgress() {
		mPagesLoader.cancel(true);
		mLoadingProgressIndicator.setVisibility(View.GONE);
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

}
