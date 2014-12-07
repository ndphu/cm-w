package ndphu.app.android.cw.fragment;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.ReadingActivity;
import ndphu.app.android.cw.adapter.ChapterAdapter;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class BookDetailsFragment extends Fragment implements LoadBookListener, OnItemClickListener, android.view.View.OnClickListener {
	private static final String TAG = BookDetailsFragment.class.getSimpleName();
	private ListView mChapterList;
	private ChapterAdapter mAdapter;
	private TextView mBookSummary;
	private Button mShowChapters;
	private ImageView mBookCover;
	private AsyncTask<Void, Void, Object> mLoadBookDetailsClass;
	private ViewGroup mParentContainer = null;
	private Book mBook;
	private SearchResult mTarget;
	private ActionBar mActionBar;
	private FragmentManager mFragmentManager;
	private MenuItem mMenuItemClose;
	private ProgressDialog mProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setTitle("Loading");
		mProgressDialog.setMessage("Gathering information...");
		mProgressDialog.setCancelable(false);
	}

	public void setTarget(SearchResult target) {
		mTarget = target;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mParentContainer = container;
		setHasOptionsMenu(true);
		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mFragmentManager = ((ActionBarActivity) getActivity()).getSupportFragmentManager();
		return getContainerView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mActionBar.setDisplayShowCustomEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(true);
		mLoadBookDetailsClass = new LoadBookTask(mTarget, this).execute();
	}

	private View getContainerView() {
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_book_details, mParentContainer, false);
		mChapterList = (ListView) view.findViewById(R.id.fragment_book_details_listview_chapters);
		mAdapter = new ChapterAdapter(getActivity(), 0);
		mChapterList.setAdapter(mAdapter);
		mChapterList.setOnItemClickListener(this);
		mBookSummary = (TextView) view.findViewById(R.id.fragment_book_details_textview_summary);
		mBookSummary.setMovementMethod(new ScrollingMovementMethod());
		mBookCover = (ImageView) view.findViewById(R.id.fragment_book_details_cover_image);
		mShowChapters = (Button) view.findViewById(R.id.fragment_book_details_button_show_chapters);
		mShowChapters.setOnClickListener(this);
		return view;
	}

	// Loading callback
	@Override
	public void onStart(String url) {
		mActionBar.setTitle("Loading");
		mProgressDialog.show();
	}

	@Override
	public void onComplete(Book book) {
		mProgressDialog.dismiss();
		mBook = book;
		if (mBook != null) {
			mActionBar.setTitle(mBook.getName());
		}
		Picasso.with(getActivity()).load(Uri.parse(book.getCover())).into(mBookCover);
		if (book.getBookDesc() != null && !book.getBookDesc().trim().isEmpty()) {
			mBookSummary.setVisibility(View.VISIBLE);
			mBookSummary.setText(Html.fromHtml(book.getBookDesc()));
		} else {
			mBookSummary.setVisibility(View.GONE);
		}
		mAdapter.clear();
		mAdapter.addAll(book.getChapters());
	}

	@Override
	public void onError(Exception ex) {
		mProgressDialog.dismiss();
		mActionBar.setTitle("Error");
		if (getActivity() != null) {
			new AlertDialog.Builder(getActivity()).setTitle("Error").setMessage("Cannot load book details. Error: " + ex.getMessage())
					.setPositiveButton("Close", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create().show();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		mMenuItemClose = menu.findItem(R.id.action_close);
		mMenuItemClose.setVisible(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//Chapter chapter = mAdapter.getItem(position);
		//Source chapterSource = chapter.getChapterSource();
		//String chapterUrl = chapter.getChapterUrl();
		//Log.i(TAG, "Select chapter: " + chapterUrl);
		Intent intent = new Intent(getActivity(), ReadingActivity.class);
		//ArrayList<CharSequence> chapterUrlList = new ArrayList<CharSequence>();
		//for (Chapter __chapter : mBook.getChapters()) {
		//	chapterUrlList.add(__chapter.getChapterUrl());
		//}
		//
		//intent.putCharSequenceArrayListExtra(ReadingActivity.EXTRA_CHAPTER_ARRAY, chapterUrlList);
		//intent.putExtra(ReadingActivity.EXTRA_CHAPTER_URL, chapterUrl);
		//intent.putExtra(ReadingActivity.EXTRA_SOURCE, chapterSource.name());
		//
		Gson gson = new Gson();
		String json = gson.toJson(mBook);
		Log.i(TAG, json);
		intent.putExtra(ReadingActivity.EXTRA_BOOK_JSON, json);
		intent.putExtra(ReadingActivity.EXTRA_CHAPTER_INDEX, position);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_close:
			mFragmentManager.popBackStack();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		mActionBar.setDisplayHomeAsUpEnabled(true);
		if (mMenuItemClose != null) {
			mMenuItemClose.setVisible(false);
		}
		if (mLoadBookDetailsClass != null) {
			mLoadBookDetailsClass.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_book_details_button_show_chapters) {
			if (mChapterList.getVisibility() == View.VISIBLE) {
				mChapterList.setAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.slide_out_bottom));
				mChapterList.setVisibility(View.GONE);
				mShowChapters.setText(R.string.show_chapters);
			} else {
				mChapterList.setAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.slide_in_bottom));
				mChapterList.setVisibility(View.VISIBLE);
				mShowChapters.setText(R.string.hide_chapters);
			}

		}
	}
}
