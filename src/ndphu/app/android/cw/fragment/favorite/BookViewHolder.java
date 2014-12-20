package ndphu.app.android.cw.fragment.favorite;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.ReadingActivity;
import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.model.Book;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	private static final String TAG = BookViewHolder.class.getSimpleName();
	TextView name;
	ImageView cover;
	ImageView menu;
	TextView recentChapter;
	Button lastChapter;
	WeakReference<Book> bookRef;
	private WeakReference<BookViewHolderListener> mListener;

	public BookViewHolder(View itemView, BookViewHolderListener listener) {
		super(itemView);
		itemView.setOnClickListener(this);
		name = (TextView) itemView.findViewById(R.id.listview_item_book_name);
		cover = (ImageView) itemView.findViewById(R.id.listview_item_book_cover);
		menu = (ImageView) itemView.findViewById(R.id.listview_item_book_menu_button);
		recentChapter = (TextView) itemView.findViewById(R.id.listview_item_book_textview_recent_chapter);
		lastChapter = (Button) itemView.findViewById(R.id.listview_item_book_button_lastchapter);
		mListener = new WeakReference<BookViewHolder.BookViewHolderListener>(listener);
		initialize();
	}

	public void setBook(Book book) {
		bookRef = new WeakReference<Book>(book);
	}

	private void initialize() {
		lastChapter.setOnClickListener(this);
		menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PopupMenu menu = new PopupMenu(BookViewHolder.this.menu.getContext(), v);
				menu.inflate(R.menu.favorite_book_context_menu);
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
						case R.id.action_remove_favorite:
							if (mListener.get() != null) {
								mListener.get().onRemoveClick(bookRef.get());
							}
							return true;
						default:
							return false;
						}
					}
				});
				menu.show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (bookRef != null && bookRef.get() != null) {
			Book book = bookRef.get();
			switch (v.getId()) {
			case R.id.listview_item_book_button_lastchapter: {
				book.setCurrentChapter(0);
				DaoUtils.updateBook(book);
				startReadingActivity(v.getContext(), book);
			}
				break;
			default: {
				startReadingActivity(v.getContext(), book);
				break;
			}

			}
		}
	}

	private void startReadingActivity(Context context, Book book) {
		Intent intent = new Intent(context, ReadingActivity.class);
		intent.putExtra(ReadingActivity.EXTRA_BOOK_ID, book.getId());
		intent.putExtra(ReadingActivity.EXTRA_CHAPTER_INDEX, book.getCurrentChapter());
		context.startActivity(intent);
	}

	public static interface BookViewHolderListener {
		void onRemoveClick(Book book);
	}
}