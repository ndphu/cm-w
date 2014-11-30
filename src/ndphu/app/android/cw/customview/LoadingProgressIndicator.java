package ndphu.app.android.cw.customview;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadingProgressIndicator extends RelativeLayout implements OnClickListener {
	private static final String TAG = LoadingProgressIndicator.class.getSimpleName();
	private ProgressBar mProgressBar;
	private TextView mProgressText;
	private ImageView mCancelBtn;

	private WeakReference<LoadingProgressIndicatorListener> mListener;

	public LoadingProgressIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setLoadingProgressIndicatorListener(LoadingProgressIndicatorListener listener) {
		mListener = new WeakReference<LoadingProgressIndicatorListener>(listener);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!isInEditMode()) {
			mProgressBar = (ProgressBar) findViewById(R.id.cv_loading_progress_indicator_progress_bar);
			mProgressText = (TextView) findViewById(R.id.cv_loading_progress_indicator_textview_progress);
			mCancelBtn = (ImageView) findViewById(R.id.cv_loading_progress_indicator_btn_cancel);
			mCancelBtn.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cv_loading_progress_indicator_btn_cancel) {
			requestClose();
		}
	}

	private void requestClose() {
		new AlertDialog.Builder(getContext()).setTitle("Confirm").setMessage("Do you want to cancel this progress?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mListener != null && mListener.get() != null) {
							mListener.get().onCancelProgress();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
	}

	public static interface LoadingProgressIndicatorListener {
		/**
		 * @return true to accept the call
		 */
		public void onCancelProgress();

	}

	public void setProgressBarMax(int max) {
		mProgressBar.setMax(max);
		updateProgressText();
	}

	public void setProgressBarProgress(int progress) {
		mProgressBar.setProgress(progress);
		updateProgressText();
	}

	private void updateProgressText() {
		mProgressText.setText(mProgressBar.getProgress() + "/" + mProgressBar.getMax());

	}

}
