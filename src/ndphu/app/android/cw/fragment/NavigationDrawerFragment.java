package ndphu.app.android.cw.fragment;

import java.lang.ref.WeakReference;

import ndphu.app.android.cw.R;
import ndphu.app.android.cw.model.Source;
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
	private String[] mSupportedSources;

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
		mSupportedSources = new String[Source.SOURCES.size()];
		for (int i = 0; i <Source.SOURCES.size(); ++i) {
			mSupportedSources[i] = Source.SOURCES.get(i).getName();
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.listview_item_drawer_item,
				mSupportedSources));
		mListView.setItemChecked(0, true);
		mListView.setOnItemClickListener(this);
		if (mListener != null && mListener.get() != null) {
			mListener.get().onItemSelected(0);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		mListView.setSelection(position);
		if (mListener != null && mListener.get() != null) {
			mListener.get().onItemSelected(position);
		}
	}

}
