package ndphu.app.android.cw.fragment.favorite;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.fragment.favorite.BookViewHolder.BookViewHolderListener;
import ndphu.app.android.cw.model.Book;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

public class BookRecyclerAdapter extends Adapter<BookViewHolder> implements BookViewHolderListener {

	private List<Book> mBooks;
	private Context mContext;

	public BookRecyclerAdapter(Context context) {
		mContext = context;
		mBooks = new ArrayList<Book>();
	}

	public void refresh() {
		new LoadFavoriteTask().execute();
	}

	@Override
	public int getItemCount() {
		return mBooks.size();
	}

	@Override
	public void onBindViewHolder(BookViewHolder holder, int position) {
		Book book = mBooks.get(position);
		holder.setBook(book);
		if (book.getChapters().size() > 0) {
			Integer currentChapter = book.getCurrentChapter();
			if (currentChapter < 0) {
				holder.recentChapter.setText("N/A");
			} else {
				holder.recentChapter.setText(book.getChapters().get(currentChapter).getName());
			}
			holder.lastChapter.setText(book.getChapters().get(0).getName());
		} else {
			holder.recentChapter.setText("N/A");
			holder.lastChapter.setText("N/A");
		}
		holder.name.setText(book.getName());
		Picasso.with(mContext).load(book.getCover()).into(holder.cover);
	}

	@Override
	public BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_book, viewGroup, false);
		return new BookViewHolder(view, this);
	}

	private class LoadFavoriteTask extends AsyncTask<Void, Void, List<Book>> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(BookRecyclerAdapter.this.mContext);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Loading...");
			progressDialog.show();
		}

		@Override
		protected List<Book> doInBackground(Void... params) {
			return DaoUtils.getFavoriteBooks();
		}

		@Override
		protected void onPostExecute(List<Book> result) {
			super.onPostExecute(result);
			mBooks.clear();
			mBooks.addAll(result);
			notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}

	@Override
	public void onRemoveClick(Book book) {
		if (book != null) {
			book.setFavorite(false);
			DaoUtils.updateBook(book);
			mBooks.remove(book);
			notifyDataSetChanged();
		}
	}
}
