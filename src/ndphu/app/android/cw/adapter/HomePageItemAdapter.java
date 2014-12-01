package ndphu.app.android.cw.adapter;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.HomePageItem;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class HomePageItemAdapter extends ArrayAdapter<HomePageItem> {
	private static final String TAG = HomePageItemAdapter.class.getSimpleName();
	private LayoutInflater mInflater;

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
		holder.autoChapterText = (TextView) convertView.findViewById(R.id.gridview_item_homepage_item_auto_chapter);
		holder.coverImageView = (ImageView) convertView.findViewById(R.id.gridview_item_homepage_item_cover);
		return holder;
	}

	private int getLayoutResId() {
		return R.layout.gridview_item_homepage_item;
	}

	private void fillDataToItem(final ViewHolder holder, final HomePageItem item) {
		holder.bookNameText.setText(item.mBookName);
		holder.autoChapterText.setText(item.mChapterName);
		Picasso.with(getContext()).load(Uri.parse(item.mCoverUrl)).into(holder.coverImageView);
	}

	public class ViewHolder {
		ImageView coverImageView;
		TextView bookNameText;
		TextView autoChapterText;
	}

}