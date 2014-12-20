package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavigationDrawerFragment extends Fragment implements OnItemClickListener {
	private ListView mListView;
	private String[] mNavigationMenuItem;

	public interface OnNavigationItemSelected {
		public void onItemSelected(int position);
	}

	private WeakReference<OnNavigationItemSelected> mListener;

	public void setNavigationItemSelected(OnNavigationItemSelected listener) {
		mListener = new WeakReference<OnNavigationItemSelected>(listener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		mListView = (ListView) view.findViewById(R.id.fragment_navigation_drawer_listview);
//		mSupportedSources = new String[Source.SOURCES.size()];
//		for (int i = 0; i <Source.SOURCES.size(); ++i) {
//			mSupportedSources[i] = Source.SOURCES.get(i).getName();
//		}
		mNavigationMenuItem = new String[]{
				"Home",
				"My Books",
				"Settings"
		};
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.listview_item_drawer_item,
				mNavigationMenuItem));
		mListView.setItemChecked(0, true);
		mListView.setOnItemClickListener(this);
		if (mListener != null && mListener.get() != null) {
			mListener.get().onItemSelected(0);
		}
	}
	
	public void setSelection(int position) {
		selectMenu(position);
	}

	private void selectMenu(int position) {
		mListView.setItemChecked(position, true);
		if (mListener != null && mListener.get() != null) {
			mListener.get().onItemSelected(position);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		selectMenu(position);
	}

}
