package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.ChapterAdapter;
import ndphu.app.android.cw.model.Book;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class BookDetailsFragment extends Fragment implements OnItemClickListener, android.view.View.OnClickListener {
	private static final String TAG = BookDetailsFragment.class.getSimpleName();
	private ListView mChapterList;
	private ChapterAdapter mAdapter;
	private TextView mTitle;
	private TextView mBookSummary;
	private Button mShowChapters;
	private ImageView mBookCover;
	private ViewGroup mParentContainer = null;
	private Book mBook;
	private ActionBar mActionBar;
	private int mCurrentChapterIndex;
	private WeakReference<OnChapterSelectedListner> mOnChapterSelectedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setBook(Book book) {
		mBook = book;
		loadBookDetails();
	}

	public void setOnChapterSelectedListener(OnChapterSelectedListner listener) {
		mOnChapterSelectedListener = new WeakReference<OnChapterSelectedListner>(listener);
	}

	private void loadBookDetails() {
		mTitle.setText(mBook.getName());
		Picasso.with(getActivity()).load(Uri.parse(mBook.getCover())).into(mBookCover);
		if (mBook.getBookDesc() != null && !mBook.getBookDesc().trim().isEmpty()) {
			mBookSummary.setVisibility(View.VISIBLE);
			mBookSummary.setText(Html.fromHtml(mBook.getBookDesc()));
		} else {
			mBookSummary.setVisibility(View.GONE);
		}
		mAdapter.clear();
		mAdapter.addAll(mBook.getChapters());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mParentContainer = container;
		setHasOptionsMenu(true);
		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		return getContainerView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private View getContainerView() {
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_book_details, mParentContainer, false);
		mChapterList = (ListView) view.findViewById(R.id.fragment_book_details_listview_chapters);
		mAdapter = new ChapterAdapter(getActivity(), 0);
		mChapterList.setAdapter(mAdapter);
		mChapterList.setOnItemClickListener(this);
		mTitle = (TextView) view.findViewById(R.id.fragment_book_details_textview_book_title);
		mBookSummary = (TextView) view.findViewById(R.id.fragment_book_details_textview_summary);
		mBookSummary.setMovementMethod(new ScrollingMovementMethod());
		mBookCover = (ImageView) view.findViewById(R.id.fragment_book_details_cover_image);
		mShowChapters = (Button) view.findViewById(R.id.fragment_book_details_button_show_chapters);
		mShowChapters.setOnClickListener(this);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mCurrentChapterIndex = position;
		if (mOnChapterSelectedListener != null && mOnChapterSelectedListener.get() != null) {
			mOnChapterSelectedListener.get().onChapterSelected(position);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_book_details_button_show_chapters) {
			if (mChapterList.getVisibility() == View.VISIBLE) {
				mChapterList.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom));
				mChapterList.setVisibility(View.GONE);
				mShowChapters.setText(R.string.show_chapters);
			} else {
				mChapterList.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom));
				mChapterList.setVisibility(View.VISIBLE);
				mShowChapters.setText(R.string.hide_chapters);
			}

		}
	}

	public void setSetCurrentChapterIndex(int currentChapterIndex) {
		mCurrentChapterIndex = currentChapterIndex;
		mChapterList.setItemChecked(currentChapterIndex, true);
		if (mOnChapterSelectedListener != null && mOnChapterSelectedListener.get() != null) {
			mOnChapterSelectedListener.get().onChapterSelected(mCurrentChapterIndex);
		}
	}

	public static interface OnChapterSelectedListner {
		public void onChapterSelected(int chapterIndex);
	}

	public boolean isValidChapterIndex(int i) {
		return i >= 0 && i < mBook.getChapters().size();
	}
}