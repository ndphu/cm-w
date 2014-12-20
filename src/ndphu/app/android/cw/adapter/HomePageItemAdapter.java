package ndphu.app.android.cw.adapter;

import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.dao.BookDao;
import ndphu.app.android.cw.dao.ChapterDao;
import ndphu.app.android.cw.dao.DaoUtils;
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
	private ProgressDialog mProgressDialog;

	public HomePageItemAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		holder.bookNameText.setText(item.bookName);
		Picasso.with(getContext()).load(Uri.parse(item.cover)).into(holder.coverImageView);
		final String hasedUrl = Utils.getMD5Hash(item.bookUrl);
		new AsyncTask<Void, Void, Book>() {
			@Override
			protected void onPreExecute() {
				holder.favorite.setVisibility(View.INVISIBLE);
			};

			@Override
			protected Book doInBackground(Void... params) {
				return DaoUtils.getBookByHasedUrl(hasedUrl);
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
						MenuItem addFavorite = menu.getMenu().findItem(R.id.action_add_favorite);
						MenuItem removeFavorite = menu.getMenu().findItem(R.id.action_remove_favorite);
						if (book == null) {
							removeFavorite.setVisible(false);
							addFavorite.setVisible(true);
						} else {
							removeFavorite.setVisible(book.getFavorite());
							addFavorite.setVisible(!book.getFavorite());
						}
						menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem menuItem) {
								switch (menuItem.getItemId()) {
								case R.id.action_add_favorite: {
									if (book == null) {
										SearchResult result = new SearchResult(item.bookName, item.bookUrl, item.source);
										new LoadBookTask(result, HomePageItemAdapter.this).execute();
									} else {
										book.setFavorite(true);
										DaoUtils.updateBook(book);
										notifyDataSetChanged();
									}
									return true;
								}
								case R.id.action_remove_favorite: {
									book.setFavorite(false);
									DaoUtils.updateBook(book);
									notifyDataSetChanged();
									return true;
								}
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
		DaoUtils.saveOrUpdate(book);
		notifyDataSetChanged();
	}

}