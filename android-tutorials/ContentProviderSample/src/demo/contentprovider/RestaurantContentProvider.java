package demo.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * This class defines the RestaurantContentProvider.
 * When registered with in the Android manifest file, the Android runtime
 * will manage the instantiation and shutdown of the provider.
 * @author vladimir
 *
 */
public class RestaurantContentProvider extends ContentProvider {
	private RestaurantDatabase restaurantDb;

	@Override
	public boolean onCreate() {
		Context ctx = getContext();
		restaurantDb = new RestaurantDatabase(ctx);
		return true;
	}
	
	/**
	 * Utility function to return the mime type based on a given URI
	 */
	@Override
	public String getType(Uri uri) {
		final int match = ContentDescriptor.URI_MATCHER.match(uri);
		switch(match){
		case ContentDescriptor.Restaurant.PATH_TOKEN:
			return ContentDescriptor.Restaurant.CONTENT_TYPE_DIR;
		case ContentDescriptor.Restaurant.PATH_FOR_ID_TOKEN:
			return ContentDescriptor.Restaurant.CONTENT_ITEM_TYPE;
        default:
            throw new UnsupportedOperationException ("URI " + uri + " is not supported.");
		}	
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = restaurantDb.getWritableDatabase();
		int token = ContentDescriptor.URI_MATCHER.match(uri);
		switch(token){
			case ContentDescriptor.Restaurant.PATH_TOKEN:{
				long id = db.insert(ContentDescriptor.Restaurant.NAME, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentDescriptor.Restaurant.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
			}
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
		}
	}

	/**
	 * Function to query the content provider.  This example queries the backing database.
	 * It uses the SQLite API to retrieve restaurant data based on the URI specified.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = restaurantDb.getReadableDatabase();
		final int match = ContentDescriptor.URI_MATCHER.match(uri);
		switch(match){
			// retrieve restaurant list
			case ContentDescriptor.Restaurant.PATH_TOKEN:{
				SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
				builder.setTables(ContentDescriptor.Restaurant.NAME);
				return builder.query(db, null, null, null, null, null, null);
			}
			default: return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

}
