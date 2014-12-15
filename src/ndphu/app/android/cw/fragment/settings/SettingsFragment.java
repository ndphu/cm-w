package ndphu.app.android.cw.fragment.settings;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class SettingsFragment extends Fragment implements OnItemSelectedListener {

	private Spinner mSpinnerTheme;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		mSpinnerTheme = (Spinner) v.findViewById(R.id.fragment_settings_spinner_theme);
		mSpinnerTheme.post(new Runnable() {

			@Override
			public void run() {
				mSpinnerTheme.setOnItemSelectedListener(SettingsFragment.this);
			}
		});
		return v;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int currentTheme = getPref().getInt(MainActivity.PREF_APP_THEME, R.style.AppBaseTheme);
		int newTheme = 0;
		if (position == 0) {
			newTheme = R.style.AppBaseTheme;
		} else {
			newTheme = R.style.AppBaseThemeLight;
		}
		if (currentTheme != newTheme) {
			getPref().edit().putInt(MainActivity.PREF_APP_THEME, newTheme).commit();
			Intent intent = getActivity().getIntent();
			getActivity().finish();
			startActivity(intent);
		}

	}

	private SharedPreferences getPref() {
		return getActivity().getSharedPreferences(MainActivity.PREF_APP_THEME, Context.MODE_APPEND);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}
