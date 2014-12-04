package ndphu.app.android.cw.fragment.home;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

	private ViewPager mViewPager = null;

	private ScreenSlidePagerAdapter mPagerAdapter;

	private static final String[] HOME_PAGE_TITLES = new String[] {
		"New",
		"Hot",
		"Action",
		"Supernatural",
		"Sports",
		"Romance",
		"One Shot",
		"Horror",
		"Magic",
		"Adult",
		"Ecchi", };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
		mViewPager.setOffscreenPageLimit(1000);
		mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		PagerTabStrip pagerStrip = (PagerTabStrip) view.findViewById(R.id.pager_strip);
		pagerStrip.setTextColor(Color.WHITE);
		pagerStrip.setTabIndicatorColorResource(R.color.background_material_light);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity activty = ((MainActivity) getActivity());
		activty.getToolbar().setTitle("Home");
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			HomeDetailsFragment details = new HomeDetailsFragment();
			details.setHomeLoader(new HotLoader());
			return details;
		}

		@Override
		public int getCount() {
			return HOME_PAGE_TITLES.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return HOME_PAGE_TITLES[position];
		}
	}

}
