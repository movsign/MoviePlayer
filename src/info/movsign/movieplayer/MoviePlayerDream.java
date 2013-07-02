package info.movsign.movieplayer;

import android.content.Intent;
import android.service.dreams.DreamService;

public class MoviePlayerDream extends DreamService {

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setInteractive(false);
		setFullscreen(true);
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
