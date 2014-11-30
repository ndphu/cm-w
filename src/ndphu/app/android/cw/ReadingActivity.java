package ndphu.app.android.cw;

import java.io.IOException;
import java.util.List;

import ndphu.app.android.cw.customview.ExtendedViewPager;
import ndphu.app.android.cw.customview.TouchImageView;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.Page;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ReadingActivity extends Activity {
	protected static final String TAG = ReadingActivity.class.getSimpleName();

	public static String EXTRA_CHAPTER_URL = "chapter_url";

	private ExtendedViewPager mViewPager;
	private String mChapterUrl;
	private List<Page> mListPage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_reading);
		mChapterUrl = getIntent().getStringExtra(EXTRA_CHAPTER_URL);
		mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
		mLoadChapterDataTask.execute();
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
					new AlertDialog.Builder(ReadingActivity.this).setTitle("Error").setMessage(((Exception) result).getMessage()).create().show();
				} else if (result instanceof List) {
					mListPage = (List<Page>) result;
					mViewPager.setAdapter(mPageAdapter);
				}
			}
		};

	};

	private PagerAdapter mPageAdapter = new PagerAdapter() {

		@Override
		public int getCount() {
			return mListPage.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			TouchImageView img = new TouchImageView(container.getContext());
			String pageUrl = mListPage.get(position).getLink();
			Log.i(TAG, "Load page with URL: " + pageUrl);
			container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
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

	private AsyncTask<Void, Integer, Void> mPagesLoader = new AsyncTask<Void, Integer, Void>(){

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}};

}
