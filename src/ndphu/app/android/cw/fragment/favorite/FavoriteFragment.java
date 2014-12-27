package ndphu.app.android.cw.fragment.favorite;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.fragment.favorite.BookRecyclerAdapter.RefreshListener;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class FavoriteFragment extends Fragment {
	private static final String TAG = FavoriteFragment.class.getSimpleName();
	private BookRecyclerAdapter mBookAdapter;
	private RecyclerView mListViewBook;
	private ProgressBar mProgressIndicator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBookAdapter = new BookRecyclerAdapter(getActivity());
		mBookAdapter.setRefreshListener(new RefreshListener() {

			@Override
			public void onRefreshStart() {
				if (mProgressIndicator != null) {
					mProgressIndicator.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onRefreshEnd() {
				if (mProgressIndicator != null) {
					mProgressIndicator.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_favorite, container, false);
		mListViewBook = (RecyclerView) view.findViewById(R.id.fragment_favorite_recyclerview_book_list);
		mListViewBook.setHasFixedSize(true);
		LinearLayoutManager lm = new LinearLayoutManager(getActivity());
		lm.setOrientation(LinearLayoutManager.VERTICAL);
		mListViewBook.setLayoutManager(lm);
		mListViewBook.setAdapter(mBookAdapter);
		mProgressIndicator = (ProgressBar) view.findViewById(R.id.fragment_favorite_progressbar_loading_indicator);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBookAdapter.refresh();
	}
}
