package info.movsign.movieplayer;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class TouchCountProvider extends ContentProvider {
	TouchCountDatabaseHelper databaseHelper;

	@Override
	public boolean onCreate() {
	    databaseHelper = new TouchCountDatabaseHelper(getContext());
	    return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	    SQLiteDatabase db = databaseHelper.getReadableDatabase();
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    qb.setTables(TouchCountDatabaseHelper.table); //テーブル名
	    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);
	    return c;

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    SQLiteDatabase db = databaseHelper.getWritableDatabase();
	 // setting the format to sql date time
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date date = new Date();
	    values.put("datetime", dateFormat.format(date));
	    db.insert(TouchCountDatabaseHelper.table, null, values);
	    return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	class TouchCountDatabaseHelper extends SQLiteOpenHelper {
		static final String table = "touch";

		public TouchCountDatabaseHelper(Context context) {
            super(context, "touchcount1.db", null, 1);
        }

		@Override
		public void onCreate(SQLiteDatabase db) {
	          db.execSQL("CREATE TABLE " + table + " ("
	                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
	                    + "uri TEXT,"
	                    + "datetime TEXT,"
	                    + "clicked INTEGER"
	                    + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}
}
