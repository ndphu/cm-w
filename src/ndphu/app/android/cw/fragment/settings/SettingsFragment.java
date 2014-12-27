package ndphu.app.android.cw.fragment.settings;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;

public class SettingsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		final RadioGroup rgSwipeMode = (RadioGroup) v.findViewById(R.id.fragment_settings_radiogroup_swipemode);
		final SharedPreferences appSettingsPref = getActivity().getSharedPreferences(MainActivity.PREF_APP_SETTINGS,
				Context.MODE_APPEND);
		int rdId = 0;
		switch (appSettingsPref.getInt(MainActivity.PREF_SWIPE_MODE, MainActivity.SWIPE_MODE_VERTICAL)) {
		case MainActivity.SWIPE_MODE_HORIZONTAL:
			rdId = R.id.fragment_settings_radiobutton_swipemode_horizontal;
			break;
		default:
			rdId = R.id.fragment_settings_radiobutton_swipemode_vertical;
			break;
		}
		((RadioButton) rgSwipeMode.findViewById(rdId)).setChecked(true);
		rgSwipeMode.post(new Runnable() {

			@Override
			public void run() {
				rgSwipeMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						Editor editor = appSettingsPref.edit();
						switch (checkedId) {
						case R.id.fragment_settings_radiobutton_swipemode_vertical:
							editor.putInt(MainActivity.PREF_SWIPE_MODE, MainActivity.SWIPE_MODE_VERTICAL);
							break;
						case R.id.fragment_settings_radiobutton_swipemode_horizontal:
							editor.putInt(MainActivity.PREF_SWIPE_MODE, MainActivity.SWIPE_MODE_HORIZONTAL);
							break;
						}
						editor.commit();
					}
				});
			}
		});

		final Switch switchPreloadNextChapter = (Switch) v
				.findViewById(R.id.fragment_settings_switch_preload_next_chapter);
		boolean preloadNextChap = appSettingsPref.getBoolean(MainActivity.PREF_PRELOAD_NEXT_CHAPTER, true);
		switchPreloadNextChapter.setChecked(preloadNextChap);
		switchPreloadNextChapter.post(new Runnable() {

			@Override
			public void run() {
				switchPreloadNextChapter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor editor = appSettingsPref.edit();
						editor.putBoolean(MainActivity.PREF_PRELOAD_NEXT_CHAPTER, isChecked);
						editor.commit();
					}
				});
			}
		});
		
		final Switch swithEnableVolumnKey = (Switch) v
				.findViewById(R.id.fragment_settings_switch_enable_volumn_key);
		boolean useVolumnKey = appSettingsPref.getBoolean(MainActivity.PREF_ENABLE_VOLUMN_KEY, true);
		swithEnableVolumnKey.setChecked(useVolumnKey);
		swithEnableVolumnKey.post(new Runnable() {

			@Override
			public void run() {
				swithEnableVolumnKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor editor = appSettingsPref.edit();
						editor.putBoolean(MainActivity.PREF_ENABLE_VOLUMN_KEY, isChecked);
						editor.commit();
					}
				});
			}
		});
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

}
