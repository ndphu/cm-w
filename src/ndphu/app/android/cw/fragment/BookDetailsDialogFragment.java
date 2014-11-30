package ndphu.app.android.cw.fragment;

import java.util.ArrayList;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.ReadingActivity;
import ndphu.app.android.cw.adapter.ChapterAdapter;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class BookDetailsDialogFragment extends DialogFragment implements LoadBookListener, OnMenuItemClickListener,
		OnItemClickListener {
	private static final String TAG = BookDetailsDialogFragment.class.getSimpleName();
	private ListView mChapterList;
	private ChapterAdapter mAdapter;
	private TextView mBookSummary;
	private ImageView mBookCover;
	private String mBookUrl;
	private AsyncTask<Void, Void, Object> mLoadBookDetailsClass;
	private Toolbar mToolbar;
	private ViewGroup mParentContainer = null;
	private Book mBook;

	public void setBookUrl(String bookUrl) {
		mBookUrl = bookUrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mParentContainer = container;
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_book_details, mParentContainer, false);
		mChapterList = (ListView) view.findViewById(R.id.fragment_book_details_listview_chapters);
		mAdapter = new ChapterAdapter(getActivity(), 0);
		mChapterList.setAdapter(mAdapter);
		mChapterList.setOnItemClickListener(this);
		mBookSummary = (TextView) view.findViewById(R.id.fragment_book_details_textview_summary);
		mBookSummary.setMovementMethod(new ScrollingMovementMethod());
		mBookCover = (ImageView) view.findViewById(R.id.fragment_book_details_cover_image);
		mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.inflateMenu(R.menu.book_details_menu);
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(view).create();
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				mLoadBookDetailsClass = new LoadBookTask(mBookUrl, BookDetailsDialogFragment.this).execute();
			}
		});
		// .setPositiveButton("Close", new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// }
		// })
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;

	}

	@Override
	public void onStart(String url) {
		if (getActivity() != null) {

		}
	}

	@Override
	public void onComplete(Book book) {
		mBook = book;
		mToolbar.setTitle(book.getName());
		if (getDialog() != null) {
			getDialog().setTitle(book.getName());
		}
		Picasso.with(getActivity()).load(Uri.parse(book.getCover())).into(mBookCover);
		mBookSummary.setText(Html.fromHtml(book.getBookDesc()));
		mAdapter.clear();
		mAdapter.addAll(book.getChapters());
	}

	@Override
	public void onError(Exception ex) {
		if (getActivity() != null) {
			new AlertDialog.Builder(getActivity()).setTitle("Error")
					.setMessage("Cannot load book details. Error: " + ex.getMessage())
					.setPositiveButton("Close", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (getDialog() != null) {
								getDialog().dismiss();
							}
						}
					}).create().show();
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_close:
			getDialog().dismiss();
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mLoadBookDetailsClass != null) {
			mLoadBookDetailsClass.cancel(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Chapter chapter = mAdapter.getItem(position);
		String chapterUrl = chapter.getChapterUrl();
		Log.i(TAG, "Select chapter: " + chapterUrl);
		Intent intent = new Intent(getActivity(), ReadingActivity.class);
		ArrayList<CharSequence> chapterUrlList = new ArrayList<CharSequence>();
		for (Chapter __chapter : mBook.getChapters()) {
			chapterUrlList.add(__chapter.getChapterUrl());
		}
		intent.putCharSequenceArrayListExtra(ReadingActivity.EXTRA_CHAPTER_ARRAY, chapterUrlList);
		intent.putExtra(ReadingActivity.EXTRA_CHAPTER_URL, chapterUrl);
		startActivity(intent);
	}
}
