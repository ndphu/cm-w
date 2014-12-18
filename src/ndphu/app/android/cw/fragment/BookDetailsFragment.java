package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.adapter.ChapterAdapter;
import ndphu.app.android.cw.dao.BookDao;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
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
	private Button mGoToLastChapter;
	private Button mGoToFirstChapter;
	private ImageView mBookCover;
	private ViewGroup mParentContainer = null;
	private ImageView mFavoriteIcon;
	private Book mBook;
	private ActionBar mActionBar;
	private int mCurrentChapterIndex;
	private WeakReference<OnChapterSelectedListener> mOnChapterSelectedListener;
	private BookDao mBookDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBookDao = new BookDao(getActivity());
	}

	public void setBook(Book book) {
		mBook = book;
		loadBookDetails();
	}

	public void setOnChapterSelectedListener(OnChapterSelectedListener listener) {
		mOnChapterSelectedListener = new WeakReference<OnChapterSelectedListener>(listener);
	}

	private void loadBookDetails() {
		mTitle.setText(mBook.getName());
		Picasso.with(getActivity()).load(Uri.parse(mBook.getCover())).into(mBookCover);
		if (mBook.getDescription() != null && !mBook.getDescription().trim().isEmpty()) {
			mBookSummary.setText(Html.fromHtml(mBook.getDescription()));
		}
		mAdapter.clear();
		mAdapter.addAll(mBook.getChapters());
		updateFavoriteIcon();

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
		showChapterList();
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
		mFavoriteIcon = (ImageView) view.findViewById(R.id.fragment_book_details_imageview_favorite);
		mFavoriteIcon.setOnClickListener(this);
		mShowChapters = (Button) view.findViewById(R.id.fragment_book_details_button_show_chapters);
		mShowChapters.setOnClickListener(this);
		mGoToFirstChapter = (Button) view.findViewById(R.id.fragment_book_details_button_go_to_first_chapter);
		mGoToFirstChapter.setOnClickListener(this);
		mGoToLastChapter = (Button) view.findViewById(R.id.fragment_book_details_button_go_to_last_chapter);
		mGoToLastChapter.setOnClickListener(this);
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
		switch (v.getId()) {
		case R.id.fragment_book_details_imageview_favorite:
			// toggle
			mBook.setFavorite(!mBook.getFavorite());
			mBookDao.update(mBook.getId(), mBook);
			updateFavoriteIcon();
			break;
		case R.id.fragment_book_details_button_show_chapters:
			if (mChapterList.getVisibility() == View.VISIBLE) {
				hideChapterList();
			} else {
				showChapterList();
			}
			break;
		case R.id.fragment_book_details_button_go_to_first_chapter:
			setCurrentChapterIndex(mAdapter.getCount() - 1);
			break;
		case R.id.fragment_book_details_button_go_to_last_chapter:
			setCurrentChapterIndex(0);
			break;
		}
	}

	private void showChapterList() {
		mChapterList.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom));
		mChapterList.setVisibility(View.VISIBLE);
		mShowChapters.setText(R.string.show_info);
		Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
		slideOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mBookSummary.setVisibility(View.GONE);
			}
		});
		mBookSummary.setAnimation(slideOut);

	}

	private void hideChapterList() {
		Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
		slideOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mChapterList.setVisibility(View.GONE);
			}
		});
		mChapterList.setAnimation(slideOut);
		mShowChapters.setText(R.string.show_chapters);
		mBookSummary.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top));
		mBookSummary.setVisibility(View.VISIBLE);
	}

	public void setCurrentChapterIndex(int currentChapterIndex) {
		mCurrentChapterIndex = currentChapterIndex;
		mChapterList.setItemChecked(currentChapterIndex, true);
		mChapterList.setSelection(currentChapterIndex);
		if (mOnChapterSelectedListener != null && mOnChapterSelectedListener.get() != null) {
			mOnChapterSelectedListener.get().onChapterSelected(mCurrentChapterIndex);
		}
	}

	public static interface OnChapterSelectedListener {
		public void onChapterSelected(int chapterIndex);
	}

	public boolean isValidChapterIndex(int i) {
		return i >= 0 && i < mBook.getChapters().size();
	}

	private void updateFavoriteIcon() {
		if (mBook.getFavorite()) {
			mFavoriteIcon.setImageResource(R.drawable.ic_favorite);
		} else {
			mFavoriteIcon.setImageResource(R.drawable.ic_not_favorite);
		}
	}
}
