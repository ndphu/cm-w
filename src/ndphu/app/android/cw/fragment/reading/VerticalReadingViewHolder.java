package ndphu.app.android.cw.fragment.reading;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.customview.TouchImageView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView.ScaleType;

public class VerticalReadingViewHolder extends ViewHolder {

	TouchImageView mContent;

	public VerticalReadingViewHolder(View itemView) {
		super(itemView);
		mContent = (TouchImageView) itemView.findViewById(R.id.listview_item_page_reclycler_imageview_content);
		mContent.setScaleType(ScaleType.FIT_CENTER);
	}

}
