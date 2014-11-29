package ndphu.app.android.cw.adapter;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.Chapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChapterAdapter extends ArrayAdapter<Chapter> {

	private LayoutInflater mInflater;

	public ChapterAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_chapter, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.listview_item_chapter_title);
			convertView.setTag(holder);
		}
		Chapter chapter = getItem(position);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.title.setText(chapter.getName());
		return convertView;
	}

	private static class ViewHolder {
		TextView title;
	}

}
