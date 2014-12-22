package ndphu.app.android.cw.fragment.reading;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.customview.VerticalViewPager;
import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.fragment.favorite.BookViewHolder;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.CachedImage;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.runable.DownloadFileRunnable;
import ndphu.app.android.cw.runable.DownloadFileRunnable.DownloadFileListener;
import ndphu.app.android.cw.task.ImageLoaderTask;
import ndphu.app.android.cw.task.LoadChapterTask;
import ndphu.app.android.cw.task.LoadChapterTask.LoadChapterTaskListener;
import ndphu.app.android.cw.taskmanager.TaskManager;
import ndphu.app.android.cw.util.Utils;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class VerticalReadingFragment extends Fragment implements OnMenuItemClickListener, DrawerListener,
		OnClickListener {
	private static final String EXTRA_CHAPTER_ID = "chapter_id";
	private static final String TAG = PagesPagerAdapter.class.getSimpleName();
	private static final float MIN_SCALE = 0.7f;

	private PagesPagerAdapter mAdapter;
	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;

	private Chapter mChapter;
	private List<Page> mPages = new ArrayList<Page>();
	private Book mBook;

	public void setChapter(Chapter chapter) {
		mChapter = chapter;
		mBook = DaoUtils.getBookAndChapters(mChapter.getBookId());
		for (int i = 0; i < mBook.getChapters().size(); ++i) {
			if (mBook.getChapters().get(i).getId().equals(chapter.getId())) {
				mBook.setCurrentChapter(i);
				DaoUtils.saveOrUpdate(mBook);
			}
		}
		synchronized (mPages) {
			mPages.clear();
			mPages.addAll(mChapter.getPages());
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
		if (mToolbar != null) {
			mToolbar.setSubtitle(mChapter.getName());
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
		VerticalViewPager verticalPager = (VerticalViewPager) view
				.findViewById(R.id.fragment_reading_vertical_viewpager_page_list);
		verticalPager.setAdapter(mAdapter);
		verticalPager.setPageTransformer(false, new VerticalTransformer());
		verticalPager.setOnPageChangeListener(new PageChangedListener());
		mToolbar = (Toolbar) view.findViewById(R.id.fragment_reading_vertical_toolbar);
		mToolbar.setNavigationIcon(R.drawable.ic_action_close);
		mToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.background_translucent_darker));
		//mToolbar.setAlpha(0.7f);
		// mToolbar.inflateMenu(R.menu.reading_menu);
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
		mToolbar.setTitle(mBook.getName());
		mToolbar.setSubtitle(mChapter.getName());
		mToolbar.setOnClickListener(this);
		// Init drawer
		mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerLayout.setDrawerListener(this);

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
						// Download task is not scheduled yet, we will schedule
						// it.
						CachedImage cachedImage = new CachedImage();
						cachedImage.setFilePath(null);
						cachedImage.setHasedUrl(hasedUrl);
						cachedImage.setUrl(targetPage.getUrl());
						DaoUtils.saveOrUpdate(cachedImage);
						DownloadFileRunnable dfr = new DownloadFileRunnable(targetPage.getUrl(), getActivity()
								.getExternalCacheDir().getAbsolutePath() + "/" + hasedUrl);
						dfr.setDownloadFileListener(this);
						TaskManager.getInstance().downloadFile(dfr);
					} else if (cachedFile.getFilePath() == null) {
						// Image is not downloaded. Skip this time.
					} else {
						// Got image. Start loading into view.
						new ImageLoaderTask().execute(cachedFile.getFilePath(), hasedUrl,
								new WeakReference<TouchImageView>(img));
					}
					container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT);
					img.setTag(hasedUrl);
					return img;
				} else {
					return null;
				}/*
				 * else { } if (mChapterNavigation == null) { mChapterNavigation
				 * = (ChapterNavigationBar)
				 * getLayoutInflater().inflate(R.layout.cv_chapter_navigation,
				 * container, false);
				 * mChapterNavigation.setChapterNavigationBarListener
				 * (ReadingActivity.this); }
				 * mChapterNavigation.showPrev(mHasPrev);
				 * mChapterNavigation.showNext(mHasNext);
				 * container.addView(mChapterNavigation,
				 * LinearLayout.LayoutParams.MATCH_PARENT,
				 * LinearLayout.LayoutParams.MATCH_PARENT); return
				 * mChapterNavigation; }
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
				view.setAlpha(1 + position);
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
		switch (item.getItemId()) {
		case R.id.action_show_chapters:
			toggleDrawer();
			break;

		default:
			break;
		}
		return false;
	}

	private void toggleDrawer() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			mDrawerLayout.closeDrawer(GravityCompat.END);
		} else {
			mDrawerLayout.openDrawer(GravityCompat.END);
		}
	}

	@Override
	public void onDrawerClosed(View drawer) {

	}

	@Override
	public void onDrawerOpened(View drawer) {

	}

	@Override
	public void onDrawerSlide(View drawer, float position) {

	}

	@Override
	public void onDrawerStateChanged(int state) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fragment_reading_vertical_toolbar:
			showPopupMenuForChapters(v);
			break;

		default:
			break;
		}
	}

	private void showPopupMenuForChapters(View source) {
		PopupMenu menu = new PopupMenu(source.getContext(), source);
		for (int idx = 0; idx < mBook.getChapters().size(); ++idx) {
			Chapter thisChap = mBook.getChapters().get(idx);
			MenuItem menuItem = menu.getMenu().add(thisChap.getName());
			Intent intent = new Intent();
			intent.putExtra(EXTRA_CHAPTER_ID, thisChap.getId());
			menuItem.setIntent(intent);
			if (thisChap.getId().longValue() == mChapter.getId().longValue()) {
				Log.i(TAG, "Selected chapter id = " + thisChap.getId());
				menuItem.setCheckable(true);
				menuItem.setChecked(true);
			} else {
				menuItem.setChecked(false);
			}
		}
		menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				Intent intent = menuItem.getIntent();
				Long chapterId = intent.getLongExtra(EXTRA_CHAPTER_ID, -1);
				if (chapterId < 0) {
					// Error
				} else {
					Chapter selectedChapter = DaoUtils.getChapterById(chapterId);
					if (selectedChapter == null) {
						// Error
					} else {
						LoadChapterTask task = new LoadChapterTask(chapterId, new LoadChapterTaskListener() {
							ProgressDialog mProgressDialog;

							@Override
							public void onErrorOccurred(Exception cause) {
								mProgressDialog.dismiss();
							}

							@Override
							public void onCompleted(Chapter result) {
								VerticalReadingFragment.this.setChapter(result);
								mProgressDialog.dismiss();
							}

							@Override
							public void onBegin() {
								mProgressDialog = new ProgressDialog(getActivity());
								mProgressDialog.setMessage("Loading");
								mProgressDialog.setCancelable(false);
								mProgressDialog.show();
							}
						});
						task.execute();
					}
				}
				return true;
			}
		});
		menu.show();
	}

}
