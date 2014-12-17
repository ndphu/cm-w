package ndphu.app.android.cw.adapter;

import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.dao.BookDao;
import ndphu.app.android.cw.dao.ChapterDao;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.task.LoadBookTask;
import ndphu.app.android.cw.task.LoadBookTask.LoadBookListener;
import ndphu.app.android.cw.util.Utils;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class HomePageItemAdapter extends ArrayAdapter<HomePageItem> implements LoadBookListener {
	private static final String TAG = HomePageItemAdapter.class.getSimpleName();
	private LayoutInflater mInflater;
	private BookDao mBookDao;
	private ChapterDao mChapterDao;
	private ProgressDialog mProgressDialog;

	public HomePageItemAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBookDao = new BookDao(context);
		mChapterDao = new ChapterDao(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(getLayoutResId(), parent, false);
			ViewHolder holder = createViewHolder(convertView);
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		HomePageItem item = getItem(position);
		fillDataToItem(holder, item);
		return convertView;
	}

	private ViewHolder createViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.bookNameText = (TextView) convertView.findViewById(R.id.gridview_item_homepage_item_book_name);
		holder.coverImageView = (ImageView) convertView.findViewById(R.id.gridview_item_homepage_item_cover);
		holder.menu = (ImageView) convertView.findViewById(R.id.gridview_item_homepage_item_imageview_menu);
		holder.favorite = (ImageView) convertView.findViewById(R.id.gridview_item_homepage_item_imageview_favorite);
		return holder;
	}

	private int getLayoutResId() {
		return R.layout.gridview_item_homepage_item;
	}

	private void fillDataToItem(final ViewHolder holder, final HomePageItem item) {
		holder.bookNameText.setText(item.mBookName);
		Picasso.with(getContext()).load(Uri.parse(item.mCoverUrl)).into(holder.coverImageView);
		final String hasedUrl = Utils.getMD5Hash(item.mBookUrl);
		new AsyncTask<Void, Void, Book>() {
			@Override
			protected void onPreExecute() {
				holder.favorite.setVisibility(View.INVISIBLE);
			};
			@Override
			protected Book doInBackground(Void... params) {
				final List<Book> books = mBookDao.readAllWhere(Book.COL_HASED_URL, hasedUrl);
				if (books.size() > 0) {
					return books.get(0);
				}
				return null;
			}

			@Override
			protected void onPostExecute(final Book book) {
				super.onPostExecute(book);
				if (book == null) {
					holder.favorite.setVisibility(View.INVISIBLE);
				} else {
					if (book.getFavorite()) {
						holder.favorite.setVisibility(View.VISIBLE);
					} else {
						holder.favorite.setVisibility(View.INVISIBLE);
					}
				}
				holder.menu.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PopupMenu menu = new PopupMenu(getContext(), v);
						menu.inflate(R.menu.homepage_context_menu);
						menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem menuItem) {
								switch (menuItem.getItemId()) {
								case R.id.action_add_to_favorite:
									Log.d(TAG, "BookHased:" + hasedUrl);
									if (book == null) {
										SearchResult result = new SearchResult(item.mBookName, item.mBookUrl, item.mSource);
										new LoadBookTask(result, HomePageItemAdapter.this).execute();
									} else {
										book.setFavorite(true);
										mBookDao.update(book.getId(), book);
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
		}.execute();
	}

	public class ViewHolder {
		ImageView coverImageView;
		TextView bookNameText;
		ImageView menu;
		ImageView favorite;
	}

	@Override
	public void onStartLoading(String url) {
		mProgressDialog = new ProgressDialog(getContext());
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage("Please wait...");
		mProgressDialog.show();
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
		mProgressDialog.dismiss();
	}

	@Override
	public void onComplete(Book book) {
		mProgressDialog.dismiss();
		book.setFavorite(true);
		long newBookId = mBookDao.create(book);
		book.setId(newBookId);
		Log.d(TAG, "New book id: " + newBookId);
		for (Chapter chapter : book.getChapters()) {
			chapter.setBookId(newBookId);
			mChapterDao.create(chapter);
		}
	}

}