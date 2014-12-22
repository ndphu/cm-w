package ndphu.app.android.cw.fragment.reading;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.Page;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

public class VerticalReadingRecyclerAdapter extends Adapter<VerticalReadingViewHolder> {

	private static final String TAG = VerticalReadingRecyclerAdapter.class.getSimpleName();
	private List<Page> mPages = new ArrayList<Page>();

	void refresh(List<Page> pages) {
		synchronized (mPages) {
			mPages.clear();
			mPages.addAll(pages);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		synchronized (mPages) {
			return mPages.size();
		}
	}

	@Override
	public void onBindViewHolder(VerticalReadingViewHolder vh, int position) {
		synchronized (mPages) {
			Picasso.with(vh.mContent.getContext()).load(mPages.get(position).getUrl()).into(vh.mContent);
		}
	}

	@Override
	public VerticalReadingViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_page_recycler, viewGroup, false);
		return new VerticalReadingViewHolder(view);
	}

}
