package com.sociocast.android.content_provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class QueuedEventContentProvider extends ContentProvider {
	
	public static final String TAG = QueuedEventContentProvider.class.getName();
	
	private static final String DATABASE_NAME = "events.db";
	private static final int DATABASE_VERSION = 5;	
	public static final String KEY_ID = "id";
	public static final String KEY_TIME_STAMP = "timestamp";	
	public static final String KEY_JSON = "json";		
	public static final String KEY_ACTION = "action";
	private static final String EVENTS_TABLE = "events";
	
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.sociocast.android.provider.events/events");
	
	//Create the constants used to differentiate between the different URI requests.
	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;	
	
	//Allocate the UriMatcher object, where a URI ending in 'checkins' will
	//correspond to a request for all earthquakes, and 'checkins' with a trailing '/[Unique ID]' will represent a single earthquake row.
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.sociocast.android.provider.events", "events", EVENTS);
		uriMatcher.addURI("com.sociocast.android.provider.events", "events/*", EVENT_ID);
	}	
	
	/** The underlying database */
	private SQLiteDatabase eventsDB;	
	
	@Override
	public boolean onCreate() {
		Context context = getContext();
		
		EventsDatabaseHelper dbHelper = new EventsDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	    try {
	    	eventsDB = dbHelper.getWritableDatabase();
	    } catch (SQLiteException e) {
	    	eventsDB = null;
	        Log.d(TAG, "Database Opening exception");
	    }
	      
	    return (eventsDB == null) ? false : true;		
	}	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
	    switch (uriMatcher.match(uri)) {
	    	case EVENTS: return "vnd.android.cursor.dir/com.sociocast.android.provider.events";
	    	case EVENT_ID: return "vnd.android.cursor.item/com.sociocast.android.provider.events";
	    	default: throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }		
	}

	@Override
	public Uri insert(Uri _uri, ContentValues values) {
		// Insert the new row, will return the row number if successful.
	    long rowID = eventsDB.insert(EVENTS_TABLE, "event", values);
	          
	    // Return a URI to the newly inserted row on success.
	    if (rowID > 0) {
	    	Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
	    	getContext().getContentResolver().notifyChange(uri, null);
	    	return uri;
	    }
	    throw new SQLException("Failed to insert row into " + _uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EVENTS_TABLE);

	    // If this is a row query, limit the result set to the passed in row. 
	    switch (uriMatcher.match(uri)) {
	    	case EVENT_ID : qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
	    		break;
	    	default : break;
	    }

	    // If no sort order is specified sort by date / time
	    String orderBy;
	    if (TextUtils.isEmpty(sortOrder)) {
	      orderBy = KEY_TIME_STAMP + " ASC";
	    } else {
	      orderBy = sortOrder;
	    }

	    // Apply the query to the underlying database.
	    Cursor c = qb.query(eventsDB, 
	                        projection, 
	                        selection, selectionArgs, 
	                        null, null, orderBy);

	    // Register the contexts ContentResolver to be notified if
	    // the cursor result set changes. 
	    c.setNotificationUri(getContext().getContentResolver(), uri);
	    
	    // Return a cursor to the query result.
	    return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int count;
	    switch (uriMatcher.match(uri)) {
	    	case EVENTS: count = eventsDB.update(EVENTS_TABLE, values, selection, selectionArgs);
	                   break;

	    	case EVENT_ID: String segment = uri.getPathSegments().get(1);
	                     count = eventsDB.update(EVENTS_TABLE, values, KEY_ID 
	                             + "=" + segment 
	                             + (!TextUtils.isEmpty(selection) ? " AND (" 
	                             + selection + ')' : ""), selectionArgs);
	                     break;

	    	default: throw new IllegalArgumentException("Unknown URI " + uri);
	    }

	    getContext().getContentResolver().notifyChange(uri, null);
	    return count;
	}
	
	// Helper class for opening, creating, and managing database version control
	private static class EventsDatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE =
				"create table " + EVENTS_TABLE + " ("
						+ KEY_ID + " TEXT primary key, "
						+ KEY_JSON + " TEXT, "
						+ KEY_ACTION + " TEXT, "
						+ KEY_TIME_STAMP + " LONG); ";
	        
	    public EventsDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
	    	super(context, name, factory, version);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	Log.d(TAG, "Creating database " + DATABASE_CREATE);
	    	db.execSQL(DATABASE_CREATE);           
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                  + newVersion + ", which will destroy all old data");
	              
	    	db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
	    	onCreate(db);
	    }
	}	

}
