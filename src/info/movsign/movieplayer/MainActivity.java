package info.movsign.movieplayer;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
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
import android.widget.TextView;
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
    protected void onNewIntent(Intent intent) {
		if (intent.getBooleanExtra("EXIT", false)) {
			finish();
		}
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		onNewIntent(getIntent());

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		TextView textView = (TextView) findViewById(R.id.textView1);
		textView.setText(getMarqueeText());
		textView.requestFocus();

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

	private void setNextVideo() {
		final int position = (int) (Math.random() * thumbnailList.getCount());
		new Handler().postDelayed(new Runnable() {
			public void run() {
				thumbnailList.smoothScrollToPosition(position);
				playNextVideo(position);
			}
		}, 3000);
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

	private String getMarqueeText() {
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
		return pm.getString("marqueetext_key", "");
	}
}
