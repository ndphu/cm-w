package ndphu.app.android.cw.customview;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ChapterNavigationBar extends RelativeLayout implements OnClickListener {

	private ImageView mNextButton;
	private ImageView mPrevButton;
	private WeakReference<ChapterNavigationBarListener> mListener;

	public static interface ChapterNavigationBarListener {
		void onNextClick();

		void onPrevClick();
	}

	public void setChapterNavigationBarListener(ChapterNavigationBarListener listener) {
		mListener = new WeakReference<ChapterNavigationBarListener>(listener);
	}

	public ChapterNavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!isInEditMode()) {
			mPrevButton = (ImageView) findViewById(R.id.cv_page_navigation_prev);
			mPrevButton.setOnClickListener(this);
			mNextButton = (ImageView) findViewById(R.id.cv_page_navigation_next);
			mNextButton.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		if (mListener != null && mListener.get() != null) {
			switch (v.getId()) {
			case R.id.cv_page_navigation_next:
				mListener.get().onNextClick();
				break;
			case R.id.cv_page_navigation_prev:
				mListener.get().onPrevClick();
				break;

			default:
				break;
			}
		}
	}

}
