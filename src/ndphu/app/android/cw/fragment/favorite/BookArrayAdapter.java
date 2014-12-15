package ndphu.app.android.cw.fragment.favorite;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.ReadingActivity;
import ndphu.app.android.cw.dao.BookDao;
import ndphu.app.android.cw.dao.ChapterDao;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class BookArrayAdapter extends ArrayAdapter<Book> {

	private LayoutInflater mInflater;
	private BookDao mBookDao;
	private ChapterDao mChapterDao;

	public BookArrayAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBookDao = new BookDao(getContext());
		mChapterDao = new ChapterDao(getContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_book, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.listview_item_book_name);
			holder.cover = (ImageView) convertView.findViewById(R.id.listview_item_book_cover);
			holder.menu = (ImageView) convertView.findViewById(R.id.listview_item_book_menu_button);
			holder.lastChapter = (Button) convertView.findViewById(R.id.listview_item_book_button_lastchapter);
			convertView.setTag(holder);
		}
		final ViewHolder holder = (ViewHolder) convertView.getTag();
		final Book book = getItem(position);

		holder.menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PopupMenu menu = new PopupMenu(getContext(), v);
				menu.inflate(R.menu.favorite_book_context_menu);
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
						case R.id.action_remove_favorite:
							book.setFavorite(false);
							mBookDao.update(book.getId(), book);
							remove(book);
							return true;
						default:
							return false;
						}
					}
				});
				menu.show();
			}
		});
		holder.name.setText(book.getName());
		holder.lastChapter.setText("Loading...");
		new AsyncTask<Void, Void, Chapter>() {

			@Override
			protected Chapter doInBackground(Void... params) {
				return mChapterDao.getChaptersInBook(book.getId()).get(0);
			}

			@Override
			protected void onPostExecute(Chapter chapter) {
				super.onPostExecute(chapter);
				if (chapter == null) {
					holder.lastChapter.setVisibility(View.GONE);
				} else {
					holder.lastChapter.setVisibility(View.VISIBLE);
					holder.lastChapter.setText(chapter.getName());
					holder.lastChapter.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getContext(), ReadingActivity.class);
							intent.putExtra(ReadingActivity.EXTRA_BOOK_ID, book.getId());
							book.setCurrentChapter(0);
							mBookDao.update(book.getId(), book);
							intent.putExtra(ReadingActivity.EXTRA_CHAPTER_INDEX, 0);
							getContext().startActivity(intent);
						}
					});
				}
			}

		}.execute();

		Picasso.with(getContext()).load(Uri.parse(book.getCover())).into(holder.cover);
		return convertView;
	}

	private static class ViewHolder {
		TextView name;
		ImageView cover;
		ImageView menu;
		Button lastChapter;
	}

}
