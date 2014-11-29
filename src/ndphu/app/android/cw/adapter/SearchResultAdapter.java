package ndphu.app.android.cw.adapter;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.SearchResult;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class SearchResultAdapter extends ArrayAdapter<SearchResult> {
	public static enum DisplayMode {
		LIST, GRID
	}

	private static final String TAG = SearchResultAdapter.class.getSimpleName();
	private LayoutInflater mInflater;
	private DisplayMode mDisplayMode;

	public void setDisplayMode(DisplayMode displayMode) {
		mDisplayMode = displayMode;
	}

	public SearchResultAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDisplayMode = DisplayMode.GRID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(getLayoutResId(), parent, false);
			ViewHolder holder = addViewHolder(convertView);
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		SearchResult item = getItem(position);
		fillDataToItem(holder, item);
		return convertView;
	}

	private ViewHolder addViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		if (this.mDisplayMode == DisplayMode.GRID) {
			holder.bookName = (TextView) convertView.findViewById(R.id.gridview_item_search_book_item_book_name);
			holder.bookUrl = (TextView) convertView.findViewById(R.id.gridview_item_search_book_item_desc);
			holder.cover = (ImageView) convertView.findViewById(R.id.gridview_item_search_book_item_cover);
		} else if (this.mDisplayMode == DisplayMode.LIST) {
			holder.bookName = (TextView) convertView.findViewById(R.id.listview_item_searchbook_title);
			holder.bookUrl = (TextView) convertView.findViewById(R.id.listview_item_searchbook_description);
		}

		return holder;
	}

	private int getLayoutResId() {
		switch (mDisplayMode) {
		case LIST:
			return R.layout.listview_item_searchbook;
		case GRID:
			return R.layout.gridview_item_searchbook;
		default:
			return -1;
		}
	}

	private void fillDataToItem(final ViewHolder holder, final SearchResult item) {
		holder.bookName.setText(item.bookName);
		holder.bookUrl.setText(item.bookLink);
		if (mDisplayMode == DisplayMode.GRID) {
			Picasso.with(getContext()).load(Uri.parse(item.bookCoverLink)).into(holder.cover);
		}
	}

	public class ViewHolder {
		ImageView cover;
		TextView bookName;
		TextView bookUrl;
	}

}