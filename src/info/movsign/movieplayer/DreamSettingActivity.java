package info.movsign.movieplayer;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class DreamSettingActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(android.R.id.content,
				new SamplePreferenceFragment());
		fragmentTransaction.commit();
	}

	public static class SamplePreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle bundle) {
			super.onCreate(bundle);

			addPreferencesFromResource(R.xml.dream_prefs);
		}
	}
}