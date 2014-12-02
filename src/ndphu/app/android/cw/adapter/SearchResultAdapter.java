package ndphu.app.android.cw.adapter;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.SearchResult;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends ArrayAdapter<SearchResult> {

	private static final String TAG = SearchResultAdapter.class.getSimpleName();
	private LayoutInflater mInflater;

	public SearchResultAdapter(Context context, int textViewResourceId) {
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
		SearchResult item = getItem(position);
		fillDataToItem(holder, item);
		return convertView;
	}

	private ViewHolder createViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.bookName = (TextView) convertView.findViewById(R.id.listview_item_searchbook_title);
		return holder;
	}

	private int getLayoutResId() {
		return R.layout.listview_item_searchbook;
	}

	private void fillDataToItem(final ViewHolder holder, final SearchResult item) {
		if (item.bookSource.equals(SearchResult.Source.BLOGTRUYEN)) {
			holder.bookName.setText(Html.fromHtml(item.bookName));
		} else {
			holder.bookName.setText(item.bookName);
		}
	}

	public class ViewHolder {
		TextView bookName;
	}

}