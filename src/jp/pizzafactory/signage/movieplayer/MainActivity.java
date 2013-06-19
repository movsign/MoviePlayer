package jp.pizzafactory.signage.movieplayer;

import java.util.Calendar;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.VideoView;

public class MainActivity extends Activity {

	protected static final int LOADER_ID = 0;
	protected static final Uri CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	protected static final String[] PROJECTION = new String[] { MediaStore.Video.Media._ID };
	private VideoView videoView;

	// The adapter that binds our data to the ListView
	private SimpleCursorAdapter adapter;
	private ListView thumbnailList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		videoView = (VideoView) findViewById(R.id.videoView);

		videoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer arg0) {
				videoView.start();
			}
		});

		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				setNextVideo();
			}
		});

		getContentResolver().registerContentObserver(CONTENT_URI, true,
				new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);
						adapter.notifyDataSetChanged();
					}
				});
		adapter = new SimpleCursorAdapter(this, R.layout.row_thumbnail, null,
				PROJECTION, new int[] { R.id.thumbnail_image }, 0);
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int index) {
				boolean result;
				if (index == 0) {
					ImageView iv = (ImageView) view;
					long id = cursor.getLong(cursor
							.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
					Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
							getContentResolver(), id,
							MediaStore.Video.Thumbnails.MINI_KIND, null);
					iv.setImageBitmap(bitmap);
					Uri movieUri = ContentUris.withAppendedId(CONTENT_URI, id);
					iv.setTag(movieUri);
					result = true;
				} else {
					result = false;
				}
				return result;
			}
		});

		thumbnailList = (ListView) findViewById(R.id.thumbnailList);
		thumbnailList.setAdapter(adapter);
		thumbnailList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				thumbnailList.setSelection(position);
				playNextVideo(position);
			}
		});
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, loaderCallbacks);

		setNextVideo();
	}

	private int[] hoursTable = new int[] { R.string.enable0, R.string.enable1,
			R.string.enable2, R.string.enable3, R.string.enable4,
			R.string.enable5, R.string.enable6, R.string.enable7,
			R.string.enable8, R.string.enable9, R.string.enable10,
			R.string.enable11, R.string.enable12, R.string.enable13,
			R.string.enable14, R.string.enable15, R.string.enable16,
			R.string.enable17, R.string.enable18, R.string.enable19,
			R.string.enable20, R.string.enable21, R.string.enable22,
			R.string.enable23 };

	private void setNextVideo() {
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Calendar calendar = Calendar.getInstance();
		String idString = getString(hoursTable[calendar
				.get(Calendar.HOUR_OF_DAY)]);
		int visibility = sp.getBoolean(idString, true) ? View.VISIBLE
				: View.INVISIBLE;

		findViewById(R.id.thumbnailList).setVisibility(visibility);
		findViewById(R.id.videoView).setVisibility(visibility);
		if (visibility == View.INVISIBLE) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					setNextVideo();
				}
			}, 60000);
		} else {
			final int position = (int) (Math.random() * thumbnailList.getCount());
			new Handler().postDelayed(new Runnable() {
				public void run() {
					thumbnailList.smoothScrollToPosition(position);
					playNextVideo(position);
				}
			}, 3000);
		}
	}

	private void playNextVideo(int position) {
		Cursor cursor = (Cursor) thumbnailList.getItemAtPosition(position);
		if (cursor != null) {
			long id = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			Uri movieUri = ContentUris.withAppendedId(CONTENT_URI, id);
			videoView.setVideoURI(movieUri);
		}
	}

	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(MainActivity.this, CONTENT_URI, PROJECTION,
					null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			switch (loader.getId()) {
			case LOADER_ID:
				// The asynchronous load is complete and the data
				// is now available for use. Only now can we associate
				// the queried Cursor with the SimpleCursorAdapter.
				adapter.swapCursor(cursor);
				break;
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			adapter.swapCursor(null);
		}
	};
}
