package ndphu.app.android.cw.fragment.reading;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.customview.VerticalViewPager;
import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.model.CachedImage;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.runable.DownloadFileRunnable;
import ndphu.app.android.cw.runable.DownloadFileRunnable.DownloadFileListener;
import ndphu.app.android.cw.task.ImageLoaderTask;
import ndphu.app.android.cw.taskmanager.TaskManager;
import ndphu.app.android.cw.util.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class VerticalReadingFragment extends Fragment implements OnMenuItemClickListener {
	private static final String TAG = PagesPagerAdapter.class.getSimpleName();
	private List<Page> mPages = new ArrayList<Page>();
	private PagesPagerAdapter mAdapter;
	private static final float MIN_SCALE = 0.95f;
	private Toolbar mToolbar;
	private Chapter mChapter;

	public void setChapter(Chapter chapter) {
		mChapter = chapter;
		synchronized (mPages) {
			mPages.clear();
			mPages.addAll(mChapter.getPages());
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
		if (mToolbar != null) {
			mToolbar.setTitle(mChapter.getName());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new PagesPagerAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_reading_vertical, container, false);
		VerticalViewPager verticalPager = (VerticalViewPager) view.findViewById(R.id.fragment_reading_vertical_viewpager_page_list);
		verticalPager.setAdapter(mAdapter);
		verticalPager.setPageTransformer(false, new VerticalTransformer());
		verticalPager.setOnPageChangeListener(new PageChangedListener());
		mToolbar = (Toolbar) view.findViewById(R.id.fragment_reading_vertical_toolbar);
		mToolbar.setNavigationIcon(R.drawable.ic_action_back);
		mToolbar.inflateMenu(R.menu.reading_menu);
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
		mToolbar.setTitle(mChapter.getName());
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((ActionBarActivity) getActivity()).getSupportActionBar().hide();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		((ActionBarActivity) getActivity()).getSupportActionBar().show();
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private class PagesPagerAdapter extends PagerAdapter implements DownloadFileListener {

		@Override
		public int getCount() {
			synchronized (mPages) {
				return mPages.size();
			}
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			synchronized (mPages) {
				if (position < mPages.size()) {
					final TouchImageView img = new TouchImageView(container.getContext());
					img.setImageResource(R.drawable.ic_placeholder_loading);
					img.setScaleType(ScaleType.CENTER_INSIDE);
					Page targetPage = mPages.get(position);
					String hasedUrl = targetPage.getHashedUrl();
					CachedImage cachedFile = getBitmapFileFromCache(hasedUrl);
					if (cachedFile == null) {
						// Download task is not scheduled yet, we will schedule it.
						CachedImage cachedImage = new CachedImage();
						cachedImage.setFilePath(null);
						cachedImage.setHasedUrl(hasedUrl);
						cachedImage.setUrl(targetPage.getUrl());
						DaoUtils.saveOrUpdate(cachedImage);
						DownloadFileRunnable dfr = new DownloadFileRunnable(targetPage.getUrl(), getActivity().getExternalCacheDir().getAbsolutePath() + "/"
								+ hasedUrl);
						dfr.setDownloadFileListener(this);
						TaskManager.getInstance().downloadFile(dfr);
					} else if (cachedFile.getFilePath() == null) {
						// Image is not downloaded. Skip this time.
					} else {
						// Got image. Start loading into view.
						new ImageLoaderTask().execute(cachedFile.getFilePath(), hasedUrl, new WeakReference<TouchImageView>(img));
					}
					container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					img.setTag(hasedUrl);
					return img;
				} else {
					return null;
				}/*
				 * else {
				 * }
				 * if (mChapterNavigation == null) {
				 * mChapterNavigation = (ChapterNavigationBar) getLayoutInflater().inflate(R.layout.cv_chapter_navigation, container, false);
				 * mChapterNavigation.setChapterNavigationBarListener(ReadingActivity.this);
				 * }
				 * mChapterNavigation.showPrev(mHasPrev);
				 * mChapterNavigation.showNext(mHasNext);
				 * container.addView(mChapterNavigation, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				 * return mChapterNavigation;
				 * }
				 */
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

		@Override
		public void onCompleted(final String url, final String destination, final long fileSize) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String hasedUrl = Utils.getMD5Hash(url);
					CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(hasedUrl);
					cachedImage.setFilePath(destination);
					cachedImage.setFileSize(fileSize);
					DaoUtils.saveOrUpdate(cachedImage);
					notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onFailed(String url, String destination, Exception ex) {

		}
	};

	private class VerticalTransformer implements PageTransformer {

		@Override
		public void transformPage(View view, float position) {
			int pageHeight = view.getHeight();

			if (position < -1) {
				view.setAlpha(0);
			} else if (position <= 0) {
				view.setAlpha(1 + 2 * position);
				view.setTranslationY(pageHeight * -position);
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);
			} else if (position <= 1) { // (0,1]
				view.setAlpha(1);
				view.setTranslationX(0);
				view.setScaleX(1);
				view.setScaleY(1);
			} else {
				view.setAlpha(0);
			}
		}
	}

	private class PageChangedListener implements OnPageChangeListener {

		private int mPosition = -1;

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			if (position < mPosition) {
				// scroll up
				if (mToolbar.getVisibility() == View.GONE) {
					mToolbar.setVisibility(View.VISIBLE);
					mToolbar.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top));
				}
			} else {
				// scroll up
				if (mToolbar.getVisibility() == View.VISIBLE) {
					mToolbar.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top));
					mToolbar.setVisibility(View.GONE);
				}
			}
			mPosition = position;
		}

	}

	public CachedImage getBitmapFileFromCache(String key) {
		return DaoUtils.getCachedImageByHasedUrl(key);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}

}
