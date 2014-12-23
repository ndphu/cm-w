package ndphu.app.android.cw.fragment.reading;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.ChapterAdapter;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.customview.VerticalViewPager;
import ndphu.app.android.cw.dao.DaoUtils;
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
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class VerticalReadingFragment extends Fragment implements OnMenuItemClickListener, DrawerListener, OnClickListener {
	private static final String TAG = PagesPagerAdapter.class.getSimpleName();
	static final float MIN_SCALE = 0.7f;

	private ProgressDialog mPd;
	private PagesPagerAdapter mAdapter;
	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;

	private Chapter mChapter;
	private List<Page> mPages = new ArrayList<Page>();
	private Book mBook;
	private VerticalViewPager mVerticalPager;

	private ListView mChapterListView;
	private ChapterAdapter mChapterAdapter;
	private Toast mPageInfoToast;

	public void setBook(Book book) {
		mBook = book;
	}

	private void updateChapter() {
		Long chapterId = mBook.getChapters().get(mBook.getCurrentChapter()).getId();
		LoadChapterTask loadChapterTask = new LoadChapterTask(chapterId, new LoadChapterTaskListener() {

			@Override
			public void onErrorOccurred(Exception cause) {
				mPd.dismiss();
				new AlertDialog.Builder(getActivity()).setTitle("Error").setMessage(cause.getMessage()).setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
			}

			@Override
			public void onCompleted(Chapter result) {
				mChapter = result;
				updatePages();
				mPd.dismiss();
			}

			@Override
			public void onBegin() {
				mPd = new ProgressDialog(getActivity());
				mPd.setMessage("Loading");
				mPd.setCancelable(false);
				mPd.show();
			}
		});
		loadChapterTask.execute();

		if (mBook.getCurrentChapter() > 0) {
			Long nextChapterId = mBook.getChapters().get(mBook.getCurrentChapter() - 1).getId();
			LoadChapterTask loadNextChapter = new LoadChapterTask(nextChapterId, new LoadChapterTaskListener() {

				@Override
				public void onErrorOccurred(Exception cause) {
					Log.i(TAG, "Load next chapter error");
				}

				@Override
				public void onCompleted(Chapter result) {
					Log.i(TAG, "Load next chapter completed");
					for (Page targetPage : result.getPages()) {
						CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(targetPage.getHashedUrl());
						if (cachedImage == null) {
							cachedImage = new CachedImage();
							cachedImage.setFilePath(null);
							cachedImage.setHasedUrl(targetPage.getHashedUrl());
							cachedImage.setUrl(targetPage.getUrl());
							DaoUtils.saveOrUpdate(cachedImage);
						}
						if (cachedImage.getFilePath() == null) {
							DownloadFileRunnable dfr = new DownloadFileRunnable(targetPage.getUrl(), getActivity().getExternalCacheDir().getAbsolutePath()
									+ "/" + targetPage.getHashedUrl());
							dfr.setDownloadFileListener(new DownloadFileListener() {

								@Override
								public void onCompleted(final String url, final String destination, final long fileSize) {
									final String hasedUrl = Utils.getMD5Hash(url);
									CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(hasedUrl);
									cachedImage.setFilePath(destination);
									cachedImage.setFileSize(fileSize);
									DaoUtils.saveOrUpdate(cachedImage);
								}

								@Override
								public void onFailed(String url, String destination, Exception ex) {

								}
							});
							TaskManager.getInstance().downloadFile(dfr);
						}
					}
				}

				@Override
				public void onBegin() {
					Log.i(TAG, "Begin load next chapter...");
				}
			});
			loadNextChapter.execute();
		}

	}

	private void updatePages() {
		synchronized (mPages) {
			mPages.clear();
			for (Page targetPage : mChapter.getPages()) {
				CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(targetPage.getHashedUrl());
				if (cachedImage == null) {
					cachedImage = new CachedImage();
					cachedImage.setFilePath(null);
					cachedImage.setHasedUrl(targetPage.getHashedUrl());
					cachedImage.setUrl(targetPage.getUrl());
					DaoUtils.saveOrUpdate(cachedImage);
				}
				if (cachedImage.getFilePath() == null) {
					DownloadFileRunnable dfr = new DownloadFileRunnable(targetPage.getUrl(), getActivity().getExternalCacheDir().getAbsolutePath() + "/"
							+ targetPage.getHashedUrl());
					dfr.setDownloadFileListener(new DownloadFileListener() {

						@Override
						public void onCompleted(final String url, final String destination, final long fileSize) {
							final String hasedUrl = Utils.getMD5Hash(url);
							CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(hasedUrl);
							cachedImage.setFilePath(destination);
							cachedImage.setFileSize(fileSize);
							DaoUtils.saveOrUpdate(cachedImage);
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									TouchImageView viewToBeUpdated = (TouchImageView) mVerticalPager.findViewWithTag(hasedUrl);
									new ImageLoaderTask().execute(destination, hasedUrl, new WeakReference<TouchImageView>(viewToBeUpdated));
									mAdapter.notifyDataSetChanged();
								}
							});
						}

						@Override
						public void onFailed(String url, String destination, Exception ex) {

						}
					});
					TaskManager.getInstance().downloadFile(dfr);
				}
				mPages.add(targetPage);
				mAdapter = new PagesPagerAdapter();
				mVerticalPager.setAdapter(mAdapter);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("ShowToast")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_reading_vertical, container, false);
		mVerticalPager = (VerticalViewPager) view.findViewById(R.id.fragment_reading_vertical_viewpager_page_list);
		mVerticalPager.setPageTransformer(false, new VerticalTransformer());
		mVerticalPager.setOnPageChangeListener(new PageChangedListener());
		mToolbar = (Toolbar) view.findViewById(R.id.fragment_reading_vertical_toolbar);
		// Hide toolbar
		mToolbar.setVisibility(View.GONE);
		mToolbar.setNavigationIcon(R.drawable.ic_action_close);
		mToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.background_translucent_darker));
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
		mToolbar.setTitle(mBook.getName());
		mToolbar.setOnClickListener(this);
		// Init drawer
		mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerLayout.setDrawerListener(this);
		// Chapter adapter
		mChapterListView = (ListView) view.findViewById(R.id.fragment_reading_listview_chapter);
		mChapterListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mBook.setCurrentChapter(position);
				DaoUtils.saveOrUpdate(mBook);
				updateChapter();
			}
		});
		mChapterAdapter = new ChapterAdapter(getActivity(), 0);
		mChapterListView.setAdapter(mChapterAdapter);
		mPageInfoToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((ActionBarActivity) getActivity()).getSupportActionBar().hide();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		updateChapter();
		mChapterAdapter.clear();
		mChapterAdapter.addAll(mBook.getChapters());
		mChapterListView.setItemChecked(mBook.getCurrentChapter(), true);
		mChapterListView.setSelection(mBook.getCurrentChapter());
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		((ActionBarActivity) getActivity()).getSupportActionBar().show();
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private class PagesPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			synchronized (mPages) {
				if (mBook.getCurrentChapter() > 0) {
					return mPages.size() + 1;
				} else {
					return mPages.size();
				}
			}
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {

			synchronized (mPages) {
				if (position < mPages.size()) {
					Page targetPage = mPages.get(position);
					String hasedUrl = targetPage.getHashedUrl();
					final TouchImageView img = new TouchImageView(container.getContext());
					img.setImageResource(R.drawable.ic_placeholder_loading);
					img.setScaleType(ScaleType.CENTER_INSIDE);
					img.setTag(hasedUrl);
					CachedImage cachedFile = getCachedImage(hasedUrl);
					if (cachedFile != null && cachedFile.getFilePath() != null) {
						new ImageLoaderTask().execute(cachedFile.getFilePath(), hasedUrl, new WeakReference<TouchImageView>(img));
					}
					container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					return img;
				} else {
					final TouchImageView img = new TouchImageView(container.getContext());
					img.setImageResource(R.drawable.ic_placeholder_loading);
					img.setScaleType(ScaleType.CENTER_INSIDE);
					container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					return img;
				}
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

	private class PageChangedListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			synchronized (mPages) {
				if (position == mPages.size()) {
					// Lastpage, go to next chapter
					int nextPageIdx = mBook.getCurrentChapter() - 1;
					mBook.setCurrentChapter(nextPageIdx);
					DaoUtils.saveOrUpdate(mBook);
					mChapterListView.setItemChecked(nextPageIdx, true);
					mChapterListView.setSelection(nextPageIdx);
					updateChapter();
				} else {
					synchronized (mPages) {
						mPageInfoToast.setText((position + 1) + "/" + mPages.size());
						mPageInfoToast.show();
					}
				}
			}
		}

	}

	public CachedImage getCachedImage(String key) {
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
			break;
		default:
			break;
		}
	}

}
