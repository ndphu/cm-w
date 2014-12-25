package ndphu.app.android.cw.fragment.reading;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

class HorizontalTransformer implements PageTransformer {
	@Override
	public void transformPage(View view, float position) {
		int pageWidth = view.getWidth();

		if (position < -1) {
			view.setAlpha(0);
		} else if (position <= 0) {
			view.setAlpha(1 + position);
			view.setTranslationX(pageWidth * -position);
			float scaleFactor = ReadingFragment.MIN_SCALE + (1 - ReadingFragment.MIN_SCALE) * (1 - Math.abs(position));
			view.setScaleX(scaleFactor);
			view.setScaleY(scaleFactor);
		} else if (position <= 1) { // (0,1]
			view.setAlpha(1);
			view.setTranslationX(0);
			view.setScaleX(1);
			view.setScaleY(1);
		} else {
			view.setAlpha(0);
		}
	}
}