package ndphu.app.android.cw.fragment.favorite;

import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.ReadingActivity;
import ndphu.app.android.cw.dao.BookDao;
import ndphu.app.android.cw.model.Book;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FavoriteFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = FavoriteFragment.class.getSimpleName();
	private BookDao mBookDao;
	private BookArrayAdapter mBookAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBookDao = new BookDao(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_favorite, container, false);
		ListView mListView = (ListView) view.findViewById(R.id.fragment_favorite_listview);
		if (mBookAdapter == null) {
			mBookAdapter = new BookArrayAdapter(getActivity(), 0);
		}
		mListView.setAdapter(mBookAdapter);
		mListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mBookAdapter.clear();
		List<Book> favoriteBooks = mBookDao.readAllWhere(Book.COL_FAVORITE, "1");
		if (favoriteBooks != null && favoriteBooks.size() > 0) {
			mBookAdapter.addAll(favoriteBooks);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Book book = mBookAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), ReadingActivity.class);
		intent.putExtra(ReadingActivity.EXTRA_BOOK_ID, book.getId());
		getActivity().startActivity(intent);
	}
}
